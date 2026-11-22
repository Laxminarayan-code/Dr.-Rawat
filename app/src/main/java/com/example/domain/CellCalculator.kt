package com.example.domain

import com.example.model.DensityPreset
import com.example.model.HemocytometerResult
import com.example.model.PlateFormat
import com.example.model.SeedingPlanResult
import com.example.model.SeedingStatus
import java.util.Locale
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * Pure scientific calculation engine for Improved Neubauer hemocytometer counts
 * and multi-well plate cell seeding dilutions.
 */
object CellCalculator {

    /**
     * Standard Improved Neubauer large square volume is 1 mm × 1 mm × 0.1 mm = 0.1 mm³ = 10⁻⁴ mL.
     * Multiplying average cells per large square by 10,000 yields cells/mL.
     */
    const val HEMOCYTOMETER_FACTOR = 10_000.0

    fun calculateHemocytometer(
        liveCellsCounted: Double,
        deadCellsCounted: Double,
        squaresCounted: Int,
        dilutionFactor: Double,
        suspensionVolumeMl: Double,
        chamberVolumeUl: Double = squaresCounted.coerceAtLeast(1) * 0.1,
        isChamberVolumeMode: Boolean = false
    ): HemocytometerResult {
        val safeSquares = squaresCounted.coerceAtLeast(1)
        val safeLive = liveCellsCounted.coerceAtLeast(0.0)
        val safeDead = deadCellsCounted.coerceAtLeast(0.0)
        val safeDf = dilutionFactor.coerceAtLeast(0.0)
        val safeVol = suspensionVolumeMl.coerceAtLeast(0.0)
        val safeChamberVolUl = chamberVolumeUl.coerceAtLeast(0.0)

        val valid = safeLive > 0.0 && safeDf > 0.0 && (!isChamberVolumeMode || safeChamberVolUl > 0.0)
        val avgPerSquare = safeLive / safeSquares.toDouble()
        val cellsPerMl = if (valid) {
            if (isChamberVolumeMode) {
                (safeLive / safeChamberVolUl) * safeDf * 1_000.0
            } else {
                avgPerSquare * safeDf * HEMOCYTOMETER_FACTOR
            }
        } else 0.0
        val cellsPerUl = cellsPerMl / 1_000.0
        val totalCellsInVolume = cellsPerMl * safeVol

        val totalObserved = safeLive + safeDead
        val viability = if (safeDead > 0.0 && totalObserved > 0.0) {
            (safeLive / totalObserved) * 100.0
        } else {
            null
        }

        return HemocytometerResult(
            totalLiveCounted = safeLive,
            totalDeadCounted = safeDead,
            squaresCounted = safeSquares,
            chamberVolumeUl = if (isChamberVolumeMode) safeChamberVolUl else safeSquares * 0.1,
            isChamberVolumeMode = isChamberVolumeMode,
            dilutionFactor = safeDf,
            suspensionVolumeMl = safeVol,
            averagePerSquare = avgPerSquare,
            cellsPerMl = cellsPerMl,
            cellsPerUl = cellsPerUl,
            totalCellsInVolume = totalCellsInVolume,
            viabilityPercent = viability,
            isValid = valid
        )
    }

    fun calculateSeedingPlan(
        plate: PlateFormat,
        wellsRequested: Int,
        overagePercent: Int,
        workingVolumePerWellMl: Double,
        targetCellsPerWell: Double,
        stockCellsPerMl: Double,
        availableVolumeMl: Double?
    ): SeedingPlanResult {
        val safeWells = wellsRequested.coerceAtLeast(1)
        val safeOverage = overagePercent.coerceIn(0, 100)
        val safeVolPerWell = workingVolumePerWellMl.coerceAtLeast(0.01)
        val safeTargetPerWell = targetCellsPerWell.coerceAtLeast(0.0)
        val targetPerCm2 = if (plate.surfaceAreaCm2 > 0.0) {
            safeTargetPerWell / plate.surfaceAreaCm2
        } else 0.0

        val overageMultiplier = 1.0 + (safeOverage / 100.0)
        val effectiveWells = safeWells * overageMultiplier
        val totalCellsNeeded = effectiveWells * safeTargetPerWell
        val totalMasterMixMl = effectiveWells * safeVolPerWell
        val finalMixConc = if (safeVolPerWell > 0.0) safeTargetPerWell / safeVolPerWell else 0.0

        val totalAvailableCells = if (availableVolumeMl != null && availableVolumeMl > 0.0 && stockCellsPerMl > 0.0) {
            stockCellsPerMl * availableVolumeMl
        } else {
            null
        }

        if (stockCellsPerMl <= 0.0 || safeTargetPerWell <= 0.0) {
            return SeedingPlanResult(
                plate = plate,
                wellsRequested = safeWells,
                overagePercent = safeOverage,
                effectiveWells = effectiveWells,
                workingVolumePerWellMl = safeVolPerWell,
                targetCellsPerWell = safeTargetPerWell,
                targetCellsPerCm2 = targetPerCm2,
                stockCellsPerMl = stockCellsPerMl,
                availableVolumeMl = availableVolumeMl,
                totalAvailableCells = totalAvailableCells,
                totalCellsNeeded = totalCellsNeeded,
                totalMasterMixMl = totalMasterMixMl,
                requiredCellSuspensionMl = 0.0,
                requiredFreshMediaMl = totalMasterMixMl,
                finalMixConcentrationCellsPerMl = finalMixConc,
                stockUtilizationFraction = null,
                status = SeedingStatus.AwaitingStock
            )
        }

        val requiredCellSuspensionMl = totalCellsNeeded / stockCellsPerMl
        val requiredFreshMediaMl = max(0.0, totalMasterMixMl - requiredCellSuspensionMl)

        val utilization = if (totalAvailableCells != null && totalAvailableCells > 0.0) {
            (totalCellsNeeded / totalAvailableCells).toFloat()
        } else {
            null
        }

        val status = when {
            totalAvailableCells != null && totalCellsNeeded > totalAvailableCells * 1.0001 -> {
                val maxWellsWithOverage = floor(
                    totalAvailableCells / (safeTargetPerWell * overageMultiplier)
                ).toInt().coerceAtLeast(0)
                SeedingStatus.InsufficientCells(
                    maxSeedableWells = maxWellsWithOverage,
                    shortfallCells = (totalCellsNeeded - totalAvailableCells).coerceAtLeast(0.0)
                )
            }
            requiredCellSuspensionMl > totalMasterMixMl * 1.0001 -> {
                SeedingStatus.CentrifugeRequired(
                    spinDownVolumeMl = requiredCellSuspensionMl,
                    resuspendMediaMl = totalMasterMixMl
                )
            }
            else -> SeedingStatus.Ready
        }

        return SeedingPlanResult(
            plate = plate,
            wellsRequested = safeWells,
            overagePercent = safeOverage,
            effectiveWells = effectiveWells,
            workingVolumePerWellMl = safeVolPerWell,
            targetCellsPerWell = safeTargetPerWell,
            targetCellsPerCm2 = targetPerCm2,
            stockCellsPerMl = stockCellsPerMl,
            availableVolumeMl = availableVolumeMl,
            totalAvailableCells = totalAvailableCells,
            totalCellsNeeded = totalCellsNeeded,
            totalMasterMixMl = totalMasterMixMl,
            requiredCellSuspensionMl = requiredCellSuspensionMl,
            requiredFreshMediaMl = requiredFreshMediaMl,
            finalMixConcentrationCellsPerMl = finalMixConc,
            stockUtilizationFraction = utilization,
            status = status
        )
    }

    /**
     * Formats large cell numbers in clean scientific notation using Unicode superscripts,
     * e.g., 1,250,000 -> "1.25 × 10⁶".
     */
    fun formatScientific(value: Double, decimals: Int = 2): String {
        if (value <= 0.0 || value.isNaN() || value.isInfinite()) return "0"
        if (value < 1_000.0) {
            return if (abs(value - value.roundToInt()) < 0.01) {
                String.format(Locale.US, "%,d", value.roundToInt())
            } else {
                String.format(Locale.US, "%.1f", value)
            }
        }
        val exponent = floor(log10(value)).toInt()
        val mantissa = value / 10.0.pow(exponent)
        val formattedMantissa = String.format(Locale.US, "%.${decimals}f", mantissa)
            .trimEnd('0')
            .trimEnd('.')
        return "$formattedMantissa × 10${toSuperscript(exponent)}"
    }

    /**
     * Formats a volume in mL smartly: displays µL when < 1.0 mL and mL when >= 1.0 mL.
     */
    fun formatVolumePrimary(volumeMl: Double): String {
        if (volumeMl <= 0.0 || volumeMl.isNaN()) return "0 µL"
        return if (volumeMl < 1.0) {
            val ul = volumeMl * 1_000.0
            if (ul < 10.0) {
                String.format(Locale.US, "%.1f µL", ul)
            } else {
                String.format(Locale.US, "%.0f µL", ul)
            }
        } else if (volumeMl < 10.0) {
            String.format(Locale.US, "%.2f mL", volumeMl)
        } else {
            String.format(Locale.US, "%.1f mL", volumeMl)
        }
    }

    /**
     * Secondary unit conversion helper for quick micropipette cross-checking.
     */
    fun formatVolumeSecondary(volumeMl: Double): String {
        if (volumeMl <= 0.0 || volumeMl.isNaN()) return "0.00 mL"
        return if (volumeMl < 1.0) {
            String.format(Locale.US, "%.3f mL", volumeMl)
        } else {
            String.format(Locale.US, "%,.0f µL", volumeMl * 1_000.0)
        }
    }

    fun formatDecimal(value: Double, decimals: Int = 2): String {
        if (value.isNaN() || value.isInfinite()) return "0"
        return String.format(Locale.US, "%.${decimals}f", value)
            .trimEnd('0')
            .trimEnd('.')
    }

    private fun toSuperscript(number: Int): String {
        val map = mapOf(
            '-' to '⁻',
            '0' to '⁰',
            '1' to '¹',
            '2' to '²',
            '3' to '³',
            '4' to '⁴',
            '5' to '⁵',
            '6' to '⁶',
            '7' to '⁷',
            '8' to '⁸',
            '9' to '⁹'
        )
        return number.toString().map { map[it] ?: it }.joinToString("")
    }

    fun buildProtocolSummary(
        plan: SeedingPlanResult,
        preset: DensityPreset
    ): String = buildString {
        appendLine("CytoCount — Cell Seeding Protocol")
        appendLine("--------------------------------")
        appendLine("Plate Format: ${plan.plate.displayName} (${plan.plate.surfaceAreaCm2} cm²/well)")
        appendLine("Density Preset: ${preset.label} (${formatScientific(plan.targetCellsPerWell)} cells/well | ${formatScientific(plan.targetCellsPerCm2)} cells/cm²)")
        appendLine("Wells to Seed: ${plan.wellsRequested} wells (+${plan.overagePercent}% pipetting margin = ${formatDecimal(plan.effectiveWells, 1)} eff. wells)")
        appendLine("Stock Concentration: ${formatScientific(plan.stockCellsPerMl)} cells/mL")
        appendLine("Total Cells Needed: ${formatScientific(plan.totalCellsNeeded)} cells")
        appendLine()
        when (val s = plan.status) {
            is SeedingStatus.Ready -> {
                appendLine("MASTER MIX RECIPE:")
                appendLine("1. Cell Suspension: ${formatVolumePrimary(plan.requiredCellSuspensionMl)} (${formatVolumeSecondary(plan.requiredCellSuspensionMl)})")
                appendLine("2. Fresh Media:     ${formatVolumePrimary(plan.requiredFreshMediaMl)} (${formatVolumeSecondary(plan.requiredFreshMediaMl)})")
                appendLine("3. Total Mix:       ${formatVolumePrimary(plan.totalMasterMixMl)}")
                appendLine("4. Dispense ${formatVolumePrimary(plan.workingVolumePerWellMl)} per well across ${plan.wellsRequested} wells.")
            }
            is SeedingStatus.CentrifugeRequired -> {
                appendLine("CENTRIFUGE & RESUSPEND PROTOCOL:")
                appendLine("Stock is too dilute for direct dilution.")
                appendLine("1. Transfer ${formatVolumePrimary(s.spinDownVolumeMl)} of cell suspension to a tube and centrifuge.")
                appendLine("2. Aspirate supernatant carefully.")
                appendLine("3. Resuspend pellet in ${formatVolumePrimary(s.resuspendMediaMl)} of fresh media.")
                appendLine("4. Dispense ${formatVolumePrimary(plan.workingVolumePerWellMl)} per well across ${plan.wellsRequested} wells.")
            }
            is SeedingStatus.InsufficientCells -> {
                appendLine("WARNING: INSUFFICIENT CELLS")
                appendLine("Available cells are short by ${formatScientific(s.shortfallCells)} cells.")
                appendLine("Maximum seedable wells at this density: ${s.maxSeedableWells} wells.")
            }
            SeedingStatus.AwaitingStock -> {
                appendLine("Enter stock cell concentration to generate pipetting volumes.")
            }
        }
    }
}
