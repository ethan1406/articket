package com.trufflear.trufflear

import android.app.Application
import com.bugsnag.android.Bugsnag
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class TruffleApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        Bugsnag.start(this)
        Bugsnag.notify(RuntimeException("Test error"))
    }
}