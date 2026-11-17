package com.example.battery

enum class ChargeSource(val label: String, val shortLabel: String) {
  NONE("Sur batterie", "Batterie"),
  AC("Secteur AC", "AC"),
  USB("Port USB", "USB"),
  WIRELESS("Sans fil", "Sans fil"),
  DOCK("Station d'accueil", "Dock"),
}

enum class BatteryHealth(val label: String) {
  GOOD("Bonne"),
  OVERHEAT("Surchauffe"),
  DEAD("Défectueuse"),
  OVER_VOLTAGE("Surtension"),
  COLD("Trop froide"),
  UNKNOWN("Inconnue"),
}

data class WidgetSnapshotKey(
  val percentage: Int,
  val isCharging: Boolean,
  val isFull: Boolean,
  val chargeSource: ChargeSource,
)

data class BatteryState(
  val percentage: Int = 0,
  val isCharging: Boolean = false,
  val isFull: Boolean = false,
  val chargeSource: ChargeSource = ChargeSource.NONE,
  val healthStatus: BatteryHealth = BatteryHealth.GOOD,
  val temperatureCelsius: Float = 0f,
  val voltageMv: Int = 0,
  val technology: String = "Li-ion",
  val chargeTimeRemainingMs: Long? = null,
  val updatedAtMillis: Long = System.currentTimeMillis(),
) {
  val isLow: Boolean
    get() = !isCharging && percentage < 20

  val widgetSnapshotKey: WidgetSnapshotKey
    get() =
      WidgetSnapshotKey(
        percentage = percentage,
        isCharging = isCharging,
        isFull = isFull,
        chargeSource = chargeSource,
      )

  fun statusSubtitle(): String =
    when {
      isFull -> "Batterie pleine"
      isCharging && chargeSource != ChargeSource.NONE -> "En charge • ${chargeSource.shortLabel}"
      isCharging -> "En charge"
      isLow -> "Batterie faible"
      else -> "Sur batterie"
    }
}
