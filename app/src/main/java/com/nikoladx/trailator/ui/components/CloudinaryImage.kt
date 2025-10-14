package com.nikoladx.trailator.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.nikoladx.trailator.services.cloudinary.CloudinaryUploader

@Composable
fun CloudinaryImage(
    url: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    width: Int = 800,
    height: Int = 600,
    quality: String = "auto",
    showLoading: Boolean = true
) {
    val context = LocalContext.current
    val cloudinaryUploader = remember { CloudinaryUploader(context) }
    val optimizedUrl = remember(url, width, height, quality) {
        if (url.contains("cloudinary.com")) {
            cloudinaryUploader.getOptimizedUrl(url, width, height, quality)
        } else {
            url
        }
    }

    SubcomposeAsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(optimizedUrl)
            .crossfade(true)
            .build(),
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale,
        loading = {
            if (showLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        },
        error = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.errorContainer),
                contentAlignment = Alignment.Center
            ) {
            }
        }
    )
}

@Composable
fun CloudinaryThumbnail(
    url: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: Int = 200
) {
    val context = LocalContext.current
    val cloudinaryUploader = remember { CloudinaryUploader(context) }
    val thumbnailUrl = remember(url, size) {
        if (url.contains("cloudinary.com")) {
            cloudinaryUploader.getThumbnailUrl(url, size)
        } else {
            url
        }
    }

    SubcomposeAsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(thumbnailUrl)
            .crossfade(true)
            .build(),
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = ContentScale.Crop,
        loading = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
        }
    )
}

@Composable
fun CloudinaryImageWithBlurPlaceholder(
    url: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    var isLoaded by remember { mutableStateOf(false) }
    val blurUrl = remember(url) {
        if (url.contains("cloudinary.com")) {
            url.replace("/upload/", "/upload/w_50,e_blur:1000,q_auto:low/")
        } else {
            url
        }
    }

    Box(modifier = modifier) {
        if (!isLoaded) {
            SubcomposeAsyncImage(
                model = blurUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = contentScale
            )
        }

        SubcomposeAsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(url)
                .crossfade(300)
                .listener(
                    onSuccess = { _, _ -> isLoaded = true }
                )
                .build(),
            contentDescription = contentDescription,
            modifier = Modifier.fillMaxSize(),
            contentScale = contentScale
        )
    }
}