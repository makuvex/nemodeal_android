package com.jungbae.nemodeal.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.callbacks.onShow
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.jakewharton.rxbinding3.view.clicks
import com.jungbae.nemodeal.R
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.activity_deal_detail.*
import kotlinx.android.synthetic.main.progress_bar.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList


class DealDetailActivity : AppCompatActivity() {
    private val disposeBag = CompositeDisposable()
/*
    private lateinit var mealList: ArrayList<SimpleSchoolMealData>
    private lateinit var mealAdapter: MealDetailRecyclerAdapter

    var schoolCode: String? = null
    var officeCode: String? = null
    var schoolName: String? = null

    private lateinit var selectedBehaviorSubject: PublishSubject<SimpleSchoolMealData>
*/
    private var countDownTimer: CountDownTimer? = null
    private lateinit var url: String

    init {

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_deal_detail)

        intent?.let {
            url = it.getStringExtra("url")
        }

        initializeUI()
        bindRxUI()

        request()
    }

    override fun onDestroy() {
        super.onDestroy()
        disposeBag.clear()
    }

    fun initializeUI() {
        adView.loadAd(AdRequest.Builder().build())
        adView.adListener = object: AdListener() {
            override fun onAdLoaded() { Log.e("@@@","banner onAdLoaded") }
            override fun onAdFailedToLoad(errorCode : Int) { Log.e("@@@","banner onAdFailedToLoad code ${errorCode}") }
            override fun onAdOpened() {
                Log.e("@@@","onAdOpened")
            }
            override fun onAdLeftApplication() { Log.e("@@@","onAdLeftApplication") }
            override fun onAdClosed() { Log.e("@@@","onAdClosed") }
        }

        web_view.settings.javaScriptEnabled = true
        web_view.webViewClient = object: WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                Log.e("@@@","@@@ onPageFinished @@@")
                stopTimer()
            }

            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                Log.e("@@@","@@@ isForMainFrame ${request?.isForMainFrame}")
                Log.e("@@@","@@@ request ${request?.url}")
                Log.e("@@@","@@@ isRedirect ${request?.isRedirect}")
                Log.e("@@@","@@@ requestHeaders ${request?.requestHeaders}")
                Log.e("@@@","@@@ method ${request?.method}")
                Log.e("@@@","@@@ hasGesture ${request?.hasGesture()}")

                request?.let {
                    if(it.isRedirect || it.hasGesture()) {
                        startActivity(Intent(Intent.ACTION_VIEW, it.url))
                        //finish()
                        return true
                    }
                }

                return super.shouldOverrideUrlLoading(view, request)
            }
        }

    }

    fun bindRxUI() {
        val backDisposable = back.clicks()
            .throttleFirst(1, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                finish()
            }
        disposeBag.addAll(backDisposable)
    }

    fun showMaterialDialog() {
        AndroidSchedulers.mainThread().scheduleDirect {
            MaterialDialog(this).show {
                positiveButton(text = "확인") {
                    (windowContext as DealDetailActivity).finish()
                }
                onShow {
                    title(text = "알림")
                    message(text = "급식 정보가 없습니다.")
                }
            }
        }
    }

    fun request() {
        createTimerFor(100)
        web_view.loadUrl(url)
    }

    fun createTimerFor(millis: Long) {
        stopTimer()

        val max = 10000L
        wrap_progress_bar.visibility = View.VISIBLE
        progress_bar.progress = 0
        countDownTimer = object : CountDownTimer(max, millis) {
            override fun onTick(p0: Long) {
                val f: Float = (max  - p0)/max.toFloat()
                val p = (f * 100).toLong()
                Log.e("@@@","@@@ p0 ${p0}, p ${p}, ${p.toInt()}")

                progress_bar.progress = p.toInt()
            }

            override fun onFinish() {
                Log.e("@@@","@@@ onFinish")
                if(countDownTimer != null) {
                    createTimerFor(100)
                }
            }
        }
        countDownTimer?.start()
    }

    fun stopTimer() {
        wrap_progress_bar.visibility = View.GONE
        countDownTimer?.cancel()
        countDownTimer = null
    }
}
