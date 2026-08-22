package id.jagakeluarga.salesfunnel.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val BrandRed = Color(0xFFE04E39)
private val BrandRedDark = Color(0xFFB33827)

private val LightColors = lightColorScheme(
    primary = BrandRed,
    onPrimary = Color.White,
    secondary = BrandRedDark,
    background = Color(0xFFFDFDFD),
    surface = Color.White,
)

private val DarkColors = darkColorScheme(
    primary = BrandRed,
    onPrimary = Color.White,
    secondary = BrandRedDark,
)

@Composable
fun SalesFunnelTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(colorScheme = colors, content = content)
}
