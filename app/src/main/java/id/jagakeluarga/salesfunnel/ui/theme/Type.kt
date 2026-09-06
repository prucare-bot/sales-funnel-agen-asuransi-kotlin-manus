package id.jagakeluarga.salesfunnel.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import id.jagakeluarga.salesfunnel.R

/**
 * Redesain 2026: dua keluarga font supaya tampilan tidak monoton -
 * satu untuk judul/heading (berkarakter, tegas), satu lagi untuk isi/label
 * (ringkas, mudah dibaca di ruang sempit). Diambil lewat Google Fonts
 * Downloadable Fonts API (via Google Play Services), jadi tidak perlu
 * membundel file .ttf ke dalam APK.
 *
 * - Heading (display/headline/titleLarge): Hanken Grotesk - lebih
 *   berkarakter, dipakai untuk judul layar dan angka besar (mis. ring
 *   target closing di Beranda).
 * - Isi & label (titleMedium ke bawah): Inter - ringkas, dipilih
 *   sebelumnya karena Plus Jakarta Sans (percobaan pertama) terlalu lebar
 *   dan membuat teks di ruang sempit (badge tahap, stat ringkasan) mudah
 *   terpotong.
 *
 * Catatan: kalau perangkat tidak punya Google Play Services (atau saat font
 * pertama kali diminta belum sempat diunduh), Compose otomatis jatuh ke font
 * sistem tanpa crash - jadi ini aman meski koneksi sedang lambat.
 */
private val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs,
)

private val interFont = GoogleFont("Inter")
private val hankenGroteskFont = GoogleFont("Hanken Grotesk")

private val BodyFontFamily = FontFamily(
    Font(googleFont = interFont, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = interFont, fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = interFont, fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = interFont, fontProvider = provider, weight = FontWeight.Bold),
)

private val HeadingFontFamily = FontFamily(
    Font(googleFont = hankenGroteskFont, fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = hankenGroteskFont, fontProvider = provider, weight = FontWeight.Bold),
    Font(googleFont = hankenGroteskFont, fontProvider = provider, weight = FontWeight.ExtraBold),
)

// Letter-spacing dibuat lebih rapat dari baseline Material3 (yang dirancang untuk
// Roboto) supaya label pendek di ruang sempit (badge tahap, stat ringkasan, dsb)
// tidak mudah terpotong.
val SalesFunnelTypography = Typography(
    displayLarge = TextStyle(fontFamily = HeadingFontFamily, fontWeight = FontWeight.ExtraBold, fontSize = 57.sp, lineHeight = 64.sp, letterSpacing = (-0.25).sp),
    displayMedium = TextStyle(fontFamily = HeadingFontFamily, fontWeight = FontWeight.Bold, fontSize = 45.sp, lineHeight = 52.sp),
    displaySmall = TextStyle(fontFamily = HeadingFontFamily, fontWeight = FontWeight.Bold, fontSize = 36.sp, lineHeight = 44.sp),
    headlineLarge = TextStyle(fontFamily = HeadingFontFamily, fontWeight = FontWeight.Bold, fontSize = 32.sp, lineHeight = 40.sp),
    headlineMedium = TextStyle(fontFamily = HeadingFontFamily, fontWeight = FontWeight.Bold, fontSize = 28.sp, lineHeight = 36.sp),
    headlineSmall = TextStyle(fontFamily = HeadingFontFamily, fontWeight = FontWeight.Bold, fontSize = 24.sp, lineHeight = 32.sp),
    titleLarge = TextStyle(fontFamily = HeadingFontFamily, fontWeight = FontWeight.Bold, fontSize = 22.sp, lineHeight = 28.sp),
    titleMedium = TextStyle(fontFamily = BodyFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.sp),
    titleSmall = TextStyle(fontFamily = BodyFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.sp),
    bodyLarge = TextStyle(fontFamily = BodyFontFamily, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.15.sp),
    bodyMedium = TextStyle(fontFamily = BodyFontFamily, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp),
    bodySmall = TextStyle(fontFamily = BodyFontFamily, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.1.sp),
    labelLarge = TextStyle(fontFamily = BodyFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.sp),
    labelMedium = TextStyle(fontFamily = BodyFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.sp),
    labelSmall = TextStyle(fontFamily = BodyFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, lineHeight = 16.sp, letterSpacing = 0.sp),
)
