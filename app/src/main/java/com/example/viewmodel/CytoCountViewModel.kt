package com.example.viewmodel

import androidx.lifecycle.ViewModel
import com.example.domain.CellCalculator
import com.example.model.AppDestination
import com.example.model.CountInputMode
import com.example.model.DensityInputUnit
import com.example.model.DensityPreset
import com.example.model.HemocytometerResult
import com.example.model.PlateFormat
import com.example.model.SeedingPlanResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class CytoCountUiState(
    val currentDestination: AppDestination = AppDestination.COUNT,
    val darkroomModeOverride: Boolean? = null,

    // Hemocytometer inputs
    val inputMode: CountInputMode = CountInputMode.QUICK_TOTAL,
    val quickTotalCountInput: String = "",
    val quadrantInputs: List<String> = listOf("", "", "", ""),
    val activeQuadrantIndex: Int = 0,
    val squaresCountedInput: String = "4",
    val dilutionFactorInput: String = "2",
    val suspensionVolumeMlInput: String = "5",
    val showViabilityInputs: Boolean = false,
    val deadCellsCountInput: String = "",

    // Seeding planner inputs
    val useManualStockOverride: Boolean = false,
    val manualStockConcentrationInput: String = "",
    val manualStockExponent: Int = 6, // e.g., 1.25 × 10^6 cells/mL
    val manualAvailableVolumeInput: String = "",
    val selectedPlate: PlateFormat = PlateFormat.WELL_24,
    val selectedDensityPreset: DensityPreset = DensityPreset.MODERATE,
    val customDensityInput: String = "50000",
    val customDensityUnit: DensityInputUnit = DensityInputUnit.CELLS_PER_WELL,
    val wellsToSeedInput: String = "24",
    val workingVolumePerWellInput: String = "0.5",
    val overagePercent: Int = 10,

    // Computed outputs
    val countResult: HemocytometerResult = CellCalculator.calculateHemocytometer(
        liveCellsCounted = 0.0,
        deadCellsCounted = 0.0,
        squaresCounted = 4,
        dilutionFactor = 2.0,
        suspensionVolumeMl = 5.0
    ),
    val seedingPlan: SeedingPlanResult = CellCalculator.calculateSeedingPlan(
        plate = PlateFormat.WELL_24,
        wellsRequested = 24,
        overagePercent = 10,
        workingVolumePerWellMl = 0.5,
        targetCellsPerWell = PlateFormat.WELL_24.moderateDensityCellsPerWell,
        stockCellsPerMl = 0.0,
        availableVolumeMl = 5.0
    )
)

class CytoCountViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(CytoCountUiState())
    val uiState: StateFlow<CytoCountUiState> = _uiState.asStateFlow()

    fun navigateTo(destination: AppDestination) {
        _uiState.update { it.copy(currentDestination = destination) }
    }

    fun toggleDarkroomMode(currentIsDark: Boolean) {
        _uiState.update { it.copy(darkroomModeOverride = !currentIsDark) }
    }

    fun setCountInputMode(mode: CountInputMode) {
        updateAndRecalculate { state ->
            val updatedSquares = if (mode == CountInputMode.FOUR_QUADRANT) "4" else state.squaresCountedInput
            state.copy(
                inputMode = mode,
                squaresCountedInput = updatedSquares
            )
        }
    }

    fun updateQuickTotalCount(value: String) {
        val cleaned = sanitizeNumericInput(value, allowDecimal = false)
        updateAndRecalculate { it.copy(quickTotalCountInput = cleaned) }
    }

    fun selectQuadrant(index: Int) {
        _uiState.update { it.copy(activeQuadrantIndex = index.coerceIn(0, 3)) }
    }

    fun updateQuadrantValue(index: Int, value: String) {
        val cleaned = sanitizeNumericInput(value, allowDecimal = false)
        updateAndRecalculate { state ->
            val updated = state.quadrantInputs.toMutableList()
            if (index in updated.indices) {
                updated[index] = cleaned
            }
            state.copy(quadrantInputs = updated)
        }
    }

    fun stepActiveQuadrant(delta: Int) {
        updateAndRecalculate { state ->
            val idx = state.activeQuadrantIndex.coerceIn(0, 3)
            val currentVal = state.quadrantInputs[idx].toIntOrNull() ?: 0
            val nextVal = (currentVal + delta).coerceAtLeast(0)
            val updated = state.quadrantInputs.toMutableList()
            updated[idx] = if (nextVal == 0) "" else nextVal.toString()
            state.copy(quadrantInputs = updated)
        }
    }

    fun updateSquaresCounted(value: String) {
        val cleaned = sanitizeNumericInput(value, allowDecimal = false)
        updateAndRecalculate { it.copy(squaresCountedInput = cleaned) }
    }

    fun updateDilutionFactor(value: String) {
        val cleaned = sanitizeNumericInput(value, allowDecimal = true)
        updateAndRecalculate { it.copy(dilutionFactorInput = cleaned) }
    }

    fun updateSuspensionVolume(value: String) {
        val cleaned = sanitizeNumericInput(value, allowDecimal = true)
        updateAndRecalculate { it.copy(suspensionVolumeMlInput = cleaned) }
    }

    fun toggleViabilityInputs(show: Boolean) {
        updateAndRecalculate { state ->
            state.copy(
                showViabilityInputs = show,
                deadCellsCountInput = if (show) state.deadCellsCountInput else ""
            )
        }
    }

    fun updateDeadCellsCount(value: String) {
        val cleaned = sanitizeNumericInput(value, allowDecimal = false)
        updateAndRecalculate { it.copy(deadCellsCountInput = cleaned) }
    }

    fun resetCountInputs() {
        updateAndRecalculate { state ->
            state.copy(
                quickTotalCountInput = "",
                quadrantInputs = listOf("", "", "", ""),
                activeQuadrantIndex = 0,
                deadCellsCountInput = "",
                useManualStockOverride = false
            )
        }
    }

    // Seeding Planner Controls

    fun setManualStockOverride(enabled: Boolean) {
        updateAndRecalculate { state ->
            if (enabled && state.manualStockConcentrationInput.isBlank() && state.countResult.isValid) {
                val mantissa = state.countResult.cellsPerMl / 1_000_000.0
                state.copy(
                    useManualStockOverride = true,
                    manualStockConcentrationInput = CellCalculator.formatDecimal(mantissa, 2),
                    manualStockExponent = 6,
                    manualAvailableVolumeInput = state.suspensionVolumeMlInput
                )
            } else {
                state.copy(useManualStockOverride = enabled)
            }
        }
    }

    fun updateManualStockConcentration(value: String) {
        val cleaned = sanitizeNumericInput(value, allowDecimal = true)
        updateAndRecalculate { it.copy(manualStockConcentrationInput = cleaned) }
    }

    fun updateManualStockExponent(exponent: Int) {
        updateAndRecalculate { it.copy(manualStockExponent = exponent.coerceIn(4, 8)) }
    }

    fun updateManualAvailableVolume(value: String) {
        val cleaned = sanitizeNumericInput(value, allowDecimal = true)
        updateAndRecalculate { it.copy(manualAvailableVolumeInput = cleaned) }
    }

    fun selectPlateFormat(plate: PlateFormat) {
        updateAndRecalculate { state ->
            val defaultTarget = plate.suggestedCellsPerWell(
                if (state.selectedDensityPreset == DensityPreset.CUSTOM) DensityPreset.MODERATE
                else state.selectedDensityPreset
            )
            val newCustomDensity = if (state.selectedDensityPreset != DensityPreset.CUSTOM) {
                defaultTarget.toLong().toString()
            } else {
                state.customDensityInput
            }
            state.copy(
                selectedPlate = plate,
                wellsToSeedInput = plate.wellCount.toString(),
                workingVolumePerWellInput = CellCalculator.formatDecimal(plate.defaultWorkingVolumeMl, 2),
                customDensityInput = newCustomDensity
            )
        }
    }

    fun selectDensityPreset(preset: DensityPreset) {
        updateAndRecalculate { state ->
            val updatedCustom = if (preset == DensityPreset.CUSTOM) {
                val seedVal = state.selectedPlate.suggestedCellsPerWell(
                    if (state.selectedDensityPreset == DensityPreset.CUSTOM) DensityPreset.MODERATE
                    else state.selectedDensityPreset
                )
                if (state.customDensityUnit == DensityInputUnit.CELLS_PER_WELL) {
                    seedVal.toLong().toString()
                } else {
                    (seedVal / state.selectedPlate.surfaceAreaCm2).toLong().toString()
                }
            } else {
                state.customDensityInput
            }
            state.copy(
                selectedDensityPreset = preset,
                customDensityInput = updatedCustom
            )
        }
    }

    fun updateCustomDensityInput(value: String) {
        val cleaned = sanitizeNumericInput(value, allowDecimal = false)
        updateAndRecalculate {
            it.copy(
                selectedDensityPreset = DensityPreset.CUSTOM,
                customDensityInput = cleaned
            )
        }
    }

    fun selectCustomDensityUnit(unit: DensityInputUnit) {
        updateAndRecalculate { state ->
            if (state.customDensityUnit == unit) return@updateAndRecalculate state
            val currentNumeric = state.customDensityInput.toDoubleOrNull() ?: 0.0
            val converted = if (currentNumeric > 0.0) {
                if (unit == DensityInputUnit.CELLS_PER_CM2) {
                    (currentNumeric / state.selectedPlate.surfaceAreaCm2).toLong().toString()
                } else {
                    (currentNumeric * state.selectedPlate.surfaceAreaCm2).toLong().toString()
                }
            } else {
                state.customDensityInput
            }
            state.copy(
                customDensityUnit = unit,
                customDensityInput = converted
            )
        }
    }

    fun updateWellsToSeed(value: String) {
        val cleaned = sanitizeNumericInput(value, allowDecimal = false)
        updateAndRecalculate { it.copy(wellsToSeedInput = cleaned) }
    }

    fun stepWellsToSeed(delta: Int) {
        updateAndRecalculate { state ->
            val current = state.wellsToSeedInput.toIntOrNull() ?: state.selectedPlate.wellCount
            val next = (current + delta).coerceIn(1, 384)
            state.copy(wellsToSeedInput = next.toString())
        }
    }

    fun updateWorkingVolumePerWell(value: String) {
        val cleaned = sanitizeNumericInput(value, allowDecimal = true)
        updateAndRecalculate { it.copy(workingVolumePerWellInput = cleaned) }
    }

    fun selectOveragePercent(percent: Int) {
        updateAndRecalculate { it.copy(overagePercent = percent) }
    }

    fun applyGuideSuggestion(plate: PlateFormat, preset: DensityPreset) {
        updateAndRecalculate { state ->
            state.copy(
                currentDestination = AppDestination.SEED,
                selectedPlate = plate,
                selectedDensityPreset = preset,
                wellsToSeedInput = plate.wellCount.toString(),
                workingVolumePerWellInput = CellCalculator.formatDecimal(plate.defaultWorkingVolumeMl, 2)
            )
        }
    }

    private fun updateAndRecalculate(transform: (CytoCountUiState) -> CytoCountUiState) {
        _uiState.update { previous ->
            val next = transform(previous)
            recalculate(next)
        }
    }

    private fun recalculate(state: CytoCountUiState): CytoCountUiState {
        val liveCount = when (state.inputMode) {
            CountInputMode.QUICK_TOTAL,
            CountInputMode.CHAMBER_VOLUME -> state.quickTotalCountInput.toDoubleOrNull() ?: 0.0
            CountInputMode.FOUR_QUADRANT -> state.quadrantInputs.sumOf { it.toDoubleOrNull() ?: 0.0 }
        }
        val activeSquares = when (state.inputMode) {
            CountInputMode.QUICK_TOTAL,
            CountInputMode.CHAMBER_VOLUME -> (state.squaresCountedInput.toIntOrNull() ?: 4).coerceAtLeast(1)
            CountInputMode.FOUR_QUADRANT -> {
                val entered = state.quadrantInputs.count { (it.toIntOrNull() ?: 0) > 0 }
                if (entered == 0) 4 else entered
            }
        }
        val deadCount = if (state.showViabilityInputs) {
            state.deadCellsCountInput.toDoubleOrNull() ?: 0.0
        } else 0.0
        val df = state.dilutionFactorInput.toDoubleOrNull() ?: 0.0
        val volumeMl = state.suspensionVolumeMlInput.toDoubleOrNull() ?: 0.0

        val countResult = CellCalculator.calculateHemocytometer(
            liveCellsCounted = liveCount,
            deadCellsCounted = deadCount,
            squaresCounted = activeSquares,
            dilutionFactor = df,
            suspensionVolumeMl = volumeMl,
            isChamberVolumeMode = state.inputMode == CountInputMode.CHAMBER_VOLUME
        )

        val effectiveStockConc = if (state.useManualStockOverride) {
            val mantissa = state.manualStockConcentrationInput.toDoubleOrNull() ?: 0.0
            val factor = Math.pow(10.0, state.manualStockExponent.toDouble())
            mantissa * factor
        } else {
            countResult.cellsPerMl
        }

        val effectiveAvailableVol: Double? = if (state.useManualStockOverride) {
            state.manualAvailableVolumeInput.toDoubleOrNull()?.takeIf { it > 0.0 }
        } else {
            volumeMl.takeIf { it > 0.0 }
        }

        val targetCellsPerWell = when (state.selectedDensityPreset) {
            DensityPreset.LOW -> state.selectedPlate.lowDensityCellsPerWell
            DensityPreset.MODERATE -> state.selectedPlate.moderateDensityCellsPerWell
            DensityPreset.HIGH -> state.selectedPlate.highDensityCellsPerWell
            DensityPreset.CUSTOM -> {
                val raw = state.customDensityInput.toDoubleOrNull() ?: 0.0
                if (state.customDensityUnit == DensityInputUnit.CELLS_PER_WELL) {
                    raw
                } else {
                    raw * state.selectedPlate.surfaceAreaCm2
                }
            }
        }

        val wellsRequested = (state.wellsToSeedInput.toIntOrNull() ?: state.selectedPlate.wellCount)
            .coerceAtLeast(1)
        val workingVolMl = (state.workingVolumePerWellInput.toDoubleOrNull()
            ?: state.selectedPlate.defaultWorkingVolumeMl).coerceAtLeast(0.01)

        val seedingPlan = CellCalculator.calculateSeedingPlan(
            plate = state.selectedPlate,
            wellsRequested = wellsRequested,
            overagePercent = state.overagePercent,
            workingVolumePerWellMl = workingVolMl,
            targetCellsPerWell = targetCellsPerWell,
            stockCellsPerMl = effectiveStockConc,
            availableVolumeMl = effectiveAvailableVol
        )

        return state.copy(
            countResult = countResult,
            seedingPlan = seedingPlan
        )
    }

    private fun sanitizeNumericInput(raw: String, allowDecimal: Boolean): String {
        val sb = StringBuilder()
        var hasDot = false
        for (ch in raw) {
            when {
                ch.isDigit() -> sb.append(ch)
                allowDecimal && ch == '.' && !hasDot -> {
                    hasDot = true
                    sb.append(ch)
                }
            }
        }
        return sb.toString().take(10)
    }
}
