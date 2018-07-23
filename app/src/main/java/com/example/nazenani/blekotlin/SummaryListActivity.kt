package com.example.nazenani.blekotlin

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_list.*
import org.jetbrains.anko.db.select

class SummaryListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)

        //val dbHelper: SqliteDatabaseOpenHelper = SqliteDatabaseOpenHelper.getInstance(this)
        //val dataList: List<Beacon> = dbHelper.readableDatabase.select(SqliteDatabaseOpenHelper.tableName).parseList<Beacon>(ListDataParser())
/*
        db.use
            val dataList: List<Beacon> = this.select(db.tableName).parseList<Beacon>(ListDataParser())
            list_view2.adapter = SqlListAdapter(baseContext, R.layout.item2).apply {
                addAll(dataList)
            }
        }
*/
        db.use {
            //val dataList: List<Beacon> = this.select(db.tableName, "`isIbeacon`, `uuid`, `address`, MIN(`rssi`) AS `rssiMin`, AVG(`rssi`) AS `rssiAvg`, MAX(`rssi`) AS `rssiMax`, `major`, `minor`, MIN(`distance`) AS `distanceMin`, AVG(`distance`) AS `distanceAvg`, MAX(`distance`) AS `distanceMax`, MIN(`date`) AS `dateMin`, MAX(`date`) AS `dateMax`")
            val dataList: List<Beacon> = this.select(db.tableName)
                    .column("`is_ibeacon`")
                    .column("`uuid`")
                    .column("`address`")
                    .column("MIN(`rssi`) AS `rssi_min`")
                    .column("AVG(`rssi`) AS `rssi_avg`")
                    .column("MAX(`rssi`) AS `rssi_max`")
                    .column("`major`")
                    .column("`minor`")
                    .column("MIN(`distance`) AS `distance_min`")
                    .column("AVG(`distance`) AS `distance_avg`")
                    .column("MAX(`distance`) AS `distance_max`")
                    .column("MIN(`date`) AS `date_min`")
                    .column("MAX(`date`) AS `date_max`")
                    //.whereArgs("(is_ibeacon = {isIbeacon})", "isIbeacon" to 1)
                    .groupBy("`address`")
                    .orderBy("`rssi_avg` DESC")
                    .parseList<Beacon>(ListDataParser())
            list_view2.adapter = SqlListAdapter(baseContext, R.layout.item2).apply {
                addAll(dataList)
            }
        }


    }

}