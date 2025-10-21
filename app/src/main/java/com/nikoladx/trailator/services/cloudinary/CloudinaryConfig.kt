package com.nikoladx.trailator.services.cloudinary

import android.content.Context
import com.cloudinary.android.MediaManager
import com.nikoladx.trailator.BuildConfig

object CloudinaryConfig {
    private var isInitialized = false

    private const val CLOUD_NAME = BuildConfig.CLOUDINARY_CLOUD_NAME
    private const val API_KEY = BuildConfig.CLOUDINARY_API_KEY
    private const val API_SECRET = BuildConfig.CLOUDINARY_API_SECRET
    const val UPLOAD_PRESET = BuildConfig.CLOUDINARY_UPLOAD_PRESET

    fun initialize(context: Context) {
        if (!isInitialized) {
            val config = mapOf(
                "cloud_name" to CLOUD_NAME,
                "api_key" to API_KEY,
                "api_secret" to API_SECRET,
                "secure" to true
            )

            MediaManager.init(context, config)
            isInitialized = true
        }
    }
}