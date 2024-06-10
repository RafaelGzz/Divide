package com.ragl.divide.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font

import androidx.compose.ui.text.googlefonts.GoogleFont
import com.ragl.divide.R

val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

val bodyFontFamily = FontFamily(
    Font(
        googleFont = GoogleFont("Poppins"),
        fontProvider = provider,
    )
)

//val displayFontFamily = FontFamily(
//    Font(
//        googleFont = GoogleFont("Bitter"),
//        fontProvider = provider,
//    )
//)

val displayFontFamily = FontFamily(
    Font(R.font.grtskpeta_regular, FontWeight.Normal),
    Font(R.font.grtskpeta_thin, FontWeight.Thin),
    Font(R.font.grtskpeta_thinitalic, FontWeight.Thin, FontStyle.Italic),
    Font(R.font.grtskpeta_italic, FontWeight.Normal, FontStyle.Italic),
    Font(R.font.grtskpeta_bold, FontWeight.Bold),
    Font(R.font.grtskpeta_bolditalic, FontWeight.Bold, FontStyle.Italic),
    Font(R.font.grtskpeta_semibold, FontWeight.SemiBold),
    Font(R.font.grtskpeta_semibolditalic, FontWeight.SemiBold, FontStyle.Italic),
    Font(R.font.grtskpeta_light, FontWeight.Light),
    Font(R.font.grtskpeta_lightitalic, FontWeight.Light, FontStyle.Italic),
    Font(R.font.grtskpeta_mediumitalic, FontWeight.Medium, FontStyle.Italic),
    Font(R.font.grtskpeta_medium, FontWeight.Medium),
    Font(R.font.grtskpeta_extralightitalic, FontWeight.ExtraLight, FontStyle.Italic),
    Font(R.font.grtskpeta_extralight, FontWeight.ExtraLight)
)

// Default Material 3 typography values
val baseline = Typography()

val AppTypography = Typography(
    displayLarge = baseline.displayLarge.copy(fontFamily = displayFontFamily),
    displayMedium = baseline.displayMedium.copy(fontFamily = displayFontFamily),
    displaySmall = baseline.displaySmall.copy(fontFamily = displayFontFamily),
    headlineLarge = baseline.headlineLarge.copy(fontFamily = displayFontFamily),
    headlineMedium = baseline.headlineMedium.copy(fontFamily = displayFontFamily),
    headlineSmall = baseline.headlineSmall.copy(fontFamily = displayFontFamily),
    titleLarge = baseline.titleLarge.copy(fontFamily = displayFontFamily),
    titleMedium = baseline.titleMedium.copy(fontFamily = displayFontFamily),
    titleSmall = baseline.titleSmall.copy(fontFamily = displayFontFamily),

    bodyLarge = baseline.bodyLarge.copy(fontFamily = displayFontFamily),
    bodyMedium = baseline.bodyMedium.copy(fontFamily = displayFontFamily),
    bodySmall = baseline.bodySmall.copy(fontFamily = displayFontFamily),
    labelLarge = baseline.labelLarge.copy(fontFamily = displayFontFamily),
    labelMedium = baseline.labelMedium.copy(fontFamily = displayFontFamily),
    labelSmall = baseline.labelSmall.copy(fontFamily = displayFontFamily),
)

