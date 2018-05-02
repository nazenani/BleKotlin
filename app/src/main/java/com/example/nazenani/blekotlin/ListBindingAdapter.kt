package com.example.nazenani.blekotlin

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.example.nazenani.blekotlin.databinding.ItemBinding

class ListBindingAdapter(val context: Context, val beaconGroup: MainActivity.BeaconGroup): BaseAdapter() {
    var inflater: LayoutInflater
    val beacons: MainActivity.BeaconGroup = beaconGroup


    init {
        inflater = LayoutInflater.from(context)
    }


    override fun getCount(): Int {
        return beacons.count
    }


    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
        var binding: ItemBinding
        if (convertView == null) {
            binding = ItemBinding.inflate(inflater, parent, false)
            binding.root.tag = binding
        } else {
            binding = convertView.tag as ItemBinding
        }
        binding?.beacon = getItem(position) as Beacon
        return binding?.root
    }


    override fun getItem(position: Int): Any? {
        return beaconGroup.beaconAt(position)
    }


    override fun getItemId(position: Int): Long {
        return position.toLong()
    }
}