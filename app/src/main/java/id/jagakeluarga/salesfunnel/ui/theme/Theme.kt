package id.jagakeluarga.salesfunnel.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CozyTerracotta = Color(0xFFC9785C)
private val CozyTerracottaDark = Color(0xFFA95742)
private val CozyCream = Color(0xFFFFFBF5)
private val CozySurface = Color(0xFFFFFEFC)
private val CozyPeach = Color(0xFFFFE6D8)
private val CozySage = Color(0xFFDDE9D6)
private val CozyInk = Color(0xFF4A403B)

private val CozyColors = lightColorScheme(
    primary = CozyTerracotta,
    onPrimary = Color.White,
    primaryContainer = CozyPeach,
    onPrimaryContainer = CozyTerracottaDark,
    secondary = Color(0xFF8FAF86),
    onSecondary = Color.White,
    secondaryContainer = CozySage,
    onSecondaryContainer = Color(0xFF30422C),
    tertiary = Color(0xFFD7A957),
    onTertiary = Color.White,
    background = CozyCream,
    onBackground = CozyInk,
    surface = CozySurface,
    onSurface = CozyInk,
    surfaceVariant = Color(0xFFF4EDE5),
    onSurfaceVariant = Color(0xFF756A63),
)

@Composable
fun SalesFunnelTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(colorScheme = CozyColors, content = content)
}
