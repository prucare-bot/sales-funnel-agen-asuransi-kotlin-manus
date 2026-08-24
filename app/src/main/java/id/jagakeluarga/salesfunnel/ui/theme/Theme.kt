package id.jagakeluarga.salesfunnel.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

enum class AppThemeColor(val label: String) {
    HIJAU("Hijau"),
    BIRU("Biru"),
    MERAH("Merah"),
}

private val GreenPrimary = Color(0xFF00695C)
private val GreenSecondary = Color(0xFF2A9D8F)
private val GreenContainer = Color(0xFFD9EEEA)
private val GreenBackground = Color(0xFFEAF6F3)

private val BluePrimary = Color(0xFF1565C0)
private val BlueSecondary = Color(0xFF42A5F5)
private val BlueContainer = Color(0xFFDCEBFA)
private val BlueBackground = Color(0xFFF0F7FF)

private val RedPrimary = Color(0xFFB3261E)
private val RedSecondary = Color(0xFFE76F51)
private val RedContainer = Color(0xFFFFE1DC)
private val RedBackground = Color(0xFFFFF5F3)

private val Ink = Color(0xFF243431)
private val WarmWhite = Color(0xFFFFFEFC)

private fun appColorScheme(theme: AppThemeColor) = when (theme) {
    AppThemeColor.HIJAU -> lightColorScheme(
        primary = GreenPrimary, onPrimary = Color.White,
        primaryContainer = GreenContainer, onPrimaryContainer = Color(0xFF004D40),
        secondary = GreenSecondary, onSecondary = Color.White,
        secondaryContainer = GreenBackground, onSecondaryContainer = Color(0xFF004D40),
        tertiary = Color(0xFFB4874A), onTertiary = Color.White,
        background = GreenBackground, onBackground = Ink,
        surface = WarmWhite, onSurface = Ink,
        surfaceVariant = Color(0xFFE3F0EE), onSurfaceVariant = Color(0xFF52706A),
    )
    AppThemeColor.BIRU -> lightColorScheme(
        primary = BluePrimary, onPrimary = Color.White,
        primaryContainer = BlueContainer, onPrimaryContainer = Color(0xFF0D47A1),
        secondary = BlueSecondary, onSecondary = Color.White,
        secondaryContainer = BlueBackground, onSecondaryContainer = Color(0xFF0D47A1),
        tertiary = Color(0xFF607D8B), onTertiary = Color.White,
        background = BlueBackground, onBackground = Color(0xFF26384A),
        surface = WarmWhite, onSurface = Color(0xFF26384A),
        surfaceVariant = Color(0xFFE6F0FA), onSurfaceVariant = Color(0xFF526B83),
    )
    AppThemeColor.MERAH -> lightColorScheme(
        primary = RedPrimary, onPrimary = Color.White,
        primaryContainer = RedContainer, onPrimaryContainer = Color(0xFF8C1D18),
        secondary = RedSecondary, onSecondary = Color.White,
        secondaryContainer = RedBackground, onSecondaryContainer = Color(0xFF8C1D18),
        tertiary = Color(0xFFC28E3D), onTertiary = Color.White,
        background = RedBackground, onBackground = Color(0xFF4A2925),
        surface = WarmWhite, onSurface = Color(0xFF4A2925),
        surfaceVariant = Color(0xFFF8E9E5), onSurfaceVariant = Color(0xFF765954),
    )
}

@Composable
fun SalesFunnelTheme(
    theme: AppThemeColor = AppThemeColor.HIJAU,
    darkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = appColorScheme(theme),
        shapes = Shapes(
            small = RoundedCornerShape(12.dp),
            medium = RoundedCornerShape(20.dp),
            large = RoundedCornerShape(28.dp),
            extraLarge = RoundedCornerShape(32.dp),
        ),
        content = content,
    )
}
