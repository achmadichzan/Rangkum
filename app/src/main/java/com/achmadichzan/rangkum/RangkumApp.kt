package com.achmadichzan.rangkum

import android.app.Application
import com.achmadichzan.rangkum.di.databaseModule
import com.achmadichzan.rangkum.di.domainModule
import com.achmadichzan.rangkum.di.networkModule
import com.achmadichzan.rangkum.di.repositoryModule
import com.achmadichzan.rangkum.di.viewModelModule
import com.google.firebase.FirebaseApp
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin

class RangkumApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)

        startKoin {
            androidLogger()
            androidContext(this@RangkumApp)
            modules(
                networkModule,
                databaseModule,
                repositoryModule,
                domainModule,
                viewModelModule
            )
        }
    }
}