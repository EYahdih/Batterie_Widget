package com.example.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import com.example.battery.BatteryBroadcastMonitor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class BatteryGlanceWidgetReceiver : GlanceAppWidgetReceiver() {

  override val glanceAppWidget: GlanceAppWidget = BatteryGlanceWidget()

  override fun onEnabled(context: Context) {
    super.onEnabled(context)
    BatteryBroadcastMonitor.ensureRegistered(context.applicationContext)
    CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
      BatteryBroadcastMonitor.syncWidgets(context.applicationContext, force = true)
    }
  }

  override fun onUpdate(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetIds: IntArray,
  ) {
    super.onUpdate(context, appWidgetManager, appWidgetIds)
    BatteryBroadcastMonitor.ensureRegistered(context.applicationContext)
    CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
      BatteryBroadcastMonitor.syncWidgets(context.applicationContext, force = true)
    }
  }

  override fun onDeleted(context: Context, appWidgetIds: IntArray) {
    super.onDeleted(context, appWidgetIds)
    CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
      BatteryBroadcastMonitor.refreshWidgetCount(context.applicationContext)
    }
  }

  override fun onDisabled(context: Context) {
    super.onDisabled(context)
    CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
      BatteryBroadcastMonitor.refreshWidgetCount(context.applicationContext)
    }
  }
}
