package com.example.model

enum class AppDestination {
    COUNT,
    SEED,
    GUIDE
}

enum class CountInputMode {
    QUICK_TOTAL,
    FOUR_QUADRANT,
    CHAMBER_VOLUME
}

enum class DensityPreset(
    val label: String,
    val shortLabel: String,
    val confluencyHint: String,
    val typicalUseCase: String
) {
    LOW(
        label = "Low Density",
        shortLabel = "Low",
        confluencyHint = "~20–30% at 24h",
        typicalUseCase = "72h proliferation, colony formation, live imaging, IC50 growth curves"
    ),
    MODERATE(
        label = "Moderate Density",
        shortLabel = "Moderate",
        confluencyHint = "~50–70% at 24h",
        typicalUseCase = "24–48h drug assays, transfection, Western blot, immunofluorescence"
    ),
    HIGH(
        label = "High Density",
        shortLabel = "High",
        confluencyHint = "~80–90% at 24h",
        typicalUseCase = "Overnight confluent monolayer, wound-healing, acute short-term assay"
    ),
    CUSTOM(
        label = "Custom Density",
        shortLabel = "Custom",
        confluencyHint = "User-defined target",
        typicalUseCase = "Enter your exact target cells/well or cells/cm²"
    )
}

enum class DensityInputUnit(val label: String) {
    CELLS_PER_WELL("cells/well"),
    CELLS_PER_CM2("cells/cm²")
}

/**
 * Standard multi-well tissue culture plate specifications aligned with
 * Corning/Thermo Fisher Falcon guidelines for adherent mammalian cell lines.
 */
enum class PlateFormat(
    val displayName: String,
    val shortName: String,
    val wellCount: Int,
    val rows: Int,
    val columns: Int,
    val surfaceAreaCm2: Double,
    val defaultWorkingVolumeMl: Double,
    val minRecommendedVolMl: Double,
    val maxRecommendedVolMl: Double,
    val lowDensityCellsPerWell: Double,
    val moderateDensityCellsPerWell: Double,
    val highDensityCellsPerWell: Double,
    val confluentYieldPerWell: Double
) {
    WELL_6(
        displayName = "6-Well Plate",
        shortName = "6-Well",
        wellCount = 6,
        rows = 2,
        columns = 3,
        surfaceAreaCm2 = 9.5,
        defaultWorkingVolumeMl = 2.0,
        minRecommendedVolMl = 1.5,
        maxRecommendedVolMl = 3.0,
        lowDensityCellsPerWell = 100_000.0,       // 1.0 × 10^5
        moderateDensityCellsPerWell = 300_000.0,  // 3.0 × 10^5
        highDensityCellsPerWell = 600_000.0,      // 6.0 × 10^5
        confluentYieldPerWell = 1_200_000.0       // ~1.2 × 10^6
    ),
    WELL_12(
        displayName = "12-Well Plate",
        shortName = "12-Well",
        wellCount = 12,
        rows = 3,
        columns = 4,
        surfaceAreaCm2 = 3.8,
        defaultWorkingVolumeMl = 1.0,
        minRecommendedVolMl = 0.8,
        maxRecommendedVolMl = 1.5,
        lowDensityCellsPerWell = 40_000.0,        // 4.0 × 10^4
        moderateDensityCellsPerWell = 100_000.0,  // 1.0 × 10^5
        highDensityCellsPerWell = 250_000.0,      // 2.5 × 10^5
        confluentYieldPerWell = 500_000.0         // ~5.0 × 10^5
    ),
    WELL_24(
        displayName = "24-Well Plate",
        shortName = "24-Well",
        wellCount = 24,
        rows = 4,
        columns = 6,
        surfaceAreaCm2 = 1.9,
        defaultWorkingVolumeMl = 0.5,
        minRecommendedVolMl = 0.4,
        maxRecommendedVolMl = 1.0,
        lowDensityCellsPerWell = 20_000.0,        // 2.0 × 10^4
        moderateDensityCellsPerWell = 50_000.0,   // 5.0 × 10^4
        highDensityCellsPerWell = 120_000.0,      // 1.2 × 10^5
        confluentYieldPerWell = 240_000.0         // ~2.4 × 10^5
    ),
    WELL_48(
        displayName = "48-Well Plate",
        shortName = "48-Well",
        wellCount = 48,
        rows = 6,
        columns = 8,
        surfaceAreaCm2 = 0.95,
        defaultWorkingVolumeMl = 0.3,
        minRecommendedVolMl = 0.2,
        maxRecommendedVolMl = 0.5,
        lowDensityCellsPerWell = 10_000.0,        // 1.0 × 10^4
        moderateDensityCellsPerWell = 30_000.0,   // 3.0 × 10^4
        highDensityCellsPerWell = 60_000.0,       // 6.0 × 10^4
        confluentYieldPerWell = 120_000.0         // ~1.2 × 10^5
    ),
    WELL_96(
        displayName = "96-Well Plate",
        shortName = "96-Well",
        wellCount = 96,
        rows = 8,
        columns = 12,
        surfaceAreaCm2 = 0.32,
        defaultWorkingVolumeMl = 0.1,
        minRecommendedVolMl = 0.05,
        maxRecommendedVolMl = 0.2,
        lowDensityCellsPerWell = 2_500.0,         // 2.5 × 10^3
        moderateDensityCellsPerWell = 10_000.0,   // 1.0 × 10^4
        highDensityCellsPerWell = 25_000.0,       // 2.5 × 10^4
        confluentYieldPerWell = 40_000.0          // ~4.0 × 10^4
    );

    fun suggestedCellsPerWell(preset: DensityPreset): Double = when (preset) {
        DensityPreset.LOW -> lowDensityCellsPerWell
        DensityPreset.MODERATE -> moderateDensityCellsPerWell
        DensityPreset.HIGH -> highDensityCellsPerWell
        DensityPreset.CUSTOM -> moderateDensityCellsPerWell
    }
}

data class ChamberConcentrationResult(
    val cellsCounted: Double,
    val chamberVolumeUl: Double,
    val dilutionFactor: Double,
    val cellsPerUl: Double,
    val cellsPerMl: Double,
    val isValid: Boolean
)

data class HemocytometerResult(
    val totalLiveCounted: Double,
    val totalDeadCounted: Double,
    val squaresCounted: Int,
    val chamberVolumeUl: Double,
    val isChamberVolumeMode: Boolean,
    val dilutionFactor: Double,
    val suspensionVolumeMl: Double,
    val averagePerSquare: Double,
    val cellsPerMl: Double,
    val cellsPerUl: Double,
    val totalCellsInVolume: Double,
    val viabilityPercent: Double?,
    val isValid: Boolean
)

sealed interface SeedingStatus {
    data object AwaitingStock : SeedingStatus
    data object Ready : SeedingStatus
    data class CentrifugeRequired(
        val spinDownVolumeMl: Double,
        val resuspendMediaMl: Double
    ) : SeedingStatus
    data class InsufficientCells(
        val maxSeedableWells: Int,
        val shortfallCells: Double
    ) : SeedingStatus
}

data class SeedingPlanResult(
    val plate: PlateFormat,
    val wellsRequested: Int,
    val overagePercent: Int,
    val effectiveWells: Double,
    val workingVolumePerWellMl: Double,
    val targetCellsPerWell: Double,
    val targetCellsPerCm2: Double,
    val stockCellsPerMl: Double,
    val availableVolumeMl: Double?,
    val totalAvailableCells: Double?,
    val totalCellsNeeded: Double,
    val totalMasterMixMl: Double,
    val requiredCellSuspensionMl: Double,
    val requiredFreshMediaMl: Double,
    val finalMixConcentrationCellsPerMl: Double,
    val stockUtilizationFraction: Float?,
    val status: SeedingStatus
)
