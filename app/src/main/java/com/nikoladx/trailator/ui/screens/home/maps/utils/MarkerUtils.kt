package com.nikoladx.trailator.ui.screens.home.maps.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.nikoladx.trailator.R
import com.nikoladx.trailator.data.models.TrailObjectType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL
import androidx.core.graphics.createBitmap
import androidx.core.graphics.scale
import androidx.core.graphics.toColorInt

object MarkerUtils {
    private const val MARKER_SIZE = 120
    private const val BORDER_WIDTH = 6f

    suspend fun createMarkerBitmap(
        context: Context,
        imageUrl: String?,
        type: TrailObjectType
    ): BitmapDescriptor = withContext(Dispatchers.IO) {
        val bitmap = if (!imageUrl.isNullOrEmpty()) {
            loadImageFromUrl(imageUrl) ?: createPlaceholderBitmap(context, type)
        } else {
            createPlaceholderBitmap(context, type)
        }

        val circularBitmap = createCircularBitmap(bitmap, type)
        BitmapDescriptorFactory.fromBitmap(circularBitmap)
    }

    private fun loadImageFromUrl(url: String): Bitmap? {
        return try {
            val connection = URL(url).openConnection()
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            connection.connect()
            BitmapFactory.decodeStream(connection.getInputStream())
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun createPlaceholderBitmap(context: Context, type: TrailObjectType): Bitmap {
        val drawableRes = when (type) {
            TrailObjectType.TRAIL -> R.drawable.ic_trail_placeholder
            TrailObjectType.VIEWPOINT -> R.drawable.ic_viewpoint_placeholder
            TrailObjectType.WATER_SOURCE -> R.drawable.ic_water_placeholder
            TrailObjectType.SHELTER -> R.drawable.ic_shelter_placeholder
            TrailObjectType.LANDMARK -> R.drawable.ic_landmark_placeholder
            TrailObjectType.CAMPING_SPOT -> R.drawable.ic_camping_placeholder
            TrailObjectType.DANGER_ZONE -> R.drawable.ic_danger_placeholder
        }

        return try {
            val drawable = ContextCompat.getDrawable(context, drawableRes)
            val bitmap = createBitmap(MARKER_SIZE, MARKER_SIZE)
            val canvas = Canvas(bitmap)
            drawable?.setBounds(0, 0, MARKER_SIZE, MARKER_SIZE)
            drawable?.draw(canvas)
            bitmap
        } catch (_: Exception) {
            createColoredPlaceholder(type)
        }
    }

    private fun createColoredPlaceholder(type: TrailObjectType): Bitmap {
        val bitmap = createBitmap(MARKER_SIZE, MARKER_SIZE)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = getTypeColor(type)
            style = Paint.Style.FILL
        }

        val radius = MARKER_SIZE / 2f
        canvas.drawCircle(radius, radius, radius, paint)

        return bitmap
    }

    private fun createCircularBitmap(
        source: Bitmap,
        type: TrailObjectType,
    ): Bitmap {
        val size = MARKER_SIZE
        val output = createBitmap(size, size)
        val canvas = Canvas(output)

        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = getTypeColor(type)
            style = Paint.Style.FILL
        }
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, borderPaint)

        val innerRadius = (size / 2f) - BORDER_WIDTH

        val imageBitmap = createBitmap(size, size)
        val imageCanvas = Canvas(imageBitmap)

        val whitePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            style = Paint.Style.FILL
        }
        imageCanvas.drawCircle(size / 2f, size / 2f, innerRadius, whitePaint)

        val clipPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        }

        val scaledBitmap = source.scale(size, size)
        imageCanvas.drawBitmap(scaledBitmap, 0f, 0f, clipPaint)

        canvas.drawBitmap(imageBitmap, 0f, 0f, null)

        imageBitmap.recycle()
        if (scaledBitmap != source) {
            scaledBitmap.recycle()
        }

        return output
    }

    private fun getTypeColor(type: TrailObjectType): Int {
        return when (type) {
            TrailObjectType.TRAIL -> "#4CAF50".toColorInt() // Green
            TrailObjectType.VIEWPOINT -> "#9C27B0".toColorInt() // Purple
            TrailObjectType.WATER_SOURCE -> "#00BCD4".toColorInt() // Cyan
            TrailObjectType.SHELTER -> "#FF9800".toColorInt() // Orange
            TrailObjectType.LANDMARK -> "#FFEB3B".toColorInt() // Yellow
            TrailObjectType.CAMPING_SPOT -> "#E91E63".toColorInt() // Pink
            TrailObjectType.DANGER_ZONE -> "#F44336".toColorInt() // Red
        }
    }
}