package com.jungbae.nemodeal.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.callbacks.onShow
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.formats.NativeAdOptions
import com.google.android.gms.ads.formats.NativeAdOptions.NATIVE_MEDIA_ASPECT_RATIO_SQUARE
import com.google.android.gms.ads.formats.UnifiedNativeAd
import com.google.android.material.navigation.NavigationView
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
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.progress_bar.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.concurrent.TimeUnit
import kotlin.random.Random

object RandomMaxUnit {
    const val count: Int = 10
}

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private val disposeBag = CompositeDisposable()
    private lateinit var hotDealList: ArrayList<HotDealInfo>
    private lateinit var cardAdapter: HomeRecyclerAdapter

    private lateinit var selectItemSubject: PublishSubject<HotDealInfo>
    private lateinit var backPressedSubject: BehaviorSubject<Long>

    private lateinit var adLoader: AdLoader
    private lateinit var categorySet: MutableMap<Int, Int>
    private var countDownTimer: CountDownTimer? = null
    private var adList = arrayListOf<UnifiedNativeAd>()

    //private var randomIntList = arrayListOf<Int>()
    private var lastRandomListSize: Int = 0

    var lastPosition: Int = 0
        get() = (recycler_view.layoutManager as LinearLayoutManager)?.findLastVisibleItemPosition()

    private fun loadAdDisplay() = loadAd {
        it?.let { ad ->
            if(hotDealList.size / RandomMaxUnit.count > adList.size) {
                adList.add(ad)
                val index = Random.nextInt(0, RandomMaxUnit.count) + lastRandomListSize
                Log.e("@@@", "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ ad loaded randomIdx ${index},  hotDealList size ${hotDealList.size}")

                hotDealList[index].adUser = true.getInt()
                hotDealList[index].adItem = it

                lastRandomListSize += RandomMaxUnit.count
//                hotDealList[1].adUser = 1
//                hotDealList[1].adItem = it
//                cardAdapter.notifyItemChanged(1)

                cardAdapter.notifyItemChanged(index)
            } else {
                ad.destroy()
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //window.statusBarColor = ContextCompat.getColor(this, R.color.colorPrimary)
        setContentView(R.layout.activity_main)

        initializeUI()
        bindUI()

        createTimerFor(100)
        requestCategory()

        GlobalScope.launch {
            MobileAds.initialize(applicationContext) {}
            loadAdDisplay()
        }

//        Log.e("@@@","@@@ Create intent ${intent?.getStringExtra("link")}")
    }

    override fun onStop() {
        super.onStop()
        stopTimer()
    }

    init {
        Log.e("@@@","@@@ init")
        selectItemSubject = PublishSubject.create()
        backPressedSubject = BehaviorSubject.createDefault(0L)

        hotDealList = ArrayList()
        cardAdapter = HomeRecyclerAdapter(hotDealList, selectItemSubject)
        categorySet = mutableMapOf()
    }

    override fun onDestroy() {
        adList?.forEach {
            it.destroy()
        }
        disposeBag.clear()
        super.onDestroy()
    }

    fun initializeUI() {
        setSupportActionBar(toolbar)
        setTitle(R.string.app_full_name)

        recycler_view.apply {
            layoutManager = LinearLayoutManager(applicationContext)
            adapter = cardAdapter
        }.addOnScrollListener(object: RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                categorySet.filterValues{ hotDealList[lastPosition].articleId == it }?.run {
                    if(isNotEmpty()) {
                        recyclerView.stopScroll()
                        createTimerFor(100)
                        GlobalScope.launch {
                            requestLoadMore(keys.first())
                        }
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
        drawer_layout.nav_view.itemIconTintList = null
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)

        swipe_refresh.setOnRefreshListener {
            requestCategory()
            loadAdDisplay()
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
                    Log.e("@@@","@@@ onAdFailedToLoad")
                    onLoaded(null)
                }

                override fun onAdLeftApplication() {
                    super.onAdLeftApplication()
                    Log.e("@@@","@@@ onAdLeftApplication")
                    //stopTimer()
                }

                override fun onAdOpened() {
                    super.onAdOpened()
                    Log.e("@@@","@@@ onAdOpened")
                    createTimerFor(100)
                }
            })
            .withNativeAdOptions(NativeAdOptions.Builder()
                .setMediaAspectRatio(NATIVE_MEDIA_ASPECT_RATIO_SQUARE)
                .build())
            .build()

        adLoader.loadAds(AdRequest.Builder().build(), 4)
    }

    fun bindUI() {
        val itemClicksDisposable = selectItemSubject
            .throttleFirst(1, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                Log.e("@@@", "item clicks ${it}")
                startActivity(Intent(this, DealDetailActivity::class.java)?.apply {
                    putExtra("url", it.url)
                })
            }

        val backDisposable =
            backPressedSubject
                .buffer(2, 1)
                .map{ Pair(it[0], it[1]) }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    if(it.second - it.first < TimeUnit.SECONDS.toMillis(2)) {
                        finish()
                    } else {
                        Toast.makeText(this, "뒤로 버튼을 한번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show()
                    }
                }

        disposeBag.addAll(itemClicksDisposable, backDisposable)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Log.e("@@@","@@@ onNew ${intent?.getStringExtra("link")}")
        intent?.getStringExtra("link")?.let {link ->
            showDialog("해당 상품 페이지로 이동 할까요?", link) {
                if(it) {
                    //startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link)))
                    startActivity(Intent(this, DealDetailActivity::class.java)?.apply {
                        putExtra("url", Uri.parse(link))
                    })
                }
            }
        }
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            backPressedSubject.onNext(System.currentTimeMillis())
        }
    }

    fun requestLoadMore(dbIndex: Int) {

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
                        loadAdDisplay()
                        cardAdapter.notifyDataSetChanged()
                        //applicationContext.showToast("핫딜 정보를 더 불러왔습니다.")
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
    }

    fun requestHotDeal(site: Int, id: Int = 0): Observable<HotDealData> {
        return NetworkService.getInstance().getHotDeal(site, id).observeOn(AndroidSchedulers.mainThread())
    }

    fun requestCategory() {
        hotDealList.clear()
        categorySet.clear()
        adList.clear()
        lastRandomListSize = 0

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

//                        val adIndex = Random.nextInt(0, 9)
//                        hotDealList[adIndex].adUser = true.getInt()

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
            R.id.keyword -> { startActivity(Intent(this, KeywordActivity::class.java)) }
            R.id.license -> {
                startActivity(Intent(this, LicenseActivity::class.java)?.apply {
                    putExtra("url", "http://makuvex7.cafe24.com/nemodeal_aos_license")
                })
            }
            R.id.version -> { showSingleDialog("버전 정보", "현재 버전: 1.0.0") }
        }
        drawer_layout.closeDrawer(GravityCompat.START)
        return false
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

