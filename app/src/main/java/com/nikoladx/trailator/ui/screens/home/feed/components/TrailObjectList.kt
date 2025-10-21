package com.nikoladx.trailator.ui.screens.home.feed.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.nikoladx.trailator.data.models.TrailObject

@Composable
fun TrailObjectList(
    paddingValues: PaddingValues,
    trailObjects: List<TrailObject>,
    onTrailObjectClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items(trailObjects, key = { it.id }) { trailObject ->
            TrailObjectCard(
                trailObject = trailObject,
                onClick = { onTrailObjectClick(trailObject.id) }
            )
        }
    }
}