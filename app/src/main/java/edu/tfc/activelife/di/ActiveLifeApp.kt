package edu.tfc.activelife.di

import android.app.Application
import com.google.firebase.database.FirebaseDatabase
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ActiveLifeApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Habilitar persistencia offline
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
    }
}
