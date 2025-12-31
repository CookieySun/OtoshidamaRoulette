package io.github.cookieysun.otoshidamaroulette.ui.roulette

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.cookieysun.otoshidamaroulette.ui.settings.SettingsViewModel
import io.github.cookieysun.otoshidamaroulette.ui.theme.OtoshidamaRouletteTheme

/**
 * カスタムイージング：回転中と同じ速度から始まり、最後はゆっくり減速して停止
 *
 * f(t) = 1 - (1-t)^n
 * n が大きいほど最後の減速が強くなる
 * n = 3 で最後がかなりゆっくりになる
 */
private val DecelerateFromLinearEasing = Easing { fraction ->
    val oneMinusT = 1f - fraction
    // (1-t)^3 で最後の減速を強く
    1f - oneMinusT * oneMinusT * oneMinusT
}

@Composable
fun RouletteScreen(
    onSettingsClick: () -> Unit,
    settingsViewModel: SettingsViewModel,
    viewModel: RouletteViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val state = viewModel.state
    val selectedItem = viewModel.selectedItem
    val items = settingsViewModel.getRouletteItems()

    // アニメーション用の状態
    val rotation = remember { Animatable(0f) }
    val isSpinning = state == RouletteState.SPINNING || state == RouletteState.WAITING_TO_STOP
    val isStopping = state == RouletteState.STOPPING
    val additionalRotationState = rememberUpdatedState(viewModel.additionalRotation)
    val stopDurationState = rememberUpdatedState(viewModel.stopDuration)

    // 回転中のアニメーション
    LaunchedEffect(isSpinning) {
        if (isSpinning) {
            while (true) {
                val startRotation = rotation.value
                viewModel.updateRotation(startRotation)
                rotation.animateTo(
                    targetValue = startRotation + 360f,
                    animationSpec = tween(
                        durationMillis = 576,
                        easing = LinearEasing
                    )
                )
            }
        }
    }

    // 停止アニメーション
    LaunchedEffect(isStopping) {
        if (isStopping) {
            val currentValue = rotation.value
            rotation.animateTo(
                targetValue = currentValue + additionalRotationState.value,
                animationSpec = tween(
                    durationMillis = stopDurationState.value,
                    easing = DecelerateFromLinearEasing
                )
            )
            viewModel.onStopAnimationFinished(rotation.value, items)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFFF8E1))
    ) {
        // 左上の設定ボタン
        IconButton(
            onClick = onSettingsClick,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "設定",
                tint = Color(0xFFD32F2F)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
        // タイトル
        Text(
            text = "お年玉ルーレット",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFD32F2F)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 結果表示エリア
        Box(modifier = Modifier.height(120.dp)) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .padding(horizontal = 32.dp, vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                if (selectedItem != null) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "あたり！",
                            fontSize = 18.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = selectedItem.label,
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD32F2F)
                        )
                    }
                } else if (!isSpinning) {
                    Text(
                        text = "スタートを押してね",
                        fontSize = 20.sp,
                        color = Color.Gray
                    )
                } else {
                    Text(
                        text = "ストップを押してね",
                        fontSize = 20.sp,
                        color = Color.Gray
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // 選択位置の矢印
        Box(
            modifier = Modifier
                .size(40.dp)
                .offset(y = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "▼",
                fontSize = 32.sp,
                color = Color(0xFFD32F2F)
            )
        }

        // ルーレットホイール（描画のみ）
        RouletteWheel(
            items = items,
            rotation = rotation.value
        )

        Spacer(modifier = Modifier.height(32.dp))

        // スタート/ストップボタン
        Button(
            onClick = { viewModel.onButtonClick() },
            modifier = Modifier
                .width(200.dp)
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = when (state) {
                    RouletteState.IDLE -> Color(0xFF4CAF50)
                    RouletteState.SPINNING -> Color(0xFFF44336)
                    RouletteState.WAITING_TO_STOP -> Color.Gray
                    RouletteState.STOPPING -> Color.Gray
                }
            ),
            enabled = state == RouletteState.IDLE || state == RouletteState.SPINNING,
            shape = RoundedCornerShape(28.dp)
        ) {
            Text(
                text = when (state) {
                    RouletteState.IDLE -> "スタート"
                    RouletteState.SPINNING -> "ストップ"
                    RouletteState.WAITING_TO_STOP -> "停止中..."
                    RouletteState.STOPPING -> "停止中..."
                },
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RouletteScreenPreview() {
    OtoshidamaRouletteTheme {
        RouletteScreen(
            onSettingsClick = {},
            settingsViewModel = SettingsViewModel()
        )
    }
}
