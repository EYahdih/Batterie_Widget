package com.example.battery

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class BatterySystemEventReceiver : BroadcastReceiver() {

  override fun onReceive(context: Context, intent: Intent) {
    val action = intent.action ?: return
    if (action !in SUPPORTED_ACTIONS) return

    val appContext = context.applicationContext
    BatteryBroadcastMonitor.ensureRegistered(appContext)

    val pendingResult = goAsync()
    CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
      try {
        BatteryBroadcastMonitor.syncWidgets(appContext, force = true)
      } finally {
        pendingResult.finish()
      }
    }
  }

  companion object {
    private val SUPPORTED_ACTIONS =
      setOf(
        Intent.ACTION_POWER_CONNECTED,
        Intent.ACTION_POWER_DISCONNECTED,
        Intent.ACTION_BATTERY_LOW,
        Intent.ACTION_BATTERY_OKAY,
        Intent.ACTION_BOOT_COMPLETED,
        Intent.ACTION_MY_PACKAGE_REPLACED,
      )
  }
}
