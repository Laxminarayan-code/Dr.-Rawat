package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PlateFormat
import com.example.ui.theme.LocalCytoColors
import com.example.ui.theme.MonoFontFamily
import kotlin.math.min

@Composable
fun SectionHeader(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    trailing: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(16.dp)
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        if (trailing != null) {
            trailing()
        }
    }
}

/**
 * Interactive Improved Neubauer 4-corner grid with tap-to-select quadrants and tally counter.
 */
@Composable
fun NeubauerQuadrantGrid(
    quadrantInputs: List<String>,
    activeQuadrantIndex: Int,
    onSelectQuadrant: (Int) -> Unit,
    onUpdateQuadrantValue: (Int, String) -> Unit,
    onStepActiveQuadrant: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val gridLineColor = MaterialTheme.colorScheme.outlineVariant
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val labels = listOf("Q1 (Top-L)", "Q2 (Top-R)", "Q3 (Bot-L)", "Q4 (Bot-R)")

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Interactive 2x2 corner grid simulating the Neubauer chamber layout
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                for (row in 0..1) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        for (col in 0..1) {
                            val idx = row * 2 + col
                            val isSelected = idx == activeQuadrantIndex
                            val countText = quadrantInputs.getOrElse(idx) { "" }
                            val countVal = countText.toIntOrNull() ?: 0

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(94.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
                                        else surfaceVariant.copy(alpha = 0.45f)
                                    )
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) primaryColor else gridLineColor,
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                    .clickable { onSelectQuadrant(idx) }
                                    .testTag("quadrant_tile_${idx + 1}")
                            ) {
                                // 4x4 etched Neubauer sub-grid inside each corner square
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val strokeColor = if (isSelected) {
                                        primaryColor.copy(alpha = 0.22f)
                                    } else {
                                        gridLineColor.copy(alpha = 0.5f)
                                    }
                                    for (i in 1..3) {
                                        val x = size.width * i / 4f
                                        val y = size.height * i / 4f
                                        drawLine(strokeColor, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1f)
                                        drawLine(strokeColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
                                    }
                                }
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(10.dp),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = labels[idx],
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (isSelected) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.Bottom
                                    ) {
                                        Text(
                                            text = if (countText.isBlank()) "0" else countText,
                                            fontFamily = MonoFontFamily,
                                            fontSize = 26.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (countVal > 0) MaterialTheme.colorScheme.onSurface
                                            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                        )
                                        Text(
                                            text = "cells",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Active quadrant direct entry + live microscope tally stepper
        val activeValue = quadrantInputs.getOrElse(activeQuadrantIndex) { "" }
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = activeValue,
                    onValueChange = { onUpdateQuadrantValue(activeQuadrantIndex, it) },
                    label = { Text("${labels[activeQuadrantIndex]} count") },
                    placeholder = { Text("0") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    textStyle = MaterialTheme.typography.titleMedium.copy(fontFamily = MonoFontFamily),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("active_quadrant_input")
                )
                FilledTonalIconButton(
                    onClick = { onStepActiveQuadrant(-1) },
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("quadrant_decrement_button"),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Remove,
                        contentDescription = "Subtract one cell from ${labels[activeQuadrantIndex]}"
                    )
                }
                FilledTonalIconButton(
                    onClick = { onStepActiveQuadrant(1) },
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("quadrant_increment_button"),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = "Add one cell to ${labels[activeQuadrantIndex]}"
                    )
                }
            }
        }
    }
}

/**
 * Crisp custom Canvas visual of a standard multi-well culture plate (6, 12, 24, 48, or 96-well),
 * illuminating the exact number of wells being seeded.
 */
@Composable
fun MultiWellPlateVisual(
    plate: PlateFormat,
    seededWells: Int,
    modifier: Modifier = Modifier,
    isSelected: Boolean = true,
    accentColor: Color = MaterialTheme.colorScheme.primary
) {
    val plateBg = if (isSelected) {
        MaterialTheme.colorScheme.surface
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
    }
    val plateBorder = if (isSelected) accentColor else MaterialTheme.colorScheme.outlineVariant
    val emptyWellColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)
    val filledWellColor = accentColor.copy(alpha = if (isSelected) 0.85f else 0.45f)
    val wellHighlight = Color.White.copy(alpha = 0.35f)

    Canvas(
        modifier = modifier
            .aspectRatio(1.48f)
            .semantics {
                contentDescription = "${plate.displayName} showing $seededWells of ${plate.wellCount} wells seeded"
            }
    ) {
        val pad = size.minDimension * 0.06f
        val corner = size.minDimension * 0.12f

        // Plate body with chamfered A1 orientation notch
        val plateRect = Size(size.width - pad * 2, size.height - pad * 2)
        drawRoundRect(
            color = plateBg,
            topLeft = Offset(pad, pad),
            size = plateRect,
            cornerRadius = CornerRadius(corner, corner)
        )
        drawRoundRect(
            color = plateBorder,
            topLeft = Offset(pad, pad),
            size = plateRect,
            cornerRadius = CornerRadius(corner, corner),
            style = Stroke(width = if (isSelected) 3f else 2f)
        )

        // Small A1 orientation corner triangle in top-left
        val notchSize = size.minDimension * 0.10f
        val notchPath = Path().apply {
            moveTo(pad, pad + notchSize)
            lineTo(pad + notchSize, pad)
            lineTo(pad, pad)
            close()
        }
        drawPath(notchPath, color = plateBorder.copy(alpha = 0.45f))

        val innerPadX = pad + size.width * 0.07f
        val innerPadY = pad + size.height * 0.09f
        val availW = size.width - innerPadX * 2
        val availH = size.height - innerPadY * 2

        val cellW = availW / plate.columns
        val cellH = availH / plate.rows
        val radius = min(cellW, cellH) * 0.36f

        val clampedSeeded = seededWells.coerceIn(0, plate.wellCount)
        var wellIndex = 0
        for (r in 0 until plate.rows) {
            for (c in 0 until plate.columns) {
                val cx = innerPadX + cellW * (c + 0.5f)
                val cy = innerPadY + cellH * (r + 0.5f)
                val isFilled = wellIndex < clampedSeeded

                drawCircle(
                    color = if (isFilled) filledWellColor else emptyWellColor,
                    radius = radius,
                    center = Offset(cx, cy)
                )
                if (isFilled && radius > 3.5f) {
                    drawCircle(
                        color = wellHighlight,
                        radius = radius * 0.32f,
                        center = Offset(cx - radius * 0.25f, cy - radius * 0.25f)
                    )
                }
                wellIndex++
            }
        }
    }
}

/**
 * Illustrated North-West border inclusion rule diagram for the Reference Guide screen.
 */
@Composable
fun NeubauerBorderRuleDiagram(
    modifier: Modifier = Modifier
) {
    val cytoColors = LocalCytoColors.current
    val includeColor = cytoColors.statusReady
    val excludeColor = MaterialTheme.colorScheme.error
    val gridColor = MaterialTheme.colorScheme.outlineVariant

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .size(104.dp)
                    .semantics {
                        contentDescription = "Diagram showing top and left borders included in green and bottom and right borders excluded in red"
                    }
            ) {
                val pad = 16f
                val boxSize = size.minDimension - pad * 2

                // Interior 4x4 grid lines
                for (i in 1..3) {
                    val offset = pad + boxSize * i / 4f
                    drawLine(gridColor, Offset(offset, pad), Offset(offset, pad + boxSize), strokeWidth = 1.5f)
                    drawLine(gridColor, Offset(pad, offset), Offset(pad + boxSize, offset), strokeWidth = 1.5f)
                }

                // Top & Left INCLUDED borders (teal-green)
                drawLine(includeColor, Offset(pad, pad), Offset(pad + boxSize, pad), strokeWidth = 6f)
                drawLine(includeColor, Offset(pad, pad), Offset(pad, pad + boxSize), strokeWidth = 6f)

                // Bottom & Right EXCLUDED borders (coral-red)
                drawLine(excludeColor, Offset(pad, pad + boxSize), Offset(pad + boxSize, pad + boxSize), strokeWidth = 6f)
                drawLine(excludeColor, Offset(pad + boxSize, pad), Offset(pad + boxSize, pad + boxSize), strokeWidth = 6f)

                // Included sample cells (inside + top + left borders)
                val cellR = 7.5f
                listOf(
                    Offset(pad + boxSize * 0.35f, pad), // top border
                    Offset(pad, pad + boxSize * 0.55f), // left border
                    Offset(pad + boxSize * 0.45f, pad + boxSize * 0.45f), // interior
                    Offset(pad + boxSize * 0.72f, pad + boxSize * 0.30f)  // interior
                ).forEach { center ->
                    drawCircle(includeColor, radius = cellR, center = center)
                    drawCircle(Color.White.copy(alpha = 0.45f), radius = cellR * 0.35f, center = Offset(center.x - 2f, center.y - 2f))
                }

                // Excluded sample cells (bottom + right borders)
                listOf(
                    Offset(pad + boxSize * 0.65f, pad + boxSize), // bottom border
                    Offset(pad + boxSize, pad + boxSize * 0.42f)  // right border
                ).forEach { center ->
                    drawCircle(excludeColor.copy(alpha = 0.25f), radius = cellR, center = center)
                    drawCircle(excludeColor, radius = cellR, center = center, style = Stroke(width = 2.5f))
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "L-Rule (North & West)",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(includeColor)
                    )
                    Text(
                        text = "Count cells inside and touching TOP or LEFT lines.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .border(2.dp, excludeColor, CircleShape)
                    )
                    Text(
                        text = "Exclude cells touching BOTTOM or RIGHT lines.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun PresetChipButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
        contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.height(40.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 12.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }
    }
}
