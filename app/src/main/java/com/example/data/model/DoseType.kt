package com.example.data.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.BasalBlue
import com.example.ui.theme.BasalBlueContainer
import com.example.ui.theme.BolusOrange
import com.example.ui.theme.BolusOrangeContainer

enum class DoseType(
    val title: String,
    val description: String,
    val brandExamples: String,
    val color: Color,
    val containerColor: Color
) {
    BASAL(
        title = "Bazal (Uzun Etkili)",
        description = "Günde 1 veya 2 kez sabit saatte vurulan arka plan insülini.",
        brandExamples = "Lantus, Toujeo, Levemir, Tresiba",
        color = BasalBlue,
        containerColor = BasalBlueContainer
    ),
    BOLUS(
        title = "Bolus (Hızlı Etkili)",
        description = "Öğün öncesi karbonhidrat karşılığı veya kan şekeri düzeltme dozu.",
        brandExamples = "NovoRapid, Humalog, Apidra, Fiasp",
        color = BolusOrange,
        containerColor = BolusOrangeContainer
    )
}
