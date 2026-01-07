package com.fury.fairflip.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.fury.fairflip.ui.theme.RoyalGold
import kotlin.random.Random

// Data class to hold star properties so they don't regenerate every frame
private data class StarData(
    val normX: Float, // Normalized position (0.0 - 1.0)
    val normY: Float,
    val radiusDp: Float,
    val alpha: Float
)

@Composable
fun StarField(modifier: Modifier = Modifier) {
    // 1. Generate stars ONCE and remember them.
    // We use a pale gold color for subtle integration with the theme.
    val stars = remember {
        List(120) { // Draw 120 random stars
            StarData(
                normX = Random.nextFloat(),
                normY = Random.nextFloat(),
                radiusDp = Random.nextDouble(0.5, 2.5).toFloat(), // Varied sizes
                alpha = Random.nextDouble(0.1, 0.5).toFloat() // Varied transparency
            )
        }
    }

    // 2. Draw them on a Canvas
    Canvas(modifier = modifier.fillMaxSize()) {
        stars.forEach { star ->
            drawCircle(
                color = RoyalGold.copy(alpha = star.alpha),
                radius = star.radiusDp.dp.toPx(),
                center = Offset(
                    x = star.normX * size.width,
                    // We add a slight vertical stretch to fill top/bottom areas better on tall phones
                    y = (star.normY * size.height * 1.2f) - (size.height * 0.1f)
                )
            )
        }
    }
}