package io.github.cookieysun.otoshidamaroulette.data.repository

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import io.github.cookieysun.otoshidamaroulette.ui.theme.RouletteBlue
import io.github.cookieysun.otoshidamaroulette.ui.theme.RouletteBrown
import io.github.cookieysun.otoshidamaroulette.ui.theme.RouletteCyan
import io.github.cookieysun.otoshidamaroulette.ui.theme.RouletteGreen
import io.github.cookieysun.otoshidamaroulette.ui.theme.RouletteOrange
import io.github.cookieysun.otoshidamaroulette.ui.theme.RoulettePurple
import io.github.cookieysun.otoshidamaroulette.ui.theme.RouletteRed
import io.github.cookieysun.otoshidamaroulette.ui.theme.RouletteYellow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

interface SettingsRepository {
    val amountsFlow: Flow<List<Int>>
    val colorIndicesFlow: Flow<List<Int>>
    val availableColors: List<Color>

    suspend fun saveAmounts(amounts: List<Int>)
    suspend fun saveColorIndices(colorIndices: List<Int>)
}

class SettingsRepositoryImpl(private val context: Context) : SettingsRepository {

    companion object {
        private val AMOUNTS_KEY = stringPreferencesKey("amounts")
        private val COLOR_INDICES_KEY = stringPreferencesKey("color_indices")
    }

    override val amountsFlow: Flow<List<Int>> =
            context.dataStore.data.map { preferences ->
                preferences[AMOUNTS_KEY]?.split(",")?.mapNotNull { it.toIntOrNull() }
                        ?: listOf(500, 1000, 1500, 2000, 500, 1000, 1500, 2000)
            }

    override val colorIndicesFlow: Flow<List<Int>> =
            context.dataStore.data.map { preferences ->
                preferences[COLOR_INDICES_KEY]?.split(",")?.mapNotNull { it.toIntOrNull() }
                        ?: listOf(0, 1, 2, 3, 0, 1, 2, 3)
            }

    override suspend fun saveAmounts(amounts: List<Int>) {
        context.dataStore.edit { preferences ->
            preferences[AMOUNTS_KEY] = amounts.joinToString(",")
        }
    }

    override suspend fun saveColorIndices(colorIndices: List<Int>) {
        context.dataStore.edit { preferences ->
            preferences[COLOR_INDICES_KEY] = colorIndices.joinToString(",")
        }
    }

    override val availableColors =
            listOf(
                    RouletteRed,
                    RouletteBlue,
                    RouletteGreen,
                    RouletteOrange,
                    RoulettePurple,
                    RouletteCyan,
                    RouletteYellow,
                    RouletteBrown
            )
}
