package com.aid.trader

import android.app.Application
import com.aid.trader.di.networkModule
import com.google.firebase.FirebaseApp
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class TraderApp : Application() {

    override fun onCreate() {
        super.onCreate()
        // Initialize Firebase
        FirebaseApp.initializeApp(this)

        // Start Koin
        startKoin {
            androidContext(this@TraderApp)
            modules(networkModule)
        }
    }
}
