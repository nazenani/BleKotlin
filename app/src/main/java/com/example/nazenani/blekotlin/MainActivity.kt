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
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), PermissionHelper {
    private val TAG: String = this::class.java.name

    override val message: String? get() = null
    override val caption: String? get() = null
    override val REQUEST_CODE: Int get() = 1
    override val PERMISSION: String get() = Manifest.permission.ACCESS_FINE_LOCATION

    private var mBtAdapter: BluetoothAdapter? = null;
    private var mBroadcastReceiver: LocalBroadcastManager? = null;

    private var mScanResults: MutableMap<String?, BluetoothDevice?>? = null
    private var mBleScanCallback: BleScanCallback? = null
    private var mBluetoothManager: BluetoothManager? = null


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

        // ペアリング済みデバイス一覧を取得
        //var devices: Set<BluetoothDevice> = mBtAdapter?.bondedDevices!!
        //for (device: BluetoothDevice in devices) {
            //Log.d(TAG, "Device : " + device.name + "(" + device.bondState + ")(" + device.address + ")")
        //}

        // ブルートゥースをスキャン
        mScanResults = mutableMapOf()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Log.d(TAG, "API level 21 以上")
            //val scanResults = mutableMapOf<String?, BluetoothDevice?>()
            mBleScanCallback = BleScanCallback(mScanResults!!)
            mBtAdapter?.bluetoothLeScanner?.startScan(mBleScanCallback)
        } else {
            Log.d(TAG, "API level 21 未満")
            mBluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            mBluetoothManager!!.adapter.startLeScan(OldBleScanCallback(mScanResults!!))
        }


        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }

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


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    class BleScanCallback(resultMap: MutableMap<String?, BluetoothDevice?>) : ScanCallback() {

        var resultOfScan = resultMap

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
            val bleDevice = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                scanResult?.device
            } else {
                TODO("VERSION.SDK_INT < LOLLIPOP")
            }
            val deviceAddress = bleDevice?.address
            resultOfScan[deviceAddress] = bleDevice
        }
    }


    class OldBleScanCallback(resultMap: MutableMap<String?, BluetoothDevice?>) : BluetoothAdapter.LeScanCallback {

        var resultOfScan = resultMap

        override fun onLeScan(device: BluetoothDevice, rssi: Int, bytes: ByteArray) {
            Log.d(this::class.java.name, device.name + "(" + device.address + ")" + ":" + rssi + ":" + bytes)
            resultOfScan[device.address] = device
        }

    }


}
