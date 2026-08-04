package com.qrscangera.app

import android.app.Application
import com.qrscangera.app.data.AppDatabase
import com.qrscangera.app.utils.AdsManager

class QrScanGeraApp : Application() {

    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }

    override fun onCreate() {
        super.onCreate()
        AdsManager.initialize(this)
    }
}
