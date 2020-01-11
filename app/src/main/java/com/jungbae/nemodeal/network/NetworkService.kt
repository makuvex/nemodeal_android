package com.jungbae.nemodeal.network

import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

fun Int.getBoolean(): Boolean {
    if(this == 1) {
        return true
    }
    return false
}

fun Boolean.getInt(): Int {
    if(this) {
        return 1
    }
    return 0
}

class NetworkService {

    companion object {
        private var t: NemoDealApiInterface? = null
        private var instance: NetworkService? = null

        fun getInstance(): NetworkService {
            if(instance == null) {
                instance = create()

            }
            return instance?.let{ it } ?: create()
        }

        private fun create(): NetworkService {
            val interceptor: HttpLoggingInterceptor = HttpLoggingInterceptor().apply {
                this.level = HttpLoggingInterceptor.Level.BODY
            }

            val client : OkHttpClient = OkHttpClient.Builder().apply {
                this.addInterceptor(interceptor)
            }.build()


            val retrofit = Retrofit.Builder()
                .baseUrl("http://makuvex7.cafe24.com:8080")
                //.baseUrl("http://192.168.0.106:5000")
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()

            if(t == null) {
                t = retrofit.create(NemoDealApiInterface::class.java)
            }

            return NetworkService()
        }
    }

    fun getDealList(): Observable<CategoryData> {
        return t?.let {
            it.dealList().toObservable().compose(ioMain())
        } ?: Observable.empty()
    }

    fun getHotDeal(site: Int, id: Int? = null): Observable<HotDealData> {
        return t?.let {
            it.hotDeal(site, 0).toObservable().compose(ioMain())
        } ?: Observable.empty()
    }

    fun registUser(fcmToken: String, deviceId: String): Observable<UserModel> {
        return t?.let {
            it.registId(fcmToken, deviceId).toObservable().compose(ioMain())
        } ?: Observable.empty()
    }

    fun keyword(userSeq: String): Observable<AlertKeyword> {
        return t?.let {
            it.keyword(userSeq).toObservable().compose(ioMain())
        } ?: Observable.empty()
    }

    fun registKeyword(keyword: String, userSeq: String): Observable<BaseResult> {
        return t?.let {
            it.registKeyword(keyword, userSeq).toObservable().compose(ioMain())
        } ?: Observable.empty()
    }

    fun updateKeyword(keyword: String, userSeq: String, alert: Boolean): Observable<BaseResult> {
        return t?.let {
            it.updateKeyword(keyword, userSeq, alert.getInt()).toObservable().compose(ioMain())
        } ?: Observable.empty()
    }

    fun deleteKeyword(keyword: String, userSeq: String): Observable<BaseResult> {
        return t?.let {
            it.deleteKeyword(keyword, userSeq).toObservable().compose(ioMain())
        } ?: Observable.empty()
    }

    fun <T> ioMain(): ObservableTransformer<T, T> {
        return ObservableTransformer { upstream ->
            upstream.subscribeOn(Schedulers.io()).unsubscribeOn(Schedulers.io()).observeOn(Schedulers.computation())
        }
    }
}