package com.nikoladx.trailator.services.cloudinary

import android.content.Context
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import androidx.core.net.toUri

class CloudinaryUploader(private val context: Context) {
    suspend fun uploadImage(
        uri: String,
        folder: String = "trail_objects"
    ): String = suspendCancellableCoroutine { continuation ->
        val tempFilePath: String

        try {
            tempFilePath = copyUriToFile(uri)
        } catch (e: Exception) {
            continuation.resumeWithException(e)
            return@suspendCancellableCoroutine
        }

        val requestId = MediaManager.get().upload(tempFilePath)
            .unsigned(CloudinaryConfig.UPLOAD_PRESET)
            .option("folder", folder)
            .option("resource_type", "image")
            .callback(object: UploadCallback {
                override fun onStart(requestId: String?) {
                    // Upload started
                }

                override fun onProgress(
                    requestId: String?,
                    bytes: Long,
                    totalBytes: Long
                ) {
                    // Upload progress
                }

                override fun onSuccess(
                    requestId: String?,
                    resultData: Map<*, *>?
                ) {
                    java.io.File(tempFilePath).delete()
                    val secureUrl = resultData?.get("secure_url") as? String
                    if (secureUrl != null) {
                        continuation.resume(secureUrl)
                    } else {
                        continuation.resumeWithException(
                            Exception("Failed to get secure URL from Cloudinary")
                        )
                    }
                }

                override fun onError(
                    requestId: String?,
                    error: ErrorInfo?
                ) {
                    java.io.File(tempFilePath).delete()
                    continuation.resumeWithException(
                        Exception("Cloudinary upload failed: ${error?.description}")
                    )
                }

                override fun onReschedule(
                    requestId: String?,
                    error: ErrorInfo?
                ) {
                    // Upload rescheduled
                }
            })
            .dispatch()

        continuation.invokeOnCancellation {
            MediaManager.get().cancelRequest(requestId)
            java.io.File(tempFilePath).delete()
        }
    }

    suspend fun uploadMultipleImages(
        uris: List<String>,
        folder: String = "trail_objects"
    ): List<String> {
        val uploadedUrls = mutableListOf<String>()

        for (uri in uris) {
            try {
                val url = uploadImage(uri, folder)
                uploadedUrls.add(url)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return uploadedUrls
    }

    fun getOptimizedUrl(
        originalUrl: String,
        width: Int = 800,
        height: Int = 600,
        quality: String = "auto"
    ): String {
        return originalUrl.replace(
            "/upload/",
            "/upload/w_$width,h_$height,c_fill,q_$quality/"
        )
    }

    fun getThumbnailUrl(originalUrl: String, size: Int = 200): String {
        return originalUrl.replace(
            "/upload/",
            "/upload/w_$size,h_$size,c_thumb,g_face/"
        )
    }

    private fun copyUriToFile(uri: String): String {
        val inputStream = context.contentResolver.openInputStream(uri.toUri())
            ?: throw Exception("Failed to open input stream for URI: $uri")

        val tempFile = java.io.File.createTempFile("upload", ".jpg", context.cacheDir)

        val outputStream = java.io.FileOutputStream(tempFile)
        inputStream.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }

        return tempFile.absolutePath
    }
}