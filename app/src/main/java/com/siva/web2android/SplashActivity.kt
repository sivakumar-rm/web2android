package com.siva.web2android

import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!Config.splashScreenEnabled) {
            Intent(this, MainActivity::class.java).let {
                it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(it)
                finish()
            }
            return
        }

        supportActionBar?.hide()
        setContentView(R.layout.activity_splash)
        findViewById<View>(R.id.splash_view).systemUiVisibility =
                View.SYSTEM_UI_FLAG_LOW_PROFILE or
                        View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
    }

    override fun onPostResume() {
        super.onPostResume()
        val handler = Handler()
        handler.postDelayed({
            Intent(this, MainActivity::class.java).let {
                it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(it)
                finish()
            }
        }, 6000)
    }

}