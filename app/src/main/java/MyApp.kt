package com.psyfen.taskapplication

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

 @HiltAndroidApp
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        enableAutomaticIndexing()
    }

    private fun enableAutomaticIndexing() {
        val db = FirebaseFirestore.getInstance()
        db.persistentCacheIndexManager?.apply {
            enableIndexAutoCreation()
        }
    }
}
