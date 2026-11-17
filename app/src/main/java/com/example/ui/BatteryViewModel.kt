package com.example.ui

import android.app.Application
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.battery.BatteryBroadcastMonitor
import com.example.battery.BatteryState
import com.example.widget.BatteryGlanceWidgetReceiver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class PreviewThemeMode(val label: String) {
  SYSTEM("Auto"),
  LIGHT("Clair"),
  DARK("Sombre"),
}

class BatteryViewModel(application: Application) : AndroidViewModel(application) {

  val batteryState: StateFlow<BatteryState> = BatteryBroadcastMonitor.batteryState
  val activeWidgetCount: StateFlow<Int> = BatteryBroadcastMonitor.activeWidgetCount

  private val _previewThemeMode = MutableStateFlow(PreviewThemeMode.SYSTEM)
  val previewThemeMode: StateFlow<PreviewThemeMode> = _previewThemeMode.asStateFlow()

  private val _isSyncing = MutableStateFlow(false)
  val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

  init {
    BatteryBroadcastMonitor.ensureRegistered(application)
    refreshCount()
  }

  fun setPreviewThemeMode(mode: PreviewThemeMode) {
    _previewThemeMode.value = mode
  }

  fun refreshCount() {
    viewModelScope.launch {
      BatteryBroadcastMonitor.refreshWidgetCount(getApplication())
    }
  }

  fun syncWidgetNow(onFinished: (Boolean) -> Unit = {}) {
    viewModelScope.launch {
      _isSyncing.value = true
      try {
        BatteryBroadcastMonitor.syncWidgets(getApplication(), force = true)
        onFinished(true)
      } catch (_: Exception) {
        onFinished(false)
      } finally {
        _isSyncing.value = false
      }
    }
  }

  fun requestPinWidget(context: Context, onUnsupported: () -> Unit) {
    val appWidgetManager = AppWidgetManager.getInstance(context)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && appWidgetManager.isRequestPinAppWidgetSupported) {
      val provider = ComponentName(context, BatteryGlanceWidgetReceiver::class.java)
      val callbackIntent = Intent(context, BatteryGlanceWidgetReceiver::class.java)
      val successCallback =
        PendingIntent.getBroadcast(
          context,
          1001,
          callbackIntent,
          PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
      val accepted = appWidgetManager.requestPinAppWidget(provider, null, successCallback)
      if (!accepted) {
        onUnsupported()
      }
    } else {
      onUnsupported()
    }
  }
}
