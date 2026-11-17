package com.example.battery

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.core.content.ContextCompat
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import com.example.widget.BatteryGlanceWidget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

object BatteryBroadcastMonitor {

  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
  private val syncMutex = Mutex()

  private val _batteryState = MutableStateFlow(BatteryState())
  val batteryState: StateFlow<BatteryState> = _batteryState.asStateFlow()

  private val _activeWidgetCount = MutableStateFlow(0)
  val activeWidgetCount: StateFlow<Int> = _activeWidgetCount.asStateFlow()

  @Volatile private var isRegistered = false
  @Volatile private var lastPushedWidgetSnapshot: WidgetSnapshotKey? = null

  private val batteryChangedReceiver =
    object : BroadcastReceiver() {
      override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BATTERY_CHANGED) return
        val appContext = context.applicationContext
        val manager = appContext.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
        val newState = BatteryReader.parseIntent(intent, manager)
        _batteryState.value = newState

        if (newState.widgetSnapshotKey != lastPushedWidgetSnapshot) {
          val pending = goAsync()
          scope.launch {
            try {
              syncWidgetsInternal(appContext, newState, force = false)
            } finally {
              pending.finish()
            }
          }
        }
      }
    }

  fun ensureRegistered(context: Context) {
    val appContext = context.applicationContext
    val initialState = BatteryReader.readCurrent(appContext)
    _batteryState.value = initialState

    if (!isRegistered) {
      synchronized(this) {
        if (!isRegistered) {
          ContextCompat.registerReceiver(
            appContext,
            batteryChangedReceiver,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED),
            ContextCompat.RECEIVER_NOT_EXPORTED,
          )
          isRegistered = true
        }
      }
    }
  }

  suspend fun syncWidgets(context: Context, force: Boolean = false) {
    val appContext = context.applicationContext
    val current = BatteryReader.readCurrent(appContext)
    _batteryState.value = current
    syncWidgetsInternal(appContext, current, force = force)
  }

  suspend fun refreshWidgetCount(context: Context) {
    val appContext = context.applicationContext
    val glanceIds =
      GlanceAppWidgetManager(appContext).getGlanceIds(BatteryGlanceWidget::class.java)
    _activeWidgetCount.value = glanceIds.size
  }

  private suspend fun syncWidgetsInternal(
    context: Context,
    state: BatteryState,
    force: Boolean,
  ) {
    syncMutex.withLock {
      val manager = GlanceAppWidgetManager(context)
      val glanceIds = manager.getGlanceIds(BatteryGlanceWidget::class.java)
      _activeWidgetCount.value = glanceIds.size

      if (glanceIds.isEmpty()) {
        lastPushedWidgetSnapshot = state.widgetSnapshotKey
        return
      }

      if (!force && state.widgetSnapshotKey == lastPushedWidgetSnapshot) {
        return
      }

      val widget = BatteryGlanceWidget()
      for (glanceId in glanceIds) {
        updateAppWidgetState(context, glanceId) { prefs ->
          prefs[BatteryGlanceWidget.KEY_PERCENTAGE] = state.percentage
          prefs[BatteryGlanceWidget.KEY_IS_CHARGING] = state.isCharging
          prefs[BatteryGlanceWidget.KEY_IS_FULL] = state.isFull
          prefs[BatteryGlanceWidget.KEY_CHARGE_SOURCE] = state.chargeSource.name
          prefs[BatteryGlanceWidget.KEY_UPDATED_AT] = state.updatedAtMillis
        }
        widget.update(context, glanceId)
      }
      lastPushedWidgetSnapshot = state.widgetSnapshotKey
    }
  }
}
