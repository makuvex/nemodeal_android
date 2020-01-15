package com.jungbae.nemodeal.view

import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.RecyclerView
import coil.api.load
import com.jungbae.nemodeal.CommonApplication
import com.jungbae.nemodeal.R
import com.jungbae.nemodeal.True
import com.jungbae.nemodeal.network.HotDealInfo
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.home_card_row.view.*
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import java.text.SimpleDateFormat
import java.util.*


class HomeCardHolder(inflater: LayoutInflater, parent: ViewGroup):
    RecyclerView.ViewHolder(inflater.inflate(R.layout.home_card_row, parent, false)) {

    var data: HotDealInfo? = null

    fun bind(data: HotDealInfo, selectSubject: PublishSubject<HotDealInfo>) {
        this.data = data
        updateUI(data)

        itemView.setOnClickListener {
            selectSubject?.let {
                it.onNext(data)
            }
        }
    }

    private fun updateUI(data: HotDealInfo) {
        itemView.site_icon.load(data.siteIcon)
        itemView.category.text = "[" + data.category + "]"
        itemView.title.text = data.title

        itemView.thumbnail.visibility = if(data.thumbnail == null) View.GONE else View.VISIBLE
        data.thumbnail?.let {
            MainScope().async {
                itemView.thumbnail.load(data.thumbnail)
            }
            itemView.thumbnail.clipToOutline = true
        }

        itemView.like_count.text = data.recommend.toString()

        if(data.decommend.toString() == "0") {
            itemView.unlike_count.visibility = View.GONE
            itemView.unlike_image.visibility = View.GONE
        } else {
            itemView.unlike_count.visibility = View.VISIBLE
            itemView.unlike_image.visibility = View.VISIBLE
        }
        itemView.unlike_count.text = data.decommend.toString()
        itemView.reg_date.text = ""
        itemView.invalid_item.visibility = if(data.articleEnd.True()) View.VISIBLE else View.GONE

//        val formatData=    when (data.regDate.contains("-")) {
//                true -> SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(data.regDate)
//                false -> SimpleDateFormat("yyyy.MM.dd HH:mm:ss").parse(data.regDate)
//        }

        //val current = System.currentTimeMillis()
        //val date = Date(current)
        //var sdfNow = SimpleDateFormat("yyyyMMdd")
        //val today = sdfNow.format(current)

        val seperated = data.regDate.split(" ")
        if(isToday(seperated[0])) {
            //Log.e("@@@","@@@############### today ${seperated[0]}")
            if (seperated.size > 1) {
                val time = seperated[1].split(":")
                itemView.reg_date.text = time[0] + ":" + time[1]
            }
        } else {
            //Log.e("@@@","@@@ no today ${seperated[0].substring(5)}")
            itemView.reg_date.text = seperated[0].substring(5)
        }
    }

    private fun isToday(date: String): Boolean {
        val format = if(date.contains(".")) "yyyy.MM.dd" else "yyyy-MM-dd"
        val current = System.currentTimeMillis()
        val sdfNow = SimpleDateFormat(format)
        val today = sdfNow.format(current)

        return date == today
    }
}