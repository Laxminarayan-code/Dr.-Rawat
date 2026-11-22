package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Biotech
import androidx.compose.material.icons.rounded.CheckCircleOutline
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.TableChart
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.R
import com.example.domain.CellCalculator
import com.example.model.DensityPreset
import com.example.model.PlateFormat
import com.example.ui.components.MultiWellPlateVisual
import com.example.ui.components.NeubauerBorderRuleDiagram
import com.example.ui.components.SectionHeader
import com.example.ui.theme.MonoFontFamily

@Composable
fun GuideScreen(
    onApplySuggestion: (PlateFormat, DensityPreset) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Editorial Microscopy Hero Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(176.dp)
                    .clip(RoundedCornerShape(22.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_lab_banner_1795356406276),
                    contentDescription = stringResource(R.string.cd_lab_banner),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0x33072B2E),
                                    Color(0xDD062428)
                                )
                            )
                        )
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = stringResource(R.string.guide_hero_title),
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.guide_hero_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFD2F1ED)
                    )
                }
            }
        }

        // Section 1: Multi-Well Seeding Matrix with 1-tap apply
        item {
            SectionHeader(
                title = stringResource(R.string.section_plate_reference),
                icon = Icons.Rounded.TableChart
            )
        }

        items(PlateFormat.entries) { plate ->
            PlateReferenceCard(
                plate = plate,
                onApplySuggestion = onApplySuggestion
            )
        }

        // Section 2: Hemocytometer Counting Guidelines
        item {
            SectionHeader(
                title = stringResource(R.string.section_neubauer_rules),
                icon = Icons.Rounded.Biotech
            )
        }

        item {
            NeubauerBorderRuleDiagram()
        }

        item {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    GuideBulletRow(
                        title = "Why multiply by 10⁴?",
                        detail = "Each large Neubauer square measures 1 mm × 1 mm with 0.1 mm coverslip depth = 0.1 mm³ = 10⁻⁴ mL. Multiplying average cells/square by 10,000 converts to cells/mL."
                    )
                    GuideBulletRow(
                        title = "Optimal Counting Range",
                        detail = "Aim for 20–100 cells per large square (80–400 cells across 4 corners). If >150 cells/square, dilute stock further (e.g., DF 5× or 10×) to reduce overlap error."
                    )
                    GuideBulletRow(
                        title = "Trypan Blue Dilution Factor",
                        detail = "Mixing 10 µL cell suspension + 10 µL 0.4% Trypan Blue yields a 1:2 dilution (DF = 2). Count unstained bright cells as viable and blue cells as non-viable."
                    )
                }
            }
        }

        // Section 3: About & Developer Credit
        item {
            SectionHeader(
                title = stringResource(R.string.section_about),
                icon = Icons.Rounded.Info
            )
        }

        item {
            AboutDeveloperCard()
        }

        item { Spacer(modifier = Modifier.height(12.dp)) }
    }
}

@Composable
private fun AboutDeveloperCard() {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("about_developer_card")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(R.string.about_tagline),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = stringResource(R.string.about_developer_label),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = stringResource(R.string.about_developer_name),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.End
                    )
                    Text(
                        text = stringResource(R.string.about_developer_affiliation),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.End
                    )
                }
            }
            AboutMetaRow(
                label = stringResource(R.string.about_version_label),
                value = BuildConfig.VERSION_NAME
            )
            AboutMetaRow(
                label = stringResource(R.string.about_package_label),
                value = BuildConfig.APPLICATION_ID
            )
        }
    }
}

@Composable
private fun AboutMetaRow(
    label: String,
    value: String,
    highlight: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontFamily = if (highlight) MaterialTheme.typography.titleSmall.fontFamily else MonoFontFamily,
            fontSize = if (highlight) 14.sp else 12.sp,
            fontWeight = if (highlight) FontWeight.Bold else FontWeight.Medium,
            color = if (highlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            textAlign = androidx.compose.ui.text.style.TextAlign.End,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun PlateReferenceCard(
    plate: PlateFormat,
    onApplySuggestion: (PlateFormat, DensityPreset) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MultiWellPlateVisual(
                    plate = plate,
                    seededWells = plate.wellCount,
                    modifier = Modifier.width(86.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = plate.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Area: ${plate.surfaceAreaCm2} cm²/well  •  Vol: ${CellCalculator.formatDecimal(plate.defaultWorkingVolumeMl)} mL",
                        fontFamily = MonoFontFamily,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Confluent yield ≈ ${CellCalculator.formatScientific(plate.confluentYieldPerWell, 1)} cells/well",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Quick-apply Low / Moderate / High density buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    DensityPreset.LOW to plate.lowDensityCellsPerWell,
                    DensityPreset.MODERATE to plate.moderateDensityCellsPerWell,
                    DensityPreset.HIGH to plate.highDensityCellsPerWell
                ).forEach { (preset, count) ->
                    FilledTonalButton(
                        onClick = { onApplySuggestion(plate, preset) },
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("guide_apply_${plate.wellCount}_${preset.name.lowercase()}")
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = preset.shortLabel,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = CellCalculator.formatScientific(count, 1),
                                fontFamily = MonoFontFamily,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GuideBulletRow(
    title: String,
    detail: String
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = Icons.Rounded.CheckCircleOutline,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(top = 2.dp)
                .size(18.dp)
        )
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = detail,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
