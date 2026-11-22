package com.example

import com.example.domain.CellCalculator
import com.example.model.PlateFormat
import com.example.model.SeedingStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CellCalculatorTest {

    @Test
    fun hemocytometer_standardFourSquaresWithTrypanBlue() {
        // 240 live cells across 4 large squares, DF = 2 (1:1 Trypan Blue), 5.0 mL volume
        // Avg = 60 cells/sq -> Conc = 60 * 2 * 10,000 = 1.2 × 10^6 cells/mL
        // Total cells = 1.2 × 10^6 * 5 = 6.0 × 10^6 cells
        val result = CellCalculator.calculateHemocytometer(
            liveCellsCounted = 240.0,
            deadCellsCounted = 0.0,
            squaresCounted = 4,
            dilutionFactor = 2.0,
            suspensionVolumeMl = 5.0
        )

        assertTrue(result.isValid)
        assertEquals(60.0, result.averagePerSquare, 0.001)
        assertEquals(1_200_000.0, result.cellsPerMl, 0.1)
        assertEquals(1_200.0, result.cellsPerUl, 0.01)
        assertEquals(6_000_000.0, result.totalCellsInVolume, 0.1)
        assertNull(result.viabilityPercent)
    }

    @Test
    fun hemocytometer_viabilityPercentCalculation() {
        // 180 live + 20 dead = 90.0% viability
        val result = CellCalculator.calculateHemocytometer(
            liveCellsCounted = 180.0,
            deadCellsCounted = 20.0,
            squaresCounted = 4,
            dilutionFactor = 2.0,
            suspensionVolumeMl = 3.0
        )

        assertEquals(90.0, result.viabilityPercent!!, 0.01)
    }

    @Test
    fun seedingPlan_24WellModerateWithTenPercentOverage() {
        // Stock: 1.2 × 10^6 cells/mL in 5.0 mL (= 6.0 × 10^6 total cells)
        // Plate: 24-well, Moderate = 50,000 cells/well, 0.5 mL/well, 24 wells + 10% overage (= 26.4 eff wells)
        // Total cells needed = 26.4 * 50,000 = 1,320,000 cells
        // Total master mix = 26.4 * 0.5 = 13.2 mL
        // Required cell suspension = 1,320,000 / 1,200,000 = 1.1 mL
        // Fresh media = 13.2 - 1.1 = 12.1 mL
        val plan = CellCalculator.calculateSeedingPlan(
            plate = PlateFormat.WELL_24,
            wellsRequested = 24,
            overagePercent = 10,
            workingVolumePerWellMl = 0.5,
            targetCellsPerWell = PlateFormat.WELL_24.moderateDensityCellsPerWell,
            stockCellsPerMl = 1_200_000.0,
            availableVolumeMl = 5.0
        )

        assertTrue(plan.status is SeedingStatus.Ready)
        assertEquals(1_320_000.0, plan.totalCellsNeeded, 1.0)
        assertEquals(13.2, plan.totalMasterMixMl, 0.001)
        assertEquals(1.1, plan.requiredCellSuspensionMl, 0.001)
        assertEquals(12.1, plan.requiredFreshMediaMl, 0.001)
    }

    @Test
    fun seedingPlan_96WellLowDensitySmartMicroLiter() {
        // Stock: 1.0 × 10^6 cells/mL in 4.0 mL
        // 96-well Low = 2,500 cells/well, 96 wells, 0% overage, 0.1 mL/well
        // Total cells = 240,000; cell suspension = 0.24 mL (240 µL); media = 9.36 mL
        val plan = CellCalculator.calculateSeedingPlan(
            plate = PlateFormat.WELL_96,
            wellsRequested = 96,
            overagePercent = 0,
            workingVolumePerWellMl = 0.1,
            targetCellsPerWell = PlateFormat.WELL_96.lowDensityCellsPerWell,
            stockCellsPerMl = 1_000_000.0,
            availableVolumeMl = 4.0
        )

        assertTrue(plan.status is SeedingStatus.Ready)
        assertEquals(0.24, plan.requiredCellSuspensionMl, 0.0001)
        assertEquals("240 µL", CellCalculator.formatVolumePrimary(plan.requiredCellSuspensionMl))
    }

    @Test
    fun seedingPlan_diluteStockTriggersCentrifugeRequired() {
        // Stock: 200,000 cells/mL in 20 mL (= 4.0 × 10^6 total cells - plenty of cells, but too dilute!)
        // 6-well High = 600,000 cells/well, 6 wells, 0% overage, 2.0 mL/well -> total mix 12 mL
        // Total cells needed = 3.6 × 10^6 -> needs 18 mL of stock > 12 mL master mix!
        val plan = CellCalculator.calculateSeedingPlan(
            plate = PlateFormat.WELL_6,
            wellsRequested = 6,
            overagePercent = 0,
            workingVolumePerWellMl = 2.0,
            targetCellsPerWell = PlateFormat.WELL_6.highDensityCellsPerWell,
            stockCellsPerMl = 200_000.0,
            availableVolumeMl = 20.0
        )

        assertTrue(plan.status is SeedingStatus.CentrifugeRequired)
        val status = plan.status as SeedingStatus.CentrifugeRequired
        assertEquals(18.0, status.spinDownVolumeMl, 0.01)
        assertEquals(12.0, status.resuspendMediaMl, 0.01)
    }

    @Test
    fun seedingPlan_insufficientCellsReportsMaxSeedableWells() {
        // Stock: 500,000 cells/mL in 2.0 mL (= 1,000,000 total cells)
        // 6-well Moderate = 300,000 cells/well, 6 wells, 0% overage -> needs 1,800,000 cells
        // Max seedable wells = floor(1,000,000 / 300,000) = 3 wells
        val plan = CellCalculator.calculateSeedingPlan(
            plate = PlateFormat.WELL_6,
            wellsRequested = 6,
            overagePercent = 0,
            workingVolumePerWellMl = 2.0,
            targetCellsPerWell = PlateFormat.WELL_6.moderateDensityCellsPerWell,
            stockCellsPerMl = 500_000.0,
            availableVolumeMl = 2.0
        )

        assertTrue(plan.status is SeedingStatus.InsufficientCells)
        val status = plan.status as SeedingStatus.InsufficientCells
        assertEquals(3, status.maxSeedableWells)
        assertEquals(800_000.0, status.shortfallCells, 1.0)
    }

    @Test
    fun scientificFormattingUsesSuperscripts() {
        assertEquals("1.25 × 10⁶", CellCalculator.formatScientific(1_250_000.0))
        assertEquals("2.5 × 10³", CellCalculator.formatScientific(2_500.0))
    }
}
