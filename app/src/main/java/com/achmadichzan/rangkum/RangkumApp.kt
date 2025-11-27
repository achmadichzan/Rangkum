package com.achmadichzan.rangkum

import android.app.Application
import com.google.firebase.FirebaseApp

class RangkumApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}