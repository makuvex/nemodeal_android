package com.jungbae.nemodeal

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.callbacks.onShow
import io.reactivex.android.schedulers.AndroidSchedulers
import okio.internal.commonAsUtf8ToByteArray

fun String.UTF8(): String? {
    var str = ""
    this.commonAsUtf8ToByteArray().map {
        val st = String.format("%02X", it)
        str += "%" + st
    }
    Log.e("@@@","@@@ str $str")
    return str
}

fun Int.True(): Boolean {
    return this == 1
}

fun Context.showToast(text: CharSequence, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, text, duration).show()
}

