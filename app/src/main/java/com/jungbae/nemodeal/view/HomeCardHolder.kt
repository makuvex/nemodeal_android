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
import com.jungbae.nemodeal.network.HotDealInfo
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.home_card_row.view.*
import java.text.SimpleDateFormat


class HomeCardHolder(inflater: LayoutInflater, parent: ViewGroup):
    RecyclerView.ViewHolder(inflater.inflate(R.layout.home_card_row, parent, false)) {

    var data: HotDealInfo? = null

    fun bind(data: HotDealInfo, selectSubject: PublishSubject<HotDealInfo>) {
        this.data = data

        itemView.site_icon.load(data.siteIcon)
        itemView.category.text = "[" + data.category + "]"
        itemView.title.text = data.title
        itemView.thumbnail.load(data.thumbnail)
        itemView.thumbnail.clipToOutline = true
        itemView.like_count.text = data.recommend.toString()
        itemView.unlike_count.text = data.decommend.toString()
        itemView.reg_date.text = ""

        val seperated = data.regDate.split(" ")
        if(seperated.size > 1) {
            val time = seperated[1].split(":")
            itemView.reg_date.text = time[0] + ":" + time[1]
        }
        //val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(data.regDate)

        //date.time


        /*
        itemView.school_name.text = data.name
        itemView.date.text = data.date

        if(data.meal.isNotEmpty()) {
            itemView.meal_info.text = data.meal.replace("<br/>", "\n")
            itemView.more.visibility = View.VISIBLE
        } else {
            itemView.meal_info.text = "급식 정보 없음\n(휴일, 방학 혹은 학교에서 급식 정보를\n제공하지 않습니다)"
            //itemView.more.visibility = View.GONE
        }
        itemView.increaseTouchArea(itemView.delete, 50)
        itemView.meal_time.text = data.mealKind
        itemView.extra_info.text = data.cal
*/

        itemView.setOnClickListener {
            selectSubject?.let {
                it.onNext(data)
            }
        }

        updateUI()
    }

    fun updateUI() {
//        when(option) {
//            true ->  {
//                val ani = AnimationUtils.loadAnimation(CommonApplication.context, R.anim.shake)
//                itemView.delete.startAnimation(ani)
//                itemView.delete.visibility = View.VISIBLE
//            }
//            false -> {
//                itemView.delete.clearAnimation()
//                itemView.delete.visibility = View.INVISIBLE
//            }
//        }
    }
}