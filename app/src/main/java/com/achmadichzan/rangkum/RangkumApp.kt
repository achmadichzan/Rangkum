package com.achmadichzan.rangkum

import android.app.Application
import com.achmadichzan.rangkum.di.databaseModule
import com.achmadichzan.rangkum.di.dispatcherModule
import com.achmadichzan.rangkum.di.domainModule
import com.achmadichzan.rangkum.di.networkModule
import com.achmadichzan.rangkum.di.repositoryModule
import com.achmadichzan.rangkum.di.viewModelModule
import com.google.firebase.FirebaseApp
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.logger.Level

class RangkumApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)

        startKoin {
            androidLogger(if (BuildConfig.DEBUG) Level.ERROR else Level.NONE)
            androidContext(this@RangkumApp)
            modules(
                networkModule,
                databaseModule,
                dispatcherModule,
                repositoryModule,
                domainModule,
                viewModelModule
            )
        }
    }
}