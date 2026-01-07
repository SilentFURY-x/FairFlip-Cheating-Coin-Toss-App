package com.fury.fairflip.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.fury.fairflip.R
import com.fury.fairflip.ui.theme.RoyalGoldDark

@Composable
fun GameCoin(
    rotationY: Float,
    modifier: Modifier = Modifier
) {
    // Container: 380dp (Enough space so the 190dp radius glow isn't clipped)
    Box(
        modifier = modifier.size(380.dp),
        contentAlignment = Alignment.Center
    ) {
        // --- LAYER 1: THE SOFT BLENDED GLOW ---
        // Alpha reduced to 0.5f for maximum softness against dark bg
        Canvas(modifier = Modifier.fillMaxSize().graphicsLayer { alpha = 0.5f }) {
            val shadowCenter = center.copy(y = center.y + 15f)
            // Define the exact radius where the glow hits total transparency.
            // Coin radius is 150dp. 190dp gives a tight, soft fade.
            val glowRadiusPx = 190.dp.toPx()

            val softBrush = Brush.radialGradient(
                colors = listOf(RoyalGoldDark, Color.Transparent),
                center = shadowCenter,
                radius = glowRadiusPx // Gradient ends exactly here
            )

            drawCircle(
                brush = softBrush,
                radius = glowRadiusPx, // Drawing stops exactly where gradient ends. No hard edge.
                center = shadowCenter
            )
        }

        // --- LAYER 2: THE 3D COIN OBJECT (300dp) ---
        Box(
            modifier = Modifier
                .size(300.dp)
                .graphicsLayer {
                    this.rotationY = rotationY
                    cameraDistance = 16f * density
                }
        ) {
            val normalized = (rotationY % 360f + 360f) % 360f
            val isBackVisible = normalized in 90f..270f

            // --- HEADS SIDE ---
            Image(
                painter = painterResource(id = R.drawable.coin_heads),
                contentDescription = "Heads",
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        alpha = if (isBackVisible) 0f else 1f
                    }
            )

            // --- TAILS SIDE ---
            Image(
                painter = painterResource(id = R.drawable.coin_tails),
                contentDescription = "Tails",
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        this.rotationY = 180f
                        alpha = if (isBackVisible) 1f else 0f
                    }
            )
        }
    }
}