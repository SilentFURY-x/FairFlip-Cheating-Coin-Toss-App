package com.fury.fairflip.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fury.fairflip.ui.components.FlipButton
import com.fury.fairflip.ui.components.GameCoin
import com.fury.fairflip.ui.theme.MysticBlack
import com.fury.fairflip.ui.theme.RoyalGold
import com.fury.fairflip.ui.theme.SurfaceGrey
import com.fury.fairflip.ui.theme.TextGrey
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun MainScreen() {
    // --- STATE ---
    var coinStateIsHeads by remember { mutableStateOf(true) }
    var targetRotation by remember { mutableFloatStateOf(0f) }
    var statusText by remember { mutableStateOf("TAP TO FLIP") }
    var isFlipping by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // --- ANIMATION ENGINE ---
    val currentRotation by animateFloatAsState(
        targetValue = targetRotation,
        animationSpec = tween(
            durationMillis = 1500,
            easing = FastOutSlowInEasing
        ),
        label = "CoinSpin"
    )

    // --- UI ROOT ---
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(SurfaceGrey, MysticBlack.copy(alpha = 0.95f)),
                    radius = 1400f
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- HEADER ---
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onLongPress = { statusText = "/// STEALTH MODE ///" }
                        )
                    },
                // CHANGED: Moved Alignment to TopCenter to push text up
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    // CHANGED: Added specific top padding to position it exactly where you want
                    modifier = Modifier.padding(top = 50.dp)
                ) {
                    Text(
                        text = "FAIR FLIP", // CHANGED: Name Updated
                        color = RoyalGold,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 4.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "PRO EDITION",
                        color = TextGrey,
                        fontSize = 10.sp,
                        letterSpacing = 3.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // --- HERO COIN ---
            Box(
                modifier = Modifier.weight(2f),
                contentAlignment = Alignment.Center
            ) {
                GameCoin(rotationY = currentRotation)
            }

            // --- CONTROLS ---
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(bottom = 50.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                Text(
                    text = statusText,
                    color = if (isFlipping) RoyalGold else TextGrey,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                FlipButton(
                    isEnabled = !isFlipping,
                    onClick = {
                        if (isFlipping) return@FlipButton
                        isFlipping = true
                        statusText = "FLIPPING..."

                        // 1. DECIDE RESULT
                        val nextResultIsHeads = !coinStateIsHeads

                        // 2. CALCULATE MATH
                        val minSpins = 1800f
                        val current = targetRotation
                        val remainder = current % 360f
                        val targetBase = current + minSpins - remainder

                        targetRotation = if (nextResultIsHeads) {
                            targetBase + 360f
                        } else {
                            targetBase + 180f
                        }

                        // 3. UPDATE STATE
                        scope.launch {
                            delay(1500)
                            coinStateIsHeads = nextResultIsHeads
                            statusText = if (coinStateIsHeads) "HEADS" else "TAILS"
                            isFlipping = false
                        }
                    }
                )
            }
        }
    }
}