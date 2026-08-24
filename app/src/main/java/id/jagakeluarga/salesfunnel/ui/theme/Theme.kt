package id.jagakeluarga.salesfunnel.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val TealPrimary = Color(0xFF00695C)
private val TealDark = Color(0xFF004D40)
private val TealSoft = Color(0xFFD9EEEA)
private val TealPale = Color(0xFFEAF6F3)
private val TealAccent = Color(0xFF2A9D8F)
private val WarmWhite = Color(0xFFFFFEFC)
private val Ink = Color(0xFF173B36)

private val CozyColors = lightColorScheme(
    primary = TealPrimary,
    onPrimary = Color.White,
    primaryContainer = TealSoft,
    onPrimaryContainer = TealDark,
    secondary = TealAccent,
    onSecondary = Color.White,
    secondaryContainer = TealPale,
    onSecondaryContainer = TealDark,
    tertiary = Color(0xFFB4874A),
    onTertiary = Color.White,
    background = TealPale,
    onBackground = Ink,
    surface = WarmWhite,
    onSurface = Ink,
    surfaceVariant = Color(0xFFE3F0EE),
    onSurfaceVariant = Color(0xFF52706A),
)

@Composable
fun SalesFunnelTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(colorScheme = CozyColors, content = content)
}
