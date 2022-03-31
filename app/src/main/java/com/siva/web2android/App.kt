package com.siva.web2android

import android.app.Application
import com.google.android.gms.ads.MobileAds

class App: Application() {

    override fun onCreate() {
        super.onCreate()
        MobileAds.initialize(applicationContext)
    }

}