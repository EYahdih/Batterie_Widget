package com.example

import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.testing.unit.runGlanceAppWidgetUnitTest
import androidx.glance.testing.unit.assertHasText
import androidx.glance.testing.unit.hasTestTag
import androidx.test.core.app.ApplicationProvider
import com.example.battery.BatteryHealth
import com.example.battery.BatteryReader
import com.example.battery.BatteryState
import com.example.battery.ChargeSource
import com.example.widget.BatteryWidgetContent
import com.example.widget.VoltGlanceColorScheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `app name matches Volt`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Volt", appName)
  }

  @Test
  fun `parseIntent computes exact percentage and charging state`() {
    val intent =
      Intent(Intent.ACTION_BATTERY_CHANGED).apply {
        putExtra(BatteryManager.EXTRA_LEVEL, 173)
        putExtra(BatteryManager.EXTRA_SCALE, 200)
        putExtra(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_CHARGING)
        putExtra(BatteryManager.EXTRA_PLUGGED, BatteryManager.BATTERY_PLUGGED_AC)
        putExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_GOOD)
        putExtra(BatteryManager.EXTRA_TEMPERATURE, 315)
        putExtra(BatteryManager.EXTRA_VOLTAGE, 4180)
        putExtra(BatteryManager.EXTRA_TECHNOLOGY, "Li-poly")
      }

    val state = BatteryReader.parseIntent(intent)
    assertEquals(87, state.percentage)
    assertTrue(state.isCharging)
    assertFalse(state.isFull)
    assertEquals(ChargeSource.AC, state.chargeSource)
    assertEquals(BatteryHealth.GOOD, state.healthStatus)
    assertEquals(31.5f, state.temperatureCelsius, 0.01f)
    assertEquals(4180, state.voltageMv)
    assertEquals("Li-poly", state.technology)
  }

  @Test
  fun `selectBatteryIconRes picks charging and stepped level icons`() {
    assertEquals(R.drawable.ic_battery_charging, BatteryReader.selectBatteryIconRes(45, true))
    assertEquals(R.drawable.ic_battery_level_0, BatteryReader.selectBatteryIconRes(5, false))
    assertEquals(R.drawable.ic_battery_level_3, BatteryReader.selectBatteryIconRes(50, false))
    assertEquals(R.drawable.ic_battery_level_6, BatteryReader.selectBatteryIconRes(100, false))
  }

  @Test
  fun `glance widget renders exact percentage and charging indicator`() =
    runGlanceAppWidgetUnitTest {
      setAppWidgetSize(DpSize(160.dp, 160.dp))
      provideComposable {
        GlanceTheme(colors = VoltGlanceColorScheme.colors) {
          BatteryWidgetContent(
            state =
              BatteryState(
                percentage = 74,
                isCharging = true,
                isFull = false,
                chargeSource = ChargeSource.USB,
              )
          )
        }
      }

      onNode(hasTestTag("battery_widget_percentage")).assertExists()
      onNode(hasTestTag("battery_widget_badge")).assertExists()
      onNode(hasTestTag("battery_widget_status")).assertHasText("En charge • USB")
    }

  @Test
  fun `glance widget hides charging badge on normal discharge and shows low badge at 12 percent`() =
    runGlanceAppWidgetUnitTest {
      setAppWidgetSize(DpSize(140.dp, 140.dp))
      provideComposable {
        GlanceTheme(colors = VoltGlanceColorScheme.colors) {
          BatteryWidgetContent(
            state =
              BatteryState(
                percentage = 12,
                isCharging = false,
                isFull = false,
                chargeSource = ChargeSource.NONE,
              )
          )
        }
      }

      onNode(hasTestTag("battery_widget_badge")).assertExists()
      onNode(hasTestTag("battery_widget_status")).assertHasText("Batterie faible")
    }
}
