package com.nikoladx.trailator.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = GruvboxDarkYellow,
    onPrimary = GruvboxDarkBg0,
    primaryContainer = GruvboxDarkOrange,
    onPrimaryContainer = GruvboxDarkFg0,

    secondary = GruvboxDarkAqua,
    onSecondary = GruvboxDarkBg0,
    secondaryContainer = GruvboxDarkBlue,
    onSecondaryContainer = GruvboxDarkFg0,

    tertiary = GruvboxDarkPurple,
    onTertiary = GruvboxDarkBg0,
    tertiaryContainer = GruvboxDarkPurple,
    onTertiaryContainer = GruvboxDarkFg0,

    error = GruvboxDarkRed,
    onError = GruvboxDarkBg0,
    errorContainer = GruvboxDarkRed,
    onErrorContainer = GruvboxDarkFg0,

    background = GruvboxDarkBg0,
    onBackground = GruvboxDarkFg1,

    surface = GruvboxDarkBg0,
    onSurface = GruvboxDarkFg1,
    surfaceVariant = GruvboxDarkBg1,
    onSurfaceVariant = GruvboxDarkFg2,

    outline = GruvboxDarkBg4,
    outlineVariant = GruvboxDarkBg3,

    surfaceContainer = GruvboxDarkBg1,
    surfaceContainerHigh = GruvboxDarkBg2,
    surfaceContainerHighest = GruvboxDarkBg3,
)

private val LightColorScheme = lightColorScheme(
    primary = GruvboxLightOrange,
    onPrimary = GruvboxLightBg0,
    primaryContainer = GruvboxLightYellow,
    onPrimaryContainer = GruvboxLightFg0,

    secondary = GruvboxLightAqua,
    onSecondary = GruvboxLightBg0,
    secondaryContainer = GruvboxLightBlue,
    onSecondaryContainer = GruvboxLightFg0,

    tertiary = GruvboxLightPurple,
    onTertiary = GruvboxLightBg0,
    tertiaryContainer = GruvboxLightPurple,
    onTertiaryContainer = GruvboxLightFg0,

    error = GruvboxLightRed,
    onError = GruvboxLightBg0,
    errorContainer = GruvboxLightRed,
    onErrorContainer = GruvboxLightFg0,

    background = GruvboxLightBg0,
    onBackground = GruvboxLightFg1,

    surface = GruvboxLightBg0,
    onSurface = GruvboxLightFg1,
    surfaceVariant = GruvboxLightBg1,
    onSurfaceVariant = GruvboxLightFg2,

    outline = GruvboxLightBg4,
    outlineVariant = GruvboxLightBg3,

    surfaceContainer = GruvboxLightBg1,
    surfaceContainerHigh = GruvboxLightBg2,
    surfaceContainerHighest = GruvboxLightBg3,
)

@Composable
fun TrailatorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}