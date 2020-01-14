package com.jungbae.schoolfood.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jungbae.nemodeal.network.AlertKeyword
import com.jungbae.nemodeal.network.HotDealInfo
import com.jungbae.nemodeal.view.KeywordAddHolder
import com.jungbae.nemodeal.view.KeywordListHolder
import com.jungbae.schoolfood.view.ViewHolderType.TYPE_FOOTER
import com.jungbae.schoolfood.view.ViewHolderType.TYPE_ITEM
import io.reactivex.subjects.PublishSubject

object ViewHolderType {
    val TYPE_ITEM       = 1
    val TYPE_FOOTER     = 2
}

enum class EditModeIndex(val index: Int) {
    VIEW(0),
    EDIT(1)
}

class KeywordRecyclerAdapter(private val list: List<AlertKeyword>,
                             private var toggleSubject: PublishSubject<AlertKeyword>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var mode: EditModeIndex = EditModeIndex.VIEW
        set(index)  {
            field = index
            KeywordListHolder.mode = index
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when(viewType) {
            TYPE_ITEM -> return KeywordListHolder(LayoutInflater.from(parent.context), parent)
            TYPE_FOOTER -> return KeywordAddHolder(LayoutInflater.from(parent.context), parent)
            else -> return KeywordListHolder(LayoutInflater.from(parent.context), parent)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder) {
            is KeywordListHolder -> (holder as KeywordListHolder).bind(list[position], toggleSubject)
            is KeywordAddHolder -> (holder as KeywordAddHolder).bind(toggleSubject)
            else -> {

            }
        }
    }

    override fun getItemCount(): Int {
        return list.size + 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == list.size) TYPE_FOOTER else TYPE_ITEM
    }
}