package com.powerbase.app

import android.app.Application
import com.powerbase.app.diagnostics.DiagnosticsLogger

/**
 * Application entry point. Starts diagnostics logging (on by default, Rule 7)
 * and captures crashes before the app is fully up.
 */
class PowerbaseApp : Application() {

    override fun onCreate() {
        super.onCreate()
        DiagnosticsLogger.init(this)
        DiagnosticsLogger.installCrashHandler()
        DiagnosticsLogger.log("App", "Started version ${BuildConfig.VERSION_NAME}")
    }
}
