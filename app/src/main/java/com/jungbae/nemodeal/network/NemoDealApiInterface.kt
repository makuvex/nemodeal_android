package com.jungbae.nemodeal.network

import io.reactivex.Single
import retrofit2.http.*

interface NemoDealApiInterface {

    @GET(ApiSetting.Service.DealList)
    fun dealList(): Single<CategoryData>

    @GET(ApiSetting.Service.HotDeal)
    fun hotDeal(@Query("dbTableIndex") dbTableIndex: Int,
                @Query("id") id: Int): Single<HotDealData>

    @POST(ApiSetting.Service.User)
    fun registId(@Query("fcmToken") fcmToken: String,
                 @Query("deviceId") deviceId: String): Single<UserModel>

    @GET(ApiSetting.Service.Keyword)
    fun keyword(@Query("userSeq") userSeq: String): Single<AlertKeyword>

    @POST(ApiSetting.Service.Keyword)
    fun registKeyword(@Query("keyword") keyword: String,
                      @Query("userSeq") userSeq: String): Single<BaseResult>

    @PUT(ApiSetting.Service.Keyword)
    fun updateKeyword(@Query("keyword") keyword: String,
                      @Query("userSeq") userSeq: String,
                      @Query("alert") alert: Int): Single<BaseResult>

    @DELETE(ApiSetting.Service.Keyword)
    fun deleteKeyword(@Query("keyword") keyword: String,
                      @Query("userSeq") userSeq: String): Single<BaseResult>

}