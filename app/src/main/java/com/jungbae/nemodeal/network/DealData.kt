package com.jungbae.nemodeal.network

import com.google.android.gms.ads.formats.UnifiedNativeAd


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
                       var adUser: Int = 0,
                       var dayString: String?,
                       var timeString: String?,
                       var adItem: UnifiedNativeAd?)


data class UserModel(val result: User): BaseRespData()
data class User(val seq: Int, val comment: Int, val recommend: Int)

data class Keywords(val result: ArrayList<AlertKeyword>): BaseRespData()
data class AlertKeyword(val keyword: String = "", var alert: Int = 0)

data class BaseResult(val result: Unit?): BaseRespData()

//data class AdNativeModel(val ad: UnifiedNativeAd)