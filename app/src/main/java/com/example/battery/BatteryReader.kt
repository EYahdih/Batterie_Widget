package com.example.battery

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import com.example.R
import kotlin.math.roundToInt

object BatteryReader {

  fun readCurrent(context: Context): BatteryState {
    val stickyIntent =
      context.applicationContext.registerReceiver(
        null,
        IntentFilter(Intent.ACTION_BATTERY_CHANGED),
      )
    val batteryManager =
      context.applicationContext.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
    return parseIntent(stickyIntent, batteryManager)
  }

  fun parseIntent(intent: Intent?, batteryManager: BatteryManager? = null): BatteryState {
    if (intent == null) {
      val fallbackLevel =
        batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)?.coerceIn(0, 100)
          ?: 0
      return BatteryState(percentage = fallbackLevel)
    }

    val rawLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
    val rawScale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
    val percentage =
      if (rawLevel >= 0 && rawScale > 0) {
        ((rawLevel * 100f) / rawScale.toFloat()).roundToInt().coerceIn(0, 100)
      } else {
        batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)?.coerceIn(0, 100)
          ?: 0
      }

    val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_UNKNOWN)
    val isFull = status == BatteryManager.BATTERY_STATUS_FULL || (status == BatteryManager.BATTERY_STATUS_CHARGING && percentage >= 100)
    val isCharging =
      status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL

    val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0)
    val chargeSource =
      when {
        (plugged and BatteryManager.BATTERY_PLUGGED_AC) != 0 -> ChargeSource.AC
        (plugged and BatteryManager.BATTERY_PLUGGED_USB) != 0 -> ChargeSource.USB
        (plugged and BatteryManager.BATTERY_PLUGGED_WIRELESS) != 0 -> ChargeSource.WIRELESS
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
          (plugged and BatteryManager.BATTERY_PLUGGED_DOCK) != 0 -> ChargeSource.DOCK
        else -> ChargeSource.NONE
      }

    val health =
      when (intent.getIntExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_UNKNOWN)) {
        BatteryManager.BATTERY_HEALTH_GOOD -> BatteryHealth.GOOD
        BatteryManager.BATTERY_HEALTH_OVERHEAT -> BatteryHealth.OVERHEAT
        BatteryManager.BATTERY_HEALTH_DEAD -> BatteryHealth.DEAD
        BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> BatteryHealth.OVER_VOLTAGE
        BatteryManager.BATTERY_HEALTH_COLD -> BatteryHealth.COLD
        else -> BatteryHealth.UNKNOWN
      }

    val tempTenths = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)
    val voltageMv = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0)
    val technology = intent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY)?.takeIf { it.isNotBlank() } ?: "Li-ion"

    val remainingMs =
      if (isCharging && !isFull && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && batteryManager != null) {
        runCatching { batteryManager.computeChargeTimeRemaining() }.getOrNull()?.takeIf { it > 0L }
      } else {
        null
      }

    return BatteryState(
      percentage = percentage,
      isCharging = isCharging,
      isFull = isFull,
      chargeSource = chargeSource,
      healthStatus = health,
      temperatureCelsius = tempTenths / 10f,
      voltageMv = voltageMv,
      technology = technology,
      chargeTimeRemainingMs = remainingMs,
      updatedAtMillis = System.currentTimeMillis(),
    )
  }

  fun selectBatteryIconRes(percentage: Int, isCharging: Boolean): Int {
    if (isCharging) return R.drawable.ic_battery_charging
    return when {
      percentage <= 10 -> R.drawable.ic_battery_level_0
      percentage <= 25 -> R.drawable.ic_battery_level_1
      percentage <= 42 -> R.drawable.ic_battery_level_2
      percentage <= 60 -> R.drawable.ic_battery_level_3
      percentage <= 78 -> R.drawable.ic_battery_level_4
      percentage <= 94 -> R.drawable.ic_battery_level_5
      else -> R.drawable.ic_battery_level_6
    }
  }
}
