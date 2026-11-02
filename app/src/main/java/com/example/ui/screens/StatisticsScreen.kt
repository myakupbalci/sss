package com.example.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Bloodtype
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DoseRecord
import com.example.data.model.DoseType
import com.example.data.model.InjectionSite
import com.example.ui.theme.AlertPharmacyRuby
import com.example.ui.theme.AlertRubyContainer
import com.example.ui.theme.BasalCyan
import com.example.ui.theme.BasalContainerDark
import com.example.ui.theme.BioGreenContainer
import com.example.ui.theme.BioHealingGreen
import com.example.ui.theme.BolusContainerDark
import com.example.ui.theme.BolusGold
import com.example.ui.theme.ChartSlotBg
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    doses: List<DoseRecord>,
    modifier: Modifier = Modifier,
    lang: com.example.util.AppLanguage = com.example.util.AppLanguage.TR
) {
    val context = LocalContext.current
    val todayCal = remember { Calendar.getInstance() }

    // Selected date state (defaults to today)
    var selectedCal by remember {
        mutableStateOf(Calendar.getInstance())
    }

    val isSelectedToday = remember(selectedCal) {
        selectedCal.get(Calendar.YEAR) == todayCal.get(Calendar.YEAR) &&
        selectedCal.get(Calendar.DAY_OF_YEAR) == todayCal.get(Calendar.DAY_OF_YEAR)
    }

    // Filter doses strictly belonging to the selected calendar day
    val selectedDayDoses = remember(doses, selectedCal) {
        val sYear = selectedCal.get(Calendar.YEAR)
        val sDay = selectedCal.get(Calendar.DAY_OF_YEAR)
        doses.filter { dose ->
            val dCal = Calendar.getInstance().apply { timeInMillis = dose.timestamp }
            dCal.get(Calendar.YEAR) == sYear && dCal.get(Calendar.DAY_OF_YEAR) == sDay
        }.sortedByDescending { it.timestamp }
    }

    // Calculate core metrics for chosen day
    val totalUnits = selectedDayDoses.sumOf { it.doseUnits }
    val doseCount = selectedDayDoses.size
    val bolusDoses = selectedDayDoses.filter { it.type == DoseType.BOLUS }
    val basalDoses = selectedDayDoses.filter { it.type == DoseType.BASAL }
    val bolusUnits = bolusDoses.sumOf { it.doseUnits }
    val basalUnits = basalDoses.sumOf { it.doseUnits }
    val hasDoses = totalUnits > 0.0

    val bolusRatio = if (hasDoses) (bolusUnits / totalUnits).toFloat() else 0f
    val basalRatio = if (hasDoses) (basalUnits / totalUnits).toFloat() else 0f

    // Dedicated Fasting Blood Glucose (Açlık Kan Şekeri - Doz Öncesi)
    val fastingValues = selectedDayDoses.mapNotNull { it.bloodGlucose }
    val avgFasting = if (fastingValues.isNotEmpty()) fastingValues.average() else null
    val minFasting = fastingValues.minOrNull()
    val maxFasting = fastingValues.maxOrNull()
    val inRangeFasting = fastingValues.count { it in 70..130 }
    val fastingCompliance = if (fastingValues.isNotEmpty()) (inRangeFasting * 100) / fastingValues.size else null

    // Dedicated 2-Hour Postprandial Blood Glucose (2 Saat Sonraki Tokluk Şekeri)
    val postprandialValues = selectedDayDoses.mapNotNull { it.postDoseGlucose }
    val avgPostprandial = if (postprandialValues.isNotEmpty()) postprandialValues.average() else null
    val minPostprandial = postprandialValues.minOrNull()
    val maxPostprandial = postprandialValues.maxOrNull()
    val inRangePostprandial = postprandialValues.count { it in 70..180 }
    val postprandialCompliance = if (postprandialValues.isNotEmpty()) (inRangePostprandial * 100) / postprandialValues.size else null

    // Combined average for general KPI tile
    val allDayGlucoseValues = fastingValues + postprandialValues
    val avgGlucose = if (allDayGlucoseValues.isNotEmpty()) allDayGlucoseValues.average() else null
    val minGlucose = allDayGlucoseValues.minOrNull()
    val maxGlucose = allDayGlucoseValues.maxOrNull()

    // All-time historical averages for clinical comparison & zero-data guidance
    val allFastingValues = doses.mapNotNull { it.bloodGlucose }
    val allPostprandialValues = doses.mapNotNull { it.postDoseGlucose }
    val overallAvgFasting = if (allFastingValues.isNotEmpty()) allFastingValues.average() else null
    val overallAvgPostprandial = if (allPostprandialValues.isNotEmpty()) allPostprandialValues.average() else null

    // 4 Time of day breakdown for selected day with both dose and blood glucose measurements
    val morningDoses = remember(selectedDayDoses) {
        selectedDayDoses.filter {
            val c = Calendar.getInstance().apply { timeInMillis = it.timestamp }
            c.get(Calendar.HOUR_OF_DAY) in 6..11
        }
    }
    val afternoonDoses = remember(selectedDayDoses) {
        selectedDayDoses.filter {
            val c = Calendar.getInstance().apply { timeInMillis = it.timestamp }
            c.get(Calendar.HOUR_OF_DAY) in 12..17
        }
    }
    val eveningDoses = remember(selectedDayDoses) {
        selectedDayDoses.filter {
            val c = Calendar.getInstance().apply { timeInMillis = it.timestamp }
            c.get(Calendar.HOUR_OF_DAY) in 18..22
        }
    }
    val nightDoses = remember(selectedDayDoses) {
        selectedDayDoses.filter {
            val c = Calendar.getInstance().apply { timeInMillis = it.timestamp }
            val h = c.get(Calendar.HOUR_OF_DAY)
            h in 23..24 || h in 0..5
        }
    }

    val morningUnits = morningDoses.sumOf { it.doseUnits }
    val afternoonUnits = afternoonDoses.sumOf { it.doseUnits }
    val eveningUnits = eveningDoses.sumOf { it.doseUnits }
    val nightUnits = nightDoses.sumOf { it.doseUnits }
    val maxPeriodUnits = maxOf(morningUnits, afternoonUnits, eveningUnits, nightUnits, 1.0)

    // Injection sites used on that day
    val sitesGrouped: Map<InjectionSite, List<DoseRecord>> = selectedDayDoses.groupBy { it.injectionSite }

    // General 7-day average calculation
    val sevenDaysAgoMillis = remember {
        Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -7) }.timeInMillis
    }
    val recent7DaysDoses = remember(doses) {
        doses.filter { it.timestamp >= sevenDaysAgoMillis }
    }
    val avgDailyUnits7Days = if (recent7DaysDoses.isNotEmpty()) {
        val distinctDays = recent7DaysDoses.map {
            val c = Calendar.getInstance().apply { timeInMillis = it.timestamp }
            "${c.get(Calendar.YEAR)}_${c.get(Calendar.DAY_OF_YEAR)}"
        }.distinct().size.coerceAtLeast(1)
        recent7DaysDoses.sumOf { it.doseUnits } / distinctDays
    } else 0.0

    // DatePicker Dialog launcher
    val openDatePicker = {
        val year = selectedCal.get(Calendar.YEAR)
        val month = selectedCal.get(Calendar.MONTH)
        val day = selectedCal.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(context, { _, y, m, d ->
            val newCal = Calendar.getInstance().apply {
                set(Calendar.YEAR, y)
                set(Calendar.MONTH, m)
                set(Calendar.DAY_OF_MONTH, d)
            }
            selectedCal = newCal
        }, year, month, day).show()
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("statistics_screen")
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Screen Header
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(HealthTeal, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.BarChart,
                        contentDescription = "İstatistik",
                        tint = IconOnAccent,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = if (lang == com.example.util.AppLanguage.EN) "Statistics & Analysis" else "İstatistik & Analiz",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextHealthPrimary
                    )
                    Text(
                        text = if (lang == com.example.util.AppLanguage.EN) "Historical dose analysis and consumption trends" else "Geçmişe dönük doz analizi ve tüketim eğilimleri",
                        style = MaterialTheme.typography.bodySmall,
                        color = HealthMint,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Modern Interactive Date Selector & 7-Day Calendar Strip
        item {
            val isEn = lang == com.example.util.AppLanguage.EN
            val locale = if (isEn) Locale.ENGLISH else Locale("tr")
            val formattedFullDate = SimpleDateFormat("d MMMM yyyy, EEEE", locale).format(selectedCal.time)
            val monthYearLabel = SimpleDateFormat("MMMM yyyy", locale).format(selectedCal.time).replaceFirstChar { it.uppercase(locale) }

            val diffDays = remember(selectedCal) {
                val startSelected = (selectedCal.clone() as Calendar).apply {
                    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                }.timeInMillis
                val startToday = (todayCal.clone() as Calendar).apply {
                    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                }.timeInMillis
                ((startToday - startSelected) / (24 * 3600 * 1000L)).toInt()
            }

            val relativeBadgeText = when (diffDays) {
                0 -> if (isEn) "Today" else "Bugün"
                1 -> if (isEn) "Yesterday" else "Dün"
                in 2..365 -> if (isEn) "$diffDays Days Ago" else "$diffDays Gün Önce"
                else -> if (isEn) "Selected Date" else "Seçili Tarih"
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("date_selector_card"),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = HealthSurface),
                border = BorderStroke(1.5.dp, HealthBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Top Header: Calendar Badge + Month/Year + Quick Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(HealthTeal, RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    tint = IconOnAccent,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = monthYearLabel,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = TextHealthPrimary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (isSelectedToday) BioGreenContainer else ChartSlotBg,
                                        border = BorderStroke(
                                            0.8.dp,
                                            if (isSelectedToday) BioHealingGreen else HealthBorder
                                        )
                                    ) {
                                        Text(
                                            text = relativeBadgeText,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (isSelectedToday) BioHealingGreen else HealthTealBright,
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = formattedFullDate,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextHealthSecondary
                                )
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (!isSelectedToday) {
                                Surface(
                                    onClick = { selectedCal = Calendar.getInstance() },
                                    shape = RoundedCornerShape(10.dp),
                                    color = ChartSlotBg,
                                    border = BorderStroke(1.dp, HealthTealBright)
                                ) {
                                    Text(
                                        text = if (isEn) "Today" else "Bugün",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = HealthTealBright,
                                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp)
                                    )
                                }
                            }

                            Surface(
                                onClick = openDatePicker,
                                shape = RoundedCornerShape(10.dp),
                                color = HealthTeal,
                                border = BorderStroke(1.dp, HealthTealBright)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DateRange,
                                        contentDescription = null,
                                        tint = IconOnAccent,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isEn) "Calendar" else "Takvim",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = IconOnAccent
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Modern 7-Day Interactive Calendar Capsule Strip
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val dayNameFmt = remember(locale) { SimpleDateFormat("EEE", locale) }
                        for (offset in -6..0) {
                            val dayCal = (todayCal.clone() as Calendar).apply {
                                add(Calendar.DAY_OF_YEAR, offset)
                            }
                            val dYear = dayCal.get(Calendar.YEAR)
                            val dDayOfYear = dayCal.get(Calendar.DAY_OF_YEAR)
                            val dayNum = dayCal.get(Calendar.DAY_OF_MONTH)
                            val dayShort = dayNameFmt.format(dayCal.time).take(3).replaceFirstChar { it.uppercase(locale) }

                            val isDaySelected = selectedCal.get(Calendar.YEAR) == dYear &&
                                    selectedCal.get(Calendar.DAY_OF_YEAR) == dDayOfYear

                            val dayHasDoses = remember(doses, dYear, dDayOfYear) {
                                doses.any { d ->
                                    val c = Calendar.getInstance().apply { timeInMillis = d.timestamp }
                                    c.get(Calendar.YEAR) == dYear && c.get(Calendar.DAY_OF_YEAR) == dDayOfYear
                                }
                            }

                            Surface(
                                onClick = { selectedCal = dayCal },
                                shape = RoundedCornerShape(14.dp),
                                color = if (isDaySelected) HealthTeal else ChartSlotBg,
                                border = BorderStroke(
                                    width = if (isDaySelected) 1.5.dp else 1.dp,
                                    color = if (isDaySelected) HealthTealBright else HealthBorder
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 2.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = dayShort,
                                        fontSize = 10.sp,
                                        fontWeight = if (isDaySelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                                        color = if (isDaySelected) IconOnAccent.copy(alpha = 0.85f) else TextHealthSecondary,
                                        maxLines = 1
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = dayNum.toString(),
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Black,
                                        color = if (isDaySelected) IconOnAccent else TextHealthPrimary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp)
                                            .background(
                                                color = when {
                                                    dayHasDoses && isDaySelected -> IconOnAccent
                                                    dayHasDoses -> BioHealingGreen
                                                    else -> Color.Transparent
                                                },
                                                shape = CircleShape
                                            )
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Sleek Bottom Stepper Pill Bar (< Previous Day | Selected Date Summary | Next Day >)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = ChartSlotBg,
                        border = BorderStroke(1.dp, HealthBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 6.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = {
                                    selectedCal = (selectedCal.clone() as Calendar).apply {
                                        add(Calendar.DAY_OF_YEAR, -1)
                                    }
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = if (isEn) "Previous Day" else "Önceki Gün",
                                    tint = HealthTealBright,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { openDatePicker() }
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isEn)
                                        "$formattedFullDate • ${selectedDayDoses.size} Doses"
                                    else
                                        "$formattedFullDate • ${selectedDayDoses.size} Kayıt",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextHealthPrimary
                                )
                            }

                            IconButton(
                                onClick = {
                                    if (!isSelectedToday) {
                                        selectedCal = (selectedCal.clone() as Calendar).apply {
                                            add(Calendar.DAY_OF_YEAR, 1)
                                        }
                                    }
                                },
                                enabled = !isSelectedToday,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = if (isEn) "Next Day" else "Sonraki Gün",
                                    tint = if (!isSelectedToday) HealthTealBright else TextHealthMuted.copy(alpha = 0.4f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section: Selected Day Time-of-Day Distribution (Insulin Doses + Fasting & Postprandial Glucose)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("time_of_day_distribution_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = HealthSurface),
                border = BorderStroke(1.2.dp, HealthBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(HealthTeal, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = IconOnAccent,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = if (lang == com.example.util.AppLanguage.EN) "TIME-OF-DAY DISTRIBUTION" else "GÜNÜN ZAMAN DİLİMİ DAĞILIMI",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = HealthMint,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = if (lang == com.example.util.AppLanguage.EN) "Dose (IU) • Fasting & Postprandial Glucose" else "Kullanılan Doz (IU) • Açlık & Tokluk Şekeri",
                                    fontSize = 9.5.sp,
                                    color = TextHealthSecondary
                                )
                            }
                        }

                        Surface(
                            color = HealthSurfaceVariant,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, HealthBorder)
                        ) {
                            Text(
                                text = if (lang == com.example.util.AppLanguage.EN) "$doseCount Doses" else "$doseCount Doz Kaydı",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (hasDoses) HealthTealBright else TextHealthMuted,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 2x2 Grid of periods for selected date
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatPeriodCard(
                            title = if (lang == com.example.util.AppLanguage.EN) "Morning" else "Sabah",
                            timeRange = "06:00 - 12:00",
                            units = morningUnits,
                            maxUnits = maxPeriodUnits,
                            doseCount = morningDoses.size,
                            fastingValues = morningDoses.mapNotNull { it.bloodGlucose },
                            postprandialValues = morningDoses.mapNotNull { it.postDoseGlucose },
                            icon = Icons.Default.WbSunny,
                            accentColor = if (isDayTheme) Color(0xFFB45309) else Color(0xFFFBBF24),
                            bgStartColor = if (isDayTheme) Color(0xFFF8E7C8) else Color(0xFF2E2208),
                            lang = lang,
                            modifier = Modifier.weight(1f)
                        )
                        StatPeriodCard(
                            title = if (lang == com.example.util.AppLanguage.EN) "Afternoon" else "Öğle",
                            timeRange = "12:00 - 18:00",
                            units = afternoonUnits,
                            maxUnits = maxPeriodUnits,
                            doseCount = afternoonDoses.size,
                            fastingValues = afternoonDoses.mapNotNull { it.bloodGlucose },
                            postprandialValues = afternoonDoses.mapNotNull { it.postDoseGlucose },
                            icon = Icons.Default.LightMode,
                            accentColor = if (isDayTheme) Color(0xFF0369A1) else Color(0xFF38BDF8),
                            bgStartColor = if (isDayTheme) Color(0xFFCDE5F8) else Color(0xFF092A3A),
                            lang = lang,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatPeriodCard(
                            title = if (lang == com.example.util.AppLanguage.EN) "Evening" else "Akşam",
                            timeRange = "18:00 - 23:00",
                            units = eveningUnits,
                            maxUnits = maxPeriodUnits,
                            doseCount = eveningDoses.size,
                            fastingValues = eveningDoses.mapNotNull { it.bloodGlucose },
                            postprandialValues = eveningDoses.mapNotNull { it.postDoseGlucose },
                            icon = Icons.Default.WbTwilight,
                            accentColor = if (isDayTheme) Color(0xFF6D28D9) else Color(0xFFC084FC),
                            bgStartColor = if (isDayTheme) Color(0xFFDED6F6) else Color(0xFF27153B),
                            lang = lang,
                            modifier = Modifier.weight(1f)
                        )
                        StatPeriodCard(
                            title = if (lang == com.example.util.AppLanguage.EN) "Night" else "Gece",
                            timeRange = "23:00 - 06:00",
                            units = nightUnits,
                            maxUnits = maxPeriodUnits,
                            doseCount = nightDoses.size,
                            fastingValues = nightDoses.mapNotNull { it.bloodGlucose },
                            postprandialValues = nightDoses.mapNotNull { it.postDoseGlucose },
                            icon = Icons.Default.NightsStay,
                            accentColor = if (isDayTheme) Color(0xFF047857) else Color(0xFF34D399),
                            bgStartColor = if (isDayTheme) Color(0xFFC9EDE0) else Color(0xFF0A2B24),
                            lang = lang,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // DEDICATED SECTION: AÇLIK VE TOKLUK KAN ŞEKERİ ANALİZİ
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("glucose_fasting_postprandial_section"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = HealthSurface),
                border = BorderStroke(1.2.dp, HealthBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Header Row: Blood Icon + Title + Measurement Count Chip
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .background(Color(0xFFE11D48).copy(alpha = 0.22f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bloodtype,
                                    contentDescription = null,
                                    tint = AlertPharmacyRuby,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (lang == com.example.util.AppLanguage.EN) "BLOOD GLUCOSE ANALYSIS" else "AÇLIK & TOKLUK KAN ŞEKERİ",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = HealthMint,
                                    letterSpacing = 0.6.sp
                                )
                                Text(
                                    text = if (lang == com.example.util.AppLanguage.EN) "Fasting vs 2-Hr Postprandial" else "Açlık ve 2. Saat Tokluk Dengesi",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    color = TextHealthPrimary
                                )
                            }
                        }

                        val totalReadings = fastingValues.size + postprandialValues.size
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (totalReadings > 0) BioGreenContainer else HealthSurfaceVariant,
                            border = BorderStroke(0.8.dp, if (totalReadings > 0) BioHealingGreen else HealthBorder)
                        ) {
                            Text(
                                text = if (lang == com.example.util.AppLanguage.EN) "$totalReadings Readings" else "$totalReadings Ölçüm",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (totalReadings > 0) BioHealingGreen else TextHealthMuted,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Two Dedicated Side-by-Side Cards (Açlık vs Tokluk)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Card 1: Açlık Şekeri (Doz Öncesi)
                        GlucoseMetricCard(
                            title = if (lang == com.example.util.AppLanguage.EN) "FASTING (PRE-DOSE)" else "AÇLIK (DOZ ÖNCESİ)",
                            targetRange = "70 - 130 mg/dL",
                            avgValue = avgFasting,
                            minValue = minFasting,
                            maxValue = maxFasting,
                            count = fastingValues.size,
                            targetCompliance = fastingCompliance,
                            accentColor = BioHealingGreen,
                            containerColor = FastingKpiBg,
                            borderColor = BioHealingGreen.copy(alpha = 0.55f),
                            lang = lang,
                            modifier = Modifier.weight(1f)
                        )

                        // Card 2: 2 Saat Sonraki Tokluk Şekeri
                        GlucoseMetricCard(
                            title = if (lang == com.example.util.AppLanguage.EN) "2-HR POSTPRANDIAL" else "2. SAAT TOKLUK",
                            targetRange = "70 - 180 mg/dL",
                            avgValue = avgPostprandial,
                            minValue = minPostprandial,
                            maxValue = maxPostprandial,
                            count = postprandialValues.size,
                            targetCompliance = postprandialCompliance,
                            accentColor = AlertPharmacyRuby,
                            containerColor = PostKpiBg,
                            borderColor = AlertPharmacyRuby.copy(alpha = 0.55f),
                            lang = lang,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Clinical Guidance Note
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = HealthSurfaceVariant.copy(alpha = 0.6f)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = HealthTealBright,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (lang == com.example.util.AppLanguage.EN)
                                    "Clinical target: Pre-meal fasting 70–130 mg/dL, 2-hour postprandial < 180 mg/dL."
                                else
                                    "Klinik hedef: Doz öncesi açlık 70–130 mg/dL, 2 saat sonraki tokluk < 180 mg/dL.",
                                fontSize = 10.5.sp,
                                color = TextHealthSecondary
                            )
                        }
                    }

                    // Paired Meal Glucose Comparison for Doses with Readings
                    val dosesWithGlucose = selectedDayDoses.filter { it.bloodGlucose != null || it.postDoseGlucose != null }
                    if (dosesWithGlucose.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = if (lang == com.example.util.AppLanguage.EN) "PAIRED MEAL COMPARISON" else "ÖĞÜN BAZLI AÇLIK → TOKLUK TAKİBİ",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = HealthCyanBright,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        dosesWithGlucose.forEach { d ->
                            val dTimeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(d.timestamp))
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                shape = RoundedCornerShape(10.dp),
                                color = HealthSurfaceVariant,
                                border = BorderStroke(0.8.dp, HealthBorder)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = if (d.mealTag.isNotEmpty()) "${d.mealTag} • $dTimeStr" else "Doz • $dTimeStr",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextHealthPrimary
                                        )
                                        Text(
                                            text = "%.1f IU %s".format(d.doseUnits, if (d.type == DoseType.BOLUS) "Bolus" else "Bazal"),
                                            fontSize = 10.sp,
                                            color = TextHealthMuted
                                        )
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        // Fasting Value
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = if (lang == com.example.util.AppLanguage.EN) "Fasting" else "Açlık",
                                                fontSize = 9.sp,
                                                color = TextHealthMuted
                                            )
                                            Text(
                                                text = if (d.bloodGlucose != null) "${d.bloodGlucose} mg" else "--",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = BioHealingGreen
                                            )
                                        }

                                        Text(
                                            text = " ➔ ",
                                            fontSize = 12.sp,
                                            color = TextHealthMuted,
                                            modifier = Modifier.padding(horizontal = 4.dp)
                                        )

                                        // 2-Hour Postprandial Value
                                        Column(horizontalAlignment = Alignment.Start) {
                                            Text(
                                                text = if (lang == com.example.util.AppLanguage.EN) "2-Hr Post" else "2. Saat",
                                                fontSize = 9.sp,
                                                color = TextHealthMuted
                                            )
                                            Text(
                                                text = if (d.postDoseGlucose != null) "${d.postDoseGlucose} mg" else if (lang == com.example.util.AppLanguage.EN) "Pending" else "Bekleniyor",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (d.postDoseGlucose != null) AlertPharmacyRuby else Color(0xFFFBBF24)
                                            )
                                        }

                                        // Delta badge if both are present
                                        if (d.bloodGlucose != null && d.postDoseGlucose != null) {
                                            val diff = d.postDoseGlucose - d.bloodGlucose
                                            val diffStr = if (diff >= 0) "+$diff" else "$diff"
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = if (diff in 0..60) BioGreenContainer else AlertRubyContainer
                                            ) {
                                                Text(
                                                    text = diffStr,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = if (diff in 0..60) BioHealingGreen else AlertPharmacyRuby,
                                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else if (overallAvgFasting != null || overallAvgPostprandial != null) {
                        // Helpful fallback stats from all historical records if chosen day has no measurements yet
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            color = ChartSlotBg,
                            border = BorderStroke(0.8.dp, HealthTealBright.copy(alpha = 0.4f))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = if (lang == com.example.util.AppLanguage.EN) "ALL-TIME GLUCOSE BENCHMARK" else "GENEL GEÇMİŞ ŞEKER ORTALAMANIZ",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = HealthCyanBright
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = if (lang == com.example.util.AppLanguage.EN)
                                            "General Fasting: ${if (overallAvgFasting != null) "%.0f mg/dL".format(overallAvgFasting) else "--"}"
                                        else
                                            "Genel Açlık: ${if (overallAvgFasting != null) "%.0f mg/dL".format(overallAvgFasting) else "--"}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = BioHealingGreen
                                    )
                                    Text(
                                        text = if (lang == com.example.util.AppLanguage.EN)
                                            "General 2-Hr Post: ${if (overallAvgPostprandial != null) "%.0f mg/dL".format(overallAvgPostprandial) else "--"}"
                                        else
                                            "Genel 2. Saat Tokluk: ${if (overallAvgPostprandial != null) "%.0f mg/dL".format(overallAvgPostprandial) else "--"}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = AlertPharmacyRuby
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }



        // Injection Sites Used on Selected Day
        if (sitesGrouped.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = HealthSurface),
                    border = BorderStroke(1.dp, HealthBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .background(HealthTeal, RoundedCornerShape(7.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.RotateRight,
                                    contentDescription = null,
                                    tint = IconOnAccent,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (lang == com.example.util.AppLanguage.EN) "INJECTION SITES USED THAT DAY" else "O GÜN KULLANILAN ENJEKSİYON BÖLGELERİ",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = HealthMint,
                                letterSpacing = 0.5.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        sitesGrouped.forEach { (site, siteDoses) ->
                            val siteUnits = siteDoses.sumOf { it.doseUnits }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(HealthSurfaceVariant)
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(HealthTealBright, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = com.example.util.Strings.siteDisplayName(site, lang),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextHealthPrimary
                                        )
                                        Text(
                                            text = if (lang == com.example.util.AppLanguage.EN)
                                                "Absorption: ${com.example.util.Strings.siteAbsorptionRate(site, lang)}"
                                            else
                                                "Emilim: ${site.absorptionRate}",
                                            fontSize = 9.sp,
                                            color = TextHealthMuted
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = if (lang == com.example.util.AppLanguage.EN) "${siteDoses.size} Doses" else "${siteDoses.size} Doz",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextHealthSecondary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        color = ChartSlotBg,
                                        shape = RoundedCornerShape(6.dp),
                                        border = BorderStroke(0.8.dp, HealthTealBright.copy(alpha = 0.5f))
                                    ) {
                                        Text(
                                            text = "%.1f IU".format(siteUnits),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = HealthCyanBright,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Chronological Doses for Selected Date
        item {
            Text(
                text = if (lang == com.example.util.AppLanguage.EN) "DOSE LIST FOR THAT DAY (${selectedDayDoses.size})" else "O GÜNÜN DOZ LİSTESİ (${selectedDayDoses.size})",
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = HealthMint,
                letterSpacing = 0.5.sp
            )
        }

        if (selectedDayDoses.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = HealthSurfaceVariant,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, HealthBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = TextHealthMuted,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (lang == com.example.util.AppLanguage.EN) "No insulin doses recorded for this date." else "Bu tarihte kaydedilmiş insülin dozu bulunmuyor.",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextHealthMuted
                        )
                    }
                }
            }
        } else {
            items(selectedDayDoses) { dose ->
                val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(dose.timestamp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = HealthSurface),
                    border = BorderStroke(1.dp, if (dose.type == DoseType.BOLUS) BolusGold.copy(alpha = 0.35f) else BasalCyan.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (dose.type == DoseType.BOLUS) BolusContainerDark else BasalContainerDark,
                                border = BorderStroke(1.dp, if (dose.type == DoseType.BOLUS) BolusGold else BasalCyan)
                            ) {
                                Text(
                                    text = if (dose.type == DoseType.BOLUS) "BOLUS" else (if (lang == com.example.util.AppLanguage.EN) "BASAL" else "BAZAL"),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (dose.type == DoseType.BOLUS) BolusGold else BasalCyan,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = timeStr,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = TextHealthPrimary
                                    )
                                    if (dose.mealTag.isNotBlank()) {
                                        Text(
                                            text = " • ${dose.mealTag}",
                                            fontSize = 11.sp,
                                            color = HealthMint,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Text(
                                    text = if (lang == com.example.util.AppLanguage.EN)
                                        "Site: ${com.example.util.Strings.siteDisplayName(dose.injectionSite, lang)}"
                                    else
                                        "Bölge: ${dose.injectionSite.displayName}",
                                    fontSize = 10.sp,
                                    color = TextHealthSecondary
                                )
                                if (dose.bloodGlucose != null) {
                                    Text(
                                        text = if (lang == com.example.util.AppLanguage.EN) "🩸 Fasting: ${dose.bloodGlucose} mg/dL" else "🩸 Şeker: ${dose.bloodGlucose} mg/dL",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = HealthCyanBright
                                    )
                                }
                            }
                        }

                        Text(
                            text = "%.1f IU".format(dose.doseUnits),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = if (dose.type == DoseType.BOLUS) BolusGold else BasalCyan
                        )
                    }
                }
            }
        }

        // Long-term 7-Day & All-Time Aggregate Analytics
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = HealthSurfaceVariant),
                border = BorderStroke(1.dp, HealthTealBright.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = HealthTealBright,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (lang == com.example.util.AppLanguage.EN) "GENERAL & WEEKLY TRENDS" else "GENEL VE HAFTALIK EĞİLİMLER",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = HealthMint,
                            letterSpacing = 0.5.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = if (lang == com.example.util.AppLanguage.EN) "Last 7 Days Daily Avg:" else "Son 7 Günlük Günlük Ortalama:",
                                fontSize = 11.sp,
                                color = TextHealthSecondary
                            )
                            Text(
                                text = if (lang == com.example.util.AppLanguage.EN) "%.1f IU / day".format(avgDailyUnits7Days) else "%.1f IU / gün".format(avgDailyUnits7Days),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = HealthCyanBright
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = if (lang == com.example.util.AppLanguage.EN) "Total Recorded Doses:" else "Toplam Kayıtlı Doz:",
                                fontSize = 11.sp,
                                color = TextHealthSecondary
                            )
                            Text(
                                text = if (lang == com.example.util.AppLanguage.EN) "${doses.size} Doses" else "${doses.size} Doz",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = TextHealthPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (lang == com.example.util.AppLanguage.EN)
                            "💡 Consistent statistical tracking helps your diabetes care team make precise clinical decisions during dose adjustments."
                        else
                            "💡 Düzenli istatistik takibi, doktor veya diyabet eğitimcinizle yapacağınız doz revizyonlarında en doğru klinik kararları almanızı sağlar.",
                        fontSize = 10.sp,
                        color = HealthMint,
                        lineHeight = 13.sp
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun StatMetricTile(
    title: String,
    value: String,
    unit: String,
    subtext: String,
    color: Color,
    bgColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = HealthSurfaceVariant,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold,
                color = color,
                letterSpacing = 0.4.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = color,
                    letterSpacing = (-0.5).sp
                )
                if (unit.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = unit,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = color.copy(alpha = 0.85f),
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtext,
                fontSize = 9.sp,
                color = TextHealthMuted,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun StatPeriodCard(
    title: String,
    timeRange: String,
    units: Double,
    maxUnits: Double,
    doseCount: Int,
    fastingValues: List<Int>,
    postprandialValues: List<Int>,
    icon: ImageVector,
    accentColor: Color,
    bgStartColor: Color,
    lang: com.example.util.AppLanguage,
    modifier: Modifier = Modifier
) {
    val hasUsage = units > 0.0 || fastingValues.isNotEmpty() || postprandialValues.isNotEmpty()
    val progressFraction = if (units > 0.0) {
        (units / maxUnits).toFloat().coerceIn(0.12f, 1f)
    } else 0f

    val cardBg = if (hasUsage) {
        Brush.verticalGradient(listOf(bgStartColor, HealthSurfaceVariant))
    } else {
        Brush.verticalGradient(listOf(ChartSlotBg, ChartSlotBg.copy(alpha = 0.85f)))
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(cardBg)
            .border(
                width = 1.dp,
                color = if (hasUsage) accentColor.copy(alpha = 0.5f) else HealthBorder.copy(alpha = 0.5f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(12.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header Row: Icon + Title & Time + Dose Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .background(
                                color = if (hasUsage) accentColor.copy(alpha = 0.22f) else HealthSurfaceVariant,
                                shape = RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = title,
                            tint = if (hasUsage) accentColor else TextHealthMuted,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = title,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (hasUsage) TextHealthPrimary else TextHealthMuted
                        )
                        Text(
                            text = timeRange,
                            fontSize = 8.5.sp,
                            color = TextHealthMuted
                        )
                    }
                }

                if (doseCount > 0) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = accentColor.copy(alpha = 0.2f),
                        border = BorderStroke(0.6.dp, accentColor.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = if (lang == com.example.util.AppLanguage.EN) "$doseCount Dose" else "$doseCount Doz",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = accentColor,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Dose Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = if (units > 0.0) "%.1f".format(units) else "0.0",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        color = if (units > 0.0) accentColor else TextHealthMuted
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "IU",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (units > 0.0) accentColor.copy(alpha = 0.9f) else TextHealthMuted,
                        modifier = Modifier.padding(bottom = 1.5.dp)
                    )
                }

                Text(
                    text = if (lang == com.example.util.AppLanguage.EN) "Dose" else "Doz",
                    fontSize = 9.sp,
                    color = TextHealthMuted,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Dose progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(ChartTrackBg)
            ) {
                if (progressFraction > 0f) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progressFraction)
                            .fillMaxHeight()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(accentColor.copy(alpha = 0.65f), accentColor)
                                )
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Divider separating insulin from blood glucose
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(HealthBorder.copy(alpha = 0.5f))
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Blood Glucose Measurements Section (Açlık & Tokluk)
            // Row 1: Açlık (Doz Öncesi)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(BioHealingGreen, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (lang == com.example.util.AppLanguage.EN) "Fasting:" else "Açlık:",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextHealthSecondary
                    )
                }

                Text(
                    text = if (fastingValues.isNotEmpty()) {
                        "${fastingValues.average().toInt()} mg"
                    } else "--",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (fastingValues.isNotEmpty()) BioHealingGreen else TextHealthMuted
                )
            }

            Spacer(modifier = Modifier.height(3.dp))

            // Row 2: Tokluk (2. Saat)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(AlertPharmacyRuby, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (lang == com.example.util.AppLanguage.EN) "2-Hr Post:" else "Tokluk:",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextHealthSecondary
                    )
                }

                Text(
                    text = if (postprandialValues.isNotEmpty()) {
                        "${postprandialValues.average().toInt()} mg"
                    } else "--",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (postprandialValues.isNotEmpty()) AlertPharmacyRuby else TextHealthMuted
                )
            }
        }
    }
}

@Composable
private fun GlucoseMetricCard(
    title: String,
    targetRange: String,
    avgValue: Double?,
    minValue: Int?,
    maxValue: Int?,
    count: Int,
    targetCompliance: Int?,
    accentColor: Color,
    containerColor: Color,
    borderColor: Color,
    lang: com.example.util.AppLanguage,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = containerColor,
        border = BorderStroke(1.2.dp, borderColor)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                fontSize = 9.5.sp,
                fontWeight = FontWeight.ExtraBold,
                color = accentColor,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = if (lang == com.example.util.AppLanguage.EN) "Target: $targetRange" else "Hedef: $targetRange",
                fontSize = 9.sp,
                color = TextHealthMuted
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Main Average Value
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = if (avgValue != null) "%.0f".format(avgValue) else "--",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = accentColor
                )
                if (avgValue != null) {
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "mg/dL",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor.copy(alpha = 0.85f),
                        modifier = Modifier.padding(bottom = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Min - Max or count info
            if (minValue != null && maxValue != null) {
                Text(
                    text = "Min: $minValue • Max: $maxValue",
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextHealthSecondary
                )
            } else {
                Text(
                    text = if (lang == com.example.util.AppLanguage.EN) "No reading for day" else "Bugün ölçüm yok",
                    fontSize = 9.5.sp,
                    color = TextHealthMuted
                )
            }

            if (targetCompliance != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = accentColor.copy(alpha = 0.18f)
                ) {
                    Text(
                        text = if (lang == com.example.util.AppLanguage.EN) "%$targetCompliance in target ($count)" else "Hedefte: %$targetCompliance ($count)",
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = accentColor,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

