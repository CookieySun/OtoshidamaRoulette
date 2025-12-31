package io.github.cookieysun.otoshidamaroulette.ui.roulette

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import io.github.cookieysun.otoshidamaroulette.data.model.RouletteItem

/**
 * ルーレットの描画のみを担当するコンポーザブル
 * アニメーションのロジックは持たず、渡された回転角度で描画する
 */
@Composable
fun RouletteWheel(
    items: List<RouletteItem>,
    rotation: Float,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(300.dp)) {
        val canvasSize = size.minDimension
        val radius = canvasSize / 2
        val center = Offset(size.width / 2, size.height / 2)
        val sectionAngle = 360f / items.size

        rotate(rotation, pivot = center) {
            // 各セクションを描画
            items.forEachIndexed { index, item ->
                val startAngle = index * sectionAngle - 90f

                // セクションの色を描画
                drawArc(
                    color = item.color,
                    startAngle = startAngle,
                    sweepAngle = sectionAngle,
                    useCenter = true,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2)
                )

                // セクションの境界線
                drawArc(
                    color = Color.White,
                    startAngle = startAngle,
                    sweepAngle = sectionAngle,
                    useCenter = true,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = 2f)
                )
            }

            // テキストを描画
            items.forEachIndexed { index, item ->
                val angle = Math.toRadians((index * sectionAngle + sectionAngle / 2 - 90f).toDouble())
                val textRadius = radius * 0.65f
                val textX = center.x + (textRadius * kotlin.math.cos(angle)).toFloat()
                val textY = center.y + (textRadius * kotlin.math.sin(angle)).toFloat()

                drawContext.canvas.nativeCanvas.apply {
                    val paint = android.graphics.Paint().apply {
                        color = android.graphics.Color.WHITE
                        textSize = 36f
                        textAlign = android.graphics.Paint.Align.CENTER
                        isFakeBoldText = true
                        setShadowLayer(4f, 2f, 2f, android.graphics.Color.BLACK)
                    }
                    drawText(item.label, textX, textY + 12f, paint)
                }
            }

            // 中心の円
            drawCircle(
                color = Color.White,
                radius = radius * 0.15f,
                center = center
            )
            drawCircle(
                color = Color.DarkGray,
                radius = radius * 0.12f,
                center = center
            )
        }
    }
}
