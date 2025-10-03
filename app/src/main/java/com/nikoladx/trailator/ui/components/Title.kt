package com.nikoladx.trailator.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun Title(text: String) {
    Text(text, fontSize = 32.sp, fontWeight = FontWeight.Bold)
}