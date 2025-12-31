package io.github.cookieysun.otoshidamaroulette.data.model

import androidx.compose.ui.graphics.Color

data class RouletteItem(
    val amount: Int,
    val color: Color
) {
    val label: String = "${amount}円"
}
