package com.example.android_labs

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ExpenseApplication : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}