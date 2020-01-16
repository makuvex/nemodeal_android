package com.jungbae.nemodeal.network

import android.os.Build
import com.jungbae.nemodeal.BuildConfig
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

fun Int.getBoolean(): Boolean {
    return this == 1
}

fun Boolean.getInt(): Int {
    return if(this) 1 else 0
}

class NetworkService {

//    class HttpLogger: HttpLoggingInterceptor.Logger {
//        override fun log(message: String) {
//            Log.e("@@@","@@@ HttpLogger ${message}")
//        }
//
//    }

    companion object {
        private var apiInterface: NemoDealApiInterface? = null
        private var instance: NetworkService? = null

        fun getInstance(): NetworkService {
            when(instance) {
                null -> {instance = create()}
            }
            return instance?.let{ it } ?: create()
        }

        private fun create(): NetworkService {
            val logging: HttpLoggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val client: OkHttpClient = OkHttpClient.Builder().apply {
                addInterceptor { chain ->
                    val req = chain.request().newBuilder().apply {
                        header("version", BuildConfig.VERSION_NAME)
                        header("os_info", "android_" + Build.VERSION.SDK_INT.toString())
                        header("device", Build.MANUFACTURER + "_" + Build.MODEL)
                    }.build()

                    chain.proceed(req)
                }

                addInterceptor(logging)
            }.build()

            val retrofit = Retrofit.Builder()
                .baseUrl("http://makuvex7.cafe24.com:8080")
                //.baseUrl("http://192.168.0.106:5000")
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()

            when(apiInterface) {
                null -> { apiInterface = retrofit.create(NemoDealApiInterface::class.java)}
            }

            return NetworkService()
        }
    }

    fun getDealList(): Observable<CategoryData> {
        return apiInterface?.let {
            it.dealList().toObservable().compose(ioMain())
        } ?: Observable.empty()
    }

    fun getHotDeal(site: Int, id: Int? = null): Observable<HotDealData> {
        return apiInterface?.let {
            it.hotDeal(site, id ?: 0).toObservable().compose(ioMain())
        } ?: Observable.empty()
    }

    fun registUser(fcmToken: String, deviceId: String): Observable<UserModel> {
        return apiInterface?.let {
            it.registId(fcmToken, deviceId).toObservable().compose(ioMain())
        } ?: Observable.empty()
    }

    fun keyword(userSeq: String): Observable<Keywords> {
        return apiInterface?.let {
            it.keyword(userSeq).toObservable().compose(ioMain())
        } ?: Observable.empty()
    }

    fun registKeyword(keyword: String, userSeq: String): Observable<BaseResult> {
        return apiInterface?.let {
            it.registKeyword(keyword, userSeq).toObservable().compose(ioMain())
        } ?: Observable.empty()
    }

    fun updateKeyword(keyword: String, userSeq: String, alert: Boolean): Observable<BaseResult> {
        return apiInterface?.let {
            it.updateKeyword(keyword, userSeq, alert.getInt()).toObservable().compose(ioMain())
        } ?: Observable.empty()
    }

    fun deleteKeyword(keyword: String, userSeq: String): Observable<BaseResult> {
        return apiInterface?.let {
            it.deleteKeyword(keyword, userSeq).toObservable().compose(ioMain())
        } ?: Observable.empty()
    }

    fun <T> ioMain(): ObservableTransformer<T, T> {
        return ObservableTransformer { upstream ->
            upstream.subscribeOn(Schedulers.io()).unsubscribeOn(Schedulers.io()).observeOn(Schedulers.computation())
        }
    }
}