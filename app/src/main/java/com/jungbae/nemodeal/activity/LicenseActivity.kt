package com.jungbae.nemodeal.activity

import android.os.Bundle
import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.callbacks.onShow
import com.jakewharton.rxbinding3.view.clicks
import com.jungbae.nemodeal.R
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.activity_deal_detail.*
import kotlinx.android.synthetic.main.activity_deal_detail.back
import kotlinx.android.synthetic.main.activity_deal_detail.web_view
import kotlinx.android.synthetic.main.activity_license.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList


class LicenseActivity : AppCompatActivity() {
    private val disposeBag = CompositeDisposable()
/*
    private lateinit var mealList: ArrayList<SimpleSchoolMealData>
    private lateinit var mealAdapter: MealDetailRecyclerAdapter

    var schoolCode: String? = null
    var officeCode: String? = null
    var schoolName: String? = null

    private lateinit var selectedBehaviorSubject: PublishSubject<SimpleSchoolMealData>
*/

    private lateinit var url: String

    init {

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_license)

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
        web_view.settings.loadWithOverviewMode = true
        web_view.settings.useWideViewPort = true
        web_view.settings.javaScriptEnabled = true
        web_view.webViewClient = object: WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                Log.e("@@@","@@@ url $url")
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
                    (windowContext as LicenseActivity).finish()
                }
                onShow {
                    title(text = "알림")
                    message(text = "급식 정보가 없습니다.")
                }
            }
        }
    }

    fun request() {
        web_view.loadUrl(url)
    }
}
