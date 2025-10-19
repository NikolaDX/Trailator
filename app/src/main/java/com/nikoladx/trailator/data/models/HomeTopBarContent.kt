package com.nikoladx.trailator.data.models

import androidx.compose.runtime.Composable

data class HomeTopBarContent(
    val title: String,
    val navigationIcon: (@Composable () -> Unit)? = null,
    val actions: (@Composable () -> Unit)? = null
)