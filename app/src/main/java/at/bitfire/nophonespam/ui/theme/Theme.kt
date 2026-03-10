package at.bitfire.nophonespam.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFFFFC107),
    onPrimary = Color.Black,
    primaryContainer = Color(0xFFFFECB3),
    onPrimaryContainer = Color(0xFF241A00),
    secondary = Color(0xFFFF5722),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFDBCF),
    onSecondaryContainer = Color(0xFF3B0A00),
    background = Color(0xFFFFFBF5),
    onBackground = Color(0xFF1C1B1F),
    surface = Color(0xFFFFFBF5),
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFF0E6D2),
    onSurfaceVariant = Color(0xFF4D4639),
)

@Composable
fun NoPhoneSpamTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        content = content
    )
}
