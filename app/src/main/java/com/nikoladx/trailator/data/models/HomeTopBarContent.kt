package com.nikoladx.trailator.data.models

import androidx.compose.runtime.Composable

data class HomeTopBarContent(
    val title: String,
    val actions: (@Composable () -> Unit)? = null
)