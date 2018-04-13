package com.example.nazenani.blekotlin

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.BluetoothLeScanner
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.net.wifi.WifiManager
import android.util.Log
import android.widget.Toast
import android.support.v4.app.NotificationCompat.getExtras



class ScanReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {

        var scanMode: Int = intent!!.getIntExtra("android.bluetooth.intent.SCAN_MODE", 0x3);

        when (intent?.action) {
            // Bluetoothステータス変更時
            BluetoothAdapter.ACTION_STATE_CHANGED -> bluetoothActionStatusChanged(context, intent)

            // Wifiステータス変更時
            WifiManager.WIFI_STATE_CHANGED_ACTION -> wifiActionStatusChanged(context, intent)

            // GPSステータス変更時
            LocationManager.PROVIDERS_CHANGED_ACTION -> locationActionStatusChanged(context, intent)
        }
    }


    fun bluetoothActionStatusChanged(context: Context?, intent: Intent?) {
        Toast.makeText(context, "Bluetoothステータス変更時", Toast.LENGTH_SHORT).show()
        var status: Int = intent!!.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
        if (status == BluetoothAdapter.STATE_OFF) {
        }
        Log.d(this::class.java.name, status.toString())
    }


    fun wifiActionStatusChanged(context: Context?, intent: Intent?) {
        Toast.makeText(context, "Wifiステータス変更時", Toast.LENGTH_SHORT).show()

        // 変化前の状態を取得
        var previousState: Int = intent!!.getIntExtra(WifiManager.EXTRA_PREVIOUS_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);

        // 変化後の状態を取得
        var currentState: Int = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN)

        Log.d(this::class.java.name, previousState.toString() + " -> " + currentState.toString())

        // 取得できるのは以下の状態
        // WifiManager.WIFI_STATE_DISABLED
        // WifiManager.WIFI_STATE_DISABLING
        // WifiManager.WIFI_STATE_ENABLED
        // WifiManager.WIFI_STATE_ENABLING
        // WifiManager.WIFI_STATE_UNKNOWN
    }


    fun locationActionStatusChanged(context: Context?, intent: Intent?) {
        Toast.makeText(context, "GPSステータス変更時", Toast.LENGTH_SHORT).show()
        //var location = intent!!.getExtras().get(LocationManager.KEY_LOCATION_CHANGED) as Location

        var status: Int = intent!!.getIntExtra(LocationManager.GPS_PROVIDER, -1)

        Log.d(this::class.java.name, status.toString() + ":" + LocationManager.MODE_CHANGED_ACTION)
    }

}
