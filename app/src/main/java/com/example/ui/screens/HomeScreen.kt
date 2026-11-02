package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import com.example.R
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Bloodtype
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DoseRecord
import com.example.data.model.DoseReminder
import com.example.data.model.DoseType
import com.example.data.model.InjectionSite
import com.example.data.model.InsulinStock
import com.example.ui.components.ActiveReminderBanner
import com.example.ui.components.BloodGlucoseCheckBanner
import com.example.ui.components.BodyRotationAssistant
import com.example.ui.components.HomeGlucoseInsulinDynamicsChart
import com.example.ui.components.QuickDoseDialog
import com.example.ui.components.SettingsDialog
import com.example.ui.theme.*
import com.example.viewmodel.DailySummary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    todaySummary: DailySummary,
    todayDoses: List<DoseRecord>,
    lastDose: DoseRecord?,
    recommendedSite: InjectionSite,
    activeReminder: DoseReminder?,
    stocks: List<InsulinStock> = emptyList(),
    pendingGlucoseCheckDose: DoseRecord? = null,
    allDoses: List<DoseRecord> = emptyList(),
    appLanguage: com.example.util.AppLanguage = com.example.util.AppLanguage.TR,
    onLanguageSelected: (com.example.util.AppLanguage) -> Unit = {},
    onConfirmActiveReminder: (DoseReminder) -> Unit = {},
    onSnoozeActiveReminder15: (DoseReminder) -> Unit = {},
    onRecordDose: (
        units: Double,
        type: DoseType,
        site: InjectionSite,
        mealTag: String,
        bloodGlucose: Int?,
        notes: String,
        insulinName: String,
        trackFastingGlucose: Boolean,
        trackPostGlucose: Boolean
    ) -> Unit,
    onOpenGlucoseDialogForDose: (DoseRecord) -> Unit = {},
    onSendTestGlucoseNotification: (Long, Double, DoseType) -> Unit = { _, _, _ -> },
    onAddPenToStock: (DoseType) -> Unit = {},
    onUpdateStock: (InsulinStock) -> Unit = {},
    onDeleteDose: (DoseRecord) -> Unit = {},
    onNavigateToRotation: () -> Unit = {},
    onNavigateToReminders: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showQuickDoseDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var selectedRotationSite by remember { mutableStateOf(recommendedSite) }
    var dialogInitialType by remember { mutableStateOf(DoseType.BOLUS) }
    var dialogInitialUnits by remember { mutableStateOf(10.0) }
    var dialogInitialTag by remember { mutableStateOf("") }

    // Dynamic Time-of-Day Greeting based on language
    val calendar = Calendar.getInstance()
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val greeting = com.example.util.Strings.greeting(appLanguage, hour)
    val currentDateStr = SimpleDateFormat(
        if (appLanguage == com.example.util.AppLanguage.EN) "EEEE, MMMM d, HH:mm" else "d MMMM EEEE, HH:mm",
        if (appLanguage == com.example.util.AppLanguage.EN) Locale("en") else Locale("tr")
    ).format(Date())

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. TOP HEADER: Greeting, Date, Language Switcher (TR / EN) & Settings Icon
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = greeting,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Black,
                                color = TextHealthPrimary,
                                letterSpacing = (-0.5).sp,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = currentDateStr,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = HealthMint,
                                maxLines = 1
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Language Switcher (TR / EN - Türkçe ve İngilizce)
                            Surface(
                                color = HealthSurfaceVariant,
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.2.dp, HealthBorder),
                                modifier = Modifier.testTag("language_switcher")
                            ) {
                                Row(
                                    modifier = Modifier.padding(3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Turkish Option
                                    Surface(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .clickable { onLanguageSelected(com.example.util.AppLanguage.TR) }
                                            .testTag("lang_tr_btn"),
                                        color = if (appLanguage == com.example.util.AppLanguage.TR) BioGreenContainer else Color.Transparent,
                                        border = if (appLanguage == com.example.util.AppLanguage.TR) BorderStroke(1.dp, BioHealingGreen) else null
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(text = "🇹🇷", fontSize = 11.sp)
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = "TR",
                                                fontSize = 11.sp,
                                                fontWeight = if (appLanguage == com.example.util.AppLanguage.TR) FontWeight.ExtraBold else FontWeight.Medium,
                                                color = if (appLanguage == com.example.util.AppLanguage.TR) BioHealingGreen else TextHealthSecondary
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(2.dp))

                                    // English Option
                                    Surface(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .clickable { onLanguageSelected(com.example.util.AppLanguage.EN) }
                                            .testTag("lang_en_btn"),
                                        color = if (appLanguage == com.example.util.AppLanguage.EN) HealthSurface else Color.Transparent,
                                        border = if (appLanguage == com.example.util.AppLanguage.EN) BorderStroke(1.dp, HealthTealBright) else null
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(text = "🇬🇧", fontSize = 11.sp)
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = "EN",
                                                fontSize = 11.sp,
                                                fontWeight = if (appLanguage == com.example.util.AppLanguage.EN) FontWeight.ExtraBold else FontWeight.Medium,
                                                color = if (appLanguage == com.example.util.AppLanguage.EN) HealthCyanBright else TextHealthSecondary
                                            )
                                        }
                                    }
                                }
                            }

                            // Compact Settings Icon Button next to TR/EN switcher
                            Surface(
                                color = HealthTeal,
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.2.dp, HealthBorder),
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { showSettingsDialog = true }
                                    .testTag("settings_icon_button")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = if (appLanguage == com.example.util.AppLanguage.EN) "Settings" else "Ayarlar",
                                        tint = IconOnAccent,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 1.5 2-HOUR BLOOD GLUCOSE CHECK BANNER (Bildirim & Ölçüm Girişi)
            if (pendingGlucoseCheckDose != null) {
                item {
                    BloodGlucoseCheckBanner(
                        dose = pendingGlucoseCheckDose,
                        onOpenDialog = { onOpenGlucoseDialogForDose(pendingGlucoseCheckDose) },
                        lang = appLanguage
                    )
                }
            }

            // 2. PROMINENT QUICK DOSE / LOG DOSE ACTION (EN BAŞ SIRADA)
            item {
                QuickDoseHeroAction(
                    recommendedSite = recommendedSite,
                    lang = appLanguage,
                    onOpenDialog = { type, units, tag ->
                        dialogInitialType = type
                        dialogInitialUnits = units
                        dialogInitialTag = tag
                        selectedRotationSite = recommendedSite
                        showQuickDoseDialog = true
                    }
                )
            }

            // 2.5 AVERAGE GLUCOSE PROFILE & AVERAGE INSULIN CONSUMPTION PROFILE CHARTS
            item {
                val chartDoses = if (allDoses.isNotEmpty()) allDoses else todayDoses
                HomeGlucoseInsulinDynamicsChart(
                    doses = chartDoses,
                    lang = appLanguage
                )
            }

            item {
                val chartDoses = if (allDoses.isNotEmpty()) allDoses else todayDoses
                com.example.ui.components.HomeInsulinConsumptionProfileChart(
                    doses = chartDoses,
                    lang = appLanguage
                )
            }

            // 4. ACTIVE REMINDER BANNER (Eğer hatırlatıcı saati geldiyse hemen göster)
            if (activeReminder != null) {
                item {
                    ActiveReminderBanner(
                        reminder = activeReminder,
                        recommendedSite = recommendedSite,
                        onConfirm = onConfirmActiveReminder,
                        onSnooze15 = onSnoozeActiveReminder15,
                        onCustomize = { reminder ->
                            dialogInitialType = reminder.type
                            dialogInitialUnits = reminder.defaultDoseUnits
                            dialogInitialTag = reminder.label
                            selectedRotationSite = recommendedSite
                            showQuickDoseDialog = true
                        }
                    )
                }
            }

            // 5. ANATOMICAL HUMAN BODY INJECTION ROTATION ASSISTANT
            item {
                BodyRotationAssistant(
                    lastSite = lastDose?.injectionSite,
                    recommendedSite = recommendedSite,
                    selectedSite = selectedRotationSite,
                    onSiteSelected = { site ->
                        selectedRotationSite = site
                    },
                    showDetailedGuide = true,
                    lang = appLanguage
                )
            }

            // 7. TODAY'S DOSE TIMELINE & HISTORY
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = com.example.util.Strings.todayDosesTitle(appLanguage, todayDoses.size),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextHealthPrimary,
                        letterSpacing = (-0.3).sp
                    )
                }
            }

            if (todayDoses.isEmpty()) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = HealthSurfaceVariant,
                        border = BorderStroke(1.2.dp, HealthBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                .size(50.dp)
                                .background(HealthTeal, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MedicalServices,
                                    contentDescription = null,
                                    tint = IconOnAccent,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = com.example.util.Strings.noDosesTodayTitle(appLanguage),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = TextHealthPrimary
                            )
                            Text(
                                text = com.example.util.Strings.noDosesTodaySubtitle(appLanguage),
                                style = MaterialTheme.typography.bodySmall,
                                color = TextHealthSecondary,
                                modifier = Modifier.padding(top = 4.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(todayDoses, key = { it.id }) { dose ->
                    DoseRecordCard(
                        dose = dose,
                        lang = appLanguage,
                        onDelete = { onDeleteDose(dose) },
                        onOpenGlucoseDialog = onOpenGlucoseDialogForDose
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(84.dp))
            }
        }
    }

    if (showQuickDoseDialog) {
        QuickDoseDialog(
            initialType = dialogInitialType,
            initialUnits = dialogInitialUnits,
            initialSite = selectedRotationSite,
            recommendedSite = recommendedSite,
            lastSite = lastDose?.injectionSite,
            initialMealTag = dialogInitialTag,
            onDismiss = { showQuickDoseDialog = false },
            onSave = onRecordDose,
            lang = appLanguage
        )
    }

    if (showSettingsDialog) {
        SettingsDialog(
            lang = appLanguage,
            onDismiss = { showSettingsDialog = false }
        )
    }
}

/**
 * Elegant image-backed Quick Dose Action Component placed right at the top above insulin stock.
 * Features a modern medical illustration banner button without distracting animations.
 */
@Composable
private fun QuickDoseHeroAction(
    recommendedSite: InjectionSite,
    onOpenDialog: (DoseType, Double, String) -> Unit,
    lang: com.example.util.AppLanguage = com.example.util.AppLanguage.TR
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("quick_dose_hero_action"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = HealthSurface),
        border = BorderStroke(1.2.dp, HealthBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        // High-end Image-backed Hero Action Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .clickable {
                    onOpenDialog(DoseType.BOLUS, 10.0, "")
                }
                .testTag("button_home_quick_log")
        ) {
            // Background medical banner image
            Image(
                painter = painterResource(id = R.drawable.img_dose_button),
                contentDescription = if (lang == com.example.util.AppLanguage.EN) "Inject & Log Dose" else "Doz Vur ve Kaydet",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // High contrast gradient overlay for crisp readability in both Day and Night themes
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                HealthSurface.copy(alpha = 0.94f),
                                HealthSurface.copy(alpha = 0.78f),
                                HealthSurfaceVariant.copy(alpha = 0.45f)
                            )
                        )
                    )
            )

            // Content inside the image button
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.Center) {
                    Text(
                        text = if (lang == com.example.util.AppLanguage.EN) "QUICK INSULIN LOG" else "HIZLI İNSÜLİN KAYDI",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = HealthMint,
                        letterSpacing = 0.8.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (lang == com.example.util.AppLanguage.EN) "Log & Inject Dose" else "Doz Vur & Kaydet",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = TextHealthPrimary
                    )
                }

                Surface(
                    color = HealthTeal,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = if (lang == com.example.util.AppLanguage.EN) "Log Dose" else "Giriş Yap",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = IconOnAccent,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

/**
 * Comprehensive Daily Insulin Consumption Statistics Card.
 * Displays real-time daily metrics:
 * 1. Total Daily Consumption (IU) & Dose Counter
 * 2. Basal vs. Bolus Breakdown (Units & Proportions)
 * 3. Dual-tone proportional distribution bar (Bolus gold & Basal cyan)
 * 4. Time-of-day consumption breakdown (Sabah, Öğle, Akşam, Gece)
 * 5. Clinical insulin balance guidance insight
 */
@Composable
fun DailyInsulinConsumptionCard(
    summary: DailySummary,
    todayDoses: List<DoseRecord>,
    modifier: Modifier = Modifier
) {
    val totalUnits = summary.totalUnits
    val basalUnits = summary.basalUnits
    val bolusUnits = summary.bolusUnits
    val hasDoses = totalUnits > 0.0

    // Current hour to highlight the active period
    val currentHour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }

    // Time-of-day consumption breakdown from today's doses
    val morningDoses = remember(todayDoses) {
        todayDoses.filter {
            val cal = Calendar.getInstance().apply { timeInMillis = it.timestamp }
            cal.get(Calendar.HOUR_OF_DAY) in 6..11
        }
    }
    val morningUnits = remember(morningDoses) { morningDoses.sumOf { it.doseUnits } }

    val afternoonDoses = remember(todayDoses) {
        todayDoses.filter {
            val cal = Calendar.getInstance().apply { timeInMillis = it.timestamp }
            cal.get(Calendar.HOUR_OF_DAY) in 12..17
        }
    }
    val afternoonUnits = remember(afternoonDoses) { afternoonDoses.sumOf { it.doseUnits } }

    val eveningDoses = remember(todayDoses) {
        todayDoses.filter {
            val cal = Calendar.getInstance().apply { timeInMillis = it.timestamp }
            cal.get(Calendar.HOUR_OF_DAY) in 18..22
        }
    }
    val eveningUnits = remember(eveningDoses) { eveningDoses.sumOf { it.doseUnits } }

    val nightDoses = remember(todayDoses) {
        todayDoses.filter {
            val cal = Calendar.getInstance().apply { timeInMillis = it.timestamp }
            val h = cal.get(Calendar.HOUR_OF_DAY)
            h in 23..24 || h in 0..5
        }
    }
    val nightUnits = remember(nightDoses) { nightDoses.sumOf { it.doseUnits } }

    val maxPeriodUnits = maxOf(morningUnits, afternoonUnits, eveningUnits, nightUnits, 1.0)
    val bolusRatio = if (hasDoses) (bolusUnits / totalUnits).toFloat() else 0.5f
    val basalRatio = if (hasDoses) (basalUnits / totalUnits).toFloat() else 0.5f

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("today_summary_card")
            .testTag("daily_insulin_consumption_stats"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = HealthSurface),
        border = BorderStroke(1.2.dp, HealthBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header: Title, Subtitle and Status Tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(HealthTeal, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = "Günlük Tüketim İstatistikleri",
                            tint = IconOnAccent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Günlük İnsülin Tüketimi",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextHealthPrimary
                        )
                        Text(
                            text = "Günün Doz Analizi & Dağılımı",
                            fontSize = 10.sp,
                            color = HealthMint,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Surface(
                    color = HealthSurfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, HealthBorder)
                ) {
                    Text(
                        text = if (hasDoses) "${summary.doseCount} Doz Vuruldu" else "0 Doz",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (hasDoses) HealthTealBright else TextHealthMuted,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 3 Modern Metric Summary Tiles
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Total
                ModernSummaryTile(
                    title = "TOPLAM TÜKETİM",
                    value = "%.1f".format(totalUnits),
                    unit = "IU",
                    color = HealthCyanBright,
                    bgColor = BasalKpiBg,
                    modifier = Modifier.weight(1f)
                )

                // Bolus
                ModernSummaryTile(
                    title = "BOLUS (ÖĞÜN)",
                    value = "%.1f".format(bolusUnits),
                    unit = if (hasDoses) "%${(bolusRatio * 100).toInt()}" else "IU",
                    color = BolusGold,
                    bgColor = BolusContainerDark,
                    modifier = Modifier.weight(1f)
                )

                // Basal
                ModernSummaryTile(
                    title = "BAZAL (UZUN)",
                    value = "%.1f".format(basalUnits),
                    unit = if (hasDoses) "%${(basalRatio * 100).toInt()}" else "IU",
                    color = BasalCyan,
                    bgColor = BasalContainerDark,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Proportional Distribution Visual Bar (Bolus vs Basal)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(HealthSurfaceVariant)
                    .padding(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "BAZAL / BOLUS DENGESİ",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = HealthMint,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = if (hasDoses) "Toplam %.1f IU".format(totalUnits) else "Doz Kaydı Yok",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextHealthMuted
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                if (hasDoses) {
                    // Split Dual Colored Progress Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                    ) {
                        if (bolusUnits > 0) {
                            Box(
                                modifier = Modifier
                                    .weight(bolusRatio.coerceAtLeast(0.02f))
                                    .fillMaxHeight()
                                    .background(BolusGold)
                            )
                        }
                        if (basalUnits > 0) {
                            Box(
                                modifier = Modifier
                                    .weight(basalRatio.coerceAtLeast(0.02f))
                                    .fillMaxHeight()
                                    .background(BasalCyan)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "⚡ Bolus: %.1f IU (%d%%)".format(bolusUnits, (bolusRatio * 100).toInt()),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = BolusGold
                        )
                        Text(
                            text = "🌙 Bazal: %.1f IU (%d%%)".format(basalUnits, (basalRatio * 100).toInt()),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = BasalCyan
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(HealthBorder)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Bugün için henüz insülin dozu girilmedi",
                        fontSize = 10.sp,
                        color = TextHealthMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Time of Day Consumption Breakdown (Sabah, Öğle, Akşam, Gece)
            Column(modifier = Modifier.fillMaxWidth()) {
                // Section Header: Title + Context Indicator
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .background(HealthTeal, RoundedCornerShape(6.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = IconOnAccent,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "ZAMAN DİLİMİNE GÖRE DAĞILIM",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = HealthMint,
                            letterSpacing = 0.5.sp
                        )
                    }

                    // Active time slot pill indicator
                    Surface(
                        color = ChartSlotBg,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(0.8.dp, HealthBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(HealthTealBright, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Şu an: ${when (currentHour) {
                                    in 6..11 -> "Sabah"
                                    in 12..17 -> "Öğle"
                                    in 18..22 -> "Akşam"
                                    else -> "Gece"
                                }}",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = HealthTealBright
                            )
                        }
                    }
                }

                val morningPeriod = TimePeriodItem(
                    title = "Sabah",
                    timeRange = "06:00 - 12:00",
                    units = morningUnits,
                    doseCount = morningDoses.size,
                    icon = Icons.Default.WbSunny,
                    accentColor = if (isDayTheme) Color(0xFFB45309) else Color(0xFFFBBF24),
                    bgStartColor = if (isDayTheme) Color(0xFFF8E7C8) else Color(0xFF2E2208),
                    isCurrent = currentHour in 6..11
                )

                val afternoonPeriod = TimePeriodItem(
                    title = "Öğle",
                    timeRange = "12:00 - 18:00",
                    units = afternoonUnits,
                    doseCount = afternoonDoses.size,
                    icon = Icons.Default.LightMode,
                    accentColor = if (isDayTheme) Color(0xFF0369A1) else Color(0xFF38BDF8),
                    bgStartColor = if (isDayTheme) Color(0xFFCDE5F8) else Color(0xFF092A3A),
                    isCurrent = currentHour in 12..17
                )

                val eveningPeriod = TimePeriodItem(
                    title = "Akşam",
                    timeRange = "18:00 - 23:00",
                    units = eveningUnits,
                    doseCount = eveningDoses.size,
                    icon = Icons.Default.WbTwilight,
                    accentColor = if (isDayTheme) Color(0xFF6D28D9) else Color(0xFFC084FC),
                    bgStartColor = if (isDayTheme) Color(0xFFDED6F6) else Color(0xFF27153B),
                    isCurrent = currentHour in 18..22
                )

                val nightPeriod = TimePeriodItem(
                    title = "Gece",
                    timeRange = "23:00 - 06:00",
                    units = nightUnits,
                    doseCount = nightDoses.size,
                    icon = Icons.Default.NightsStay,
                    accentColor = if (isDayTheme) Color(0xFF047857) else Color(0xFF34D399),
                    bgStartColor = if (isDayTheme) Color(0xFFC9EDE0) else Color(0xFF0A2B24),
                    isCurrent = currentHour in 23..24 || currentHour in 0..5
                )

                // 2x2 Grid Layout with spacious, readable period cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TimePeriodCard(
                        data = morningPeriod,
                        maxPeriodUnits = maxPeriodUnits,
                        modifier = Modifier.weight(1f)
                    )
                    TimePeriodCard(
                        data = afternoonPeriod,
                        maxPeriodUnits = maxPeriodUnits,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TimePeriodCard(
                        data = eveningPeriod,
                        maxPeriodUnits = maxPeriodUnits,
                        modifier = Modifier.weight(1f)
                    )
                    TimePeriodCard(
                        data = nightPeriod,
                        maxPeriodUnits = maxPeriodUnits,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Guidance & Clinical Insight Note
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(HealthSurfaceVariant)
                    .border(0.8.dp, HealthBorder, RoundedCornerShape(10.dp))
                    .padding(horizontal = 10.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when {
                        !hasDoses -> "💡 Bugün henüz doz vurulmadı. Yukarıdaki 'Doz Vur & Kaydet' butonuyla anında kaydedebilirsiniz."
                        bolusUnits > 0 && basalUnits > 0 -> "✓ Bazal ve Bolus dengeniz aktif kaydediliyor. İdeal günlük denge genellikle ~%50 Bolus / ~%50 Bazal civarındadır."
                        bolusUnits > 0 -> "ℹ️ Bugün yalnızca hızlı etkili bolus dozu kaydedildi. Akşam bazal dozu hatırlatıcınızı kontrol etmeyi unutmayın."
                        else -> "ℹ️ Bugün yalnızca uzun etkili bazal dozu kaydedildi. Öğün bolus dozlarınızı zamanında kaydetmeye özen gösterin."
                    },
                    fontSize = 10.sp,
                    color = HealthMint,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 13.sp
                )
            }
        }
    }
}

data class TimePeriodItem(
    val title: String,
    val timeRange: String,
    val units: Double,
    val doseCount: Int,
    val icon: ImageVector,
    val accentColor: Color,
    val bgStartColor: Color,
    val isCurrent: Boolean
)

@Composable
fun TimePeriodCard(
    data: TimePeriodItem,
    maxPeriodUnits: Double,
    modifier: Modifier = Modifier
) {
    val hasUsage = data.units > 0.0
    val progressFraction = if (hasUsage) {
        (data.units / maxPeriodUnits).toFloat().coerceIn(0.12f, 1f)
    } else 0f

    val cardBg = if (hasUsage) {
        Brush.verticalGradient(
            colors = listOf(
                data.bgStartColor,
                HealthSurfaceVariant
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                ChartSlotBg,
                ChartSlotBg.copy(alpha = 0.85f)
            )
        )
    }

    val borderColor = when {
        data.isCurrent && hasUsage -> data.accentColor
        data.isCurrent -> data.accentColor.copy(alpha = 0.7f)
        hasUsage -> data.accentColor.copy(alpha = 0.45f)
        else -> HealthBorder.copy(alpha = 0.5f)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(cardBg)
            .border(
                width = if (data.isCurrent) 1.5.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(14.dp)
            )
            .padding(10.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header: Icon badge + Period title + Time range or "ŞİMDİ" tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(
                                color = if (hasUsage || data.isCurrent) data.accentColor.copy(alpha = 0.18f) else HealthSurfaceVariant,
                                shape = RoundedCornerShape(7.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = data.icon,
                            contentDescription = data.title,
                            tint = if (hasUsage || data.isCurrent) data.accentColor else TextHealthMuted,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = data.title,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (hasUsage || data.isCurrent) TextHealthPrimary else TextHealthMuted
                        )
                        Text(
                            text = data.timeRange,
                            fontSize = 8.5.sp,
                            color = TextHealthMuted
                        )
                    }
                }

                if (data.isCurrent) {
                    Surface(
                        color = data.accentColor.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(0.6.dp, data.accentColor)
                    ) {
                        Text(
                            text = "ŞİMDİ",
                            fontSize = 7.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = data.accentColor,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Value & Dose Count
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "%.1f".format(data.units),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = if (hasUsage) data.accentColor else TextHealthMuted,
                        letterSpacing = (-0.5).sp
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "IU",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (hasUsage) data.accentColor.copy(alpha = 0.85f) else TextHealthMuted,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }

                Text(
                    text = if (data.doseCount > 0) "${data.doseCount} Doz" else "0 Doz",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (data.doseCount > 0) TextHealthPrimary else TextHealthMuted,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Horizontal Pill Progress Track
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
                                    listOf(
                                        data.accentColor.copy(alpha = 0.65f),
                                        data.accentColor
                                    )
                                )
                            )
                    )
                }
            }
        }
    }
}

@Composable
fun TodaySummaryCard(
    summary: DailySummary,
    onOpenDoseDialog: () -> Unit = {}
) {
    DailyInsulinConsumptionCard(
        summary = summary,
        todayDoses = emptyList()
    )
}

@Composable
fun ModernSummaryTile(
    title: String,
    value: String,
    unit: String,
    color: Color,
    bgColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            fontSize = 9.sp,
            fontWeight = FontWeight.ExtraBold,
            color = color.copy(alpha = 0.9f),
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = color
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = unit,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = color.copy(alpha = 0.8f),
                modifier = Modifier.padding(bottom = 2.dp)
            )
        }
    }
}

@Composable
fun DoseRecordCard(
    dose: DoseRecord,
    onDelete: () -> Unit,
    onOpenGlucoseDialog: (DoseRecord) -> Unit = {},
    lang: com.example.util.AppLanguage = com.example.util.AppLanguage.TR
) {
    val isBasal = dose.type == DoseType.BASAL
    val tagColor = if (isBasal) BasalIndigo else BolusSunset
    val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(dose.timestamp))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("dose_record_${dose.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = HealthSurface),
        border = BorderStroke(1.2.dp, HealthBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Type badge
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(tagColor.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isBasal) {
                        if (lang == com.example.util.AppLanguage.EN) "BASAL" else "BAZAL"
                    } else "BOLUS",
                    fontWeight = FontWeight.ExtraBold,
                    color = tagColor,
                    fontSize = 10.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "%.1f IU".format(dose.doseUnits),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = tagColor
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "• $timeStr",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = HealthMint
                    )
                    if (dose.mealTag.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = HealthSurfaceVariant,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = dose.mealTag,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextHealthPrimary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Site",
                        modifier = Modifier.size(14.dp),
                        tint = HealthMint
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = com.example.util.Strings.siteDisplayName(dose.injectionSite, lang),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = TextHealthPrimary
                    )
                }

                // Row of Glucose Badges: Pre-Dose Fasting & 2-Hour Postprandial
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Pre-Dose Fasting Glucose Badge (Açlık Şekeri)
                    if (dose.bloodGlucose != null) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = BioGreenContainer,
                            border = BorderStroke(0.8.dp, BioHealingGreen)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bloodtype,
                                    contentDescription = null,
                                    modifier = Modifier.size(10.dp),
                                    tint = BioHealingGreen
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = if (lang == com.example.util.AppLanguage.EN) "Fasting: ${dose.bloodGlucose} mg/dL" else "Açlık: ${dose.bloodGlucose} mg/dL",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BioHealingGreen
                                )
                            }
                        }
                    }

                    // 2-Hour Postprandial Glucose Badge (Tokluk Şekeri)
                    if (dose.postDoseGlucose != null) {
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { onOpenGlucoseDialog(dose) }
                                .testTag("post_glucose_badge_${dose.id}"),
                            color = AlertPharmacyRuby.copy(alpha = 0.18f),
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, AlertPharmacyRuby.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bloodtype,
                                    contentDescription = null,
                                    modifier = Modifier.size(10.dp),
                                    tint = AlertPharmacyRuby
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = if (lang == com.example.util.AppLanguage.EN) "2-Hr: ${dose.postDoseGlucose} mg/dL" else "Tokluk: ${dose.postDoseGlucose} mg/dL",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = AlertPharmacyRuby
                                )
                            }
                        }
                    } else if (dose.trackPostGlucose) {
                        // Prompt to enter 2-hour postprandial glucose (only when post-dose tracking is enabled)
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { onOpenGlucoseDialog(dose) }
                                .testTag("add_post_glucose_badge_${dose.id}"),
                            color = HealthSurfaceVariant,
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, AlertPharmacyRuby.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bloodtype,
                                    contentDescription = null,
                                    modifier = Modifier.size(10.dp),
                                    tint = AlertPharmacyRuby
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = if (lang == com.example.util.AppLanguage.EN) "+ 2-Hr Post Glucose" else "+ 2. Saat Tokluk Şekeri",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AlertPharmacyRuby
                                )
                            }
                        }
                    }
                }

                if (dose.notes.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = dose.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextHealthSecondary,
                        maxLines = 1
                    )
                }
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("delete_dose_${dose.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = com.example.util.Strings.delete(lang),
                    tint = TextHealthSecondary.copy(alpha = 0.5f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
