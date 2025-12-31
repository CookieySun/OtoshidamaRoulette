package io.github.cookieysun.otoshidamaroulette.ui.settings

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.cookieysun.otoshidamaroulette.data.model.RouletteItem
import io.github.cookieysun.otoshidamaroulette.data.repository.SettingsRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val repository: SettingsRepository? = null,
    private val mainDispatcher: CoroutineDispatcher = Dispatchers.Main,
    externalScope: CoroutineScope? = null
) : ViewModel() {

    private val scope = externalScope ?: viewModelScope

    // 選択可能な色のリスト
    val availableColors: List<Color>
        get() = repository?.availableColors ?: emptyList()

    // 金額のリスト（編集可能）
    val amounts: StateFlow<List<Int>> =
        (repository?.amountsFlow ?: kotlinx.coroutines.flow.flowOf(emptyList())).stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = listOf(500, 1000, 1500, 2000, 500, 1000, 1500, 2000)
        )

    // 色のインデックスリスト（編集可能）
    // amountsと同期してサイズを調整する処理を含める
    val colorIndices: StateFlow<List<Int>> = if (repository != null) {
        combine(
            repository.amountsFlow,
            repository.colorIndicesFlow
        ) { currentAmounts, currentColors ->
            ensureColorIndices(currentAmounts, currentColors)
        }.stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = listOf(0, 1, 2, 3, 0, 1, 2, 3)
        )
    } else {
        kotlinx.coroutines.flow.MutableStateFlow(listOf(0, 1, 2, 3, 0, 1, 2, 3))
    }

    // Helper method to ensure color indices match amounts size
    private fun ensureColorIndices(amounts: List<Int>, colors: List<Int>): List<Int> {
        val newColors = colors.toMutableList()
        val availableSize = if (availableColors.isNotEmpty()) availableColors.size else 1

        while (newColors.size < amounts.size) {
            newColors.add(newColors.size % availableSize)
        }
        while (newColors.size > amounts.size) {
            newColors.removeLastOrNull()
        }
        return newColors
    }

    // Helper to save data (Read-Modify-Write is done by passing new lists)
    private fun saveToRepository(newAmounts: List<Int>, newColors: List<Int>) {
        repository?.let { repo ->
            scope.launch(mainDispatcher) {
                repo.saveAmounts(newAmounts)
                repo.saveColorIndices(newColors)
            }
        }
    }

    fun getColorForIndex(index: Int): Color {
        val currentColors = colorIndices.value
        val available = availableColors
        if (available.isEmpty()) return Color.Gray // Fallback

        val colorIndex = if (index in currentColors.indices) {
            currentColors[index]
        } else {
            index % available.size
        }
        return available[colorIndex % available.size]
    }

    fun updateAmount(index: Int, newAmount: Int) {
        val currentAmounts = amounts.value
        if (index in currentAmounts.indices && newAmount >= 0) {
            val newAmounts = currentAmounts.toMutableList()
            newAmounts[index] = newAmount
            saveToRepository(newAmounts, colorIndices.value)
        }
    }

    fun updateColor(index: Int, colorIndex: Int) {
        val currentColors = colorIndices.value
        if (index in currentColors.indices && colorIndex in availableColors.indices) {
            val newColors = currentColors.toMutableList()
            newColors[index] = colorIndex
            saveToRepository(amounts.value, newColors)
        }
    }

    fun addItem(amount: Int = 1000) {
        val newAmounts = amounts.value.toMutableList()
        newAmounts.add(amount)

        val newColors = colorIndices.value.toMutableList()
        if (availableColors.isNotEmpty()) {
            // Fixed: Use newColors.size to ensure next sequential index
            newColors.add(newColors.size % availableColors.size)
        } else {
            newColors.add(0)
        }

        saveToRepository(newAmounts, newColors)
    }

    fun removeItem(index: Int) {
        val currentAmounts = amounts.value
        if (currentAmounts.size > 2 && index in currentAmounts.indices) {
            val newAmounts = currentAmounts.toMutableList()
            newAmounts.removeAt(index)

            val newColors = colorIndices.value.toMutableList()
            if (index in newColors.indices) {
                newColors.removeAt(index)
            }

            saveToRepository(newAmounts, newColors)
        }
    }

    fun moveItem(fromIndex: Int, toIndex: Int) {
        val currentAmounts = amounts.value
        if (fromIndex !in currentAmounts.indices || toIndex !in currentAmounts.indices || fromIndex == toIndex) {
            return
        }

        val newAmounts = currentAmounts.toMutableList()
        val newColors = colorIndices.value.toMutableList()

        val availableSize = if (availableColors.isNotEmpty()) availableColors.size else 1
        while (newColors.size < newAmounts.size) newColors.add(newColors.size % availableSize)

        // Swap amounts
        val temp = newAmounts[fromIndex]
        newAmounts[fromIndex] = newAmounts[toIndex]
        newAmounts[toIndex] = temp

        // Swap colors
        val tempColor = newColors[fromIndex]
        newColors[fromIndex] = newColors[toIndex]
        newColors[toIndex] = tempColor

        saveToRepository(newAmounts, newColors)
    }

    fun getRouletteItems(): List<RouletteItem> {
        return amounts.value.mapIndexed { index, amount ->
            RouletteItem(amount, getColorForIndex(index))
        }
    }
}
