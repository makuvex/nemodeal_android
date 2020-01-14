package com.jungbae.nemodeal.activity

import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
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
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging
import com.jungbae.nemodeal.R
import com.jungbae.nemodeal.network.*
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
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private val disposeBag = CompositeDisposable()
    private lateinit var hotDealList: ArrayList<HotDealInfo>
    private lateinit var cardAdapter: HomeRecyclerAdapter

    private lateinit var selectItemSubject: PublishSubject<HotDealInfo>
    //private lateinit var categorySubject: PublishSubject<ArrayList<DealSite>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //window.statusBarColor = ContextCompat.getColor(this, R.color.colorPrimary)
        setContentView(R.layout.activity_main)

        initializeUI()
        bindUI()
//asd
        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.e("@@@", "getInstanceId failed", task.exception)
                    return@OnCompleteListener
                }

                // Get new Instance ID token
                val token = task.result?.token

                // Log and toast
                Log.e("@@@", "@@@ token $token")
            })

        requestCategory()
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
    }

    fun initializeUI() {
        setSupportActionBar(toolbar)

        recycler_view.apply {
            layoutManager = LinearLayoutManager(applicationContext)
            adapter = cardAdapter
        }.setOnScrollChangeListener { view, i, i2, i3, i4 ->
            Log.e("","@@@ i2 $i2, i4 $i4")
        }

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

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    fun requestHotDeal(site: Int): Observable<HotDealData> {
        return NetworkService.getInstance().getHotDeal(site).observeOn(AndroidSchedulers.mainThread())
    }

    fun requestCategory() {
//        NetworkService.getInstance().getDealList().observeOn(AndroidSchedulers.mainThread()).subscribeWith(ObservableResponse<CategoryData>(
//            onSuccess = {
//                Log.e("@@@", "@@@ onSuccess ${it.reflectionToString()}")
//
//            }, onError = {
//                Log.e("@@@", "@@@ error $it")
//            }
//        ))
        hotDealList.clear()

        NetworkService.getInstance().getDealList().observeOn(AndroidSchedulers.mainThread())
            .flatMap {
                it.result.toObservable()
            }.flatMap {
                requestHotDeal(it.id)
            }.subscribeWith(ObservableResponse<HotDealData>(
                onSuccess = {
                    Log.e("@@@", "@@@ onSuccess ${it.reflectionToString()}")

                    AndroidSchedulers.mainThread().scheduleDirect {
                        recycler_view.visibility = View.INVISIBLE
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
                        } catch(e: Exception) {
                            e.printStackTrace()
                        }
//                        sortByDate()
                        cardAdapter.notifyDataSetChanged()
                        swipe_refresh.isRefreshing = false
                        recycler_view.visibility = View.VISIBLE
                    }
                }, onError = {
                    Log.e("@@@", "@@@ error $it")
                }
            ))
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
        // Handle navigation view item clicks here.
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
        //val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }
}
