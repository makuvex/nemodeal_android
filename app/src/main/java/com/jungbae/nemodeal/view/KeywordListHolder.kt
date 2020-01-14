package com.jungbae.nemodeal.view

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import com.jungbae.nemodeal.CommonApplication
import com.jungbae.nemodeal.R
import com.jungbae.nemodeal.network.AlertKeyword
import com.jungbae.nemodeal.network.getInt
import com.jungbae.schoolfood.view.EditModeIndex
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.keyword_list_row.view.*


class KeywordListHolder(inflater: LayoutInflater, parent: ViewGroup):
    RecyclerView.ViewHolder(inflater.inflate(R.layout.keyword_list_row, parent, false)) {

    companion object {
        open var mode: EditModeIndex = EditModeIndex.VIEW
    }

    var data: AlertKeyword? = null

    fun bind(data: AlertKeyword, toggleSubject: PublishSubject<AlertKeyword>) {
        this.data = data

        itemView.keyword.text = data.keyword
        itemView.on_off_button.isSelected = data.alert == 1

        var resource: Int
        when(mode) {
            EditModeIndex.VIEW -> { resource = R.drawable.on_off_selector }
            EditModeIndex.EDIT -> { resource = R.drawable.delete  }
        }
        itemView.on_off_button.background = AppCompatResources.getDrawable(CommonApplication.context, resource)

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

        itemView.on_off_button.setOnClickListener { view ->
            toggleSubject?.let {
                view.isSelected = !view.isSelected
                it.onNext(AlertKeyword(data.keyword, view.isSelected.getInt()))
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