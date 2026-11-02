package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Vaccines
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DoseRecord
import com.example.data.model.DoseType
import com.example.ui.theme.AlertPharmacyRuby
import com.example.ui.theme.BasalCyan
import com.example.ui.theme.BasalKpiBg
import com.example.ui.theme.BioHealingGreen
import com.example.ui.theme.BolusGold
import com.example.ui.theme.BolusKpiBg
import com.example.ui.theme.ChartCanvasBg
import com.example.ui.theme.ChartSlotBg
import com.example.ui.theme.ChartSlotSelectedBg
import com.example.ui.theme.ChartTrackBg
import com.example.ui.theme.FastingKpiBg
import com.example.ui.theme.HealthBorder
import com.example.ui.theme.HealthCyanBright
import com.example.ui.theme.HealthMint
import com.example.ui.theme.HealthSurface
import com.example.ui.theme.HealthSurfaceVariant
import com.example.ui.theme.HealthTeal
import com.example.ui.theme.HealthTealBright
import com.example.ui.theme.IconOnAccent
import com.example.ui.theme.PostKpiBg
import com.example.ui.theme.TextHealthMuted
import com.example.ui.theme.TextHealthPrimary
import com.example.ui.theme.TextHealthSecondary
import com.example.ui.theme.isDayTheme
import com.example.util.AppLanguage
import com.example.util.Strings
import java.util.Calendar
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Data representation for the 4 core daily time slots:
 * 1. Sabah (Morning / Breakfast)
 * 2. Öğle (Lunch / Midday)
 * 3. Akşam (Dinner / Evening)
 * 4. Yatarken (Bedtime / Night)
 */
data class TimeSlotProfile(
    val slotKey: String,
    val title: String,
    val icon: String,
    val timeHint: String,
    val avgFasting: Int?,
    val avgPost: Int?,
    val sampleCount: Int
) {
    val delta: Int?
        get() = if (avgFasting != null && avgPost != null) avgPost - avgFasting else null
}

data class InsulinSlotProfile(
    val slotKey: String,
    val title: String,
    val icon: String,
    val timeHint: String,
    val avgBolus: Double?,
    val avgBasal: Double?,
    val sampleCount: Int
)

/**
 * Clean, Standardized 4-Time-Slot Average Blood Glucose Chart:
 * - Displays exactly Sabah, Öğle, Akşam, Yatarken across the screen.
 * - All labels and numbers fit completely without clipping ("yazılar tam sığsın").
 * - Injected insulin dosages removed as all readings are averages ("vurulan insülin miktarını kaldır sonucta degerler ortalama").
 * - Balanced Açlık & Tokluk KPI cards with colored progress bars and targets inside the same square.
 * - No "Hedef İçi" badge.
 */
@Composable
fun HomeGlucoseInsulinDynamicsChart(
    doses: List<DoseRecord>,
    lang: AppLanguage,
    modifier: Modifier = Modifier
) {
    // Exclusively Last 7 Days Average ("son 7 gün ortalaması olsun sadece")
    val activeDoses = remember(doses) {
        val sevenDaysAgo = System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000)
        doses.sortedBy { it.timestamp }.filter { it.timestamp >= sevenDaysAgo }
    }

    // Helper to get hour of day
    fun getHourOfDay(timestamp: Long): Int {
        val cal = Calendar.getInstance().apply { timeInMillis = timestamp }
        return cal.get(Calendar.HOUR_OF_DAY)
    }

    // Classify into exactly 4 slots: Sabah, Öğle, Akşam, Yatarken
    val trLocale = remember { Locale("tr", "TR") }
    val timeSlots = remember(activeDoses, lang) {
        val morningList = activeDoses.filter {
            val tag = it.mealTag.lowercase(trLocale)
            tag.contains("kahvaltı") || tag.contains("breakfast") || tag.contains("sabah") || (getHourOfDay(it.timestamp) in 5..10)
        }
        val lunchList = activeDoses.filter {
            val tag = it.mealTag.lowercase(trLocale)
            tag.contains("öğle") || tag.contains("ogle") || tag.contains("lunch") || (getHourOfDay(it.timestamp) in 11..15)
        }
        val dinnerList = activeDoses.filter {
            val tag = it.mealTag.lowercase(trLocale)
            tag.contains("akşam") || tag.contains("aksam") || tag.contains("dinner") || (getHourOfDay(it.timestamp) in 16..20)
        }
        val bedtimeList = activeDoses.filter {
            val tag = it.mealTag.lowercase(trLocale)
            tag.contains("yatarken") || tag.contains("gece") || tag.contains("night") || tag.contains("bedtime") ||
                    it.type == DoseType.BASAL || (getHourOfDay(it.timestamp) in 21..24 || getHourOfDay(it.timestamp) in 0..4)
        }

        fun computeProfile(key: String, title: String, icon: String, timeHint: String, list: List<DoseRecord>): TimeSlotProfile {
            val fastings = list.mapNotNull { it.bloodGlucose }
            val posts = list.mapNotNull { it.postDoseGlucose }
            val avgF = if (fastings.isNotEmpty()) fastings.average().toInt() else null
            val avgP = if (posts.isNotEmpty()) posts.average().toInt() else null
            return TimeSlotProfile(
                slotKey = key,
                title = title,
                icon = icon,
                timeHint = timeHint,
                avgFasting = avgF,
                avgPost = avgP,
                sampleCount = list.size
            )
        }

        listOf(
            computeProfile(
                key = "morning",
                title = Strings.chartSlotMorning(lang),
                icon = "🌅",
                timeHint = "07:00–10:00",
                list = morningList
            ),
            computeProfile(
                key = "lunch",
                title = Strings.chartSlotLunch(lang),
                icon = "☀️",
                timeHint = "12:00–14:00",
                list = lunchList
            ),
            computeProfile(
                key = "dinner",
                title = Strings.chartSlotDinner(lang),
                icon = "🌆",
                timeHint = "18:00–20:00",
                list = dinnerList
            ),
            computeProfile(
                key = "bedtime",
                title = Strings.chartSlotBedtime(lang),
                icon = "🌙",
                timeHint = "22:00–00:00",
                list = bedtimeList
            )
        )
    }

    var selectedSlotIndex by remember { mutableIntStateOf(0) }

    // Overall averages across the active dataset
    val overallFastingValues = remember(activeDoses) { activeDoses.mapNotNull { it.bloodGlucose } }
    val overallPostValues = remember(activeDoses) { activeDoses.mapNotNull { it.postDoseGlucose } }

    val overallAvgFasting = remember(overallFastingValues) {
        if (overallFastingValues.isNotEmpty()) overallFastingValues.average().toInt() else null
    }
    val overallAvgPost = remember(overallPostValues) {
        if (overallPostValues.isNotEmpty()) overallPostValues.average().toInt() else null
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("home_glucose_insulin_dynamics_chart"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = HealthSurface),
        border = BorderStroke(1.2.dp, HealthBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: "Ortalama Şeker Profili"
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(HealthTeal, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ShowChart,
                        contentDescription = Strings.chartTitle(lang),
                        tint = IconOnAccent,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = Strings.chartTitle(lang),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextHealthPrimary,
                        letterSpacing = (-0.3).sp
                    )
                    Text(
                        text = Strings.chartSubtitle(lang),
                        style = MaterialTheme.typography.bodySmall,
                        color = HealthMint,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ==============================================================
            // 2 Symmetrical, Spacious KPI Cards with Integrated Colored Progress Bar
            // & Clinical Target directly inside the same square! (NO "Hedef İçi" badge)
            // ==============================================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Card 1: Ortalama Açlık (Average Fasting)
                GlucoseKpiCard(
                    title = Strings.chartAvgFasting(lang),
                    value = overallAvgFasting,
                    targetText = Strings.chartTargetFasting(lang),
                    targetMin = 70,
                    targetMax = 130,
                    isCeiling = false,
                    themeColor = BioHealingGreen,
                    bgColor = FastingKpiBg,
                    modifier = Modifier.weight(1f)
                )

                // Card 2: Ortalama Tokluk (Average Postprandial)
                GlucoseKpiCard(
                    title = Strings.chartAvgPost(lang),
                    value = overallAvgPost,
                    targetText = Strings.chartTargetPost(lang),
                    targetMin = 70,
                    targetMax = 180,
                    isCeiling = true,
                    themeColor = AlertPharmacyRuby,
                    bgColor = PostKpiBg,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ==============================================================
            // 4-PERIOD STANDARDIZED PROFILE: SABAH, ÖĞLE, AKŞAM, YATARKEN
            // Equal-height slot cards with aligned bottom labels & time ranges
            // ==============================================================
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(ChartCanvasBg)
                    .border(1.dp, HealthBorder, RoundedCornerShape(16.dp))
                    .padding(top = 10.dp, bottom = 10.dp, start = 8.dp, end = 8.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(152.dp)
                    ) {
                        // Background Guidelines (70, 130, 180)
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val canvasW = size.width
                            val canvasH = size.height
                            val topPad = 16.dp.toPx()
                            val botPad = 38.dp.toPx()
                            val plotH = (canvasH - topPad - botPad).coerceAtLeast(1f)

                            val minG = 50f
                            val maxG = 220f
                            fun gY(g: Float): Float {
                                val ratio = ((g - minG) / (maxG - minG)).coerceIn(0f, 1f)
                                return (canvasH - botPad) - (ratio * plotH)
                            }

                            val y70 = gY(70f)
                            val y130 = gY(130f)
                            val y180 = gY(180f)

                            // Shaded Safe Zone Band (70 - 130 mg/dL)
                            drawRect(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        BioHealingGreen.copy(alpha = 0.12f),
                                        BioHealingGreen.copy(alpha = 0.04f)
                                    ),
                                    startY = y130,
                                    endY = y70
                                ),
                                topLeft = Offset(0f, y130),
                                size = Size(canvasW, (y70 - y130).coerceAtLeast(1f))
                            )

                            // 70 line
                            drawLine(
                                color = BioHealingGreen.copy(alpha = 0.35f),
                                start = Offset(0f, y70),
                                end = Offset(canvasW, y70),
                                strokeWidth = 1.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
                            )

                            // 130 line
                            drawLine(
                                color = BioHealingGreen.copy(alpha = 0.45f),
                                start = Offset(0f, y130),
                                end = Offset(canvasW, y130),
                                strokeWidth = 1.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
                            )

                            // 180 ceiling line
                            drawLine(
                                color = AlertPharmacyRuby.copy(alpha = 0.45f),
                                start = Offset(0f, y180),
                                end = Offset(canvasW, y180),
                                strokeWidth = 1.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
                            )
                        }

                        // Left Y-Axis indicators
                        Column(
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(start = 2.dp, top = 8.dp, bottom = 38.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "180",
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = AlertPharmacyRuby.copy(alpha = 0.85f)
                            )
                            Text(
                                text = "130",
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = BioHealingGreen.copy(alpha = 0.85f)
                            )
                            Text(
                                text = "70",
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = BioHealingGreen.copy(alpha = 0.75f)
                            )
                        }

                        // Exactly 4 Symmetrical Slots (Sabah, Öğle, Akşam, Yatarken)
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(start = 26.dp, end = 2.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.Bottom
                        ) {
                            timeSlots.forEachIndexed { idx, slot ->
                                val isSelected = idx == selectedSlotIndex
                                val borderCol by animateColorAsState(
                                    if (isSelected) HealthCyanBright else HealthBorder.copy(alpha = 0.35f),
                                    label = "slotBorder"
                                )

                                val maxBarDp = 76f
                                fun calcH(valMg: Int?): Float {
                                    if (valMg == null) return 14f
                                    val r = ((valMg - 50f) / (220f - 50f)).coerceIn(0.14f, 1f)
                                    return r * maxBarDp
                                }

                                val fH = calcH(slot.avgFasting)
                                val pH = calcH(slot.avgPost)

                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            if (isSelected) ChartSlotSelectedBg else ChartSlotBg
                                        )
                                        .border(
                                            width = if (isSelected) 1.5.dp else 1.dp,
                                            color = borderCol,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clickable { selectedSlotIndex = idx }
                                        .padding(horizontal = 3.dp, vertical = 6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Bottom
                                ) {
                                    // Fixed-height Bar Area so all 4 columns align at the exact same bottom baseline
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(1f),
                                        contentAlignment = Alignment.BottomCenter
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.Bottom,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            // Açlık Bar
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Bottom
                                            ) {
                                                Text(
                                                    text = if (slot.avgFasting != null) "${slot.avgFasting}" else "—",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = BioHealingGreen,
                                                    maxLines = 1
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .width(14.dp)
                                                        .height(fH.dp)
                                                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                                        .background(
                                                            Brush.verticalGradient(
                                                                colors = listOf(
                                                                    if (isDayTheme) Color(0xFF10B981) else BioHealingGreen,
                                                                    if (isDayTheme) Color(0xFF047857) else Color(0xFF0A4F3E)
                                                                )
                                                            )
                                                        )
                                                )
                                            }

                                            // Tokluk Bar
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Bottom
                                            ) {
                                                Text(
                                                    text = if (slot.avgPost != null) "${slot.avgPost}" else "—",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = if (slot.avgPost != null) AlertPharmacyRuby else TextHealthMuted,
                                                    maxLines = 1
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .width(14.dp)
                                                        .height(pH.dp)
                                                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                                        .background(
                                                            if (slot.avgPost != null) {
                                                                Brush.verticalGradient(
                                                                    colors = listOf(
                                                                        if (isDayTheme) Color(0xFFF43F5E) else AlertPharmacyRuby,
                                                                        if (isDayTheme) Color(0xFFBE123C) else Color(0xFF5A1422)
                                                                    )
                                                                )
                                                            } else {
                                                                Brush.verticalGradient(
                                                                    colors = listOf(
                                                                        ChartTrackBg,
                                                                        ChartTrackBg.copy(alpha = 0.7f)
                                                                    )
                                                                )
                                                            }
                                                        )
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    // Time Slot Title (Sabah, Öğle, Akşam, Yatarken)
                                    Text(
                                        text = "${slot.icon} ${slot.title}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = TextHealthPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(1.dp))
                                    // Time Range Hint (07:00–10:00, 12:00–14:00, 18:00–20:00, 22:00–00:00)
                                    Text(
                                        text = slot.timeHint,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = TextHealthMuted,
                                        maxLines = 1,
                                        overflow = TextOverflow.Visible,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Chart Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Açlık legend
                Box(modifier = Modifier.size(8.dp).background(BioHealingGreen, RoundedCornerShape(2.dp)))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = Strings.chartLegendFasting(lang),
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextHealthSecondary
                )

                Spacer(modifier = Modifier.width(14.dp))

                // Tokluk legend
                Box(modifier = Modifier.size(8.dp).background(AlertPharmacyRuby, RoundedCornerShape(2.dp)))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = Strings.chartLegendPost(lang),
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextHealthSecondary
                )

                Spacer(modifier = Modifier.width(14.dp))

                // Target Zone legend
                Box(modifier = Modifier.size(width = 12.dp, height = 4.dp).background(BioHealingGreen.copy(alpha = 0.5f)))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (lang == AppLanguage.EN) "Target (70–130)" else "Hedef (70–130)",
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextHealthSecondary
                )
            }
        }
    }
}

/**
 * Average Insulin Consumption Profile Chart ("Ortalama İnsülin Tüketim Profili")
 * Built in the exact same visual format as HomeGlucoseInsulinDynamicsChart:
 * - Header: "Ortalama İnsülin Tüketim Profili" & "Son 7 Gün Ortalaması (Bolus ve Bazal)"
 * - 2 Symmetrical KPI Cards at top: Ortalama Bolus & Ortalama Bazal with colored progress bars
 * - 4 Symmetrical Time Slots: Sabah (07:00–10:00), Öğle (12:00–14:00), Akşam (18:00–20:00), Yatarken (22:00–00:00)
 */
@Composable
fun HomeInsulinConsumptionProfileChart(
    doses: List<DoseRecord>,
    lang: AppLanguage,
    modifier: Modifier = Modifier
) {
    // Exclusively Last 7 Days Average
    val activeDoses = remember(doses) {
        val sevenDaysAgo = System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000)
        doses.sortedBy { it.timestamp }.filter { it.timestamp >= sevenDaysAgo }
    }

    fun getHourOfDay(timestamp: Long): Int {
        val cal = Calendar.getInstance().apply { timeInMillis = timestamp }
        return cal.get(Calendar.HOUR_OF_DAY)
    }

    val trLocale = remember { Locale("tr", "TR") }
    val insulinSlots = remember(activeDoses, lang) {
        val morningList = activeDoses.filter {
            val tag = it.mealTag.lowercase(trLocale)
            tag.contains("kahvaltı") || tag.contains("breakfast") || tag.contains("sabah") || (getHourOfDay(it.timestamp) in 5..10)
        }
        val lunchList = activeDoses.filter {
            val tag = it.mealTag.lowercase(trLocale)
            tag.contains("öğle") || tag.contains("ogle") || tag.contains("lunch") || (getHourOfDay(it.timestamp) in 11..15)
        }
        val dinnerList = activeDoses.filter {
            val tag = it.mealTag.lowercase(trLocale)
            tag.contains("akşam") || tag.contains("aksam") || tag.contains("dinner") || (getHourOfDay(it.timestamp) in 16..20)
        }
        val bedtimeList = activeDoses.filter {
            val tag = it.mealTag.lowercase(trLocale)
            tag.contains("yatarken") || tag.contains("gece") || tag.contains("night") || tag.contains("bedtime") ||
                    it.type == DoseType.BASAL || (getHourOfDay(it.timestamp) in 21..24 || getHourOfDay(it.timestamp) in 0..4)
        }

        fun computeInsulinSlot(key: String, title: String, icon: String, timeHint: String, list: List<DoseRecord>): InsulinSlotProfile {
            val bolusUnits = list.filter { it.type == DoseType.BOLUS }.map { it.doseUnits }
            val basalUnits = list.filter { it.type == DoseType.BASAL }.map { it.doseUnits }
            val avgBolus = if (bolusUnits.isNotEmpty()) bolusUnits.average() else null
            val avgBasal = if (basalUnits.isNotEmpty()) basalUnits.average() else null
            return InsulinSlotProfile(
                slotKey = key,
                title = title,
                icon = icon,
                timeHint = timeHint,
                avgBolus = avgBolus,
                avgBasal = avgBasal,
                sampleCount = list.size
            )
        }

        listOf(
            computeInsulinSlot(
                key = "morning",
                title = Strings.chartSlotMorning(lang),
                icon = "🌅",
                timeHint = "07:00–10:00",
                list = morningList
            ),
            computeInsulinSlot(
                key = "lunch",
                title = Strings.chartSlotLunch(lang),
                icon = "☀️",
                timeHint = "12:00–14:00",
                list = lunchList
            ),
            computeInsulinSlot(
                key = "dinner",
                title = Strings.chartSlotDinner(lang),
                icon = "🌆",
                timeHint = "18:00–20:00",
                list = dinnerList
            ),
            computeInsulinSlot(
                key = "bedtime",
                title = Strings.chartSlotBedtime(lang),
                icon = "🌙",
                timeHint = "22:00–00:00",
                list = bedtimeList
            )
        )
    }

    var selectedSlotIndex by remember { mutableIntStateOf(0) }

    val overallBolusValues = remember(activeDoses) {
        activeDoses.filter { it.type == DoseType.BOLUS }.map { it.doseUnits }
    }
    val overallBasalValues = remember(activeDoses) {
        activeDoses.filter { it.type == DoseType.BASAL }.map { it.doseUnits }
    }

    val overallAvgBolus = remember(overallBolusValues) {
        if (overallBolusValues.isNotEmpty()) overallBolusValues.average() else null
    }
    val overallAvgBasal = remember(overallBasalValues) {
        if (overallBasalValues.isNotEmpty()) overallBasalValues.average() else null
    }

    fun formatIu(v: Double?): String {
        if (v == null) return "—"
        val rounded1 = (v * 10.0).roundToInt() / 10.0
        return if (rounded1 % 1.0 == 0.0) "${rounded1.toInt()}" else "%.1f".format(Locale.US, rounded1)
    }

    val chartTitle = if (lang == AppLanguage.EN) "Average Insulin Consumption Profile" else "Ortalama İnsülin Tüketim Profili"
    val chartSubtitle = if (lang == AppLanguage.EN) "Last 7 Days Averages (Bolus & Basal)" else "Son 7 Gün Ortalaması (Bolus ve Bazal)"

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("home_insulin_consumption_profile_chart"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = HealthSurface),
        border = BorderStroke(1.2.dp, HealthBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: "Ortalama İnsülin Tüketim Profili"
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(HealthTeal, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Vaccines,
                        contentDescription = chartTitle,
                        tint = IconOnAccent,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = chartTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextHealthPrimary,
                        letterSpacing = (-0.3).sp
                    )
                    Text(
                        text = chartSubtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = HealthMint,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2 Symmetrical KPI Cards at Top (Ortalama Bolus & Ortalama Bazal)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Card 1: Ortalama Bolus (Hızlı Etkili)
                InsulinKpiCard(
                    title = if (lang == AppLanguage.EN) "Avg Bolus" else "Ortalama Bolus",
                    valueText = formatIu(overallAvgBolus),
                    hasValue = overallAvgBolus != null,
                    rawValue = overallAvgBolus ?: 0.0,
                    maxScale = 25f,
                    subtitleText = if (lang == AppLanguage.EN) "Rapid-Acting (Mealtime)" else "Hızlı Etkili (Öğün Dozu)",
                    themeColor = BolusGold,
                    bgColor = BolusKpiBg,
                    modifier = Modifier.weight(1f)
                )

                // Card 2: Ortalama Bazal (Uzun Etkili)
                InsulinKpiCard(
                    title = if (lang == AppLanguage.EN) "Avg Basal" else "Ortalama Bazal",
                    valueText = formatIu(overallAvgBasal),
                    hasValue = overallAvgBasal != null,
                    rawValue = overallAvgBasal ?: 0.0,
                    maxScale = 35f,
                    subtitleText = if (lang == AppLanguage.EN) "Long-Acting (24-Hr Base)" else "Uzun Etkili (Temel Doz)",
                    themeColor = BasalCyan,
                    bgColor = BasalKpiBg,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4-PERIOD STANDARDIZED INSULIN PROFILE: SABAH, ÖĞLE, AKŞAM, YATARKEN
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(ChartCanvasBg)
                    .border(1.dp, HealthBorder, RoundedCornerShape(16.dp))
                    .padding(top = 10.dp, bottom = 10.dp, start = 8.dp, end = 8.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(152.dp)
                    ) {
                        // Background Reference Lines (8 IU, 16 IU, 24 IU)
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val canvasW = size.width
                            val canvasH = size.height
                            val topPad = 16.dp.toPx()
                            val botPad = 38.dp.toPx()
                            val plotH = (canvasH - topPad - botPad).coerceAtLeast(1f)

                            val minU = 0f
                            val maxU = 28f
                            fun uY(u: Float): Float {
                                val ratio = ((u - minU) / (maxU - minU)).coerceIn(0f, 1f)
                                return (canvasH - botPad) - (ratio * plotH)
                            }

                            val y8 = uY(8f)
                            val y16 = uY(16f)
                            val y24 = uY(24f)

                            // Shaded Typical Bolus-Basal Band (8 - 16 IU)
                            drawRect(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        BasalCyan.copy(alpha = 0.10f),
                                        BolusGold.copy(alpha = 0.04f)
                                    ),
                                    startY = y16,
                                    endY = y8
                                ),
                                topLeft = Offset(0f, y16),
                                size = Size(canvasW, (y8 - y16).coerceAtLeast(1f))
                            )

                            drawLine(
                                color = BolusGold.copy(alpha = 0.35f),
                                start = Offset(0f, y8),
                                end = Offset(canvasW, y8),
                                strokeWidth = 1.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
                            )

                            drawLine(
                                color = BasalCyan.copy(alpha = 0.40f),
                                start = Offset(0f, y16),
                                end = Offset(canvasW, y16),
                                strokeWidth = 1.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
                            )

                            drawLine(
                                color = BasalCyan.copy(alpha = 0.45f),
                                start = Offset(0f, y24),
                                end = Offset(canvasW, y24),
                                strokeWidth = 1.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
                            )
                        }

                        // Left Y-Axis indicators (IU)
                        Column(
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(start = 2.dp, top = 8.dp, bottom = 38.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "24u",
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = BasalCyan.copy(alpha = 0.85f)
                            )
                            Text(
                                text = "16u",
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = BasalCyan.copy(alpha = 0.80f)
                            )
                            Text(
                                text = "8u",
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = BolusGold.copy(alpha = 0.85f)
                            )
                        }

                        // 4 Symmetrical Slots: Sabah, Öğle, Akşam, Yatarken
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(start = 26.dp, end = 2.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.Bottom
                        ) {
                            insulinSlots.forEachIndexed { idx, slot ->
                                val isSelected = idx == selectedSlotIndex
                                val borderCol by animateColorAsState(
                                    if (isSelected) HealthCyanBright else HealthBorder.copy(alpha = 0.35f),
                                    label = "insulinSlotBorder"
                                )

                                val maxBarDp = 76f
                                fun calcH(valIu: Double?): Float {
                                    if (valIu == null || valIu <= 0.0) return 14f
                                    val r = (valIu.toFloat() / 28f).coerceIn(0.16f, 1f)
                                    return r * maxBarDp
                                }

                                val bolusH = calcH(slot.avgBolus)
                                val basalH = calcH(slot.avgBasal)

                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            if (isSelected) ChartSlotSelectedBg else ChartSlotBg
                                        )
                                        .border(
                                            width = if (isSelected) 1.5.dp else 1.dp,
                                            color = borderCol,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clickable { selectedSlotIndex = idx }
                                        .padding(horizontal = 3.dp, vertical = 6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Bottom
                                ) {
                                    // Fixed-height Bar Area so all 4 columns align at the exact same bottom baseline
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(1f),
                                        contentAlignment = Alignment.BottomCenter
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.Bottom,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            // Bolus Bar
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Bottom
                                            ) {
                                                Text(
                                                    text = formatIu(slot.avgBolus),
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = if (slot.avgBolus != null) BolusGold else TextHealthMuted,
                                                    maxLines = 1
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .width(14.dp)
                                                        .height(bolusH.dp)
                                                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                                        .background(
                                                            if (slot.avgBolus != null) {
                                                                Brush.verticalGradient(
                                                                    colors = listOf(
                                                                        if (isDayTheme) Color(0xFFF59E0B) else BolusGold,
                                                                        if (isDayTheme) Color(0xFFD97706) else Color(0xFF6B4204)
                                                                    )
                                                                )
                                                            } else {
                                                                Brush.verticalGradient(
                                                                    colors = listOf(
                                                                        ChartTrackBg,
                                                                        ChartTrackBg.copy(alpha = 0.7f)
                                                                    )
                                                                )
                                                            }
                                                        )
                                                )
                                            }

                                            // Basal Bar
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Bottom
                                            ) {
                                                Text(
                                                    text = formatIu(slot.avgBasal),
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = if (slot.avgBasal != null) BasalCyan else TextHealthMuted,
                                                    maxLines = 1
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .width(14.dp)
                                                        .height(basalH.dp)
                                                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                                        .background(
                                                            if (slot.avgBasal != null) {
                                                                Brush.verticalGradient(
                                                                    colors = listOf(
                                                                        if (isDayTheme) Color(0xFF0EA5E9) else BasalCyan,
                                                                        if (isDayTheme) Color(0xFF0284C7) else Color(0xFF0A4766)
                                                                    )
                                                                )
                                                            } else {
                                                                Brush.verticalGradient(
                                                                    colors = listOf(
                                                                        ChartTrackBg,
                                                                        ChartTrackBg.copy(alpha = 0.7f)
                                                                    )
                                                                )
                                                            }
                                                        )
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    // Time Slot Title (Sabah, Öğle, Akşam, Yatarken)
                                    Text(
                                        text = "${slot.icon} ${slot.title}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = TextHealthPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(1.dp))
                                    // Time Range Hint (07:00–10:00, 12:00–14:00, 18:00–20:00, 22:00–00:00)
                                    Text(
                                        text = slot.timeHint,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = TextHealthMuted,
                                        maxLines = 1,
                                        overflow = TextOverflow.Visible,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Chart Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Bolus legend
                Box(modifier = Modifier.size(8.dp).background(BolusGold, RoundedCornerShape(2.dp)))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (lang == AppLanguage.EN) "Bolus (Rapid)" else "Bolus (Hızlı)",
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextHealthSecondary
                )

                Spacer(modifier = Modifier.width(14.dp))

                // Basal legend
                Box(modifier = Modifier.size(8.dp).background(BasalCyan, RoundedCornerShape(2.dp)))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (lang == AppLanguage.EN) "Basal (Long)" else "Bazal (Uzun)",
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextHealthSecondary
                )

                Spacer(modifier = Modifier.width(14.dp))

                // Unit indicator
                Box(modifier = Modifier.size(width = 12.dp, height = 4.dp).background(BasalCyan.copy(alpha = 0.5f)))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (lang == AppLanguage.EN) "Unit: IU" else "Birim: IU (Ünite)",
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextHealthSecondary
                )
            }
        }
    }
}

/**
 * KPI Tile for Insulin Consumption Profile matching GlucoseKpiCard format:
 * - Header Label
 * - Big Primary IU value
 * - Colored progress / range indicator bar
 * - Subtitle directly underneath within the same square
 */
@Composable
private fun InsulinKpiCard(
    title: String,
    valueText: String,
    hasValue: Boolean,
    rawValue: Double,
    maxScale: Float,
    subtitleText: String,
    themeColor: Color,
    bgColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .border(1.2.dp, themeColor.copy(alpha = 0.45f), RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        // 1. Header Title
        Text(
            text = title,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.ExtraBold,
            color = themeColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(4.dp))

        // 2. Big Numerical Value
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = valueText,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = themeColor
            )
            if (hasValue) {
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = "IU",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColor.copy(alpha = 0.85f),
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 3. Colored Progress Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(7.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(ChartTrackBg)
        ) {
            if (hasValue) {
                val fraction = (rawValue.toFloat() / maxScale).coerceIn(0.10f, 1f)
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(fraction)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    themeColor.copy(alpha = 0.6f),
                                    themeColor
                                )
                            )
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 4. Subtitle inside the SAME square
        Text(
            text = subtitleText,
            fontSize = 9.5.sp,
            fontWeight = FontWeight.Bold,
            color = TextHealthMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * KPI Tile with:
 * - Header Label
 * - Big Primary mg/dL value
 * - "çubuklu renkli": Colored progress / range indicator bar
 * - Target directly underneath within the same square! (No "Hedef İçi" badge)
 */
@Composable
private fun GlucoseKpiCard(
    title: String,
    value: Int?,
    targetText: String,
    targetMin: Int,
    targetMax: Int,
    isCeiling: Boolean,
    themeColor: Color,
    bgColor: Color,
    modifier: Modifier = Modifier
) {
    val inRange = when {
        value == null -> null
        isCeiling -> value <= targetMax
        else -> value in targetMin..targetMax
    }

    val statusColor = when (inRange) {
        true -> BioHealingGreen
        false -> AlertPharmacyRuby
        null -> TextHealthMuted
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .border(1.2.dp, themeColor.copy(alpha = 0.45f), RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        // 1. Header Title
        Text(
            text = title,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.ExtraBold,
            color = themeColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(4.dp))

        // 2. Big Numerical Value
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = if (value != null) "$value" else "—",
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = themeColor
            )
            if (value != null) {
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = "mg/dL",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColor.copy(alpha = 0.85f),
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 3. "ÇUBUKLU RENKLİ" - Colored Range / Progress Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(7.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(ChartTrackBg)
        ) {
            if (value != null) {
                val minScale = 50f
                val maxScale = 220f
                val fraction = ((value - minScale) / (maxScale - minScale)).coerceIn(0.08f, 1f)

                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(fraction)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    statusColor.copy(alpha = 0.6f),
                                    statusColor
                                )
                            )
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 4. Clinical Target inside the SAME square! (Clean, no redundant badges)
        Text(
            text = targetText,
            fontSize = 9.5.sp,
            fontWeight = FontWeight.Bold,
            color = TextHealthMuted,
            maxLines = 1
        )
    }
}
