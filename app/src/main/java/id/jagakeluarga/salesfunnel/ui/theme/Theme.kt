package id.jagakeluarga.salesfunnel.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightBlue = Color(0xFF5B9BD5)
private val LightBlueDark = Color(0xFF2F6FAD)
private val PaleBlue = Color(0xFFF4F9FD)
private val PaleBlueSurface = Color(0xFFFFFFFF)
private val DarkBlue = Color(0xFF9CCBEE)
private val DarkBlueContainer = Color(0xFF1D3A52)

private val LightColors = lightColorScheme(
    primary = LightBlueDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDCEEFF),
    onPrimaryContainer = Color(0xFF0B2940),
    secondary = LightBlue,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE3F2FD),
    onSecondaryContainer = Color(0xFF102A3A),
    background = PaleBlue,
    surface = PaleBlueSurface,
)

private val DarkColors = darkColorScheme(
    primary = DarkBlue,
    onPrimary = Color(0xFF07314D),
    primaryContainer = DarkBlueContainer,
    onPrimaryContainer = Color(0xFFDCEEFF),
    secondary = Color(0xFF9DD4F5),
    onSecondary = Color(0xFF07314D),
    background = Color(0xFF10171D),
    surface = Color(0xFF161F27),
)

@Composable
fun SalesFunnelTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(colorScheme = colors, content = content)
}
