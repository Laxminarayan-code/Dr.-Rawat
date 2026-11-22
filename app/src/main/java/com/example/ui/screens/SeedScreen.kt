package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Science
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.domain.CellCalculator
import com.example.model.DensityInputUnit
import com.example.model.DensityPreset
import com.example.model.PlateFormat
import com.example.model.SeedingPlanResult
import com.example.model.SeedingStatus
import com.example.ui.components.MultiWellPlateVisual
import com.example.ui.components.PresetChipButton
import com.example.ui.components.SectionHeader
import com.example.ui.theme.LocalCytoColors
import com.example.ui.theme.MonoFontFamily
import com.example.viewmodel.CytoCountUiState

@Composable
fun SeedScreen(
    uiState: CytoCountUiState,
    onToggleManualStock: (Boolean) -> Unit,
    onManualStockConcChange: (String) -> Unit,
    onManualStockExpChange: (Int) -> Unit,
    onManualAvailVolChange: (String) -> Unit,
    onSelectPlate: (PlateFormat) -> Unit,
    onSelectDensityPreset: (DensityPreset) -> Unit,
    onCustomDensityChange: (String) -> Unit,
    onCustomDensityUnitChange: (DensityInputUnit) -> Unit,
    onWellsChange: (String) -> Unit,
    onStepWells: (Int) -> Unit,
    onWorkingVolumeChange: (String) -> Unit,
    onOverageChange: (Int) -> Unit,
    onCopyProtocol: () -> Unit,
    onGoToCount: () -> Unit,
    modifier: Modifier = Modifier
) {
    val plan = uiState.seedingPlan

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Stock Source Strip (Synced from Count or Manual Override)
        item {
            StockSourceCard(
                uiState = uiState,
                onToggleManualStock = onToggleManualStock,
                onManualStockConcChange = onManualStockConcChange,
                onManualStockExpChange = onManualStockExpChange,
                onManualAvailVolChange = onManualAvailVolChange,
                onGoToCount = onGoToCount
            )
        }

        // 2. Hero Master Mix Recipe Output Card (front-and-center feedback!)
        item {
            MasterMixRecipeCard(
                plan = plan,
                preset = uiState.selectedDensityPreset,
                onCopyProtocol = onCopyProtocol,
                onAdjustToMaxWells = { maxW -> onWellsChange(maxW.coerceAtLeast(1).toString()) },
                onGoToCount = onGoToCount
            )
        }

        // 3. Multi-Well Plate Format Selector (6, 12, 24, 48, 96-well)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionHeader(
                    title = stringResource(R.string.section_plate_format),
                    icon = Icons.Rounded.GridView
                )
                PlateFormatSelectorRow(
                    selectedPlate = uiState.selectedPlate,
                    wellsRequested = plan.wellsRequested,
                    onSelectPlate = onSelectPlate
                )
            }
        }

        // 4. Experimental Density Suggestion Selector (Low / Moderate / High / Custom)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionHeader(
                    title = stringResource(R.string.section_density_suggestion),
                    icon = Icons.Rounded.Layers
                )
                DensitySuggestionSection(
                    plate = uiState.selectedPlate,
                    selectedPreset = uiState.selectedDensityPreset,
                    customDensityInput = uiState.customDensityInput,
                    customDensityUnit = uiState.customDensityUnit,
                    onSelectPreset = onSelectDensityPreset,
                    onCustomDensityChange = onCustomDensityChange,
                    onCustomUnitChange = onCustomDensityUnitChange
                )
            }
        }

        // 5. Wells to Seed, Working Volume & Pipetting Overage
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionHeader(
                    title = stringResource(R.string.section_well_setup),
                    icon = Icons.Rounded.Tune
                )
                WellAndPipettingSetupCard(
                    plate = uiState.selectedPlate,
                    wellsInput = uiState.wellsToSeedInput,
                    wellsNumeric = plan.wellsRequested,
                    workingVolInput = uiState.workingVolumePerWellInput,
                    overagePercent = uiState.overagePercent,
                    onWellsChange = onWellsChange,
                    onStepWells = onStepWells,
                    onWorkingVolumeChange = onWorkingVolumeChange,
                    onOverageChange = onOverageChange
                )
            }
        }

        item { Spacer(modifier = Modifier.height(12.dp)) }
    }
}

@Composable
private fun StockSourceCard(
    uiState: CytoCountUiState,
    onToggleManualStock: (Boolean) -> Unit,
    onManualStockConcChange: (String) -> Unit,
    onManualStockExpChange: (Int) -> Unit,
    onManualAvailVolChange: (String) -> Unit,
    onGoToCount: () -> Unit
) {
    val plan = uiState.seedingPlan
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (uiState.useManualStockOverride) Icons.Rounded.Edit else Icons.Rounded.Sync,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = if (uiState.useManualStockOverride) {
                                stringResource(R.string.label_manual_stock)
                            } else {
                                stringResource(R.string.label_synced_from_count)
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (plan.stockCellsPerMl > 0.0) {
                                "${CellCalculator.formatScientific(plan.stockCellsPerMl)} cells/mL" +
                                    (plan.availableVolumeMl?.let { "  •  ${CellCalculator.formatDecimal(it)} mL" } ?: "")
                            } else {
                                "No stock concentration yet"
                            },
                            fontFamily = MonoFontFamily,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                TextButton(
                    onClick = { onToggleManualStock(!uiState.useManualStockOverride) },
                    modifier = Modifier.testTag("toggle_manual_stock_button")
                ) {
                    Text(
                        text = if (uiState.useManualStockOverride) {
                            stringResource(R.string.btn_use_counted_stock)
                        } else {
                            stringResource(R.string.btn_edit_stock)
                        }
                    )
                }
            }

            if (!uiState.useManualStockOverride && plan.stockCellsPerMl <= 0.0) {
                FilledTonalButton(
                    onClick = onGoToCount,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("go_to_count_from_seed_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Rounded.Science, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Count on Hemocytometer First")
                }
            }

            AnimatedVisibility(
                visible = uiState.useManualStockOverride,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = uiState.manualStockConcentrationInput,
                            onValueChange = onManualStockConcChange,
                            label = { Text("Stock Value") },
                            placeholder = { Text("1.5") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            textStyle = MaterialTheme.typography.titleMedium.copy(fontFamily = MonoFontFamily),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("manual_stock_conc_input")
                        )
                        OutlinedTextField(
                            value = uiState.manualAvailableVolumeInput,
                            onValueChange = onManualAvailVolChange,
                            label = { Text("Avail. Vol (mL)") },
                            placeholder = { Text("5.0") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            textStyle = MaterialTheme.typography.titleMedium.copy(fontFamily = MonoFontFamily),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("manual_stock_vol_input")
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(4 to "×10⁴", 5 to "×10⁵", 6 to "×10⁶", 7 to "×10⁷").forEach { (exp, label) ->
                            PresetChipButton(
                                label = label,
                                selected = uiState.manualStockExponent == exp,
                                onClick = { onManualStockExpChange(exp) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("manual_exp_chip_$exp")
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MasterMixRecipeCard(
    plan: SeedingPlanResult,
    preset: DensityPreset,
    onCopyProtocol: () -> Unit,
    onAdjustToMaxWells: (Int) -> Unit,
    onGoToCount: () -> Unit
) {
    val cytoColors = LocalCytoColors.current
    val (statusLabel, statusColor, statusBg, statusIcon) = when (plan.status) {
        is SeedingStatus.Ready -> Quad(
            stringResource(R.string.status_ready),
            cytoColors.statusReady,
            cytoColors.statusReadyContainer,
            Icons.Rounded.CheckCircle
        )
        is SeedingStatus.CentrifugeRequired -> Quad(
            stringResource(R.string.status_centrifuge),
            cytoColors.statusWarning,
            cytoColors.statusWarningContainer,
            Icons.Rounded.WarningAmber
        )
        is SeedingStatus.InsufficientCells -> Quad(
            stringResource(R.string.status_insufficient),
            MaterialTheme.colorScheme.error,
            MaterialTheme.colorScheme.errorContainer,
            Icons.Rounded.ErrorOutline
        )
        SeedingStatus.AwaitingStock -> Quad(
            stringResource(R.string.status_awaiting_stock),
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.primaryContainer,
            Icons.Rounded.Science
        )
    }

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.5.dp, statusColor.copy(alpha = 0.45f)),
        shadowElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("master_mix_recipe_card")
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header + Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.section_seeding_recipe),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${plan.plate.shortName} · ${preset.shortLabel} · ${plan.wellsRequested} wells (+${plan.overagePercent}%)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = statusBg
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = statusIcon,
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = statusLabel,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                    }
                }
            }

            when (val status = plan.status) {
                SeedingStatus.AwaitingStock -> {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Target cells needed: ${CellCalculator.formatScientific(plan.totalCellsNeeded)} cells",
                                fontFamily = MonoFontFamily,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Total master mix volume: ${CellCalculator.formatVolumePrimary(plan.totalMasterMixMl)} (${CellCalculator.formatVolumePrimary(plan.workingVolumePerWellMl)}/well)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Enter a hemocytometer count on the Count tab or tap Edit above to compute exact cell suspension & media volumes.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                is SeedingStatus.Ready -> {
                    // Two bold pipetting volumes side-by-side
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        RecipeVolumeTile(
                            label = stringResource(R.string.label_take_cell_suspension),
                            primaryVolume = CellCalculator.formatVolumePrimary(plan.requiredCellSuspensionMl),
                            secondaryVolume = CellCalculator.formatVolumeSecondary(plan.requiredCellSuspensionMl),
                            accentColor = MaterialTheme.colorScheme.primary,
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("required_cell_volume_tile")
                        )
                        RecipeVolumeTile(
                            label = stringResource(R.string.label_add_fresh_media),
                            primaryVolume = CellCalculator.formatVolumePrimary(plan.requiredFreshMediaMl),
                            secondaryVolume = CellCalculator.formatVolumeSecondary(plan.requiredFreshMediaMl),
                            accentColor = MaterialTheme.colorScheme.secondary,
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("required_media_volume_tile")
                        )
                    }

                    // Mix & dispense summary strip
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = stringResource(R.string.label_total_master_mix),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${CellCalculator.formatVolumePrimary(plan.totalMasterMixMl)}  (${CellCalculator.formatVolumePrimary(plan.workingVolumePerWellMl)}/well)",
                                    fontFamily = MonoFontFamily,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = stringResource(R.string.label_total_cells_required),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${CellCalculator.formatScientific(plan.totalCellsNeeded)} cells",
                                    fontFamily = MonoFontFamily,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                is SeedingStatus.CentrifugeRequired -> {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = cytoColors.statusWarningContainer.copy(alpha = 0.7f)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Stock is too dilute for direct mixing (${CellCalculator.formatVolumePrimary(plan.requiredCellSuspensionMl)} exceeds ${CellCalculator.formatVolumePrimary(plan.totalMasterMixMl)} master mix).",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "1. Centrifuge ${CellCalculator.formatVolumePrimary(status.spinDownVolumeMl)} of your cell stock.",
                                fontFamily = MonoFontFamily,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = cytoColors.statusWarning
                            )
                            Text(
                                text = "2. Resuspend pellet in ${CellCalculator.formatVolumePrimary(status.resuspendMediaMl)} fresh media, then dispense ${CellCalculator.formatVolumePrimary(plan.workingVolumePerWellMl)}/well.",
                                fontFamily = MonoFontFamily,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                is SeedingStatus.InsufficientCells -> {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Need ${CellCalculator.formatScientific(plan.totalCellsNeeded)} cells, but only ${CellCalculator.formatScientific(plan.totalAvailableCells ?: 0.0)} cells are available in your suspension.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = "Max seedable wells at this density (+${plan.overagePercent}% margin): ${status.maxSeedableWells} wells",
                                fontFamily = MonoFontFamily,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                            if (status.maxSeedableWells > 0) {
                                Button(
                                    onClick = { onAdjustToMaxWells(status.maxSeedableWells) },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.error,
                                        contentColor = MaterialTheme.colorScheme.onError
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(44.dp)
                                        .testTag("adjust_max_wells_button")
                                ) {
                                    Text("${stringResource(R.string.btn_adjust_max_wells)} (${status.maxSeedableWells})")
                                }
                            }
                        }
                    }
                }
            }

            // Stock utilization progress meter when available volume is known
            if (plan.stockUtilizationFraction != null && plan.status !is SeedingStatus.AwaitingStock) {
                val pct = (plan.stockUtilizationFraction * 100f).toInt()
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Stock Cell Usage",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$pct% of available stock",
                            fontFamily = MonoFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (pct > 100) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    LinearProgressIndicator(
                        progress = { plan.stockUtilizationFraction.coerceIn(0f, 1f) },
                        color = statusColor,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )
                }
            }

            // Copy Protocol Button
            if (plan.status !is SeedingStatus.AwaitingStock) {
                FilledTonalButton(
                    onClick = onCopyProtocol,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("copy_protocol_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ContentCopy,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.btn_copy_recipe),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

@Composable
private fun RecipeVolumeTile(
    label: String,
    primaryVolume: String,
    secondaryVolume: String,
    accentColor: Color,
    containerColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = containerColor
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = primaryVolume,
                fontFamily = MonoFontFamily,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )
            Text(
                text = secondaryVolume,
                fontFamily = MonoFontFamily,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PlateFormatSelectorRow(
    selectedPlate: PlateFormat,
    wellsRequested: Int,
    onSelectPlate: (PlateFormat) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(PlateFormat.entries) { plate ->
            val isSelected = plate == selectedPlate
            Surface(
                onClick = { onSelectPlate(plate) },
                shape = RoundedCornerShape(16.dp),
                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                else MaterialTheme.colorScheme.surface,
                border = BorderStroke(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                ),
                modifier = Modifier
                    .width(120.dp)
                    .testTag("plate_card_${plate.wellCount}")
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    MultiWellPlateVisual(
                        plate = plate,
                        seededWells = if (isSelected) wellsRequested else plate.wellCount,
                        isSelected = isSelected,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = plate.shortName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${plate.surfaceAreaCm2} cm²",
                        fontFamily = MonoFontFamily,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun DensitySuggestionSection(
    plate: PlateFormat,
    selectedPreset: DensityPreset,
    customDensityInput: String,
    customDensityUnit: DensityInputUnit,
    onSelectPreset: (DensityPreset) -> Unit,
    onCustomDensityChange: (String) -> Unit,
    onCustomUnitChange: (DensityInputUnit) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // 2x2 Grid of Low, Moderate, High, Custom suggestion cards
        val topRow = listOf(DensityPreset.LOW, DensityPreset.MODERATE)
        val bottomRow = listOf(DensityPreset.HIGH, DensityPreset.CUSTOM)

        listOf(topRow, bottomRow).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                rowItems.forEach { preset ->
                    val isSelected = preset == selectedPreset
                    val suggestedVal = if (preset != DensityPreset.CUSTOM) {
                        CellCalculator.formatScientific(plate.suggestedCellsPerWell(preset), 1)
                    } else {
                        "Custom target"
                    }

                    Surface(
                        onClick = { onSelectPreset(preset) },
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
                        else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("density_preset_${preset.name.lowercase()}")
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = preset.label,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (preset == DensityPreset.CUSTOM) suggestedVal else "$suggestedVal / well",
                                fontFamily = MonoFontFamily,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = preset.confluencyHint,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Experimental use-case tip for the selected density
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Suggested for: ${selectedPreset.typicalUseCase}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (selectedPreset != DensityPreset.CUSTOM) {
                    val perCm2 = plate.suggestedCellsPerWell(selectedPreset) / plate.surfaceAreaCm2
                    Text(
                        text = "Surface seeding density ≈ ${CellCalculator.formatScientific(perCm2, 1)} cells/cm²",
                        fontFamily = MonoFontFamily,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Custom density input box when CUSTOM is active
        AnimatedVisibility(
            visible = selectedPreset == DensityPreset.CUSTOM,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        DensityInputUnit.entries.forEachIndexed { idx, unit ->
                            SegmentedButton(
                                selected = customDensityUnit == unit,
                                onClick = { onCustomUnitChange(unit) },
                                shape = SegmentedButtonDefaults.itemShape(idx, DensityInputUnit.entries.size),
                                modifier = Modifier.testTag("custom_unit_${unit.name.lowercase()}")
                            ) {
                                Text(unit.label)
                            }
                        }
                    }
                    OutlinedTextField(
                        value = customDensityInput,
                        onValueChange = onCustomDensityChange,
                        label = { Text("Target Density (${customDensityUnit.label})") },
                        placeholder = { Text("50000") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        textStyle = MaterialTheme.typography.titleLarge.copy(fontFamily = MonoFontFamily),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("custom_density_input")
                    )
                }
            }
        }
    }
}

@Composable
private fun WellAndPipettingSetupCard(
    plate: PlateFormat,
    wellsInput: String,
    wellsNumeric: Int,
    workingVolInput: String,
    overagePercent: Int,
    onWellsChange: (String) -> Unit,
    onStepWells: (Int) -> Unit,
    onWorkingVolumeChange: (String) -> Unit,
    onOverageChange: (Int) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Wells to seed + interactive stepper
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = wellsInput,
                    onValueChange = onWellsChange,
                    label = { Text(stringResource(R.string.label_wells_to_seed)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    textStyle = MaterialTheme.typography.titleLarge.copy(fontFamily = MonoFontFamily),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("wells_to_seed_input")
                )
                FilledTonalIconButton(
                    onClick = { onStepWells(-1) },
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("wells_decrement_button")
                ) {
                    Icon(Icons.Rounded.Remove, contentDescription = "Decrease wells to seed")
                }
                FilledTonalIconButton(
                    onClick = { onStepWells(1) },
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("wells_increment_button")
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = "Increase wells to seed")
                }
            }

            // Quick well count chips tailored to the selected plate
            val halfPlate = (plate.wellCount / 2).coerceAtLeast(1)
            val quickWells = listOf(
                3 to "3 (Trip.)",
                halfPlate to "Half ($halfPlate)",
                plate.wellCount to "Full (${plate.wellCount})"
            ).distinctBy { it.first }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                quickWells.forEach { (count, label) ->
                    PresetChipButton(
                        label = label,
                        selected = wellsNumeric == count,
                        onClick = { onWellsChange(count.toString()) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("quick_wells_chip_$count")
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))

            // Working Volume per well
            OutlinedTextField(
                value = workingVolInput,
                onValueChange = onWorkingVolumeChange,
                label = { Text(stringResource(R.string.label_volume_per_well)) },
                supportingText = {
                    Text(
                        "Recommended for ${plate.shortName}: ${CellCalculator.formatDecimal(plate.minRecommendedVolMl)}–${CellCalculator.formatDecimal(plate.maxRecommendedVolMl)} mL/well"
                    )
                },
                suffix = { Text("mL", fontFamily = MonoFontFamily, fontWeight = FontWeight.Bold) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                textStyle = MaterialTheme.typography.titleLarge.copy(fontFamily = MonoFontFamily),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("working_volume_input")
            )

            // Pipetting Overage Margin
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.label_overage_margin),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(0, 10, 15, 20).forEach { pct ->
                        PresetChipButton(
                            label = if (pct == 0) "Exact" else "+$pct%",
                            selected = overagePercent == pct,
                            onClick = { onOverageChange(pct) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("overage_chip_$pct")
                        )
                    }
                }
            }
        }
    }
}

private data class Quad<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)
