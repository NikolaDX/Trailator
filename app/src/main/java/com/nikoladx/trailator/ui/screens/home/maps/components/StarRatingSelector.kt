package com.nikoladx.trailator.ui.screens.home.maps.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun StarRatingSelector(
    currentRating: Int,
    onRatingChange: (Int) -> Unit
) {
    val maxRating = 5

    Row {
        (1..maxRating).forEach { starIndex ->
            val isSelected = starIndex <= currentRating

            Icon(
                imageVector = if (isSelected) Icons.Filled.Star else Icons.Filled.StarOutline,
                contentDescription = "$starIndex star rating",
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .size(32.dp)
                    .clickable {
                        onRatingChange(starIndex)
                    }
            )
        }
    }
}