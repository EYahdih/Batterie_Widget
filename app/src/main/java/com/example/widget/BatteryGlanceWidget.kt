package com.example.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.os.Build
import android.widget.RemoteViews
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.material3.ColorProviders
import androidx.glance.semantics.contentDescription
import androidx.glance.semantics.semantics
import androidx.glance.semantics.testTag
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.example.MainActivity
import com.example.R
import com.example.battery.BatteryReader
import com.example.battery.BatteryState
import com.example.battery.ChargeSource
import com.example.ui.theme.VoltDarkColorScheme
import com.example.ui.theme.VoltLightColorScheme

object VoltGlanceColorScheme {
  val colors = ColorProviders(light = VoltLightColorScheme, dark = VoltDarkColorScheme)
}

class BatteryGlanceWidget : GlanceAppWidget() {

  override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition
  override val sizeMode: SizeMode = SizeMode.Exact

  override suspend fun provideGlance(context: Context, id: GlanceId) {
    val initialSnapshot = BatteryReader.readCurrent(context)
    provideContent {
      val prefs = currentState<Preferences>()
      val state = resolveState(prefs, initialSnapshot)

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        GlanceTheme { BatteryWidgetContent(state = state) }
      } else {
        GlanceTheme(colors = VoltGlanceColorScheme.colors) { BatteryWidgetContent(state = state) }
      }
    }
  }

  override fun onCompositionError(
    context: Context,
    glanceId: GlanceId,
    appWidgetId: Int,
    throwable: Throwable,
  ) {
    val current = BatteryReader.readCurrent(context)
    val fallback =
      RemoteViews(context.packageName, R.layout.battery_widget_preview).apply {
        // Fallback uses standard layout cleanly
      }
    AppWidgetManager.getInstance(context).updateAppWidget(appWidgetId, fallback)
  }

  private fun resolveState(prefs: Preferences, fallback: BatteryState): BatteryState {
    val percentage = prefs[KEY_PERCENTAGE] ?: return fallback
    val isCharging = prefs[KEY_IS_CHARGING] ?: fallback.isCharging
    val isFull = prefs[KEY_IS_FULL] ?: fallback.isFull
    val sourceName = prefs[KEY_CHARGE_SOURCE]
    val source =
      sourceName?.let { runCatching { ChargeSource.valueOf(it) }.getOrNull() }
        ?: fallback.chargeSource
    return fallback.copy(
      percentage = percentage.coerceIn(0, 100),
      isCharging = isCharging,
      isFull = isFull,
      chargeSource = source,
    )
  }

  companion object {
    val KEY_PERCENTAGE = intPreferencesKey("battery_percentage")
    val KEY_IS_CHARGING = booleanPreferencesKey("battery_is_charging")
    val KEY_IS_FULL = booleanPreferencesKey("battery_is_full")
    val KEY_CHARGE_SOURCE = stringPreferencesKey("battery_charge_source")
    val KEY_UPDATED_AT = longPreferencesKey("battery_updated_at")
  }
}

@Composable
fun BatteryWidgetContent(state: BatteryState) {
  val size = LocalSize.current
  val isCompact = size.width < 135.dp || size.height < 135.dp
  val outerPadding = if (isCompact) 11.dp else 14.dp
  val percentFontSize = if (isCompact) 34.sp else 42.sp
  val symbolFontSize = if (isCompact) 18.sp else 20.sp
  val iconSize = if (isCompact) 22.dp else 26.dp

  val accentColor =
    if (state.isLow) GlanceTheme.colors.error else GlanceTheme.colors.primary

  val surfaceColor =
    if (state.isLow) GlanceTheme.colors.errorContainer
    else GlanceTheme.colors.widgetBackground
  val borderColor =
    if (state.isLow) GlanceTheme.colors.error
    else GlanceTheme.colors.widgetBackground
  val primaryTextColor =
    if (state.isLow) GlanceTheme.colors.onErrorContainer
    else GlanceTheme.colors.onSurface
  val secondaryTextColor =
    if (state.isLow) GlanceTheme.colors.onErrorContainer
    else GlanceTheme.colors.onSurfaceVariant
  val inactiveTrackColor =
    if (state.isLow) GlanceTheme.colors.surface
    else GlanceTheme.colors.surfaceVariant

  val iconRes = BatteryReader.selectBatteryIconRes(state.percentage, state.isCharging)
  val activeSegments =
    if (state.percentage <= 0) 0
    else ((state.percentage + 5) / 10).coerceIn(1, 10)

  val statusSummary = state.statusSubtitle()

  Box(
    modifier =
      GlanceModifier.fillMaxSize()
        .appWidgetBackground()
        .background(borderColor)
        .cornerRadius(R.dimen.widget_background_radius)
        .padding(if (state.isLow) 2.dp else 0.dp)
        .clickable(actionStartActivity<MainActivity>())
        .semantics {
          testTag = "battery_widget_root"
          contentDescription = "Batterie ${state.percentage}%, $statusSummary"
        },
  ) {
    Column(
      modifier =
        GlanceModifier.fillMaxSize()
          .background(surfaceColor)
          .cornerRadius(
            if (state.isLow) R.dimen.widget_inner_radius
            else R.dimen.widget_background_radius
          )
          .padding(outerPadding),
      verticalAlignment = Alignment.Top,
      horizontalAlignment = Alignment.Start,
    ) {
      // Top Row: Dynamic battery level icon + charging / low status pill
      Row(
        modifier = GlanceModifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Image(
          provider = ImageProvider(iconRes),
          contentDescription = "Niveau de batterie ${state.percentage}%",
          colorFilter = ColorFilter.tint(accentColor),
          modifier = GlanceModifier.size(iconSize).semantics { testTag = "battery_widget_icon" },
        )

        Spacer(modifier = GlanceModifier.defaultWeight())

        if (state.isCharging || state.isLow) {
          val pillBg =
            if (state.isLow) GlanceTheme.colors.error
            else GlanceTheme.colors.primaryContainer
          val pillFg =
            if (state.isLow) GlanceTheme.colors.onError
            else GlanceTheme.colors.onPrimaryContainer
          val badgeText =
            when {
              state.isFull -> "Pleine"
              state.isCharging -> "Charge"
              else -> "Faible"
            }

          Row(
            modifier =
              GlanceModifier.background(pillBg)
                .cornerRadius(50.dp)
                .padding(horizontal = 8.dp, vertical = 3.dp)
                .semantics { testTag = "battery_widget_badge" },
            verticalAlignment = Alignment.CenterVertically,
          ) {
            if (state.isCharging) {
              Image(
                provider = ImageProvider(R.drawable.ic_bolt),
                contentDescription = "Indicateur de charge",
                colorFilter = ColorFilter.tint(pillFg),
                modifier = GlanceModifier.size(11.dp),
              )
              Spacer(modifier = GlanceModifier.width(3.dp))
            }
            Text(
              text = badgeText,
              style =
                TextStyle(
                  color = pillFg,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Medium,
                ),
              maxLines = 1,
            )
          }
        }
      }

      Spacer(modifier = GlanceModifier.defaultWeight())

      // Hero exact percentage
      Row(
        verticalAlignment = Alignment.Bottom,
        modifier = GlanceModifier.semantics { testTag = "battery_widget_percentage" },
      ) {
        Text(
          text = "${state.percentage}",
          style =
            TextStyle(
              color = primaryTextColor,
              fontSize = percentFontSize,
              fontWeight = FontWeight.Bold,
            ),
          maxLines = 1,
        )
        Spacer(modifier = GlanceModifier.width(2.dp))
        Text(
          text = "%",
          style =
            TextStyle(
              color = secondaryTextColor,
              fontSize = symbolFontSize,
              fontWeight = FontWeight.Medium,
            ),
          modifier = GlanceModifier.padding(bottom = 4.dp),
          maxLines = 1,
        )
      }

      Spacer(modifier = GlanceModifier.height(6.dp))

      // Minimalist 10-pill segmented dynamic level bar
      Row(
        modifier = GlanceModifier.fillMaxWidth().semantics { testTag = "battery_widget_meter" },
        verticalAlignment = Alignment.CenterVertically,
      ) {
        for (index in 0 until 10) {
          val isFilled = index < activeSegments
          val segmentColor =
            if (isFilled) accentColor else inactiveTrackColor
          Box(
            modifier =
              GlanceModifier.defaultWeight()
                .height(6.dp)
                .background(segmentColor)
                .cornerRadius(4.dp),
            content = {},
          )
          if (index < 9) {
            Spacer(modifier = GlanceModifier.width(3.dp))
          }
        }
      }

      Spacer(modifier = GlanceModifier.height(6.dp))

      // Subtle status subtitle
      Text(
        text = statusSummary,
        style =
          TextStyle(
            color = secondaryTextColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
          ),
        maxLines = 1,
        modifier = GlanceModifier.semantics { testTag = "battery_widget_status" },
      )
    }
  }
}
