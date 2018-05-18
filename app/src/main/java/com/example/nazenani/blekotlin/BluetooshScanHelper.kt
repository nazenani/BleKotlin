package com.example.nazenani.blekotlin

import android.annotation.TargetApi
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.os.Build
import android.util.Log
import java.lang.Math
import kotlin.experimental.and

class BluetooshScanHelper(context: Context, listener: BeaconListener) {
    private val TAG: String = this::class.java.name

    private var mListener: BeaconListener? = null

    private var mBleAdapter: BluetoothAdapter? = null
    private var mBluetoothManager: BluetoothManager? = null

    private var mBleScanCallback: BleScanCallback? = null
    private var mOldBleScanCallback: OldBleScanCallback? = null

    init {
        // API LEVEL 21 以上の場合
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mBleAdapter = BluetoothAdapter.getDefaultAdapter()
            mBleScanCallback = BleScanCallback(this)
        // API LEVEL 21 未満の場合
        } else {
            mBluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            mOldBleScanCallback = OldBleScanCallback(this)
        }

        // リスナーを登録
        mListener = listener
    }


    fun startScan() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Log.d(TAG, "API level 21 以上: START")
            mBleAdapter?.bluetoothLeScanner?.startScan(mBleScanCallback)
        } else {
            Log.d(TAG, "API level 21 未満: START")
            mBluetoothManager!!.adapter.startLeScan(mOldBleScanCallback)
        }
    }


    fun stopScan() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Log.d(TAG, "API level 21 以上: STOP")
            mBleAdapter?.bluetoothLeScanner?.stopScan(mBleScanCallback)
        } else {
            Log.d(TAG, "API level 21 未満: STOP")
            mBluetoothManager!!.adapter.stopLeScan(mOldBleScanCallback)
        }
    }


    private fun format(device: BluetoothDevice?, rssi: Int?, bytes: ByteArray?) {

        if (device == null || rssi == null || bytes == null) {
            Log.d(TAG, "Something errors... : ${device} : ${rssi} : ${bytes}")
            return
        }

        // スキャンレコードが30バイト以上で6バイト目から9バイト目の値が0x4c000215の場合はiBeaconと判定
        var isIbeacon = false
        var proximityUuid: String = ""
        var major: String = ""
        var minor: String = ""
        var distance: Double = 0.0

        // IBeaconを判定
        if (bytes.size >= 30 && bytes[5] == 0x4c.toByte() && bytes[6] == 0x00.toByte() && bytes[7] == 0x02.toByte() && bytes[8] == 0x15.toByte()) {

            isIbeacon = true
            // 0埋めの16進数に変換
            proximityUuid = arrayOf(
                    "${String.format("%02x", bytes[9])}${String.format("%02x", bytes[10])}${String.format("%02x", bytes[11])}${String.format("%02x", bytes[12])}",
                    "${String.format("%02x", bytes[13])}${String.format("%02x", bytes[14])}",
                    "${String.format("%02x", bytes[15])}${String.format("%02x", bytes[16])}",
                    "${String.format("%02x", bytes[17])}${String.format("%02x", bytes[18])}",
                    "${String.format("%02x", bytes[19])}${String.format("%02x", bytes[20])}${String.format("%02x", bytes[21])}${String.format("%02x", bytes[22])}${String.format("%02x", bytes[23])}${String.format("%02x", bytes[24])}"
            ).joinToString("-").toUpperCase()

            // メジャー値とマイナー値を取得
            major = String.format("%02x", bytes[25]) + String.format("%02x", bytes[26])
            minor = String.format("%02x", bytes[27]) + String.format("%02x", bytes[28])

            // 送信出力を推定
            //val txPower: Int = (bytes[29] and 0xff.toByte()) - 256
            val txPower: Int = (bytes[29] and 0xff.toByte()).toInt()
            // パワー計算（推定距離：メートル）
            distance = Math.pow(10.0, (txPower - rssi) / (10.0 * 2.0))
        }

        // インターフェイスのリスナーに登録
        mListener?.find(isIbeacon, device.address, rssi, proximityUuid, major.toUpperCase(), minor.toUpperCase(), distance)

        Log.d("addScanResult", "${isIbeacon}:${device.address}:${rssi}:$proximityUuid:${major.toUpperCase()}:${minor.toUpperCase()}:${distance}")

    }


    /**
     * API Level 21 以上用コールバック関数
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    class BleScanCallback(bluetoothScanHelper: BluetooshScanHelper) : ScanCallback() {

        val mBluetoothScanHelper: BluetooshScanHelper = bluetoothScanHelper

        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            addScanResult(result)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Log.d(this::class.java.name, "I found a ble device : ${String.format("%02x", result?.scanRecord?.bytes!![3])} : ${String.format("%02x", result?.scanRecord?.bytes!![4])} : ${String.format("%02x", result?.scanRecord?.bytes!![29])} : ${result?.device?.address} : ${result?.rssi}")
            }
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            results?.forEach { result -> addScanResult(result) }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.d(this::class.java.name, "Bluetooth LE scan failed. Error code: $errorCode")
        }

        private fun addScanResult(scanResult: ScanResult?) {
            mBluetoothScanHelper.format(scanResult?.device, scanResult?.rssi, scanResult?.scanRecord?.bytes)
        }

    }


    /**
     * API Level 21 未満用コールバック関数
     */
    class OldBleScanCallback(bluetooshScanHelper: BluetooshScanHelper) : BluetoothAdapter.LeScanCallback {

        val mBluetoothScanHelper: BluetooshScanHelper = bluetooshScanHelper

        override fun onLeScan(device: BluetoothDevice, rssi: Int, bytes: ByteArray) {
            mBluetoothScanHelper.format(device, rssi, bytes)
        }

    }

}

