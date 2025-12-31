package io.github.cookieysun.otoshidamaroulette.ui.roulette

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.cookieysun.otoshidamaroulette.data.model.RouletteItem
import kotlin.math.floor
import kotlin.random.Random
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


enum class RouletteState {
    IDLE,
    SPINNING,
    WAITING_TO_STOP,
    STOPPING
}

class RouletteViewModel : ViewModel() {


    var state by mutableStateOf(RouletteState.IDLE)
        private set

    // 現在の回転角度
    var rotation by mutableFloatStateOf(0f)
        private set

    // 停止時の追加回転量
    var additionalRotation by mutableFloatStateOf(0f)
        private set

    // 停止アニメーションの時間（ミリ秒）
    var stopDuration by mutableIntStateOf(1500)
        private set

    var selectedItem by mutableStateOf<RouletteItem?>(null)
        private set

    fun onButtonClick() {
        when (state) {
            RouletteState.IDLE -> startSpinning()
            RouletteState.SPINNING -> stopSpinning()
            RouletteState.WAITING_TO_STOP -> { /* 何もしない */
            }

            RouletteState.STOPPING -> { /* 何もしない */
            }
        }
    }

    fun updateRotation(newRotation: Float) {
        rotation = newRotation
    }

    private fun startSpinning() {
        state = RouletteState.SPINNING
        selectedItem = null
        additionalRotation = 0f
    }

    private fun stopSpinning() {
        state = RouletteState.WAITING_TO_STOP

        viewModelScope.launch {
            // 1.5〜2秒のランダムな時間待つ
            val waitTime = Random.nextLong(1000, 1500)
            delay(waitTime)

            // WAITING_TO_STOPに変更
            state = RouletteState.WAITING_TO_STOP

            // 追加で2〜4周回転してから停止
            additionalRotation = Random.nextFloat() * 360f + 720f

            // 回転中の速度: 576msで360度 → 0.625度/ms
            // イージング f(t) = 1 - (1-t)^3 の傾き f'(0) = 3
            // 初速を一致させる: additionalRotation / stopDuration × 3 = 0.625
            // stopDuration = additionalRotation × 3 / 0.625
            stopDuration = (additionalRotation * 3f / 0.625f).toInt()
            // STOPPINGに変更
            state = RouletteState.STOPPING
        }
    }

    fun onStopAnimationFinished(finalRotation: Float, items: List<RouletteItem>) {
        val itemsSize = items.size
        val selectedIndex = floor((finalRotation / (360 / itemsSize)) % itemsSize).toInt()

        selectedItem = items.reversed()[selectedIndex]
        state = RouletteState.IDLE
    }
}
