package com.skycast.app

import android.app.Application
import android.util.Log
import com.skycast.app.util.Constants

/**
 * Application entry point. Centralised here mainly to log app start-up —
 * SkyCast uses Android's built-in [Log] class throughout (LOG_TAG =
 * [Constants.LOG_TAG]) rather than println/System.out, per the "strategic use
 * of logging" requirement, so log levels (d/i/w/e) can be filtered in Logcat
 * and stripped from release builds via ProGuard if desired.
 */
class SkyCastApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Log.i(Constants.LOG_TAG, "SkyCast application starting, versionName=${BuildConfig.VERSION_NAME}")
    }
}
