package com.example.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

enum class AppThemeMode {
    NIGHT, // Gece Teması (Koyu)
    DAY    // Gündüz Teması (Göz Yormayan Pastel Aydınlık)
}

enum class AppAccentColor(val titleTr: String, val titleEn: String, val previewColor: Color) {
    TEAL("Medikal Turkuaz", "Medical Teal", Color(0xFF0D9488)),
    OCEAN("Okyanus Mavisi", "Ocean Blue", Color(0xFF0284C7)),
    EMERALD("Zümrüt Yeşili", "Bio Emerald", Color(0xFF059669)),
    VIOLET("Kraliyet Moru", "Royal Violet", Color(0xFF7C3AED))
}

object ThemeState {
    var themeMode by mutableStateOf(AppThemeMode.NIGHT)
    var accentColor by mutableStateOf(AppAccentColor.TEAL)
}

val isDayTheme: Boolean
    get() = ThemeState.themeMode == AppThemeMode.DAY

// 1. Zemin ve Kartlar (Saf Beyaz İçermeyen, Göz Yormayan Pastel Gündüz & Derin Gece Tonları)
val HealthBg: Color
    get() = if (isDayTheme) {
        when (ThemeState.accentColor) {
            AppAccentColor.TEAL -> Color(0xFFD5EBE7)
            AppAccentColor.OCEAN -> Color(0xFFD6E7F5)
            AppAccentColor.EMERALD -> Color(0xFFD6ECE1)
            AppAccentColor.VIOLET -> Color(0xFFE0DCF2)
        }
    } else {
        when (ThemeState.accentColor) {
            AppAccentColor.TEAL -> Color(0xFF041922)
            AppAccentColor.OCEAN -> Color(0xFF041726)
            AppAccentColor.EMERALD -> Color(0xFF041C18)
            AppAccentColor.VIOLET -> Color(0xFF0E1026)
        }
    }

val HealthSurface: Color
    get() = if (isDayTheme) {
        when (ThemeState.accentColor) {
            AppAccentColor.TEAL -> Color(0xFFE3F3F0)
            AppAccentColor.OCEAN -> Color(0xFFE4F0FA)
            AppAccentColor.EMERALD -> Color(0xFFE4F5EC)
            AppAccentColor.VIOLET -> Color(0xFFECE9F8)
        }
    } else {
        when (ThemeState.accentColor) {
            AppAccentColor.TEAL -> Color(0xFF092937)
            AppAccentColor.OCEAN -> Color(0xFF09253C)
            AppAccentColor.EMERALD -> Color(0xFF082C25)
            AppAccentColor.VIOLET -> Color(0xFF171A3A)
        }
    }

val HealthSurfaceVariant: Color
    get() = if (isDayTheme) {
        when (ThemeState.accentColor) {
            AppAccentColor.TEAL -> Color(0xFFC7E4DF)
            AppAccentColor.OCEAN -> Color(0xFFC6DFF3)
            AppAccentColor.EMERALD -> Color(0xFFC6E6D6)
            AppAccentColor.VIOLET -> Color(0xFFD3CEEC)
        }
    } else {
        when (ThemeState.accentColor) {
            AppAccentColor.TEAL -> Color(0xFF0F3A4C)
            AppAccentColor.OCEAN -> Color(0xFF0F3554)
            AppAccentColor.EMERALD -> Color(0xFF0E3E34)
            AppAccentColor.VIOLET -> Color(0xFF232752)
        }
    }

val HealthBorder: Color
    get() = if (isDayTheme) {
        when (ThemeState.accentColor) {
            AppAccentColor.TEAL -> Color(0xFF76C2B7)
            AppAccentColor.OCEAN -> Color(0xFF72B3E5)
            AppAccentColor.EMERALD -> Color(0xFF72C59E)
            AppAccentColor.VIOLET -> Color(0xFF9E8FE0)
        }
    } else {
        when (ThemeState.accentColor) {
            AppAccentColor.TEAL -> Color(0xFF155E75)
            AppAccentColor.OCEAN -> Color(0xFF1B5886)
            AppAccentColor.EMERALD -> Color(0xFF166553)
            AppAccentColor.VIOLET -> Color(0xFF4338CA)
        }
    }

// Grafik ve Özel Alan Zemin Renkleri (Gündüz Temasında Karanlık Durmaması İçin)
val ChartCanvasBg: Color
    get() = if (isDayTheme) {
        when (ThemeState.accentColor) {
            AppAccentColor.TEAL -> Color(0xFFD2E9E4)
            AppAccentColor.OCEAN -> Color(0xFFD3E6F5)
            AppAccentColor.EMERALD -> Color(0xFFD3EBE0)
            AppAccentColor.VIOLET -> Color(0xFFDDD8F2)
        }
    } else {
        Color(0xFF02131C)
    }

val ChartSlotBg: Color
    get() = if (isDayTheme) {
        when (ThemeState.accentColor) {
            AppAccentColor.TEAL -> Color(0xFFE8F6F3)
            AppAccentColor.OCEAN -> Color(0xFFE9F3FB)
            AppAccentColor.EMERALD -> Color(0xFFE9F7F0)
            AppAccentColor.VIOLET -> Color(0xFFF1EFFA)
        }
    } else {
        Color(0xFF021721).copy(alpha = 0.65f)
    }

val ChartSlotSelectedBg: Color
    get() = if (isDayTheme) {
        when (ThemeState.accentColor) {
            AppAccentColor.TEAL -> Color(0xFFBBE0DA)
            AppAccentColor.OCEAN -> Color(0xFFBCD9F0)
            AppAccentColor.EMERALD -> Color(0xFFBBE3CE)
            AppAccentColor.VIOLET -> Color(0xFFC9C2E8)
        }
    } else {
        Color(0xFF042430)
    }

val ChartTrackBg: Color
    get() = if (isDayTheme) {
        when (ThemeState.accentColor) {
            AppAccentColor.TEAL -> Color(0xFFB3D8D1)
            AppAccentColor.OCEAN -> Color(0xFFB4D1E8)
            AppAccentColor.EMERALD -> Color(0xFFB3DAC6)
            AppAccentColor.VIOLET -> Color(0xFFC2BBE0)
        }
    } else {
        Color(0xFF0F1E24)
    }

val FastingKpiBg: Color
    get() = if (isDayTheme) Color(0xFFCCF0E0) else Color(0xFF03251D)

val PostKpiBg: Color
    get() = if (isDayTheme) Color(0xFFF9D8E0) else Color(0xFF280E16)

val BolusKpiBg: Color
    get() = if (isDayTheme) Color(0xFFF7E4C4) else Color(0xFF281C07)

val BasalKpiBg: Color
    get() = if (isDayTheme) Color(0xFFCCE5F8) else Color(0xFF052230)

// Vurgu Kutuları İçindeki Kategori İkonları İçin Yüksek Kontrastlı Renk
val IconOnAccent: Color
    get() = Color(0xFFF8FAFC)

// 2. Canlı Sağlık İkon ve Vurgu Renkleri (Seçilen Tema Rengine Duyarlı)
val HealthTeal: Color
    get() = when (ThemeState.accentColor) {
        AppAccentColor.TEAL -> if (isDayTheme) Color(0xFF0D9488) else Color(0xFF0D9488)
        AppAccentColor.OCEAN -> if (isDayTheme) Color(0xFF0284C7) else Color(0xFF0284C7)
        AppAccentColor.EMERALD -> if (isDayTheme) Color(0xFF059669) else Color(0xFF059669)
        AppAccentColor.VIOLET -> if (isDayTheme) Color(0xFF6D28D9) else Color(0xFF6D28D9)
    }

val HealthTealBright: Color
    get() = when (ThemeState.accentColor) {
        AppAccentColor.TEAL -> if (isDayTheme) Color(0xFF0F766E) else Color(0xFF14B8A6)
        AppAccentColor.OCEAN -> if (isDayTheme) Color(0xFF0369A1) else Color(0xFF38BDF8)
        AppAccentColor.EMERALD -> if (isDayTheme) Color(0xFF047857) else Color(0xFF10B981)
        AppAccentColor.VIOLET -> if (isDayTheme) Color(0xFF5B21B6) else Color(0xFFA78BFA)
    }

val HealthMint: Color
    get() = when (ThemeState.accentColor) {
        AppAccentColor.TEAL -> if (isDayTheme) Color(0xFF115E59) else Color(0xFF2DD4BF)
        AppAccentColor.OCEAN -> if (isDayTheme) Color(0xFF075985) else Color(0xFF7DD3FC)
        AppAccentColor.EMERALD -> if (isDayTheme) Color(0xFF065F46) else Color(0xFF34D399)
        AppAccentColor.VIOLET -> if (isDayTheme) Color(0xFF4C1D95) else Color(0xFFC4B5FD)
    }

val HealthCyan: Color
    get() = if (isDayTheme) Color(0xFF0369A1) else Color(0xFF06B6D4)

val HealthCyanBright: Color
    get() = when (ThemeState.accentColor) {
        AppAccentColor.TEAL -> if (isDayTheme) Color(0xFF0F766E) else Color(0xFF38BDF8)
        AppAccentColor.OCEAN -> if (isDayTheme) Color(0xFF0369A1) else Color(0xFF38BDF8)
        AppAccentColor.EMERALD -> if (isDayTheme) Color(0xFF047857) else Color(0xFF34D399)
        AppAccentColor.VIOLET -> if (isDayTheme) Color(0xFF5B21B6) else Color(0xFFC4B5FD)
    }

// 3. İnsülin Türleri için Canlı Ayrım
val BasalVibrantBlue: Color
    get() = Color(0xFF2563EB)

val BasalContainerDark: Color
    get() = if (isDayTheme) Color(0xFFC9E2F8) else Color(0xFF1E3A8A)

val BasalCyan: Color
    get() = if (isDayTheme) Color(0xFF0369A1) else Color(0xFF38BDF8)

val BolusVibrantOrange: Color
    get() = Color(0xFFEA580C)

val BolusContainerDark: Color
    get() = if (isDayTheme) Color(0xFFF7E2C2) else Color(0xFF7C2D12)

val BolusGold: Color
    get() = if (isDayTheme) Color(0xFFB45309) else Color(0xFFFDBA74)

// 4. Durum, Rotasyon ve Uyarılar
val BioHealingGreen: Color
    get() = if (isDayTheme) Color(0xFF047857) else Color(0xFF10B981)

val BioGreenContainer: Color
    get() = if (isDayTheme) Color(0xFFC4EEDB) else Color(0xFF064E3B)

val BioGreenGlow: Color
    get() = if (isDayTheme) Color(0xFF065F46) else Color(0xFF34D399)

val WarningAmberHealth: Color
    get() = if (isDayTheme) Color(0xFFB45309) else Color(0xFFF59E0B)

val WarningAmberContainer: Color
    get() = if (isDayTheme) Color(0xFFF7E4BE) else Color(0xFF78350F)

val AlertPharmacyRuby: Color
    get() = if (isDayTheme) Color(0xFFBE123C) else Color(0xFFF43F5E)

val AlertRubyContainer: Color
    get() = if (isDayTheme) Color(0xFFF8D4DC) else Color(0xFF881337)

// 5. Yüksek Okunabilirlik Metin Renkleri
val TextHealthPrimary: Color
    get() = if (isDayTheme) Color(0xFF0F293A) else Color(0xFFF0FDFA)

val TextHealthSecondary: Color
    get() = if (isDayTheme) {
        when (ThemeState.accentColor) {
            AppAccentColor.TEAL -> Color(0xFF115E59)
            AppAccentColor.OCEAN -> Color(0xFF075985)
            AppAccentColor.EMERALD -> Color(0xFF065F46)
            AppAccentColor.VIOLET -> Color(0xFF4C1D95)
        }
    } else {
        when (ThemeState.accentColor) {
            AppAccentColor.TEAL -> Color(0xFF5EEAD4)
            AppAccentColor.OCEAN -> Color(0xFF7DD3FC)
            AppAccentColor.EMERALD -> Color(0xFF6EE7B7)
            AppAccentColor.VIOLET -> Color(0xFFC4B5FD)
        }
    }

val TextHealthMuted: Color
    get() = if (isDayTheme) Color(0xFF334E68) else Color(0xFF94A3B8)

// Degrade / Gradient Fırçaları
val HealthCardGradient: Brush
    get() = if (isDayTheme) {
        Brush.verticalGradient(colors = listOf(HealthSurface, HealthBg))
    } else {
        Brush.verticalGradient(colors = listOf(Color(0xFF0B3040), Color(0xFF072430)))
    }

val HealthHeroGradient: Brush
    get() = if (isDayTheme) {
        Brush.linearGradient(colors = listOf(HealthSurfaceVariant, HealthSurface))
    } else {
        Brush.linearGradient(colors = listOf(Color(0xFF0E4358), Color(0xFF062B39)))
    }

val TealGlowBrush: Brush
    get() = Brush.linearGradient(
        colors = listOf(HealthTeal, HealthCyan)
    )

val BioEmeraldBrush: Brush
    get() = Brush.linearGradient(
        colors = listOf(BioHealingGreen, HealthTealBright)
    )

// Geriye dönük uyumluluk takma adları
val SkyPrimary: Color get() = HealthTealBright
val SkyPrimaryDark: Color get() = HealthTeal
val SkyPrimaryLight: Color get() = HealthMint
val SkyPrimaryContainer: Color get() = HealthSurfaceVariant
val SkyOnPrimaryContainer: Color get() = TextHealthPrimary

val EmeraldSecondary: Color get() = BioHealingGreen
val EmeraldContainer: Color get() = BioGreenContainer
val SuccessGreen: Color get() = BioHealingGreen
val BioEmerald: Color get() = BioHealingGreen
val AlertRed: Color get() = AlertPharmacyRuby
val AlertRedContainer: Color get() = AlertRubyContainer
val WarningAmber: Color get() = WarningAmberHealth
val WarningContainer: Color get() = WarningAmberContainer
val AmberGlow: Color get() = WarningAmberHealth

val BasalBlue: Color get() = BasalCyan
val BasalBlueContainer: Color get() = BasalContainerDark
val BasalIndigo: Color get() = BasalCyan
val BolusOrange: Color get() = BolusGold
val BolusOrangeContainer: Color get() = BolusContainerDark
val BolusSunset: Color get() = BolusGold
val NeonCyan: Color get() = HealthTealBright
val NeonCyanDark: Color get() = HealthTealBright
val NeonRose: Color get() = AlertPharmacyRuby
