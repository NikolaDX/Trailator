package com.nikoladx.trailator.ui.screens.home.maps.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private const val MIN_RADIUS = 0f
private const val MAX_RADIUS = 5000f
private const val DEFAULT_THUMB_SIZE = 24
private const val PRESSED_THUMB_SIZE = 32

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RadiusSlider(
    modifier: Modifier,
    radius: Float,
    onRadiusChange: (Float) -> Unit,
    onUpdateFinished: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val isDragged by interactionSource.collectIsDraggedAsState()
    val isInteracting = isPressed || isDragged
    val thumbSize by animateDpAsState(
        targetValue = if (isInteracting) PRESSED_THUMB_SIZE.dp else DEFAULT_THUMB_SIZE.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )
    val animatedRadius by animateFloatAsState(
        targetValue = radius,
        animationSpec = tween(durationMillis = 100)
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (animatedRadius > 0) {
            Text(
                text = "Radius: ${animatedRadius.toInt()} m",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        } else {
            Text(
                text = "Radius Search Off",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        Slider(
            value = radius,
            onValueChange = { newValue ->
                onRadiusChange(newValue)
            },
            onValueChangeFinished = {
                onUpdateFinished()
            },
            valueRange = MIN_RADIUS..MAX_RADIUS,
            modifier = Modifier.fillMaxWidth(0.9f),
            interactionSource = interactionSource,
            thumb = {
                Box(
                    modifier = Modifier
                        .padding(0.dp)
                        .size(thumbSize)
                        .background(
                            MaterialTheme.colorScheme.primary,
                            CutCornerShape(20.dp)
                        )
                )
            },
            track = { sliderState ->
                val fraction by remember {
                    derivedStateOf {
                        (sliderState.value - sliderState.valueRange.start) / (sliderState.valueRange.endInclusive - sliderState.valueRange.start)
                    }
                }

                Box(Modifier.fillMaxWidth()) {
                    Box(
                        Modifier
                            .fillMaxWidth(fraction)
                            .align(Alignment.CenterStart)
                            .height(6.dp)
                            .padding(end = 16.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                    )
                    Box(
                        Modifier
                            .fillMaxWidth(1f - fraction)
                            .align(Alignment.CenterEnd)
                            .height(1.dp)
                            .padding(start = 16.dp)
                            .background(MaterialTheme.colorScheme.onSurface, CircleShape)
                    )
                }
            },
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.onSurface,
            )
        )


    }
}