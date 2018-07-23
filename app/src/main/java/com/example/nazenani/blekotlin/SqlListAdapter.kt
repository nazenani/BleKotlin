package com.example.nazenani.blekotlin

import android.content.Context
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.BaseAdapter
import com.example.nazenani.blekotlin.databinding.ItemBinding
import kotlinx.android.synthetic.main.item2.view.*

/**
 * Beaconデータクラスを使用したアダプタ
 */
class SqlListAdapter: ArrayAdapter<Beacon> {

    constructor(context: Context?, resource: Int) : super(context, resource)
    constructor(context: Context?, resource: Int, textViewResourceId: Int) : super(context, resource, textViewResourceId)
    constructor(context: Context?, resource: Int, objects: Array<out Beacon>?) : super(context, resource, objects)
    constructor(context: Context?, resource: Int, textViewResourceId: Int, objects: Array<out Beacon>?) : super(context, resource, textViewResourceId, objects)
    constructor(context: Context?, resource: Int, objects: MutableList<Beacon>?) : super(context, resource, objects)
    constructor(context: Context?, resource: Int, textViewResourceId: Int, objects: MutableList<Beacon>?) : super(context, resource, textViewResourceId, objects)

    var inflater: LayoutInflater


    init {
        inflater = LayoutInflater.from(context)
    }


    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {

        val newView = convertView ?: inflater.inflate(R.layout.item2, null)

        getItem(position)?.run {

            val rssiAvg: Double = Math.rint((rssi_avg * 100)) / 100

            val distanceMin: Double = Math.rint((distance_min * 100)) / 100
            val distanceAvg: Double = Math.rint((distance_avg * 100)) / 100
            val distanceMax: Double = Math.rint((distance_max * 100)) / 100

            newView.is_ibeacon.text = is_ibeacon.toString()
            newView.uuid.text = uuid
            newView.address.text = address
            newView.rssi.text = rssi_min.toString() + " << " + rssiAvg.toString() + " << " + rssi_max.toString()
            newView.major.text = major
            newView.minor.text = minor
            newView.distance.text = distanceMin.toString() + " << " + distanceAvg.toString() + " << " + distanceMax.toString()
            newView.date.text = DateFormat.format("MM-dd HH:mm:ss", date_min * 1000L).toString() + " - " + DateFormat.format("MM-dd HH:mm:ss", date_max * 1000L).toString()
        }

        return newView

    }

/*
    override fun getItem(position: Int): Beacon? {
        return null
    }
*/

}