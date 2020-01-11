package com.jungbae.nemodeal.activity

import android.os.Bundle
import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.callbacks.onShow
import com.jakewharton.rxbinding3.view.clicks
import com.jungbae.nemodeal.R
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.activity_deal_detail.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList


class KeywordActivity : AppCompatActivity() {
    private val disposeBag = CompositeDisposable()
/*
    private lateinit var mealList: ArrayList<SimpleSchoolMealData>
    private lateinit var mealAdapter: MealDetailRecyclerAdapter

    var schoolCode: String? = null
    var officeCode: String? = null
    var schoolName: String? = null

    private lateinit var selectedBehaviorSubject: PublishSubject<SimpleSchoolMealData>
*/



    init {

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_keyword)
        //window.statusBarColor = ContextCompat.getColor(this, R.color.colorPrimary)

        initializeUI()
        bindRxUI()

        request()
    }

    override fun onDestroy() {
        super.onDestroy()
        disposeBag.clear()
    }


    fun initializeUI() {

    }

    fun bindRxUI() {
//        val backDisposable = back.clicks()
//            .throttleFirst(1, TimeUnit.SECONDS)
//            .observeOn(AndroidSchedulers.mainThread())
//            .subscribe {
//                finish()
//            }
//        disposeBag.addAll(backDisposable)
    }

    fun showMaterialDialog() {
        AndroidSchedulers.mainThread().scheduleDirect {
            MaterialDialog(this).show {
                positiveButton(text = "확인") {
                    (windowContext as KeywordActivity).finish()
                }
                onShow {
                    title(text = "알림")
                    message(text = "급식 정보가 없습니다.")
                }
            }
        }
    }

    fun request() {
        //web_view.loadUrl(url)
    }
}
