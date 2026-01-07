package com.fury.fairflip.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.fury.fairflip.R

@Composable
fun GameCoin(
    isHeads: Boolean,
    rotationY: Float, // We will control this for the 3D animation
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(280.dp) // Large hero size
            .graphicsLayer {
                // This is the magic 3D rotation logic
                this.rotationY = rotationY
                cameraDistance = 12f * density // Adds depth perspective
            },
        contentAlignment = Alignment.Center
    ) {
        // We render the image based on the rotation angle to simulate 2 sides
        // If rotation is between 90 and 270, we are looking at the "back"
        val showHeads = if (rotationY % 360 in 90f..270f) !isHeads else isHeads

        Image(
            painter = painterResource(id = if (showHeads) R.drawable.coin_heads else R.drawable.coin_tails),
            contentDescription = "Coin Face",
            modifier = Modifier
                .matchParentSize()
                .shadow(elevation = 25.dp, shape = CircleShape, spotColor = com.fury.fairflip.ui.theme.RoyalGold)
        )
    }
}