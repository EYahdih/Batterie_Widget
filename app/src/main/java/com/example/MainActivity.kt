package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.battery.BatteryBroadcastMonitor
import com.example.ui.BatteryStudioScreen
import com.example.ui.BatteryViewModel
import com.example.ui.theme.VoltTheme

class MainActivity : ComponentActivity() {

  private val viewModel: BatteryViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    BatteryBroadcastMonitor.ensureRegistered(applicationContext)

    setContent {
      VoltTheme {
        BatteryStudioScreen(viewModel = viewModel)
      }
    }
  }

  override fun onResume() {
    super.onResume()
    viewModel.refreshCount()
    viewModel.syncWidgetNow()
  }
}
