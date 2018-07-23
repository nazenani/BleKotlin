package com.example.nazenani.blekotlin

/**
 * Beacon用データクラス
 *
 * @param  Boolean is_ibeacon    Iビーコンフラグ
 * @param  String  uuid          UUID
 * @param  String  address       アドレス
 * @param  Int     rssi          RSSI
 * @param  String  major         メジャー
 * @param  String  minor         マイナー
 * @param  Double  distance      推定距離
 * @param  Int     rssi_min      最小RSSI
 * @param  Double  rssi_avg      平均RSSI
 * @param  Int     rssi_max      最大RSSI
 * @param  Int     distance_min  最小推定距離
 * @param  Double  distance_avg  平均推定距離
 * @param  Int     distance_max  最大推定距離
 * @param  Int     date_min      最小計測日時
 * @param  Int     date_max      最大計測日時
 * @return void
 */
data class Beacon(
        val is_ibeacon: Boolean = false,
        val uuid: String = "",
        val address: String = "",
        val rssi: Int = 0,
        val major: String = "",
        val minor: String = "",
        val distance: Double = 0.0,
        val rssi_min: Int = 0,
        val rssi_avg: Double = 0.0,
        val rssi_max: Int = 0,
        val distance_min: Double = 0.0,
        val distance_avg: Double = 0.0,
        val distance_max: Double = 0.0,
        val date_min: Int = 0,
        val date_max: Int = 0
) {
}