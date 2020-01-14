package com.jungbae.nemodeal.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jungbae.nemodeal.R
import com.jungbae.nemodeal.network.AlertKeyword
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.keyword_add_row.view.*
import kotlinx.android.synthetic.main.keyword_list_row.view.*


class KeywordAddHolder(inflater: LayoutInflater, parent: ViewGroup):
    RecyclerView.ViewHolder(inflater.inflate(R.layout.keyword_add_row, parent, false)) {


    fun bind(toggleSubject: PublishSubject<AlertKeyword>) {

        itemView.add.setOnClickListener {
            toggleSubject?.let {
                it.onNext(AlertKeyword())
            }
        }

        //updateUI()
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