package com.nikoladx.trailator.ui.screens.home.feed.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nikoladx.trailator.data.models.TrailObject

@Composable
fun TrailObjectGrid(
    paddingValues: PaddingValues,
    trailObjects: List<TrailObject>,
    onTrailObjectClick: (String) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(trailObjects, key = { it.id }) { trailObject ->
            TrailObjectCard(
                trailObject = trailObject,
                onClick = { onTrailObjectClick(trailObject.id) }
            )
        }
    }
}