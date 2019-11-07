package com.example.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        recycler.layoutManager = LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false)
        recycler.adapter = SnapHelperAdapter(this,initData())
        GallerySnapHelper().attachToRecyclerView(recycler)
    }

    private fun initData(): List<String> {
        val list = arrayListOf<String>()
        (0 until 60).mapTo(list){ "$it=atu"}
        return list
    }
}
