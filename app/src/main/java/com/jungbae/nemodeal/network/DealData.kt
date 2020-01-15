package com.jungbae.nemodeal.network

import com.google.gson.annotations.SerializedName


data class CategoryData(val result: ArrayList<DealSite>): BaseRespData()
data class DealSite(val id: Int, var name: String)

data class HotDealData(val result: ArrayList<HotDealInfo>): BaseRespData()
data class HotDealInfo(val siteId: Int,
                       val siteIcon: String,
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


data class UserModel(val result: User): BaseRespData()
data class User(val seq: Int, val comment: Int, val recommend: Int)

data class Keywords(val result: ArrayList<AlertKeyword>): BaseRespData()
data class AlertKeyword(val keyword: String = "", var alert: Int = 0)

data class BaseResult(val result: Unit?): BaseRespData()
