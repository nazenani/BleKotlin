package com.example.nazenani.blekotlin

/**
 * Beacon用データクラス
 */
data class Beacon(val isIbeacon: Boolean, val uuid: String, val address: String, val rssi: Int, val major: String, val minor: String, val distance: Double) {
}