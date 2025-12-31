package io.github.cookieysun.otoshidamaroulette

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import io.github.cookieysun.otoshidamaroulette.ui.navigation.RouletteRoute
import io.github.cookieysun.otoshidamaroulette.ui.navigation.SettingsRoute
import io.github.cookieysun.otoshidamaroulette.ui.roulette.RouletteScreen
import io.github.cookieysun.otoshidamaroulette.ui.settings.SettingsScreen
import io.github.cookieysun.otoshidamaroulette.ui.settings.SettingsViewModel
import io.github.cookieysun.otoshidamaroulette.ui.settings.SettingsViewModelFactory
import io.github.cookieysun.otoshidamaroulette.ui.theme.OtoshidamaRouletteTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OtoshidamaRouletteTheme {
                val context = LocalContext.current
                val repository = remember {
                    io.github.cookieysun.otoshidamaroulette.data.repository.SettingsRepositoryImpl(context)
                }
                val settingsViewModel: SettingsViewModel =
                        viewModel(factory = SettingsViewModelFactory(repository))
                val backStack = rememberNavBackStack(RouletteRoute)

                Scaffold(
                    modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavDisplay<NavKey>(
                            backStack = backStack,
                            entryProvider = { key: NavKey ->
                                when (key) {
                                    RouletteRoute ->
                                            NavEntry(key) {
                                                RouletteScreen(
                                                        onSettingsClick = {
                                                            backStack.add(SettingsRoute)
                                                        },
                                                        settingsViewModel = settingsViewModel,
                                                        modifier = Modifier.padding(innerPadding)
                                                )
                                            }
                                    SettingsRoute ->
                                            NavEntry(key) {
                                                SettingsScreen(
                                                        settingsViewModel = settingsViewModel,
                                                        onBackClick = {
                                                            if (backStack.isNotEmpty())
                                                                    backStack.removeAt(
                                                                            backStack.lastIndex
                                                                    )
                                                        }
                                                )
                                            }
                                    else -> NavEntry(key) {}
                                }
                            }
                    )
                }
            }
        }
    }
}
