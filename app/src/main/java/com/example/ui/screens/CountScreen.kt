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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Biotech
import androidx.compose.material.icons.rounded.Functions
import androidx.compose.material.icons.rounded.GridOn
import androidx.compose.material.icons.rounded.Science
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.domain.CellCalculator
import com.example.model.CountInputMode
import com.example.model.HemocytometerResult
import com.example.ui.components.NeubauerQuadrantGrid
import com.example.ui.components.PresetChipButton
import com.example.ui.components.SectionHeader
import com.example.ui.theme.LocalCytoColors
import com.example.ui.theme.MonoFontFamily
import com.example.viewmodel.CytoCountUiState

@Composable
fun CountScreen(
    uiState: CytoCountUiState,
    onModeChange: (CountInputMode) -> Unit,
    onQuickTotalChange: (String) -> Unit,
    onSelectQuadrant: (Int) -> Unit,
    onUpdateQuadrantValue: (Int, String) -> Unit,
    onStepActiveQuadrant: (Int) -> Unit,
    onSquaresChange: (String) -> Unit,
    onDilutionFactorChange: (String) -> Unit,
    onVolumeChange: (String) -> Unit,
    onToggleViability: (Boolean) -> Unit,
    onDeadCellsChange: (String) -> Unit,
    onProceedToSeed: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Hero Live Cell Count Readout Card
        item {
            HemocytometerHeroCard(
                result = uiState.countResult,
                onProceedToSeed = onProceedToSeed
            )
        }

        // 2. Hemocytometer Input Mode & Cell Count Entry
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionHeader(
                    title = stringResource(R.string.section_hemocytometer_input),
                    icon = Icons.Rounded.GridOn
                )

                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SegmentedButton(
                        selected = uiState.inputMode == CountInputMode.QUICK_TOTAL,
                        onClick = { onModeChange(CountInputMode.QUICK_TOTAL) },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                        modifier = Modifier.testTag("mode_quick_total_button")
                    ) {
                        Text(stringResource(R.string.mode_quick_total))
                    }
                    SegmentedButton(
                        selected = uiState.inputMode == CountInputMode.FOUR_QUADRANT,
                        onClick = { onModeChange(CountInputMode.FOUR_QUADRANT) },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                        modifier = Modifier.testTag("mode_quadrant_grid_button")
                    ) {
                        Text(stringResource(R.string.mode_quadrant_grid))
                    }
                }

                if (uiState.inputMode == CountInputMode.QUICK_TOTAL) {
                    QuickTotalInputCard(
                        totalInput = uiState.quickTotalCountInput,
                        squaresInput = uiState.squaresCountedInput,
                        onTotalChange = onQuickTotalChange,
                        onSquaresChange = onSquaresChange
                    )
                } else {
                    NeubauerQuadrantGrid(
                        quadrantInputs = uiState.quadrantInputs,
                        activeQuadrantIndex = uiState.activeQuadrantIndex,
                        onSelectQuadrant = onSelectQuadrant,
                        onUpdateQuadrantValue = onUpdateQuadrantValue,
                        onStepActiveQuadrant = onStepActiveQuadrant
                    )
                }
            }
        }

        // 3. Suspension Parameters (Dilution Factor & Total Volume)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionHeader(
                    title = stringResource(R.string.section_suspension_params),
                    icon = Icons.Rounded.Science
                )
                SuspensionParametersCard(
                    dilutionInput = uiState.dilutionFactorInput,
                    volumeInput = uiState.suspensionVolumeMlInput,
                    onDilutionChange = onDilutionFactorChange,
                    onVolumeChange = onVolumeChange
                )
            }
        }

        // 4. Optional Trypan Blue Viability Card
        item {
            ViabilitySectionCard(
                enabled = uiState.showViabilityInputs,
                deadCellsInput = uiState.deadCellsCountInput,
                viabilityPercent = uiState.countResult.viabilityPercent,
                onToggle = onToggleViability,
                onDeadCellsChange = onDeadCellsChange
            )
        }

        // 5. Transparent Live Formula Breakdown
        item {
            FormulaVerificationCard(result = uiState.countResult)
        }

        item { Spacer(modifier = Modifier.height(12.dp)) }
    }
}

@Composable
private fun HemocytometerHeroCard(
    result: HemocytometerResult,
    onProceedToSeed: () -> Unit
) {
    val cytoColors = LocalCytoColors.current
    val gridOverlayColor = Color.White.copy(alpha = 0.06f)

    Surface(
        shape = RoundedCornerShape(24.dp),
        tonalElevation = 4.dp,
        shadowElevation = 3.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("hemocytometer_hero_card")
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(cytoColors.heroStart, cytoColors.heroEnd)
                    )
                )
                .drawBehind {
                    // Subtle etched hemocytometer reticle backdrop
                    val step = 36.dp.toPx()
                    var x = step
                    while (x < size.width) {
                        drawLine(gridOverlayColor, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1f)
                        x += step
                    }
                    var y = step
                    while (y < size.height) {
                        drawLine(gridOverlayColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
                        y += step
                    }
                }
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                if (!result.isValid) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color.White.copy(alpha = 0.14f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Biotech,
                                contentDescription = null,
                                tint = cytoColors.onHero,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Ready to Count",
                                style = MaterialTheme.typography.titleLarge,
                                color = cytoColors.onHero
                            )
                            Text(
                                text = stringResource(R.string.hint_enter_count),
                                style = MaterialTheme.typography.bodySmall,
                                color = cytoColors.onHeroMuted
                            )
                        }
                    }
                } else {
                    // Primary Readouts: Total Cells in Volume & Stock Concentration
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.label_total_cells),
                                style = MaterialTheme.typography.labelMedium,
                                color = cytoColors.onHeroMuted
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = CellCalculator.formatScientific(result.totalCellsInVolume),
                                fontFamily = MonoFontFamily,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold,
                                color = cytoColors.onHero,
                                modifier = Modifier.testTag("total_cells_readout")
                            )
                            Text(
                                text = "cells in ${CellCalculator.formatDecimal(result.suspensionVolumeMl)} mL",
                                style = MaterialTheme.typography.bodySmall,
                                color = cytoColors.onHeroMuted
                            )
                        }

                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(60.dp)
                                .background(Color.White.copy(alpha = 0.22f))
                                .align(Alignment.CenterVertically)
                        )

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.label_stock_concentration),
                                style = MaterialTheme.typography.labelMedium,
                                color = cytoColors.onHeroMuted
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = CellCalculator.formatScientific(result.cellsPerMl),
                                fontFamily = MonoFontFamily,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = cytoColors.onHero,
                                modifier = Modifier.testTag("concentration_readout")
                            )
                            Text(
                                text = stringResource(R.string.label_cells_per_ml),
                                style = MaterialTheme.typography.bodySmall,
                                color = cytoColors.onHeroMuted
                            )
                        }
                    }

                    // Secondary metrics strip
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color.White.copy(alpha = 0.12f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            HeroMiniStat(
                                label = stringResource(R.string.label_avg_per_square),
                                value = CellCalculator.formatDecimal(result.averagePerSquare, 1),
                                textColor = cytoColors.onHero,
                                labelColor = cytoColors.onHeroMuted
                            )
                            HeroMiniStat(
                                label = stringResource(R.string.label_cells_per_ul),
                                value = CellCalculator.formatScientific(result.cellsPerUl, 1),
                                textColor = cytoColors.onHero,
                                labelColor = cytoColors.onHeroMuted
                            )
                            HeroMiniStat(
                                label = stringResource(R.string.label_viability),
                                value = result.viabilityPercent?.let {
                                    "${CellCalculator.formatDecimal(it, 1)}%"
                                } ?: "—",
                                textColor = cytoColors.onHero,
                                labelColor = cytoColors.onHeroMuted
                            )
                        }
                    }
                }

                // Direct CTA to jump into Experimental Plate Seeding
                Button(
                    onClick = onProceedToSeed,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("proceed_to_seed_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = cytoColors.heroStart
                    )
                ) {
                    Text(
                        text = stringResource(R.string.btn_plan_seeding),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroMiniStat(
    label: String,
    value: String,
    textColor: Color,
    labelColor: Color
) {
    Column(horizontalAlignment = Alignment.Start) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = labelColor
        )
        Text(
            text = value,
            fontFamily = MonoFontFamily,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = textColor
        )
    }
}

@Composable
private fun QuickTotalInputCard(
    totalInput: String,
    squaresInput: String,
    onTotalChange: (String) -> Unit,
    onSquaresChange: (String) -> Unit
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
            OutlinedTextField(
                value = totalInput,
                onValueChange = onTotalChange,
                label = { Text(stringResource(R.string.label_total_cells_counted)) },
                placeholder = { Text("Enter counted viable cells") },
                suffix = { Text("cells", style = MaterialTheme.typography.labelMedium) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                textStyle = MaterialTheme.typography.headlineSmall.copy(fontFamily = MonoFontFamily),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("total_cells_counted_input")
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.label_squares_counted),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    OutlinedTextField(
                        value = squaresInput,
                        onValueChange = onSquaresChange,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        textStyle = MaterialTheme.typography.titleMedium.copy(fontFamily = MonoFontFamily),
                        modifier = Modifier
                            .width(88.dp)
                            .testTag("squares_counted_input")
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("1", "2", "4", "5", "8").forEach { sq ->
                        PresetChipButton(
                            label = "$sq sq",
                            selected = squaresInput == sq,
                            onClick = { onSquaresChange(sq) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("squares_chip_$sq")
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SuspensionParametersCard(
    dilutionInput: String,
    volumeInput: String,
    onDilutionChange: (String) -> Unit,
    onVolumeChange: (String) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Dilution Factor
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = dilutionInput,
                    onValueChange = onDilutionChange,
                    label = { Text(stringResource(R.string.label_dilution_factor)) },
                    placeholder = { Text("2") },
                    supportingText = { Text("1:1 with Trypan Blue = DF 2  •  Undiluted = DF 1") },
                    suffix = { Text("×", fontFamily = MonoFontFamily, fontWeight = FontWeight.Bold) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    textStyle = MaterialTheme.typography.titleLarge.copy(fontFamily = MonoFontFamily),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dilution_factor_input")
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("1" to "1×", "2" to "2× (1:1)", "5" to "5×", "10" to "10×").forEach { (value, label) ->
                        PresetChipButton(
                            label = label,
                            selected = dilutionInput == value,
                            onClick = { onDilutionChange(value) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("df_chip_$value")
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))

            // Total Suspension Volume
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = volumeInput,
                    onValueChange = onVolumeChange,
                    label = { Text(stringResource(R.string.label_suspension_volume)) },
                    placeholder = { Text("5.0") },
                    suffix = { Text("mL", fontFamily = MonoFontFamily, fontWeight = FontWeight.Bold) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    textStyle = MaterialTheme.typography.titleLarge.copy(fontFamily = MonoFontFamily),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("suspension_volume_input")
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("1", "2", "5", "10").forEach { vol ->
                        PresetChipButton(
                            label = "$vol mL",
                            selected = volumeInput == vol,
                            onClick = { onVolumeChange(vol) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("vol_chip_$vol")
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ViabilitySectionCard(
    enabled: Boolean,
    deadCellsInput: String,
    viabilityPercent: Double?,
    onToggle: (Boolean) -> Unit,
    onDeadCellsChange: (String) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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
                            .background(MaterialTheme.colorScheme.tertiaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.WaterDrop,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = stringResource(R.string.label_trypan_viability),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (viabilityPercent != null) {
                                "Viability: ${CellCalculator.formatDecimal(viabilityPercent, 1)}%"
                            } else {
                                "Track blue-stained non-viable cells"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Switch(
                    checked = enabled,
                    onCheckedChange = onToggle,
                    modifier = Modifier.testTag("viability_switch")
                )
            }

            AnimatedVisibility(
                visible = enabled,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    OutlinedTextField(
                        value = deadCellsInput,
                        onValueChange = onDeadCellsChange,
                        label = { Text(stringResource(R.string.label_dead_cells_counted)) },
                        placeholder = { Text("Enter blue/dead cells in same squares") },
                        suffix = { Text("cells", style = MaterialTheme.typography.labelMedium) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        textStyle = MaterialTheme.typography.titleMedium.copy(fontFamily = MonoFontFamily),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dead_cells_input")
                    )
                }
            }
        }
    }
}

@Composable
private fun FormulaVerificationCard(result: HemocytometerResult) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Functions,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = stringResource(R.string.label_formula_check),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = "Conc. = (Live ÷ Squares) × DF × 10⁴",
                fontFamily = MonoFontFamily,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (result.isValid) {
                val liveStr = CellCalculator.formatDecimal(result.totalLiveCounted, 0)
                val dfStr = CellCalculator.formatDecimal(result.dilutionFactor, 2)
                val volStr = CellCalculator.formatDecimal(result.suspensionVolumeMl, 2)
                Text(
                    text = "= ($liveStr ÷ ${result.squaresCounted}) × $dfStr × 10⁴ = ${CellCalculator.formatScientific(result.cellsPerMl)} cells/mL",
                    fontFamily = MonoFontFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Total = ${CellCalculator.formatScientific(result.cellsPerMl)} × $volStr mL = ${CellCalculator.formatScientific(result.totalCellsInVolume)} cells",
                    fontFamily = MonoFontFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
