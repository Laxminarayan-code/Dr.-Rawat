package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Biotech
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.rounded.Biotech
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.domain.CellCalculator
import com.example.model.AppDestination
import com.example.ui.screens.CountScreen
import com.example.ui.screens.GuideScreen
import com.example.ui.screens.SeedScreen
import com.example.ui.theme.CytoCountTheme
import com.example.viewmodel.CytoCountViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: CytoCountViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val systemDark = isSystemInDarkTheme()
            val useDarkTheme = uiState.darkroomModeOverride ?: systemDark

            CytoCountTheme(darkTheme = useDarkTheme) {
                CytoCountApp(
                    viewModel = viewModel,
                    isDarkTheme = useDarkTheme
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CytoCountApp(
    viewModel: CytoCountViewModel,
    isDarkTheme: Boolean
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current
    val copiedMessage = stringResource(R.string.msg_recipe_copied)

    // Handle system back on secondary tabs cleanly
    BackHandler(enabled = uiState.currentDestination != AppDestination.COUNT) {
        viewModel.navigateTo(AppDestination.COUNT)
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useNavRail = maxWidth >= 600.dp

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            contentWindowInsets = WindowInsets.safeDrawing,
            containerColor = MaterialTheme.colorScheme.background,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = stringResource(R.string.app_name),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = stringResource(R.string.app_subtitle),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    actions = {
                        if (uiState.currentDestination == AppDestination.COUNT) {
                            IconButton(
                                onClick = { viewModel.resetCountInputs() },
                                modifier = Modifier.testTag("reset_count_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.RestartAlt,
                                    contentDescription = stringResource(R.string.action_reset_count)
                                )
                            }
                        }
                        IconButton(
                            onClick = { viewModel.toggleDarkroomMode(isDarkTheme) },
                            modifier = Modifier.testTag("toggle_darkroom_button")
                        ) {
                            Icon(
                                imageVector = if (isDarkTheme) Icons.Rounded.LightMode else Icons.Rounded.DarkMode,
                                contentDescription = stringResource(R.string.action_toggle_theme)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onBackground
                    )
                )
            },
            bottomBar = {
                if (!useNavRail) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        AppDestination.entries.forEach { destination ->
                            val selected = uiState.currentDestination == destination
                            NavigationBarItem(
                                selected = selected,
                                onClick = { viewModel.navigateTo(destination) },
                                icon = {
                                    Icon(
                                        imageVector = destinationIcon(destination, selected),
                                        contentDescription = destinationLabel(destination)
                                    )
                                },
                                label = { Text(destinationLabel(destination)) },
                                modifier = Modifier.testTag("nav_tab_${destination.name.lowercase()}")
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (useNavRail) {
                    NavigationRail(
                        containerColor = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        AppDestination.entries.forEach { destination ->
                            val selected = uiState.currentDestination == destination
                            NavigationRailItem(
                                selected = selected,
                                onClick = { viewModel.navigateTo(destination) },
                                icon = {
                                    Icon(
                                        imageVector = destinationIcon(destination, selected),
                                        contentDescription = destinationLabel(destination)
                                    )
                                },
                                label = { Text(destinationLabel(destination)) },
                                modifier = Modifier.testTag("rail_tab_${destination.name.lowercase()}")
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .widthIn(max = 720.dp)
                    ) {
                        when (uiState.currentDestination) {
                            AppDestination.COUNT -> CountScreen(
                                uiState = uiState,
                                onModeChange = viewModel::setCountInputMode,
                                onQuickTotalChange = viewModel::updateQuickTotalCount,
                                onSelectQuadrant = viewModel::selectQuadrant,
                                onUpdateQuadrantValue = viewModel::updateQuadrantValue,
                                onStepActiveQuadrant = viewModel::stepActiveQuadrant,
                                onSquaresChange = viewModel::updateSquaresCounted,
                                onDilutionFactorChange = viewModel::updateDilutionFactor,
                                onVolumeChange = viewModel::updateSuspensionVolume,
                                onToggleViability = viewModel::toggleViabilityInputs,
                                onDeadCellsChange = viewModel::updateDeadCellsCount,
                                onProceedToSeed = { viewModel.navigateTo(AppDestination.SEED) }
                            )

                            AppDestination.SEED -> SeedScreen(
                                uiState = uiState,
                                onToggleManualStock = viewModel::setManualStockOverride,
                                onManualStockConcChange = viewModel::updateManualStockConcentration,
                                onManualStockExpChange = viewModel::updateManualStockExponent,
                                onManualAvailVolChange = viewModel::updateManualAvailableVolume,
                                onSelectPlate = viewModel::selectPlateFormat,
                                onSelectDensityPreset = viewModel::selectDensityPreset,
                                onCustomDensityChange = viewModel::updateCustomDensityInput,
                                onCustomDensityUnitChange = viewModel::selectCustomDensityUnit,
                                onWellsChange = viewModel::updateWellsToSeed,
                                onStepWells = viewModel::stepWellsToSeed,
                                onWorkingVolumeChange = viewModel::updateWorkingVolumePerWell,
                                onOverageChange = viewModel::selectOveragePercent,
                                onCopyProtocol = {
                                    val summary = CellCalculator.buildProtocolSummary(
                                        plan = uiState.seedingPlan,
                                        preset = uiState.selectedDensityPreset
                                    )
                                    clipboardManager.setText(AnnotatedString(summary))
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar(copiedMessage)
                                    }
                                },
                                onGoToCount = { viewModel.navigateTo(AppDestination.COUNT) }
                            )

                            AppDestination.GUIDE -> GuideScreen(
                                onApplySuggestion = viewModel::applyGuideSuggestion
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun destinationLabel(destination: AppDestination): String = when (destination) {
    AppDestination.COUNT -> stringResource(R.string.nav_count)
    AppDestination.SEED -> stringResource(R.string.nav_seed)
    AppDestination.GUIDE -> stringResource(R.string.nav_guide)
}

private fun destinationIcon(destination: AppDestination, selected: Boolean) = when (destination) {
    AppDestination.COUNT -> if (selected) Icons.Rounded.Biotech else Icons.Outlined.Biotech
    AppDestination.SEED -> if (selected) Icons.Rounded.GridView else Icons.Outlined.GridView
    AppDestination.GUIDE -> if (selected) Icons.Rounded.MenuBook else Icons.Outlined.MenuBook
}
