package com.example.animalwiki.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// 浅色主题配色
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF2E7D32),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE8F5E9),
    onPrimaryContainer = Color(0xFF1B5E20),
    secondary = Color(0xFF558B2F),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDDECD7),
    onSecondaryContainer = Color(0xFF33691E),
    tertiary = Color(0xFF827717),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFF9FBE7),
    onTertiaryContainer = Color(0xFF5C5600),
    background = Color(0xFFFAFAFA),
    onBackground = Color(0xFF1C1B1F),
    surface = Color.White,
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFE8E8E8),
    onSurfaceVariant = Color(0xFF49454F),
    outline = Color(0xFF79747E),
    error = Color(0xFFB3261E),
    onError = Color.White,
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B)
)

// 深色主题配色
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF81C784),
    onPrimary = Color(0xFF1B5E20),
    primaryContainer = Color(0xFF2E7D32),
    onPrimaryContainer = Color(0xFFC8E6C9),
    secondary = Color(0xFF9CCC65),
    onSecondary = Color(0xFF33691E),
    secondaryContainer = Color(0xFF558B2F),
    onSecondaryContainer = Color(0xFFDDECD7),
    tertiary = Color(0xFFD4E157),
    onTertiary = Color(0xFF5C5600),
    tertiaryContainer = Color(0xFF827717),
    onTertiaryContainer = Color(0xFFF9FBE7),
    background = Color(0xFF121212),
    onBackground = Color(0xFFE6E1E5),
    surface = Color(0xFF1E1E1E),
    onSurface = Color(0xFFE6E1E5),
    surfaceVariant = Color(0xFF2D2D2D),
    onSurfaceVariant = Color(0xFFCAC4D0),
    outline = Color(0xFF938F99),
    error = Color(0xFFF2B8B5),
    onError = Color(0xFF601410),
    errorContainer = Color(0xFF8C1D18),
    onErrorContainer = Color(0xFFF9DEDC)
)

// 主题模式枚举
enum class ThemeMode(val label: String) {
    LIGHT("浅色模式"),
    DARK("深色模式"),
    SYSTEM("跟随系统")
}

@Composable
fun AnimalWikiTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// 扩展属性，方便获取常用颜色
object AnimalWikiColors {
    val success: Color @Composable get() = Color(0xFF4CAF50)
    val warning: Color @Composable get() = Color(0xFFFF9800)
    val favorite: Color @Composable get() = Color(0xFFE91E63)
    val cardSurfaceLight: Color @Composable get() = Color(0xFFFFEBEE)
    val cardSurfaceDark: Color @Composable get() = Color(0xFFE3F2FD)
}
