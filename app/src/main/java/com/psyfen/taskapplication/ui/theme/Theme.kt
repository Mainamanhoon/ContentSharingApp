package com.psyfen.taskapplication.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

val MainColor = Color(0xFF101322)
val Orange = Color(0xFFfd511e)
val DarkBlue = Color(0xFF1c213c)
val OffWhite = Color(0xFFC6CBC5)
val Red = Color(0xFFFF3333)

private val DarkColorScheme = darkColorScheme(
    primary = Orange,
    secondary = DarkBlue,
    tertiary = OffWhite,
    background = MainColor,
    surface = DarkBlue,
    error = Red,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
)

private val LightColorScheme = lightColorScheme(
    primary = Orange,
    secondary = DarkBlue,
    tertiary = OffWhite,
    background = Color.White,
    surface = Color.White,
    error = Red,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = MainColor,
    onBackground = MainColor,
    onSurface = MainColor,
)
@Composable
fun TaskApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
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