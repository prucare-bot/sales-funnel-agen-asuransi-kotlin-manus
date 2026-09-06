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

// Redesain 2026: palet "navy / teal / marigold" — lihat mockup redesign-semua-layar.html
private val GreenPrimary = Color(0xFF1C6E62)
private val GreenSecondary = Color(0xFF12253A)
private val GreenContainer = Color(0xFFE9F3F1)
private val GreenBackground = Color(0xFFF7F3EA)
private val GreenTertiary = Color(0xFFE4A335)
private val GreenNavyDeep = Color(0xFF0B1A2B)

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
        primaryContainer = GreenContainer, onPrimaryContainer = GreenNavyDeep,
        secondary = GreenSecondary, onSecondary = Color.White,
        secondaryContainer = GreenBackground, onSecondaryContainer = GreenSecondary,
        tertiary = GreenTertiary, onTertiary = GreenNavyDeep,
        background = GreenBackground, onBackground = Color(0xFF1E2A28),
        surface = WarmWhite, onSurface = Color(0xFF1E2A28),
        surfaceVariant = Color(0xFFEDE8DA), onSurfaceVariant = Color(0xFF6B7671),
        surfaceContainerLowest = Color(0xFFFFFFFF),
        surfaceContainerLow = Color(0xFFF3EFE3),
        surfaceContainer = Color(0xFFEFEBDD),
        surfaceContainerHigh = Color(0xFFE9E4D4),
        surfaceContainerHighest = Color(0xFFE3DDCB),
        outline = Color(0xFFB8B0A0),
        outlineVariant = Color(0xFFE6E0D2),
        inverseSurface = GreenNavyDeep,
        inverseOnSurface = GreenBackground,
        inversePrimary = Color(0xFFB9E0D6),
        scrim = Color(0xFF000000),
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
        surfaceContainerLowest = Color(0xFFFFFFFF),
        surfaceContainerLow = Color(0xFFEDF4FC),
        surfaceContainer = Color(0xFFE3EEFA),
        surfaceContainerHigh = Color(0xFFD9E8F7),
        surfaceContainerHighest = Color(0xFFCDE1F4),
        outline = Color(0xFFA3B7C9),
        outlineVariant = Color(0xFFDCE7F2),
        inverseSurface = Color(0xFF16324D),
        inverseOnSurface = BlueBackground,
        inversePrimary = Color(0xFFA9CDF2),
        scrim = Color(0xFF000000),
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
        surfaceContainerLowest = Color(0xFFFFFFFF),
        surfaceContainerLow = Color(0xFFFCEEEA),
        surfaceContainer = Color(0xFFFAE4DE),
        surfaceContainerHigh = Color(0xFFF6D9D1),
        surfaceContainerHighest = Color(0xFFF2CDC3),
        outline = Color(0xFFCBA69E),
        outlineVariant = Color(0xFFF0DBD4),
        inverseSurface = Color(0xFF4A2925),
        inverseOnSurface = RedBackground,
        inversePrimary = Color(0xFFF2B4AC),
        scrim = Color(0xFF000000),
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
        typography = SalesFunnelTypography,
        shapes = Shapes(
            small = RoundedCornerShape(12.dp),
            medium = RoundedCornerShape(20.dp),
            large = RoundedCornerShape(28.dp),
            extraLarge = RoundedCornerShape(32.dp),
        ),
        content = content,
    )
}
