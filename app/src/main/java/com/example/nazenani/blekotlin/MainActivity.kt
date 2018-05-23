package com.example.nazenani.blekotlin

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AlphaAnimation
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*

/**
 * BEACONの仕様について下記を参考
 * TODO https://www.gaprot.jp/pickup/ibeacon/abeacon
 * TODO https://qiita.com/TakahikoKawasaki/items/a2062147b5fa82abc0b3
 */

class MainActivity : AppCompatActivity(), PermissionHelper, BeaconListener {
    private val TAG: String = this::class.java.name

    // TODO 許可を求める処理を複数可にする

    override val message: String? get() = null
    override val caption: String? get() = null
    override val REQUEST_CODE: Int get() = 101
    override val PERMISSIONS: Array<String> get() = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)

    private var mBtAdapter: BluetoothAdapter? = null;
    private var mBroadcastReceiver: LocalBroadcastManager? = null;

    private var mScanFlag: Boolean = false

    private var mBeaconList: MutableList<Beacon>? = null
    private var mListBindingAdapter: ListBindingAdapter? = null



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

        // Bluetoothが有効か判定
        if (!mBtAdapter?.isEnabled!!) {

            // フェードインアニメーション
            val alphaAnimation: AlphaAnimation = AlphaAnimation(0f, 1f)
            // 表示時間を指定
            alphaAnimation.duration = 1000
            alphaAnimation.fillAfter = true
            // 実行
            button.startAnimation(alphaAnimation)

            // Bluetoothを有効にする
            button.setOnClickListener { view ->
                mBtAdapter?.enable()

                // アニメーション起動中に非表示にするとレイアウトが崩れるのでアニメーションを止める
                alphaAnimation.cancel()
                button.visibility = View.GONE
            }

        } else {
            button.visibility = View.GONE
        }

        // ペアリング済みデバイス一覧を取得
        //var devices: Set<BluetoothDevice> = mBtAdapter?.bondedDevices!!
        //for (device: BluetoothDevice in devices) {
            //Log.d(TAG, "Device : " + device.name + "(" + device.bondState + ")(" + device.address + ")")
        //}


        val bluetoothScanHelper =  BluetooshScanHelper(this, this)

        fab.setOnClickListener { view ->

            //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
            //        .setAction("Action", null).show()

            // ブルートゥースをスキャン
            if (mScanFlag) {
                bluetoothScanHelper.stopScan()
                mScanFlag = false
            } else {
                bluetoothScanHelper.startScan()
                mScanFlag = true
            }

        }


        // 更新可能なミュータブルリストを初期化
        mBeaconList = mutableListOf<Beacon>()

        // リストビュー表示用アダプタを初期化
        mListBindingAdapter = ListBindingAdapter(this, mBeaconList)
        list_view.adapter = mListBindingAdapter
    }


    override fun onResume() {
        Log.d(TAG, "onResume")

        // パーミッション判定
        requests(this)

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
        val intent: Intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        // Fragmentの場合はgetContext().getPackageName()
        val uri: Uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivity(intent)
    }


    /**
     * インターフェイスリスナー
     */
    override fun find(isIbeacon: Boolean, address: String, rssi: Int, proximityUuid: String, major: String, minor: String, distance: Double) {
        //val beacon: Beacon = Beacon(isIbeacon = isIbeacon, uuid = proximityUuid, address = address, rssi = rssi, major = major, minor = minor)
        //val mainActivityBinding: ItemBinding = DataBindingUtil.setContentView<ItemBinding>(this, R.layout.item)
        //mainActivityBinding.beacon = beacon

        // キーに対してミュータブルリスト内に同一のアドレスが存在すれば更新
        var key: Int? = null
        for ((index, value) in mBeaconList!!.withIndex()) {
            if (value.address == address) {
                key = index
                break
            }
        }

        // データクラスに値を格納
        val beacon: Beacon = Beacon(
            isIbeacon = isIbeacon,
            uuid = proximityUuid,
            address = address,
            rssi = rssi,
            major = major,
            minor = minor,
            distance = distance
        )

        // キーが存在しなければ追加し、キーが存在すれば更新
        if (key == null) {
            mBeaconList?.add(beacon)
        } else {
            mBeaconList?.set(key, beacon)
        }
        // アダプタに対してリスト全体を更新
        mListBindingAdapter!!.notifyDataSetChanged()
        Log.d("TAG", list_view.count.toString())
    }

}


/**
 * インターフェイス
 */
interface BeaconListener {
    fun find(isIbeacon: Boolean, address: String, rssi: Int, proximityUuid: String, major: String, minor: String, distance: Double)
}
