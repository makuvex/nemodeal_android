package com.jungbae.nemodeal.activity

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.callbacks.onShow
import com.afollestad.materialdialogs.input.getInputField
import com.afollestad.materialdialogs.input.input
import com.jakewharton.rxbinding3.view.clicks
import com.jungbae.nemodeal.CommonApplication
import com.jungbae.nemodeal.CommonApplication.Companion.context


import com.jungbae.nemodeal.R
import com.jungbae.nemodeal.network.*
import com.jungbae.nemodeal.preference.PreferenceManager
import com.jungbae.nemodeal.showToast
import com.jungbae.nemodeal.view.increaseTouchArea
import com.jungbae.schoolfood.view.EditModeIndex
import com.jungbae.schoolfood.view.KeywordRecyclerAdapter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.activity_keyword.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.content_main.recycler_view
import java.util.concurrent.TimeUnit


class KeywordActivity : AppCompatActivity() {
    private val disposeBag = CompositeDisposable()
    private lateinit var keywordList: ArrayList<AlertKeyword>
    private lateinit var listAdapter: KeywordRecyclerAdapter
    private lateinit var toggleSubject: PublishSubject<AlertKeyword>

/*
    private lateinit var mealList: ArrayList<SimpleSchoolMealData>
    private lateinit var mealAdapter: MealDetailRecyclerAdapter

    var schoolCode: String? = null
    var officeCode: String? = null
    var schoolName: String? = null

    private lateinit var selectedBehaviorSubject: PublishSubject<SimpleSchoolMealData>
*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_keyword)
        //window.statusBarColor = ContextCompat.getColor(this, R.color.colorPrimary)

        initializeUI()
        bindRxUI()

        getKeyword()
    }

    override fun onDestroy() {
        disposeBag.clear()
        super.onDestroy()
    }

    init {
        Log.e("@@@","@@@ init")
        toggleSubject = PublishSubject.create()
        keywordList = ArrayList()
        listAdapter = KeywordRecyclerAdapter(keywordList, toggleSubject)
    }

    fun initializeUI() {

        recycler_view.apply {
            layoutManager = LinearLayoutManager(applicationContext)
            adapter = listAdapter
        }

        applicationContext.increaseTouchArea(back, 50)
        applicationContext.increaseTouchArea(remove, 50)

        remove.isEnabled = false
    }

    fun bindRxUI() {
        val toggleDisposable = toggleSubject
            .throttleFirst(1, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { alertKeyword ->
                if(alertKeyword.keyword.isEmpty()) {
                    if(keywordList.size >= 10) {
                        context.showToast("키워드는 10까지 등록 할수 있습니다.")
                    } else {
                        addDialog()
                    }
                } else {
                    if(listAdapter.mode == EditModeIndex.EDIT) {
                        deleteDialog(alertKeyword.keyword)
                    } else {
                        updateKeyword(alertKeyword.keyword, alertKeyword.alert.getBoolean()) {
                            val index = keywordList.indexOfFirst { it.keyword == alertKeyword.keyword }
                            val data = keywordList.removeAt(index)?.apply { alert = alertKeyword.alert }
                            keywordList.add(index, data)

                            when(alertKeyword.alert.getBoolean()) {
                                true -> {
                                    CommonApplication.subscribeTopic(alertKeyword.keyword)
                                }
                                false -> {
                                    CommonApplication.unsubscribeTopic(alertKeyword.keyword)
                                }
                            }
                        }
                    }
                }
            }

        val backDisposable = back.clicks()
            .throttleFirst(1, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                finish()
            }

        val removeDisposable = remove.clicks()
            .subscribeOn(AndroidSchedulers.mainThread())
            .subscribe {
                listAdapter.mode = if(listAdapter.mode == EditModeIndex.VIEW) EditModeIndex.EDIT else EditModeIndex.VIEW
                remove.isSelected = !remove.isSelected
                listAdapter.notifyDataSetChanged()
            }

        disposeBag.addAll(toggleDisposable, backDisposable, removeDisposable)
    }

    fun deleteDialog(keyword: String) {
        AndroidSchedulers.mainThread().scheduleDirect {
            MaterialDialog(this).show {
                positiveButton(text = "확인") {
                    deleteKeyword(keyword) {
                        Toast.makeText(CommonApplication.context, "키워드가 삭제 되었습니다.", Toast.LENGTH_SHORT).show()
                        CommonApplication.unsubscribeTopic(keyword)

                        if(keywordList.size == 0) {
                            listAdapter.mode = EditModeIndex.VIEW
                            this@KeywordActivity.remove.isSelected = !this@KeywordActivity.remove.isSelected
                            this@KeywordActivity.remove.isEnabled = keywordList.size > 0
                        }
                    }
                }
                negativeButton(text = "취소")
                onShow {
                    title(text = "키워드 삭제")
                    message(text = "${keyword}을(를) 삭제 할까요?")
                }
            }
        }
    }

    fun addDialog() {
        AndroidSchedulers.mainThread().scheduleDirect {
            MaterialDialog(this).show {
                input(allowEmpty = false, maxLength = 10, hint = "알림 키워드를 입력해 주세요.")
                positiveButton(text = "확인") {
                    val input = it.getInputField().text.toString()
                    if(keywordList.filter{ it.keyword == input }.isNotEmpty()) {
                        Toast.makeText(context, "이미 등록되어 있는 키워드 입니다.", Toast.LENGTH_SHORT).show()
                    } else if(input.trim().isEmpty()) {
                        Toast.makeText(context, "공백은 입력할 수 없습니다.", Toast.LENGTH_SHORT).show()
                    } else {
                        addKeyword(input) {
                            this@KeywordActivity.remove.isEnabled = keywordList.size > 0
                        }
                    }
                }
                negativeButton(text = "취소")
                onShow {
                    title(text = "키워드 추가")
                }
            }
        }
    }

    fun addKeyword(keyword: String, complete: ((Boolean) -> Unit)? = null) {
        Log.e("@@@","@@@ requestAddKeyword $keyword")
        val disposable = NetworkService.getInstance().registKeyword(keyword, PreferenceManager.userSeq.toString())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(ObservableResponse<BaseResult>(
                onSuccess = {
                    Log.e("@@@", "@@@ onSuccess ${it.reflectionToString()}")
                    CommonApplication.subscribeTopic(keyword)
                    keywordList.add(AlertKeyword(keyword, 1))
                    listAdapter.notifyDataSetChanged()
                    complete?.let{ it(true) }
                }, onError = {
                    Log.e("@@@", "@@@ error $it")
                    complete?.let{ it(false) }
                }
            ))
        disposeBag.add(disposable)
    }

    fun getKeyword() {
        keywordList.clear()
        val disposable = NetworkService.getInstance().keyword(PreferenceManager.userSeq.toString())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(ObservableResponse<Keywords>(
                onSuccess = {
                    Log.e("@@@", "@@@ onSuccess ${it.reflectionToString()}")
                    keywordList.addAll(it.result)
                    listAdapter.notifyDataSetChanged()
                    remove.isEnabled = keywordList.size > 0

                }, onError = {
                    Log.e("@@@", "@@@ error $it")
                }
            ))
        disposeBag.add(disposable)
    }

    fun updateKeyword(keyword: String, toggle: Boolean, complete: ((Boolean) -> Unit)? = null) {
        Log.e("@@@","@@@ updateKeyword $keyword, $toggle")

        val disposable = NetworkService.getInstance().updateKeyword(keyword, PreferenceManager.userSeq.toString(), toggle)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(ObservableResponse<BaseResult>(
                onSuccess = {
                    Log.e("@@@", "@@@ onSuccess ${it.reflectionToString()}")
                    //keywordList.add(AlertKeyword(keyword, 1))
                    complete?.let{ it(true) }
                }, onError = {
                    Log.e("@@@", "@@@ error $it")
                    complete?.let{ it(false) }
                }
            ))
        disposeBag.add(disposable)
    }

    fun deleteKeyword(keyword: String, completion: (() -> Unit)? = null) {
        Log.e("@@@","@@@ deleteKeyword $keyword")
        val disposable = NetworkService.getInstance().deleteKeyword(keyword, PreferenceManager.userSeq.toString())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(ObservableResponse<BaseResult>(
                onSuccess = {
                    Log.e("@@@", "@@@ onSuccess ${it.reflectionToString()}")
                    keywordList.removeIf { data ->
                        keyword == data.keyword
                    }
                    listAdapter.notifyDataSetChanged()
                    completion?.let{ it() }
                }, onError = {
                    Log.e("@@@", "@@@ error $it")
                    completion?.let{ it() }
                }
            ))
        disposeBag.add(disposable)
    }

}
