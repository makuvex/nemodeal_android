package com.jungbae.nemodeal.preference

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.afollestad.rxkprefs.RxkPrefs
import com.afollestad.rxkprefs.rxkPrefs
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.jungbae.nemodeal.CommonApplication


object PreferencesConstant {
    val fcm_token = "FCM_TOKEN"
}

class PreferenceManager {

    companion object {
        private var instance: RxkPrefs? = null
        private val self: PreferenceManager = PreferenceManager()
        //private lateinit var schoolCodeList: MutableList<SimpleSchoolData>

        init {
            Log.e("@@@","@@@ PreferenceManager init")
            instance = instance ?: rxkPrefs(CommonApplication.context, CommonApplication.context.packageName, AppCompatActivity.MODE_PRIVATE)
        }

        @JvmStatic
        var fcmToken: String?
            get() {
                instance?.run {
                    return string(PreferencesConstant.fcm_token, "").get()
                }
                return null
            }
            set(data) {
                instance?.let {} ?: return
                data?.let {
                    instance?.string(PreferencesConstant.fcm_token, "")?.set(it)
                }
            }

        /*
        @JvmStatic
        var schoolData: MutableSet<SimpleSchoolData>?
            get() {
                instance?.run {
                    val gson = GsonBuilder().create()
                    val data = string(PreferencesConstant.SCHOOL_CODE, "").get()
                    Log.e("@@@","schoolData get data ${data}")

                    return gson.fromJson(data, object: TypeToken<MutableSet<SimpleSchoolData>>(){}.type)
                }
                return null
            }
            set(data) {
                instance?.let {
                    Log.e("@@@","schoolData set data@@@")
                    val json = GsonBuilder().create().toJson(data)
                    it.string(PreferencesConstant.SCHOOL_CODE, "").set(json)
                }
            }

        fun addSchoolData(data: SimpleSchoolData) {
            instance?.run {
                if(schoolData == null) {
                    schoolData = mutableSetOf<SimpleSchoolData>(data)
                } else {
                    schoolData?.let {
                        it.add(data)
                        schoolData = it
                    }
                }
            }
        }

        fun removeSchoolData(officeCode: String, schoolCode: String) {
            instance?.run {
                schoolData?.let {
                    it.removeIf { it.officeCode == officeCode && it.schoolCode == schoolCode }
                    schoolData = it
                }
            }
        }
        */

    }

}



