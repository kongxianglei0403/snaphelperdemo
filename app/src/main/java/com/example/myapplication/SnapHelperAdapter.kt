package com.example.myapplication

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/**
 *Create by 阿土 on ${date}
 */
class SnapHelperAdapter(val context: Context,val list: List<String>): RecyclerView.Adapter<SnapHelperAdapter.SnapHelperViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SnapHelperViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_layout,parent,false)
        return SnapHelperViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: SnapHelperViewHolder, position: Int) {
        holder.tv.text = list[position]
    }

    inner class SnapHelperViewHolder(view: View): RecyclerView.ViewHolder(view){
        val tv = view.findViewById(R.id.tv) as TextView
    }
}