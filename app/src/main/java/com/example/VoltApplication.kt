package com.example

import android.app.Application
import com.example.battery.BatteryBroadcastMonitor

class VoltApplication : Application() {
  override fun onCreate() {
    super.onCreate()
    BatteryBroadcastMonitor.ensureRegistered(this)
  }
}
