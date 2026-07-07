package com.oqba26.abzarforoush.ui.theme

import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView

private val PurpleDarkScheme = darkColorScheme(primary = Purple80, secondary = PurpleGrey80, tertiary = Pink80)
private val PurpleLightScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40,
    onPrimary = Color.White,
    surface = Color.White,
    onSurface = Color.Black
)

private val BlueLightScheme = lightColorScheme(
    primary = BluePrimary,
    onPrimary = Color.White,
    secondary = BlueSecondary,
    onSecondary = Color.White,
    tertiary = BlueTertiary,
    surface = Color.White,
    onSurface = Color.Black,
    primaryContainer = Color(0xFFE3F2FD),
    onPrimaryContainer = BluePrimary,
    secondaryContainer = Color(0xFFE1F5FE),
    onSecondaryContainer = BlueSecondary
)

private val GreenLightScheme = lightColorScheme(
    primary = GreenPrimary,
    onPrimary = Color.White,
    secondary = GreenSecondary,
    onSecondary = Color.White,
    tertiary = GreenTertiary,
    surface = Color.White,
    onSurface = Color.Black,
    primaryContainer = Color(0xFFE8F5E9),
    onPrimaryContainer = GreenPrimary,
    secondaryContainer = Color(0xFFF1F8E9),
    onSecondaryContainer = GreenSecondary
)

@Composable
fun AbzarForoushTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    themeName: String = "Purple",
    fontFamily: androidx.compose.ui.text.font.FontFamily = Vazirmatn,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        else -> when (themeName) {
            "Blue" -> BlueLightScheme
            "Green" -> GreenLightScheme
            else -> if (darkTheme) PurpleDarkScheme else PurpleLightScheme
        }
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val context = view.context
            if (context is ComponentActivity) {
                // Force white icons (SystemBarStyle.dark) because we have a colored (Purple/Blue/Green) TopAppBar
                context.enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.dark(colorScheme.primary.toArgb()),
                    navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                )
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = getTypography(fontFamily),
        content = content
    )
}
