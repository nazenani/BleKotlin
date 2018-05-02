package com.example.nazenani.blekotlin

import android.Manifest
import android.annotation.TargetApi
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.databinding.DataBindingUtil
import android.location.LocationManager
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ListView
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import com.example.nazenani.blekotlin.databinding.ItemBinding
import kotlinx.android.synthetic.main.content_main.*


class MainActivity : AppCompatActivity(), PermissionHelper, BeaconListener {
    private val TAG: String = this::class.java.name

    override val message: String? get() = "許可が得られなかったので使用できません"
    override val caption: String? get() = "NG"
    override val REQUEST_CODE: Int get() = 1
    //override val PERMISSION: Array<String> get() = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
    override val PERMISSION: String get() = Manifest.permission.ACCESS_FINE_LOCATION

    private var mBtAdapter: BluetoothAdapter? = null;
    private var mBroadcastReceiver: LocalBroadcastManager? = null;

    private var mScanResults: MutableMap<String?, BluetoothDevice?>? = null
    private var mBleScanCallback: BleScanCallback? = null
    private var mOldBleScanCallback: BluetoothAdapter.LeScanCallback? = null
    private var mBluetoothManager: BluetoothManager? = null

    private var mScanFlag: Boolean = false

    private var beaconList: MutableList<Beacon>? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        // デバイスがBLEに対応していなければトースト表示
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "デバイスがBLEに対応していません", Toast.LENGTH_LONG).show()
            return
        }

        // BluetoothAdapterインスタンス取得
        mBtAdapter = BluetoothAdapter.getDefaultAdapter()

        // Bluetoothを有効にする
        if (!mBtAdapter?.isEnabled!!) {
            mBtAdapter?.enable()
        }

        mScanResults = mutableMapOf()
        mBleScanCallback = BleScanCallback(mScanResults!!)
        mOldBleScanCallback = OldBleScanCallback(mScanResults!!)

        // ペアリング済みデバイス一覧を取得
        //var devices: Set<BluetoothDevice> = mBtAdapter?.bondedDevices!!
        //for (device: BluetoothDevice in devices) {
            //Log.d(TAG, "Device : " + device.name + "(" + device.bondState + ")(" + device.address + ")")
        //}

        fab.setOnClickListener { view ->
            //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
            //        .setAction("Action", null).show()


            // ブルートゥースをスキャン
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Log.d(TAG, "API level 21 以上:" + mScanFlag.toString())
                if (mScanFlag) {
                    mBtAdapter?.bluetoothLeScanner?.stopScan(mBleScanCallback)
                    mScanFlag = false
                } else {
                    mBtAdapter?.bluetoothLeScanner?.startScan(mBleScanCallback)
                    mScanFlag = true
                }
            } else {
                Log.d(TAG, "API level 21 未満:" + mScanFlag.toString())
                mBluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
                if (mScanFlag) {
                    mBluetoothManager!!.adapter.stopLeScan(mOldBleScanCallback)
                    mScanFlag = false
                } else {
                    mBluetoothManager!!.adapter.startLeScan(mOldBleScanCallback)
                    mScanFlag = true
                }
            }


/*
            // TODO サンプル
            val beaconList = mutableListOf<Beacon>()
            for (i in 0..100) {
                beaconList.add(Beacon(
                        isIbeacon = true,
                        uuid = "uuid$i",
                        address = "address$i",
                        rssi = i,
                        major = "major$i",
                        minor = "minor$i"
                ))
            }
            val beaconGroup = BeaconGroup(beacons = beaconList)
            list_view.adapter = ListBindingAdapter(this, beaconGroup)
*/



            beaconList = mutableListOf<Beacon>()



        }

        mBleScanCallback?.listener = this

    }


    override fun onResume() {
        Log.d(TAG, "onResume")

        // パーミッション判定
        execute(this)

        // フィルタの追加
        val filter: IntentFilter = IntentFilter()
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
        filter.addAction(LocationManager.PROVIDERS_CHANGED_ACTION)

        // ブロードキャストレシーバに登録
        //mBroadcastReceiver = LocalBroadcastManager.getInstance(this)
        //mBroadcastReceiver!!.registerReceiver(ScanReceiver(), filter)
        //mBroadcastReceiver!!.sendBroadcast(Intent(BluetoothAdapter.ACTION_STATE_CHANGED).putExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice.EXTRA_DEVICE))
        registerReceiver(ScanReceiver(), filter)

        // スキャン開始
        //mBtAdapter!!.startDiscovery()
        super.onResume()
    }


    override fun onPause() {
        Log.d(TAG, "onPause")
        try {
            //mBroadcastReceiver!!.unregisterReceiver(ScanReceiver())
            unregisterReceiver(ScanReceiver())
        } catch (e: IllegalArgumentException) {
            Log.d(TAG, e.toString())
        }
        super.onPause()
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }


    /**
     * 権限確認
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super<PermissionHelper>.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }


    /**
     * 権限付与有
     */
    override fun onAllowed() {

    }


    /**
     * 権限付与無
     */
    override fun onDenied() {
        // 許可が得られなければアプリ設定へ誘導
        var intent: Intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        // Fragmentの場合はgetContext().getPackageName()
        var uri: Uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivity(intent)
    }


    /**
     * インターフェイスリスナー
     */
    override fun find(isIbeacon: Boolean, proximityUuid: String, address: String, rssi: Int, major: String, minor: String) {
        //val beacon: Beacon = Beacon(isIbeacon = isIbeacon, uuid = proximityUuid, address = address, rssi = rssi, major = major, minor = minor)
        //val mainActivityBinding: ItemBinding = DataBindingUtil.setContentView<ItemBinding>(this, R.layout.item)
        //mainActivityBinding.beacon = beacon



        //val beaconList = mutableListOf<Beacon>()
        //for (i in 0..100) {
            beaconList?.add(Beacon(
                    isIbeacon = isIbeacon,
                    uuid = proximityUuid,
                    address = address,
                    rssi = rssi,
                    major = major,
                    minor = minor
            ))
        //}
        val beaconGroup = BeaconGroup(beacons = beaconList!!)
        list_view.adapter = ListBindingAdapter(this, beaconGroup)


    }


    /**
     * API Level 21 以上用コールバック関数
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    class BleScanCallback(resultMap: MutableMap<String?, BluetoothDevice?>) : ScanCallback() {

        var resultOfScan = resultMap
        var listener: BeaconListener? = null

        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            addScanResult(result)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Log.d(this::class.java.name, "I found a ble device ${result?.scanRecord?.bytes} : ${result?.device?.address} : ${result?.rssi}")
            }
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            results?.forEach { result -> addScanResult(result) }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.d(this::class.java.name, "Bluetooth LE scan failed. Error code: $errorCode")
        }

        fun addScanResult(scanResult: ScanResult?) {
            resultOfScan[scanResult?.device?.address] = scanResult?.device

            // スキャンレコードが30バイト以上で6バイト目から9バイト目の値が0x4c000215の場合はiBeaconと判定
            var isIbeacon = false
            if (scanResult?.scanRecord?.bytes!!.size >= 30 && scanResult.scanRecord?.bytes!![5] == 0x4c.toByte() && scanResult.scanRecord?.bytes!![6] == 0x00.toByte() && scanResult.scanRecord?.bytes!![7] == 0x02.toByte() && scanResult.scanRecord?.bytes!![8] == 0x15.toByte()) {
                isIbeacon = true
            }

            // 0埋めの16進数に変換
            val proximityUuid: String = arrayOf(
                                        "${String.format("%02x", scanResult.scanRecord?.bytes!![9])}${String.format("%02x", scanResult.scanRecord?.bytes!![10])}${String.format("%02x", scanResult.scanRecord?.bytes!![11])}${String.format("%02x", scanResult.scanRecord?.bytes!![12])}",
                                        "${String.format("%02x", scanResult.scanRecord?.bytes!![13])}${String.format("%02x", scanResult.scanRecord?.bytes!![14])}",
                                        "${String.format("%02x", scanResult.scanRecord?.bytes!![15])}${String.format("%02x", scanResult.scanRecord?.bytes!![16])}",
                                        "${String.format("%02x", scanResult.scanRecord?.bytes!![17])}${String.format("%02x", scanResult.scanRecord?.bytes!![18])}",
                                        "${String.format("%02x", scanResult.scanRecord?.bytes!![19])}${String.format("%02x", scanResult.scanRecord?.bytes!![20])}${String.format("%02x", scanResult.scanRecord?.bytes!![21])}${String.format("%02x", scanResult.scanRecord?.bytes!![22])}${String.format("%02x", scanResult.scanRecord?.bytes!![23])}${String.format("%02x", scanResult.scanRecord?.bytes!![24])}"
                                    ).joinToString("-").toUpperCase()

            // メジャー値とマイナー値を取得
            val major: String = String.format("%02x", scanResult.scanRecord?.bytes!![25]) + String.format("%02x", scanResult.scanRecord?.bytes!![26])
            val minor: String = String.format("%02x", scanResult.scanRecord?.bytes!![27]) + String.format("%02x", scanResult.scanRecord?.bytes!![28])

            // インターフェイスのリスナーに登録
            listener?.find(isIbeacon, proximityUuid, scanResult.device.address, scanResult.rssi, major.toUpperCase(), minor.toUpperCase())

            Log.d("addScanResult", "${isIbeacon}:${scanResult.device.address}:${proximityUuid}:${scanResult.rssi}::${major.toUpperCase()}:${minor.toUpperCase()}")
        }
    }


    /**
     * API Level 21 未満用コールバック関数
     */
    class OldBleScanCallback(resultMap: MutableMap<String?, BluetoothDevice?>) : BluetoothAdapter.LeScanCallback {

        var resultOfScan = resultMap

        override fun onLeScan(device: BluetoothDevice, rssi: Int, bytes: ByteArray) {
            Log.d(this::class.java.name, device.name + "(" + device.address + ")" + ":" + rssi + ":" + bytes)
            resultOfScan[device.address] = device
        }

    }


    /**
     * サンプル
     */
    class BeaconGroup(private val beacons: List<Beacon>) {
        val count: Int = beacons.count()

        fun beaconAt(index: Int): Beacon {
            return beacons[index]
        }
    }


}


/**
 * インターフェイス
 */
interface BeaconListener {
    fun find(isIbeacon: Boolean, proximityUuid: String, address: String, rssi: Int, major: String, minor: String)
}
