package com.example.nazenani.blekotlin

import android.content.Context
import android.support.annotation.IntegerRes
import android.util.Log
import org.jetbrains.anko.db.MapRowParser
import com.example.nazenani.blekotlin.SqliteDatabaseOpenHelper

class ListDataParser: MapRowParser<Beacon> {
    override fun parseRow(columns: Map<String, Any?>): Beacon {

        val isIbeacon: Boolean? = (columns["is_ibeacon"] is Long && columns["is_ibeacon"] as Long == 1L)

        val tmpRssiMin: Int? = if (columns["rssi_min"] is Long) (columns["rssi_min"] as Long).toInt() else 0
        val rssiMin: Int? = if (tmpRssiMin == 0 && columns["rssi_min"] is Int) columns["rssi_min"] as Int else tmpRssiMin

        val rssiAvg: Double? = if (columns["rssi_avg"] is Double) (columns["rssi_avg"] as Double).toDouble() else 0.0

        val tmpRssiMax: Int? = if (columns["rssi_max"] is Long) (columns["rssi_max"] as Long).toInt() else 0
        val rssiMax: Int? = if (tmpRssiMax == 0 && columns["rssi_max"] is Int) columns["rssi_max"] as Int else tmpRssiMax

        val tmpDateMin: Int? = if (columns["date_min"] is Long) (columns["date_min"] as Long).toInt() else 0
        val dateMin: Int? = if (tmpDateMin == 0 && columns["date_min"] is Int) columns["date_min"] as Int else tmpDateMin

        val tmpDateMax: Int? = if (columns["date_max"] is Long) (columns["date_max"] as Long).toInt() else 0
        val dateMax: Int? = if (tmpDateMax == 0 && columns["date_max"] is Int) columns["date_max"] as Int else tmpDateMax

        return Beacon(
                is_ibeacon = isIbeacon as Boolean,
                uuid = columns["uuid"] as String,
                address = columns["address"] as String,
                rssi_min = rssiMin as Int,
                rssi_avg = rssiAvg as Double,
                rssi_max = rssiMax as Int,
                major = columns["major"] as String,
                minor = columns["minor"] as String,
                distance_min = columns["distance_min"] as Double,
                distance_avg = columns["distance_avg"] as Double,
                distance_max = columns["distance_max"] as Double,
                date_min = dateMin as Int,
                date_max = dateMax as Int
        )
    }

}