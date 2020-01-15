package com.jungbae.nemodeal.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import com.jungbae.nemodeal.R
import kotlinx.android.synthetic.main.activity_splash.view.*


class IntroActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        window.requestFeature(Window.FEATURE_NO_TITLE)
//        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN

        setContentView(R.layout.activity_splash)

        Handler().postDelayed({
            moveMainActivity()
            finish()
        }, 500)
    }

    fun moveMainActivity() {
        startActivity(Intent(this@IntroActivity, MainActivity::class.java)?.apply {
            intent.getStringExtra("link")?.let {
                putExtra("link", it)
            }
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }

}
