package com.jungbae.nemodeal.network

import android.util.Log
import io.reactivex.observers.DisposableObserver


open class ObservableResponse<T>(val onSuccess: ((T) -> Unit)? = {}, val onError: ((T?) -> Unit)? = {}): DisposableObserver<T>() {

    override fun onNext(t: T) {
        when(checkValidResponseCode(t)) {
            true -> {
                // 정상 응답이나 response 값이 없는 경우
                onSuccess?.let {
                    it(t)
                }
            }
            false -> {
                onError?.let {
                    it(t)
                }
            }
        }
    }

    override fun onError(e: Throwable) {
        Log.d("@@@", "onError 1")
        onError?.let {
            it(null)
        }
    }

    override fun onComplete() {
        Log.d("@@@", "onComplete 1")
    }

    private fun checkValidResponseCode(t: T): Boolean {
        if (t as? BaseRespData != null) {
            return validDealData(t)
        }
        return false
    }

    private fun validDealData(t: T): Boolean {
        val resp: BaseRespData = t as BaseRespData
        if(resp.code == "2000") {
            return true
        }
        return false
    }
}