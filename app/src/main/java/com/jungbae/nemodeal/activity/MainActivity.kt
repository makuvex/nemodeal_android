package com.jungbae.nemodeal.activity

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import androidx.core.view.GravityCompat
import androidx.appcompat.app.ActionBarDrawerToggle
import android.view.MenuItem
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.view.Menu
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.callbacks.onShow
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.formats.NativeAdOptions
import com.google.android.gms.ads.formats.UnifiedNativeAd
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging
import com.jungbae.nemodeal.BuildConfig.ad_native_id
import com.jungbae.nemodeal.R
import com.jungbae.nemodeal.network.*
import com.jungbae.nemodeal.showToast
import com.jungbae.schoolfood.view.HomeRecyclerAdapter
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.toObservable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.progress_bar.*
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private val disposeBag = CompositeDisposable()
    private lateinit var hotDealList: ArrayList<HotDealInfo>
    private lateinit var cardAdapter: HomeRecyclerAdapter

    private lateinit var selectItemSubject: PublishSubject<HotDealInfo>
    //private lateinit var categorySubject: PublishSubject<ArrayList<DealSite>>
    private lateinit var adLoader: AdLoader

    private lateinit var categorySet: MutableMap<Int, Int>
    var countDownTimer: CountDownTimer? = null
    protected var loadedAds = arrayListOf<FeedAdModel>()

    var lastPosition: Int = 0
        get() = (recycler_view.layoutManager as LinearLayoutManager)?.findLastVisibleItemPosition()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //window.statusBarColor = ContextCompat.getColor(this, R.color.colorPrimary)
        setContentView(R.layout.activity_main)

        initializeUI()
        bindUI()

//        FirebaseInstanceId.getInstance().instanceId
//            .addOnCompleteListener(OnCompleteListener { task ->
//                if (!task.isSuccessful) {
//                    Log.e("@@@", "getInstanceId failed", task.exception)
//                    return@OnCompleteListener
//                }
//
//                // Get new Instance ID token
//                val token = task.result?.token
//
//                // Log and toast
//                Log.e("@@@", "@@@ token $token")
//            })

        createTimerFor(100)
        requestCategory()

        Log.e("@@@","@@@ Create intent ${intent?.getStringExtra("link")}")
//        intent?.getStringExtra("link")?.let {link ->
//            applicationContext.showDialog("링크로 이동 할까요?") {
//                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link)))
//            }
//        }

        //val toolbar: Toolbar = findViewById(R.id.toolbar)
        //Drawable drawable = ContextCompat.getDrawable(getApplicationContext(),R.drawable.change_pass);
        //toolbar.overflowIcon = ContextCompat.getDrawable(applicationContext, R.drawable.keyword)

//        val fab: FloatingActionButton = findViewById(R.id.fab)
//        fab.setOnClickListener { view ->
//
//            Log.e("@@@","@@@ floating")
//
//            requestCategory()
//            NetworkService.getInstance().getDealList().observeOn(AndroidSchedulers.mainThread()).subscribeWith(ObservableResponse<CategoryData>(
//                onSuccess = {
//                    Log.e("@@@", "@@@ onSuccess ${it.reflectionToString()}")
//                }, onError = {
//                    Log.e("@@@", "@@@ error $it")
//                }
//            ))


//            NetworkService.getInstance().getHotDeal(0).observeOn(AndroidSchedulers.mainThread()).subscribeWith(ObservableResponse<HotDealData>(
//                onSuccess = {
//                    Log.e("@@@", "@@@ onSuccess ${it.reflectionToString()}")
//                }, onError = {
//                    Log.e("@@@", "@@@ error $it")
//                }
//            ))


//            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                .setAction("Action", null).show()
//        }


    }

    init {
        Log.e("@@@","@@@ init")
        selectItemSubject = PublishSubject.create()

        hotDealList = ArrayList()
        cardAdapter = HomeRecyclerAdapter(hotDealList, selectItemSubject)
        categorySet = mutableMapOf()


    }

    fun initializeUI() {
        setSupportActionBar(toolbar)

        recycler_view.apply {
            layoutManager = LinearLayoutManager(applicationContext)
            adapter = cardAdapter
        }.addOnScrollListener(object: RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                //Log.e("@@@","@@@ onScrolled lastPosition $lastPosition, dy $dy")

                categorySet.filterValues{ hotDealList[lastPosition].articleId == it }?.run {
                    if(isNotEmpty()) {
                        recyclerView.stopScroll()
                        requestLoadMore(keys.first())
                    }
                }
            }
        })

        val toggle = ActionBarDrawerToggle(
            this, drawer_layout, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )

        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)

        swipe_refresh.setOnRefreshListener {
            requestCategory()
        }

        loadAd {
            it?.let {

            }
        }
    }

    fun loadAd(onLoaded: (ad: UnifiedNativeAd?) -> Unit) {
        adLoader = AdLoader.Builder(this, ad_native_id)
            .forUnifiedNativeAd { ad : UnifiedNativeAd ->
                Log.e("@@@","@@@ forUnifiedNativeAd ${ad.reflectionToString()}")
                onLoaded(ad)
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(errorCode: Int) {
                    // Handle the failure by logging, altering the UI, and so on.
                    Log.e("@@@","@@@ onAdFailedToLoad")
                    onLoaded(null)
                }
                override fun onAdOpened() {
                    Log.e("@@@","@@@ onAdOpened")
                }
                override fun onAdLoaded() {
                    Log.e("@@@","@@@ onAdLoaded")
                }
                override fun onAdClicked() {
                    Log.e("@@@","@@@ onAdClicked")
                }
            })
            .withNativeAdOptions(NativeAdOptions.Builder()
                // Methods in the NativeAdOptions.Builder class can be
                // used here to specify individual options settings.
                .build())
            .build()

        adLoader.loadAds(AdRequest.Builder().build(), 1)
    }

    fun bindUI() {
        val itemClicksDisposable = selectItemSubject
            .throttleFirst(1, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                Log.e("@@@", "item clicks ${it}")
//                startActivity(Intent(this, DealDetailActivity::class.java)?.apply {
//                    putExtra("url", it.url)
//                })

                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(it.url)))

//                simpleSchoolMealData = meal
//                interstitialAdBlock()
            }

        disposeBag.addAll(itemClicksDisposable)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Log.e("@@@","@@@ onNew ${intent?.getStringExtra("link")}")
        intent?.getStringExtra("link")?.let {link ->
            showDialog("해당 삼품 페이지로 이동 할까요?", link) {
                if(it) {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link)))
                }
            }
        }

//        intent?.getStringExtra("link")?.let {
//            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(it)))
//        }
    }
    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    fun requestLoadMore(dbIndex: Int) {
        createTimerFor(100)
        categorySet.get(dbIndex)?.let {
            val disposable = requestHotDeal(dbIndex, it).subscribeWith(ObservableResponse<HotDealData>(
                onSuccess = {
                    Log.e("@@@", "@@@ onSuccess ${it.reflectionToString()}")

                    AndroidSchedulers.mainThread().scheduleDirect {
                        //recycler_view.visibility = View.INVISIBLE
                        //hotDealList.addAll(it.result)
                        val list = it.result
                        val lastIndex = hotDealList.size

                        try {
                            val sort = it.result.sortedWith(compareByDescending {
                                when (it.regDate.contains("-")) {
                                    true -> SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(it.regDate)
                                    false -> SimpleDateFormat("yyyy.MM.dd HH:mm:ss").parse(it.regDate)
                                }
                            })
                            Log.e("@@@", "@@@ $sort")

                            hotDealList.addAll(lastPosition+1, sort)
                            categorySet.set(it.result.first().siteId, it.result.last().articleId)
                        } catch(e: Exception) {
                            e.printStackTrace()
                        }
//                        sortByDate()
                        cardAdapter.notifyDataSetChanged()
                        applicationContext.showToast("핫딜 정보를 더 불러왔습니다.")
                        //recycler_view.layoutManager?.scrollToPosition(lastIndex)
                        stopTimer()

                        //swipe_refresh.isRefreshing = false
                        //recycler_view.visibility = View.VISIBLE
                    }
                }, onError = {
                    Log.e("@@@", "@@@ error $it")
                    stopTimer()
                }
            ))
            disposeBag.add(disposable)
        }



//        requestHotDeal(dbIndex, categorySet.get(dbIndex))
//        val task = NetworkService.getInstance().getDealList().observeOn(AndroidSchedulers.mainThread())
//            .flatMap {
//                it.result.toObservable()
//            }.flatMap {dealSite ->
//                val lastArticleId = hotDealList.findLast { it.siteId == dealSite.id }?.articleId ?: 0
//                Log.e("@@@","@@@ lastArticleId $lastArticleId, site ${dealSite.id}")
//                requestHotDeal(dealSite.id, lastArticleId)
//            }.subscribeWith(ObservableResponse<HotDealData>(
//                onSuccess = {
//                    Log.e("@@@", "@@@ onSuccess ${it.reflectionToString()}")
//
//                    AndroidSchedulers.mainThread().scheduleDirect {
//                        //recycler_view.visibility = View.INVISIBLE
//                        //hotDealList.addAll(it.result)
//                        val list = it.result
//                        val lastIndex = hotDealList.size
//
//                        try {
//                            val sort = it.result.sortedWith(compareByDescending {
//                                when (it.regDate.contains("-")) {
//                                    true -> SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(it.regDate)
//                                    false -> SimpleDateFormat("yyyy.MM.dd HH:mm:ss").parse(it.regDate)
//                                }
//                            })
//                            Log.e("@@@", "@@@ $sort")
//
//                            hotDealList.addAll(hotDealList.size, sort)
//                            categorySet.set(it.result.first().siteId, it.result.lastIndex)
//                        } catch(e: Exception) {
//                            e.printStackTrace()
//                        }
////                        sortByDate()
//                        cardAdapter.notifyDataSetChanged()
//                        applicationContext.showToast("핫딜 정보를 더 불러왔습니다.")
//                        //recycler_view.layoutManager?.scrollToPosition(lastIndex)
//
//                        //swipe_refresh.isRefreshing = false
//                        //recycler_view.visibility = View.VISIBLE
//                    }
//                }, onError = {
//                    Log.e("@@@", "@@@ error $it")
//                }
//            ))
//        disposeBag.add(task)
    }

    fun requestHotDeal(site: Int, id: Int = 0): Observable<HotDealData> {
        return NetworkService.getInstance().getHotDeal(site, id).observeOn(AndroidSchedulers.mainThread())
    }

    fun requestCategory() {
        hotDealList.clear()
        categorySet.clear()


        var obSize = 0
        var subscribeCount = 0

        val task = NetworkService.getInstance().getDealList().observeOn(AndroidSchedulers.mainThread())
            .doAfterNext {
                recycler_view.visibility = View.INVISIBLE
            }
            .flatMap {
                obSize = it.result.size
                it.result.toObservable()
            }.flatMap {
                subscribeCount++
                requestHotDeal(it.id)
            }.subscribeWith(ObservableResponse<HotDealData>(
                onSuccess = {
                    Log.e("@@@", "@@@ onSuccess ${it.reflectionToString()}")

                    AndroidSchedulers.mainThread().scheduleDirect {

                        hotDealList.addAll(it.result)

                        try {
                            val sort = hotDealList.sortedWith(compareByDescending {
                                when (it.regDate.contains("-")) {
                                    true -> SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(it.regDate)
                                    false -> SimpleDateFormat("yyyy.MM.dd HH:mm:ss").parse(it.regDate)
                                }
                            })
                            Log.e("@@@", "@@@ $sort")
                            hotDealList.clear()
                            hotDealList.addAll(sort)

                            categorySet.set(it.result.first().siteId, it.result.last().articleId)
                        } catch(e: Exception) {
                            e.printStackTrace()
                        }
//                        sortByDate()
                        cardAdapter.notifyDataSetChanged()
                        swipe_refresh.isRefreshing = false
                        if(subscribeCount == obSize) {
                            recycler_view.visibility = View.VISIBLE
                            stopTimer()
                        }
                    }
                }, onError = {
                    Log.e("@@@", "@@@ error $it")
                    stopTimer()
                }
            ))
        disposeBag.add(task)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.toolbar_layout, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.keyword -> {
                startActivity(Intent(this, KeywordActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.keyword -> {
                startActivity(Intent(this, KeywordActivity::class.java))
            }
            R.id.license -> {
                startActivity(Intent(this, LicenseActivity::class.java)?.apply {
                    putExtra("url", "http://makuvex7.cafe24.com/nemodeal_aos_license")
                })
            }
            R.id.version -> {
                showSingleDialog("버전 정보", "현재 버전: 1.0.0")
            }
        }
        /*
        when (item.itemId) {
            R.id.nav_home -> {
                // Handle the camera action
            }
            R.id.nav_gallery -> {

            }
            R.id.nav_slideshow -> {

            }
            R.id.nav_tools -> {

            }
            R.id.nav_share -> {

            }
            R.id.nav_send -> {

            }
        }
        */
        //val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        drawer_layout.closeDrawer(GravityCompat.START)
        return false
    }

    fun createTimerFor(millis: Long) {
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

    fun showDialog(title: String, msg: String, completion: ((Boolean) -> Unit)? = null) {
        AndroidSchedulers.mainThread().scheduleDirect {
            MaterialDialog(this).show {
                positiveButton(text = "확인") { _ ->
                    completion?.let{ it(true) }
                }
                negativeButton(text = "취소") { _ ->
                    completion?.let{ it(false) }
                }
                onShow {
                    title(text = title)
                    message(text = msg)
                }
            }
        }
    }

    fun showSingleDialog(title: String, msg: String, completion: ((Boolean) -> Unit)? = null) {
        AndroidSchedulers.mainThread().scheduleDirect {
            MaterialDialog(this).show {
                positiveButton(text = "확인") { _ ->
                    completion?.let{ it(true) }
                }
                onShow {
                    title(text = title)
                    message(text = msg)
                }
            }
        }
    }

}

