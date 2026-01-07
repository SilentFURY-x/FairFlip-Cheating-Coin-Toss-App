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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fury.fairflip.ui.components.FlipButton
import com.fury.fairflip.ui.components.GameCoin
import com.fury.fairflip.ui.theme.MysticBlack
import com.fury.fairflip.ui.theme.RoyalGold
import com.fury.fairflip.ui.theme.SurfaceGrey
import com.fury.fairflip.ui.theme.TextGrey
import com.fury.fairflip.ui.viewmodel.CoinViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun MainScreen(
    viewModel: CoinViewModel = viewModel()
) {
    // --- STATE ---
    var coinStateIsHeads by remember { mutableStateOf(true) }
    var targetRotation by remember { mutableFloatStateOf(0f) }
    var statusText by remember { mutableStateOf("TAP TO FLIP") }
    var isFlipping by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Observe Parallax Data
    val parallaxOffset by viewModel.coinOffset.collectAsState()

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
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- HEADER ---
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onLongPress = {
                                val msg = viewModel.toggleStealthMode()
                                statusText = msg
                                scope.launch {
                                    delay(2000)
                                    if (!isFlipping && statusText == msg) {
                                        statusText = "TAP TO FLIP"
                                    }
                                }
                            }
                        )
                    },
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 50.dp)
                ) {
                    Text(
                        text = "FAIR FLIP",
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
            // Note: We removed the .offset modifier from THIS Box.
            Box(
                modifier = Modifier.weight(2f),
                contentAlignment = Alignment.Center
            ) {
                GameCoin(
                    rotationY = currentRotation,
                    // Pass the offset logic INTO the component
                    offsetX = parallaxOffset.first.dp,
                    offsetY = parallaxOffset.second.dp
                )
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

                        val nextResultIsHeads = viewModel.getFlipResult(coinStateIsHeads)

                        val minSpins = 1800f
                        val current = targetRotation
                        val remainder = current % 360f
                        val targetBase = current + minSpins - remainder

                        targetRotation = if (nextResultIsHeads) {
                            targetBase + 360f
                        } else {
                            targetBase + 180f
                        }

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