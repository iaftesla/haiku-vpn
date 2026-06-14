package com.haiku.vpn.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.unit.sp
import com.haiku.vpn.R

// Google Fonts Provider Config
val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

// Premium Japanese Mincho Serif Font for Haiku poems
val ShipporiMinchoFont = GoogleFont("Shippori Mincho")
val PoeticFontFamily = FontFamily(
    Font(googleFont = ShipporiMinchoFont, fontProvider = provider, weight = FontWeight.Light),
    Font(googleFont = ShipporiMinchoFont, fontProvider = provider, weight = FontWeight.Normal)
)

// Premium geometric Sans-Serif Font for interface controls & readouts
val PlusJakartaSansFont = GoogleFont("Plus Jakarta Sans")
val TechnicalFontFamily = FontFamily(
    Font(googleFont = PlusJakartaSansFont, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = PlusJakartaSansFont, fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = PlusJakartaSansFont, fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = PlusJakartaSansFont, fontProvider = provider, weight = FontWeight.Bold)
)

// Wabi-Sabi Custom Palette (Light Theme)
val CreamBg = Color(0xFFFAF6EE)        // Warm soft natural washi paper tone
val SumizomeCharcoal = Color(0xFF1E1D1B) // Deep warm charcoal ink tone
val MossGreen = Color(0xFF6B7F67)        // Earthy, muted moss green (Koke-iro)
val SakuraPink = Color(0xFFDCAEAE)       // Pale ash/sakura pink with grey undertone (Sakura-iro)
val MutedText = Color(0xFF8C857B)        // Warm stone/clay grey
val DividerGray = Color(0xFFECE6DA)      // Soft divider tone matching paper fibers
val CardCream = Color(0xFFF3EDE2)        // Harmonious container color for light theme

// Dark Theme Colors (Deep charcoal and warm textures)
val DarkBg = Color(0xFF161513)           // Soft charcoal background
val DarkCard = Color(0xFF22201D)         // Warm, deep slate-charcoal container
val DarkText = Color(0xFFECE6DB)         // Warm off-white text (washi texture)
val DarkMutedText = Color(0xFF99938B)    // Dusty charcoal-grey for subtext
val DarkDivider = Color(0xFF2D2A26)      // Warm dark divider

private val LightColorScheme = lightColorScheme(
    primary = SumizomeCharcoal,
    secondary = MossGreen,
    tertiary = SakuraPink,
    background = CreamBg,
    surface = CardCream,
    onPrimary = CreamBg,
    onSecondary = SumizomeCharcoal,
    onBackground = SumizomeCharcoal,
    onSurface = SumizomeCharcoal
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkText,
    secondary = MossGreen,
    tertiary = SakuraPink,
    background = DarkBg,
    surface = DarkCard,
    onPrimary = DarkBg,
    onSecondary = DarkText,
    onBackground = DarkText,
    onSurface = DarkText
)

// Elegant Typography using Shippori Mincho for Poetic sections and Plus Jakarta Sans for interface
val WabiSabiTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = PoeticFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.5.sp
    ),
    displayMedium = TextStyle(
        fontFamily = PoeticFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 20.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.5.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = PoeticFontFamily,
        fontWeight = FontWeight.Light,
        fontSize = 24.sp,
        lineHeight = 30.sp
    ),
    titleLarge = TextStyle(
        fontFamily = PoeticFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 1.sp
    ),
    titleMedium = TextStyle(
        fontFamily = TechnicalFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = TechnicalFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.2.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = TechnicalFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.2.sp
    ),
    labelMedium = TextStyle(
        fontFamily = TechnicalFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 15.sp,
        letterSpacing = 0.5.sp
    )
)

@Composable
fun HaikuTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = WabiSabiTypography,
        content = content
    )
}


