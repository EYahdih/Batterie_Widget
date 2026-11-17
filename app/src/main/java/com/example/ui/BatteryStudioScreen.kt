package com.example.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddToHomeScreen
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DeviceThermostat
import androidx.compose.material.icons.rounded.ElectricBolt
import androidx.compose.material.icons.rounded.HealthAndSafety
import androidx.compose.material.icons.rounded.Power
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.battery.BatteryReader
import com.example.battery.BatteryState
import com.example.ui.theme.VoltTheme
import java.util.Locale
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatteryStudioScreen(
  viewModel: BatteryViewModel,
  modifier: Modifier = Modifier,
) {
  val batteryState by viewModel.batteryState.collectAsStateWithLifecycle()
  val activeWidgetCount by viewModel.activeWidgetCount.collectAsStateWithLifecycle()
  val previewMode by viewModel.previewThemeMode.collectAsStateWithLifecycle()
  val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()

  val context = LocalContext.current
  val snackbarHostState = remember { SnackbarHostState() }
  val scope = rememberCoroutineScope()

  Scaffold(
    modifier = modifier.fillMaxSize(),
    contentWindowInsets = WindowInsets.safeDrawing,
    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    topBar = {
      CenterAlignedTopAppBar(
        title = {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Text(
              text = "Volt",
              style = MaterialTheme.typography.titleLarge,
            )
            Surface(
              shape = CircleShape,
              color = MaterialTheme.colorScheme.primaryContainer,
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
              ) {
                Box(
                  modifier =
                    Modifier.size(6.dp)
                      .clip(CircleShape)
                      .background(MaterialTheme.colorScheme.primary),
                )
                Text(
                  text = "Temps réel",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
              }
            }
          }
        },
        colors =
          TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
          ),
      )
    },
  ) { innerPadding ->
    Column(
      modifier =
        Modifier.fillMaxSize()
          .padding(innerPadding)
          .verticalScroll(rememberScrollState())
          .padding(horizontal = 20.dp, vertical = 12.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Column(
        modifier = Modifier.fillMaxWidth().widthIn(max = 560.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
      ) {
        // Section: Live 2x2 Widget Preview Stage
        WidgetPreviewStage(
          batteryState = batteryState,
          previewMode = previewMode,
          onPreviewModeChange = viewModel::setPreviewThemeMode,
          activeWidgetCount = activeWidgetCount,
        )

        // Primary Actions: Pin 2x2 Widget + Force Sync
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Button(
            onClick = {
              viewModel.requestPinWidget(context) {
                scope.launch {
                  snackbarHostState.showSnackbar(
                    "Maintenez l'écran d'accueil appuyé → Widgets → Volt (2×2)."
                  )
                }
              }
            },
            modifier =
              Modifier.fillMaxWidth()
                .height(52.dp)
                .testTag("pin_widget_button"),
            shape = RoundedCornerShape(16.dp),
            contentPadding = PaddingValues(horizontal = 20.dp),
          ) {
            Icon(
              imageVector = Icons.Rounded.AddToHomeScreen,
              contentDescription = null,
              modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
              text = "Ajouter le widget 2×2",
              style = MaterialTheme.typography.labelLarge,
            )
          }

          OutlinedButton(
            onClick = {
              viewModel.syncWidgetNow { ok ->
                scope.launch {
                  snackbarHostState.showSnackbar(
                    if (ok) "Widget synchronisé avec l'état actuel (${batteryState.percentage}%)."
                    else "Impossible de synchroniser le widget."
                  )
                }
              }
            },
            enabled = !isSyncing,
            modifier =
              Modifier.fillMaxWidth()
                .height(48.dp)
                .testTag("sync_widget_button"),
            shape = RoundedCornerShape(16.dp),
          ) {
            Icon(
              imageVector = Icons.Rounded.Refresh,
              contentDescription = null,
              modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = if (isSyncing) "Synchronisation..." else "Synchroniser maintenant",
              style = MaterialTheme.typography.labelLarge,
            )
          }
        }

        // Section: Live Hardware Telemetry
        TelemetrySection(batteryState = batteryState)

        // Footer Info Pill explaining BroadcastReceiver architecture
        Surface(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(18.dp),
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
        ) {
          Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            Icon(
              imageVector = Icons.Rounded.Widgets,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(20.dp),
            )
            Text(
              text =
                "Propulsé par Jetpack Glance & BroadcastReceiver (ACTION_BATTERY_CHANGED). Mise à jour événementielle sans polling ni drain batterie.",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
      }
    }
  }
}

@Composable
private fun WidgetPreviewStage(
  batteryState: BatteryState,
  previewMode: PreviewThemeMode,
  onPreviewModeChange: (PreviewThemeMode) -> Unit,
  activeWidgetCount: Int,
) {
  val previewDark =
    when (previewMode) {
      PreviewThemeMode.SYSTEM -> isSystemInDarkTheme()
      PreviewThemeMode.LIGHT -> false
      PreviewThemeMode.DARK -> true
    }

  Surface(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(28.dp),
    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
  ) {
    Column(
      modifier = Modifier.padding(20.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Text(
          text = "Aperçu 2×2",
          style = MaterialTheme.typography.titleMedium,
          color = MaterialTheme.colorScheme.onSurface,
        )

        Surface(
          shape = CircleShape,
          color =
            if (activeWidgetCount > 0) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceVariant,
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
          ) {
            if (activeWidgetCount > 0) {
              Icon(
                imageVector = Icons.Rounded.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(14.dp),
              )
            }
            Text(
              text =
                when (activeWidgetCount) {
                  0 -> "Non épinglé"
                  1 -> "1 widget actif"
                  else -> "$activeWidgetCount widgets actifs"
                },
              style = MaterialTheme.typography.labelSmall,
              color =
                if (activeWidgetCount > 0) MaterialTheme.colorScheme.onPrimaryContainer
                else MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
      }

      // Authentic 1:1 square replica of the 2x2 Material You Glance Widget
      VoltTheme(darkTheme = previewDark, dynamicColor = true) {
        LiveSquareWidgetReplica(
          state = batteryState,
          modifier = Modifier.width(184.dp).aspectRatio(1f).testTag("widget_live_preview"),
        )
      }

      SingleChoiceSegmentedButtonRow(
        modifier = Modifier.fillMaxWidth().testTag("preview_theme_selector"),
      ) {
        PreviewThemeMode.entries.forEachIndexed { index, mode ->
          SegmentedButton(
            selected = previewMode == mode,
            onClick = { onPreviewModeChange(mode) },
            shape =
              SegmentedButtonDefaults.itemShape(
                index = index,
                count = PreviewThemeMode.entries.size,
              ),
          ) {
            Text(
              text = mode.label,
              style = MaterialTheme.typography.labelMedium,
            )
          }
        }
      }
    }
  }
}

@Composable
private fun LiveSquareWidgetReplica(
  state: BatteryState,
  modifier: Modifier = Modifier,
) {
  val accentColor by
    animateColorAsState(
      targetValue =
        if (state.isLow) MaterialTheme.colorScheme.error
        else MaterialTheme.colorScheme.primary,
      animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
      label = "widgetAccent",
    )
  val cardSurfaceColor by
    animateColorAsState(
      targetValue =
        if (state.isLow) MaterialTheme.colorScheme.errorContainer
        else MaterialTheme.colorScheme.surface,
      animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
      label = "widgetSurface",
    )
  val borderColor by
    animateColorAsState(
      targetValue =
        if (state.isLow) MaterialTheme.colorScheme.error
        else MaterialTheme.colorScheme.outline.copy(alpha = 0.16f),
      animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
      label = "widgetBorder",
    )
  val primaryTextColor =
    if (state.isLow) MaterialTheme.colorScheme.onErrorContainer
    else MaterialTheme.colorScheme.onSurface
  val secondaryTextColor =
    if (state.isLow) MaterialTheme.colorScheme.onErrorContainer
    else MaterialTheme.colorScheme.onSurfaceVariant
  val inactiveTrackColor =
    if (state.isLow) MaterialTheme.colorScheme.surface
    else MaterialTheme.colorScheme.surfaceVariant

  val activeSegments =
    if (state.percentage <= 0) 0 else ((state.percentage + 5) / 10).coerceIn(1, 10)
  val iconRes = BatteryReader.selectBatteryIconRes(state.percentage, state.isCharging)

  Surface(
    modifier =
      modifier.border(
        width = if (state.isLow) 2.dp else 1.dp,
        color = borderColor,
        shape = RoundedCornerShape(26.dp),
      ),
    shape = RoundedCornerShape(26.dp),
    color = cardSurfaceColor,
    tonalElevation = 4.dp,
    shadowElevation = 6.dp,
  ) {
    Column(
      modifier = Modifier.fillMaxSize().padding(16.dp),
      verticalArrangement = Arrangement.SpaceBetween,
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Icon(
          painter = painterResource(id = iconRes),
          contentDescription = "Niveau batterie ${state.percentage}%",
          tint = accentColor,
          modifier = Modifier.size(26.dp),
        )

        if (state.isCharging || state.isLow) {
          val badgeBg =
            if (state.isLow) MaterialTheme.colorScheme.error
            else MaterialTheme.colorScheme.primaryContainer
          val badgeFg =
            if (state.isLow) MaterialTheme.colorScheme.onError
            else MaterialTheme.colorScheme.onPrimaryContainer
          val badgeText =
            when {
              state.isFull -> "Pleine"
              state.isCharging -> "Charge"
              else -> "Faible"
            }

          Surface(
            shape = CircleShape,
            color = badgeBg,
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(3.dp),
            ) {
              if (state.isCharging) {
                Icon(
                  imageVector = Icons.Rounded.Bolt,
                  contentDescription = "En charge",
                  tint = badgeFg,
                  modifier = Modifier.size(12.dp),
                )
              }
              Text(
                text = badgeText,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = badgeFg,
              )
            }
          }
        }
      }

      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.Bottom) {
          Text(
            text = "${state.percentage}",
            style = MaterialTheme.typography.displayMedium.copy(fontSize = 42.sp),
            color = primaryTextColor,
          )
          Spacer(modifier = Modifier.width(2.dp))
          Text(
            text = "%",
            style = MaterialTheme.typography.titleLarge,
            color = secondaryTextColor,
            modifier = Modifier.padding(bottom = 5.dp),
          )
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(3.dp),
        ) {
          for (index in 0 until 10) {
            val isFilled = index < activeSegments
            Box(
              modifier =
                Modifier.weight(1f)
                  .height(7.dp)
                  .clip(RoundedCornerShape(4.dp))
                  .background(if (isFilled) accentColor else inactiveTrackColor),
            )
          }
        }

        Text(
          text = state.statusSubtitle(),
          style = MaterialTheme.typography.labelSmall,
          color = secondaryTextColor,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }
    }
  }
}

@Composable
private fun TelemetrySection(batteryState: BatteryState) {
  Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
    Text(
      text = "Télémétrie en direct",
      style = MaterialTheme.typography.titleMedium,
      color = MaterialTheme.colorScheme.onSurface,
    )

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      TelemetryCard(
        icon = Icons.Rounded.Power,
        label = "Source",
        value =
          if (batteryState.isCharging) batteryState.chargeSource.label
          else "Sur batterie",
        modifier = Modifier.weight(1f),
      )
      TelemetryCard(
        icon = Icons.Rounded.DeviceThermostat,
        label = "Température",
        value = String.format(Locale.FRANCE, "%.1f °C", batteryState.temperatureCelsius),
        modifier = Modifier.weight(1f),
      )
    }

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      TelemetryCard(
        icon = Icons.Rounded.ElectricBolt,
        label = "Tension",
        value = "${batteryState.voltageMv} mV",
        modifier = Modifier.weight(1f),
      )
      TelemetryCard(
        icon = Icons.Rounded.HealthAndSafety,
        label = "Santé",
        value = "${batteryState.healthStatus.label} · ${batteryState.technology}",
        modifier = Modifier.weight(1f),
      )
    }

    batteryState.chargeTimeRemainingMs?.let { remainingMs ->
      val minutes = (remainingMs / 60_000L).coerceAtLeast(1L)
      val formatted =
        if (minutes >= 60) "${minutes / 60}h ${minutes % 60} min" else "$minutes min"
      TelemetryCard(
        icon = Icons.Rounded.Schedule,
        label = "Charge complète estimée",
        value = formatted,
        modifier = Modifier.fillMaxWidth(),
      )
    }
  }
}

@Composable
private fun TelemetryCard(
  icon: ImageVector,
  label: String,
  value: String,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier = modifier,
    shape = RoundedCornerShape(18.dp),
    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
  ) {
    Column(
      modifier = Modifier.padding(14.dp),
      verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(16.dp),
        )
        Text(
          text = label,
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }
      Text(
        text = value,
        style = MaterialTheme.typography.labelMedium,
        fontSize = 14.sp,
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    }
  }
}
