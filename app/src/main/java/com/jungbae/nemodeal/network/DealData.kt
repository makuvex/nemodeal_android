package com.jungbae.nemodeal.network

import com.google.gson.annotations.SerializedName


data class CategoryData(val result: ArrayList<DealSite>): BaseRespData()
data class DealSite(val id: Int, var name: String)

data class HotDealData(val result: ArrayList<HotDealInfo>): BaseRespData()
data class HotDealInfo(val siteIcon: String,
                       val articleId: Int,
                       val title: String,
                       val comment: Int,
                       val category: String,
                       val recommend: Int,
                       val decommend: Int,
                       val url: String,
                       val articleEnd: Int,
                       val thumbnail: String?,
                       val regDate: String,
                       var adUser: Int?,
                       var dayString: String?,
                       var timeString: String?)


data class UserModel(val seq: Int, val comment: Int, val recommend: Int): BaseRespData()

data class AlertKeyword(val keyword: String, val alert: Int): BaseRespData()

data class BaseResult(val result: Unit?): BaseRespData()
