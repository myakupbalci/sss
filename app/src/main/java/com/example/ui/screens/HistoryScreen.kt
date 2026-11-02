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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import com.example.ui.components.DoctorPdfReportDialog
import com.example.util.PdfReportGenerator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DoseRecord
import com.example.data.model.DoseType
import com.example.ui.theme.ChartSlotBg
import com.example.ui.theme.HealthBorder
import com.example.ui.theme.HealthCyanBright
import com.example.ui.theme.HealthMint
import com.example.ui.theme.HealthSurface
import com.example.ui.theme.HealthSurfaceVariant
import com.example.ui.theme.HealthTeal
import com.example.ui.theme.HealthTealBright
import com.example.ui.theme.IconOnAccent
import com.example.ui.theme.TextHealthMuted
import com.example.ui.theme.TextHealthPrimary
import com.example.ui.theme.TextHealthSecondary
import androidx.compose.material3.FilterChipDefaults
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    doses: List<DoseRecord>,
    onDeleteDose: (DoseRecord) -> Unit,
    modifier: Modifier = Modifier,
    lang: com.example.util.AppLanguage = com.example.util.AppLanguage.TR,
    onSeedTrialData: () -> Unit = {}
) {
    val context = LocalContext.current
    val isEn = lang == com.example.util.AppLanguage.EN
    val todayCal = remember { Calendar.getInstance() }

    // Selected calendar date (defaults to today, matching StatisticsScreen)
    var selectedCal by remember { mutableStateOf(Calendar.getInstance()) }
    var filterBySelectedDate by remember { mutableStateOf(true) }
    var selectedFilter by remember { mutableStateOf<DoseType?>(null) }
    var showPdfReportDialog by remember { mutableStateOf(false) }

    val isSelectedToday = remember(selectedCal) {
        selectedCal.get(Calendar.YEAR) == todayCal.get(Calendar.YEAR) &&
        selectedCal.get(Calendar.DAY_OF_YEAR) == todayCal.get(Calendar.DAY_OF_YEAR)
    }

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
            filterBySelectedDate = true
        }, year, month, day).show()
    }

    // Filter by date first (if filterBySelectedDate is active)
    val dateFilteredDoses = remember(doses, selectedCal, filterBySelectedDate) {
        if (!filterBySelectedDate) {
            doses.sortedByDescending { it.timestamp }
        } else {
            val sYear = selectedCal.get(Calendar.YEAR)
            val sDay = selectedCal.get(Calendar.DAY_OF_YEAR)
            doses.filter { dose ->
                val dCal = Calendar.getInstance().apply { timeInMillis = dose.timestamp }
                dCal.get(Calendar.YEAR) == sYear && dCal.get(Calendar.DAY_OF_YEAR) == sDay
            }.sortedByDescending { it.timestamp }
        }
    }

    // Then filter by dose type (All / Basal / Bolus)
    val filteredDoses = if (selectedFilter == null) {
        dateFilteredDoses
    } else {
        dateFilteredDoses.filter { it.type == selectedFilter }
    }

    val groupedDoses = filteredDoses.groupBy { dose ->
        SimpleDateFormat(
            if (isEn) "EEEE, MMMM d, yyyy" else "d MMMM yyyy, EEEE",
            if (isEn) Locale("en") else Locale("tr")
        ).format(Date(dose.timestamp))
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. HEADER
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
                        imageVector = Icons.Default.History,
                        contentDescription = if (isEn) "History" else "Geçmiş",
                        tint = IconOnAccent,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isEn) "Dose History & Records" else "Doz Geçmişi & Kayıtlar",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextHealthPrimary
                    )
                    Text(
                        text = if (isEn) "All logged injections, rotation sites, and blood glucose values" else "Onaylanan tüm enjeksiyonlar ve vücut rotasyon detayları",
                        style = MaterialTheme.typography.bodySmall,
                        color = HealthMint,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // 1B. DOCTOR PDF REPORT CARD (Doktorla Paylaşmak Üzere PDF Raporlama)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_doctor_pdf_export"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = HealthSurface),
                border = BorderStroke(1.5.dp, HealthTealBright),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(HealthTeal, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                tint = IconOnAccent,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isEn) "Doctor PDF Report" else "Doktor İçin PDF Raporlama",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextHealthPrimary
                            )
                            Text(
                                text = if (isEn)
                                    "Export your insulin doses, injection sites & fasting/postprandial glucose as an A4 PDF report for your physician."
                                else
                                    "Geçmiş insülin dozlarınızı, enjeksiyon bölgelerinizi ve açlık/tokluk kan şekeri verilerinizi doktorunuzla paylaşmak için PDF raporu oluşturun.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextHealthSecondary,
                                fontSize = 11.5.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { showPdfReportDialog = true },
                            modifier = Modifier
                                .weight(1.3f)
                                .height(44.dp)
                                .testTag("button_open_pdf_report_dialog"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = HealthTeal,
                                contentColor = IconOnAccent
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                tint = IconOnAccent,
                                modifier = Modifier.size(17.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isEn) "Create PDF Report" else "PDF Rapor Oluştur",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Black,
                                color = IconOnAccent
                            )
                        }

                        Surface(
                            onClick = {
                                val sevenDaysAgo = System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000L)
                                val reportList = doses.filter { it.timestamp >= sevenDaysAgo }.ifEmpty { doses }
                                val periodLabel = if (isEn) "Last 7 Days Summary" else "Son 7 Günlük Klinik Özet"
                                val pdfFile = PdfReportGenerator.generateDoctorPdfReport(
                                    context = context,
                                    doses = reportList,
                                    periodLabel = periodLabel,
                                    lang = lang
                                )
                                PdfReportGenerator.sharePdfFile(context, pdfFile, lang)
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = ChartSlotBg,
                            border = BorderStroke(1.2.dp, HealthTealBright),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("button_quick_share_pdf")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = null,
                                    tint = HealthTealBright,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isEn) "Quick Share" else "Hızlı Paylaş",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = HealthTealBright
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. MODERN SELECTABLE DATE CARD & 7-DAY CALENDAR STRIP (MATCHING STATISTICS SCREEN)
        item {
            val locale = if (isEn) Locale.ENGLISH else Locale("tr")
            val formattedFullDate = SimpleDateFormat("d MMMM yyyy, EEEE", locale).format(selectedCal.time)
            val monthYearLabel = SimpleDateFormat("MMMM yyyy", locale).format(selectedCal.time).replaceFirstChar { it.uppercase(locale) }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("history_date_selector_card"),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = HealthSurface),
                border = BorderStroke(1.5.dp, HealthBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Header Row: Calendar Badge + Month/Year + Quick Toggle Pills
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
                                Text(
                                    text = if (!filterBySelectedDate) {
                                        if (isEn) "All Recorded Dates" else "Tüm Kayıtlı Tarihler"
                                    } else {
                                        monthYearLabel
                                    },
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = TextHealthPrimary
                                )
                                Text(
                                    text = if (!filterBySelectedDate) {
                                        if (isEn) "Showing all ${doses.size} records" else "Toplam ${doses.size} kayıt listeleniyor"
                                    } else {
                                        formattedFullDate
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextHealthSecondary
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            if (!isSelectedToday || !filterBySelectedDate) {
                                Surface(
                                    onClick = {
                                        selectedCal = Calendar.getInstance()
                                        filterBySelectedDate = true
                                    },
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
                                shape = RoundedCornerShape(10.dp),
                                color = if (!filterBySelectedDate) HealthTeal else ChartSlotBg,
                                border = BorderStroke(1.dp, if (!filterBySelectedDate) HealthTealBright else HealthBorder),
                                modifier = Modifier
                                    .clickable { filterBySelectedDate = !filterBySelectedDate }
                                    .testTag("history_toggle_all_dates")
                            ) {
                                Text(
                                    text = if (!filterBySelectedDate) {
                                        if (isEn) "All Dates: ON" else "Tüm Günler: Açık"
                                    } else {
                                        if (isEn) "All Dates" else "Tüm Günler"
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (!filterBySelectedDate) IconOnAccent else TextHealthPrimary,
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp)
                                )
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

                            val isDaySelected = filterBySelectedDate &&
                                    selectedCal.get(Calendar.YEAR) == dYear &&
                                    selectedCal.get(Calendar.DAY_OF_YEAR) == dDayOfYear

                            val dayHasDoses = remember(doses, dYear, dDayOfYear) {
                                doses.any { d ->
                                    val c = Calendar.getInstance().apply { timeInMillis = d.timestamp }
                                    c.get(Calendar.YEAR) == dYear && c.get(Calendar.DAY_OF_YEAR) == dDayOfYear
                                }
                            }

                            Surface(
                                onClick = {
                                    selectedCal = dayCal
                                    filterBySelectedDate = true
                                },
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
                                                    dayHasDoses -> com.example.ui.theme.BioHealingGreen
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

                    // Sleek Bottom Date Navigator Pill Bar
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
                                    val prev = (selectedCal.clone() as Calendar).apply {
                                        add(Calendar.DAY_OF_YEAR, -1)
                                    }
                                    selectedCal = prev
                                    filterBySelectedDate = true
                                },
                                modifier = Modifier
                                    .size(32.dp)
                                    .testTag("history_prev_day_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = if (isEn) "Previous Day" else "Önceki Gün",
                                    tint = HealthTealBright,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Surface(
                                onClick = openDatePicker,
                                color = Color.Transparent,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 6.dp)
                                    .testTag("history_open_date_picker_btn")
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarMonth,
                                        contentDescription = null,
                                        tint = HealthTealBright,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    val titleStr = when {
                                        !filterBySelectedDate -> if (isEn) "All Dates (${doses.size} Records) • Tap Calendar" else "Tüm Tarihler (${doses.size} Kayıt) • Takvim Seç"
                                        isSelectedToday -> if (isEn) "Today ($formattedFullDate)" else "Bugün ($formattedFullDate)"
                                        else -> formattedFullDate
                                    }
                                    Text(
                                        text = titleStr,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextHealthPrimary
                                    )
                                }
                            }

                            IconButton(
                                onClick = {
                                    if (!isSelectedToday) {
                                        val next = (selectedCal.clone() as Calendar).apply {
                                            add(Calendar.DAY_OF_YEAR, 1)
                                        }
                                        selectedCal = next
                                        filterBySelectedDate = true
                                    }
                                },
                                enabled = !isSelectedToday,
                                modifier = Modifier
                                    .size(32.dp)
                                    .testTag("history_next_day_btn")
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

        // 3. FILTER CHIPS (Tümü / Bazal / Bolus)
        item {
            val chipColors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = HealthTeal,
                selectedLabelColor = IconOnAccent,
                containerColor = ChartSlotBg,
                labelColor = TextHealthSecondary
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedFilter == null,
                    onClick = { selectedFilter = null },
                    label = {
                        Text(
                            text = if (isEn) "All (${dateFilteredDoses.size})" else "Tümü (${dateFilteredDoses.size})",
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = chipColors,
                    modifier = Modifier.testTag("filter_all")
                )
                FilterChip(
                    selected = selectedFilter == DoseType.BASAL,
                    onClick = { selectedFilter = DoseType.BASAL },
                    label = {
                        Text(
                            text = if (isEn) "Basal (${dateFilteredDoses.count { it.type == DoseType.BASAL }})" else "Bazal (${dateFilteredDoses.count { it.type == DoseType.BASAL }})",
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = chipColors,
                    modifier = Modifier.testTag("filter_basal")
                )
                FilterChip(
                    selected = selectedFilter == DoseType.BOLUS,
                    onClick = { selectedFilter = DoseType.BOLUS },
                    label = {
                        Text(
                            text = if (isEn) "Bolus (${dateFilteredDoses.count { it.type == DoseType.BOLUS }})" else "Bolus (${dateFilteredDoses.count { it.type == DoseType.BOLUS }})",
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = chipColors,
                    modifier = Modifier.testTag("filter_bolus")
                )
            }
        }

        if (filteredDoses.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = HealthSurface,
                    border = BorderStroke(1.5.dp, HealthBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "📜", fontSize = 36.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isEn) "No records found for this date" else "Seçili tarihte kayıt bulunamadı",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextHealthPrimary
                        )
                        Text(
                            text = if (isEn) "Use the arrows above to browse other days or tap 'Show All Dates'." else "Yukarıdaki oklarla diğer günlere geçebilir veya 'Tüm Günleri Göster'e dokunabilirsiniz.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextHealthSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        } else {
            groupedDoses.forEach { (dateHeader, dateDoses) ->
                item {
                    val totalDayUnits = dateDoses.sumOf { it.doseUnits }
                    val totalDayText = if (isEn) "Daily Total: %.1f IU".format(totalDayUnits) else "Günlük Toplam: %.1f IU".format(totalDayUnits)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = dateHeader,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = HealthTealBright
                        )
                        Text(
                            text = totalDayText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextHealthSecondary
                        )
                    }
                }

                items(dateDoses, key = { it.id }) { dose ->
                    DoseRecordCard(dose = dose, onDelete = { onDeleteDose(dose) }, lang = lang)
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    if (showPdfReportDialog) {
        val locale = if (isEn) Locale.ENGLISH else Locale("tr")
        val selectedDateStr = SimpleDateFormat("d MMMM yyyy", locale).format(selectedCal.time)
        val filterLabel = if (!filterBySelectedDate) {
            if (isEn) "All Recorded Dates (${filteredDoses.size} Doses)" else "Tüm Kayıtlı Tarihler (${filteredDoses.size} Doz)"
        } else {
            if (isEn) "Selected Date: $selectedDateStr" else "Seçili Tarih: $selectedDateStr"
        }

        DoctorPdfReportDialog(
            allDoses = doses,
            currentFilteredDoses = filteredDoses,
            currentFilterLabel = filterLabel,
            lang = lang,
            onDismiss = { showPdfReportDialog = false }
        )
    }
}
