package com.fury.fairflip.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fury.fairflip.ui.theme.MysticBlack
import com.fury.fairflip.ui.theme.RoyalGold

@Composable
fun FlipButton(
    onClick: () -> Unit,
    isEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = isEnabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = RoyalGold,
            contentColor = MysticBlack,
            disabledContainerColor = RoyalGold.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(20.dp), // Smooth rounded corners
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 12.dp,
            pressedElevation = 4.dp
        ),
        modifier = modifier
            .fillMaxWidth(0.75f) // Take up 75% of screen width
            .height(65.dp)
    ) {
        Text(
            text = "FLIP",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
    }
}