package com.jungbae.schoolfood.view

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jungbae.nemodeal.network.HotDealInfo
import com.jungbae.nemodeal.view.HomeCardHolder
import io.reactivex.subjects.PublishSubject


class HomeRecyclerAdapter(private val list: List<HotDealInfo>,
                          private var selectSubject: PublishSubject<HotDealInfo>,
                          private var scrollSubject: PublishSubject<Int>): RecyclerView.Adapter<HomeCardHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeCardHolder {
        return HomeCardHolder(LayoutInflater.from(parent.context), parent)
    }

    override fun onBindViewHolder(holder: HomeCardHolder, position: Int) {
        //Log.e("@@@","@@@### onBindViewHolder position $position")
        scrollSubject.onNext(position)
        holder.bind(list.get(position), selectSubject)
    }

    override fun getItemCount(): Int {
        return list.size
    }

}