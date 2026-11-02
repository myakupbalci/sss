package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.DoseRecord
import com.example.data.model.DoseType
import com.example.ui.theme.AlertPharmacyRuby
import com.example.ui.theme.BasalCyan
import com.example.ui.theme.BioGreenContainer
import com.example.ui.theme.BioHealingGreen
import com.example.ui.theme.BolusGold
import com.example.ui.theme.ChartSlotBg
import com.example.ui.theme.HealthBorder
import com.example.ui.theme.HealthMint
import com.example.ui.theme.HealthSurface
import com.example.ui.theme.HealthSurfaceVariant
import com.example.ui.theme.HealthTeal
import com.example.ui.theme.HealthTealBright
import com.example.ui.theme.IconOnAccent
import com.example.ui.theme.TextHealthMuted
import com.example.ui.theme.TextHealthPrimary
import com.example.ui.theme.TextHealthSecondary
import com.example.util.AppLanguage
import com.example.util.PdfReportGenerator
import com.example.util.Strings
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DoctorPdfReportDialog(
    allDoses: List<DoseRecord>,
    currentFilteredDoses: List<DoseRecord>,
    currentFilterLabel: String,
    lang: AppLanguage = AppLanguage.TR,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val isEn = lang == AppLanguage.EN
    val locale = if (isEn) Locale.ENGLISH else Locale("tr")

    // 0 = Last 7 Days, 1 = Last 30 Days, 2 = All Records, 3 = Current History Filter
    var selectedScopeIndex by remember { mutableIntStateOf(0) }
    var patientName by remember { mutableStateOf("") }
    var doctorNote by remember { mutableStateOf("") }
    var lastSavedFileName by remember { mutableStateOf<String?>(null) }

    val nowMillis = System.currentTimeMillis()
    val sevenDaysAgo = nowMillis - (7L * 24 * 60 * 60 * 1000L)
    val thirtyDaysAgo = nowMillis - (30L * 24 * 60 * 60 * 1000L)

    val reportDoses = remember(selectedScopeIndex, allDoses, currentFilteredDoses) {
        when (selectedScopeIndex) {
            0 -> allDoses.filter { it.timestamp >= sevenDaysAgo }.sortedByDescending { it.timestamp }
            1 -> allDoses.filter { it.timestamp >= thirtyDaysAgo }.sortedByDescending { it.timestamp }
            2 -> allDoses.sortedByDescending { it.timestamp }
            else -> currentFilteredDoses.sortedByDescending { it.timestamp }
        }
    }

    val scopeTitle = when (selectedScopeIndex) {
        0 -> if (isEn) "Last 7 Days (1 Week)" else "Son 7 Gün (1 Haftalık)"
        1 -> if (isEn) "Last 30 Days (1 Month)" else "Son 30 Gün (1 Aylık)"
        2 -> if (isEn) "All Recorded History (${allDoses.size} Doses)" else "Tüm Kayıtlı Geçmiş (${allDoses.size} Doz)"
        else -> currentFilterLabel
    }

    val totalUnits = remember(reportDoses) { reportDoses.sumOf { it.doseUnits } }
    val basalUnits = remember(reportDoses) { reportDoses.filter { it.type == DoseType.BASAL }.sumOf { it.doseUnits } }
    val bolusUnits = remember(reportDoses) { reportDoses.filter { it.type == DoseType.BOLUS }.sumOf { it.doseUnits } }
    val fastingValues = remember(reportDoses) { reportDoses.mapNotNull { it.bloodGlucose } }
    val postValues = remember(reportDoses) { reportDoses.mapNotNull { it.postDoseGlucose } }
    val avgFasting = if (fastingValues.isNotEmpty()) fastingValues.average().toInt() else null
    val avgPost = if (postValues.isNotEmpty()) postValues.average().toInt() else null

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(24.dp))
                .testTag("dialog_doctor_pdf_report"),
            color = HealthSurface,
            border = BorderStroke(1.5.dp, HealthTealBright)
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
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
                                .size(42.dp)
                                .background(HealthTeal, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                tint = IconOnAccent,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (isEn) "Doctor PDF Report" else "Doktor İçin PDF Raporlama",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = TextHealthPrimary
                            )
                            Text(
                                text = if (isEn)
                                    "Export clinical dose, rotation & glucose history as A4 PDF"
                                else
                                    "Doz, rotasyon ve şeker geçmişini A4 PDF olarak paylaşın",
                                style = MaterialTheme.typography.bodySmall,
                                color = HealthMint,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("button_close_pdf_dialog")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = if (isEn) "Close" else "Kapat",
                            tint = TextHealthSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 1. Report Period Selector
                Text(
                    text = if (isEn) "1. Select Report Date Range:" else "1. Raporlanacak Tarih Aralığını Seçin:",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextHealthPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))

                val scopes = listOf(
                    0 to (if (isEn) "Last 7 Days" else "Son 7 Gün"),
                    1 to (if (isEn) "Last 30 Days" else "Son 30 Gün"),
                    2 to (if (isEn) "All Records" else "Tüm Kayıtlar"),
                    3 to (if (isEn) "Selected Date" else "Seçili Tarih")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    scopes.take(2).forEach { (idx, label) ->
                        val isSelected = selectedScopeIndex == idx
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { selectedScopeIndex = idx }
                                .testTag("pdf_scope_$idx"),
                            color = if (isSelected) HealthTeal else ChartSlotBg,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.2.dp, if (isSelected) HealthTealBright else HealthBorder)
                        ) {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isSelected) IconOnAccent else TextHealthPrimary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    scopes.drop(2).forEach { (idx, label) ->
                        val isSelected = selectedScopeIndex == idx
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { selectedScopeIndex = idx }
                                .testTag("pdf_scope_$idx"),
                            color = if (isSelected) HealthTeal else ChartSlotBg,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.2.dp, if (isSelected) HealthTealBright else HealthBorder)
                        ) {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isSelected) IconOnAccent else TextHealthPrimary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 2. Optional Patient Name & Note for Doctor
                Text(
                    text = if (isEn) "2. Patient Info & Doctor Note (Optional):" else "2. Hasta Adı ve Doktor Notu (İsteğe Bağlı):",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextHealthPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = patientName,
                    onValueChange = { patientName = it },
                    label = { Text(if (isEn) "Patient Full Name (Shown in PDF Header)" else "Hasta Adı Soyadı (PDF Başlığında Görünür)") },
                    placeholder = { Text(if (isEn) "e.g., Ahmet Yılmaz" else "Örn: Ahmet Yılmaz") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_pdf_patient_name"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HealthTealBright,
                        unfocusedBorderColor = HealthBorder,
                        focusedTextColor = TextHealthPrimary,
                        unfocusedTextColor = TextHealthPrimary,
                        focusedContainerColor = ChartSlotBg,
                        unfocusedContainerColor = ChartSlotBg
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = doctorNote,
                    onValueChange = { doctorNote = it },
                    label = { Text(if (isEn) "Short Note for Physician" else "Hekim İçin Kısa Not") },
                    placeholder = { Text(if (isEn) "e.g., Morning basal dose adjusted this week" else "Örn: Sabah açlık ölçümleri ve akşam bazal takibi") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_pdf_doctor_note"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HealthTealBright,
                        unfocusedBorderColor = HealthBorder,
                        focusedTextColor = TextHealthPrimary,
                        unfocusedTextColor = TextHealthPrimary,
                        focusedContainerColor = ChartSlotBg,
                        unfocusedContainerColor = ChartSlotBg
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                // 3. Live Clinical PDF Document Preview Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("card_pdf_live_preview"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = HealthSurfaceVariant),
                    border = BorderStroke(1.2.dp, HealthBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isEn) "📄 A4 PDF CONTENT PREVIEW" else "📄 A4 PDF RAPOR ÖZETİ ÖNİZLEMESİ",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = HealthTealBright
                            )
                            Surface(
                                color = ChartSlotBg,
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(0.8.dp, HealthBorder)
                            ) {
                                Text(
                                    text = if (isEn) "${reportDoses.size} Records" else "${reportDoses.size} Kayıt",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = TextHealthPrimary,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Text(
                            text = scopeTitle,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextHealthSecondary,
                            modifier = Modifier.padding(top = 2.dp, bottom = 10.dp)
                        )

                        // 4 Summary KPI Pills inside Preview
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            PreviewMetricBox(
                                title = if (isEn) "Total IU" else "Toplam Doz",
                                value = String.format(Locale.US, "%.1f IU", totalUnits),
                                accentColor = HealthTealBright,
                                modifier = Modifier.weight(1f)
                            )
                            PreviewMetricBox(
                                title = if (isEn) "Basal/Bolus" else "Bazal/Bolus",
                                value = String.format(Locale.US, "%.0f/%.0f", basalUnits, bolusUnits),
                                accentColor = BasalCyan,
                                modifier = Modifier.weight(1f)
                            )
                            PreviewMetricBox(
                                title = if (isEn) "Avg Fasting" else "Ort. Açlık",
                                value = if (avgFasting != null) "$avgFasting mg/dL" else "-",
                                accentColor = BioHealingGreen,
                                modifier = Modifier.weight(1f)
                            )
                            PreviewMetricBox(
                                title = if (isEn) "Avg 2-Hr" else "Ort. Tokluk",
                                value = if (avgPost != null) "$avgPost mg/dL" else "-",
                                accentColor = AlertPharmacyRuby,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        if (reportDoses.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = if (isEn) "Sample Rows Included in Table:" else "Tabloda Yer Alacak Son Kayıtlar:",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextHealthSecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))

                            val previewFmt = remember(locale) { SimpleDateFormat("dd MMM HH:mm", locale) }
                            reportDoses.take(4).forEach { dose ->
                                val dStr = previewFmt.format(Date(dose.timestamp))
                                val typeStr = if (dose.type == DoseType.BASAL) (if (isEn) "Basal" else "Bazal") else "Bolus"
                                val siteStr = if (isEn) Strings.siteDisplayName(dose.injectionSite, lang) else dose.injectionSite.displayName
                                val bgStr = buildString {
                                    if (dose.bloodGlucose != null) append(if (isEn) "F:${dose.bloodGlucose} " else "Açlık:${dose.bloodGlucose} ")
                                    if (dose.postDoseGlucose != null) append(if (isEn) "P:${dose.postDoseGlucose}" else "Tokluk:${dose.postDoseGlucose}")
                                    if (isEmpty()) append("-")
                                }

                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp),
                                    color = ChartSlotBg,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 8.dp, vertical = 5.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "$dStr • $typeStr (${String.format(Locale.US, "%.1f", dose.doseUnits)} IU)",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextHealthPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1.2f)
                                        )
                                        Text(
                                            text = siteStr,
                                            fontSize = 9.5.sp,
                                            color = TextHealthSecondary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(0.9f),
                                            textAlign = TextAlign.Center
                                        )
                                        Text(
                                            text = bgStr,
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = HealthTealBright,
                                            maxLines = 1,
                                            modifier = Modifier.weight(0.9f),
                                            textAlign = TextAlign.End
                                        )
                                    }
                                }
                            }
                            if (reportDoses.size > 4) {
                                Text(
                                    text = if (isEn)
                                        "+ ${reportDoses.size - 4} more records included in full PDF table..."
                                    else
                                        "+ ${reportDoses.size - 4} kayıt daha tam PDF tablosunda yer alıyor...",
                                    fontSize = 9.5.sp,
                                    color = TextHealthMuted,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }

                // Saved Confirmation Banner if downloaded
                lastSavedFileName?.let { savedName ->
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = BioGreenContainer,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, BioHealingGreen)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = BioHealingGreen,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isEn)
                                    "PDF saved to Downloads folder: $savedName"
                                else
                                    "PDF İndirilenler (Downloads) klasörüne kaydedildi: $savedName",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = BioHealingGreen
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Primary Action: Share PDF with Doctor (WhatsApp / Email / Drive)
                Button(
                    onClick = {
                        val file = PdfReportGenerator.generateDoctorPdfReport(
                            context = context,
                            doses = reportDoses,
                            periodLabel = scopeTitle,
                            patientName = patientName,
                            doctorNote = doctorNote,
                            lang = lang
                        )
                        PdfReportGenerator.sharePdfFile(context, file, lang)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("button_share_doctor_pdf"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = HealthTeal,
                        contentColor = IconOnAccent
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        tint = IconOnAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isEn) "Share PDF Report with Doctor" else "PDF Raporunu Doktorla Paylaş",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = IconOnAccent
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Secondary Actions Row: Download to Phone & Print / View PDF
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val file = PdfReportGenerator.generateDoctorPdfReport(
                                context = context,
                                doses = reportDoses,
                                periodLabel = scopeTitle,
                                patientName = patientName,
                                doctorNote = doctorNote,
                                lang = lang
                            )
                            val ok = PdfReportGenerator.savePdfToDownloads(context, file, lang)
                            if (ok) {
                                lastSavedFileName = file.name
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("button_download_doctor_pdf"),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.2.dp, BioHealingGreen)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            tint = BioHealingGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isEn) "Save PDF" else "Telefona İndir",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = BioHealingGreen
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            val file = PdfReportGenerator.generateDoctorPdfReport(
                                context = context,
                                doses = reportDoses,
                                periodLabel = scopeTitle,
                                patientName = patientName,
                                doctorNote = doctorNote,
                                lang = lang
                            )
                            PdfReportGenerator.printOrPreviewPdf(context, file, lang)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("button_print_doctor_pdf"),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.2.dp, BolusGold)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Print,
                            contentDescription = null,
                            tint = BolusGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isEn) "View / Print" else "Önizle / Yazdır",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = BolusGold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PreviewMetricBox(
    title: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = ChartSlotBg,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, HealthBorder)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 7.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = TextHealthSecondary,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = accentColor,
                maxLines = 1
            )
        }
    }
}
