package com.nikoladx.trailator.ui.screens.home.maps.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.nikoladx.trailator.ui.components.CloudinaryImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoCarousel(
    photoUrls: List<String>,
    onPhotoClick: (Int) -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { photoUrls.size })

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            CloudinaryImage(
                url = photoUrls[page],
                contentDescription = "Trail object photo ${page + 1}",
                modifier = Modifier
                    .fillMaxSize()
                    .height(200.dp)
                    .clickable { onPhotoClick(page) },
                height = 600,
                quality = "auto:good",
                contentScale = ContentScale.Fit
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${pagerState.currentPage + 1} / ${photoUrls.size}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}