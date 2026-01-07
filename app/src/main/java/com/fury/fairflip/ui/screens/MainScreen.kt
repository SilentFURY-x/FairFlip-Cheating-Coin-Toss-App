package com.fury.fairflip.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fury.fairflip.ui.components.FlipButton
import com.fury.fairflip.ui.components.GameCoin
import com.fury.fairflip.ui.theme.MysticBlack
import com.fury.fairflip.ui.theme.RoyalGold
import com.fury.fairflip.ui.theme.SurfaceGrey
import com.fury.fairflip.ui.theme.TextGrey

@Composable
fun MainScreen() {
    // These are temporary states just to test the UI looks good
    // We will replace these with ViewModel state in Phase 3
    var coinRotation by remember { mutableStateOf(0f) }
    var statusText by remember { mutableStateOf("Ready to Flip") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                // A subtle radial gradient for a "spotlight" effect
                brush = Brush.radialGradient(
                    colors = listOf(SurfaceGrey, MysticBlack),
                    radius = 1200f
                )
            )
    ) {
        // --- 1. STEALTH HEADER (Hidden Button) ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = {
                            statusText = "Stealth Mode Toggled" // Test feedback
                        }
                    )
                }
                .align(Alignment.TopCenter)
        )

        // --- 2. TITLE ---
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 60.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "FAIR FLIP",
                color = RoyalGold,
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 3.sp
            )
            Text(
                text = "PRO EDITION",
                color = TextGrey,
                fontSize = 12.sp,
                letterSpacing = 4.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // --- 3. THE COIN (CENTER) ---
        GameCoin(
            isHeads = true,
            rotationY = coinRotation,
            modifier = Modifier.align(Alignment.Center)
        )

        // --- 4. CONTROLS (BOTTOM) ---
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 60.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = statusText,
                color = TextGrey,
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 30.dp)
            )

            FlipButton(
                isEnabled = true,
                onClick = {
                    statusText = "Flipping..."
                    // Test rotation
                    coinRotation += 180f
                }
            )
        }
    }
}