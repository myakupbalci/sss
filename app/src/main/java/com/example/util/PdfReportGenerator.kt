package com.example.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.DoseRecord
import com.example.data.model.DoseType
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.ceil
import kotlin.math.max

object PdfReportGenerator {

    private const val PAGE_WIDTH = 595 // Standard A4 width in points (72 dpi)
    private const val PAGE_HEIGHT = 842 // Standard A4 height in points (72 dpi)
    private const val MARGIN = 32f

    /**
     * Generates a multi-page clinical A4 PDF file containing summary metrics,
     * injection site rotation breakdown, and a full table of insulin & blood glucose records.
     */
    fun generateDoctorPdfReport(
        context: Context,
        doses: List<DoseRecord>,
        periodLabel: String,
        patientName: String = "",
        doctorNote: String = "",
        lang: AppLanguage = AppLanguage.TR
    ): File {
        val isEn = lang == AppLanguage.EN
        val locale = if (isEn) Locale.ENGLISH else Locale("tr")
        val sortedDoses = doses.sortedByDescending { it.timestamp }

        val pdfDocument = PdfDocument()

        // First page holds Header + KPI Cards + Site Breakdown + up to 14 rows;
        // subsequent pages hold up to 24 rows per page.
        val firstPageCapacity = 14
        val nextPageCapacity = 24
        val totalPages = if (sortedDoses.size <= firstPageCapacity) {
            1
        } else {
            1 + ceil((sortedDoses.size - firstPageCapacity).toDouble() / nextPageCapacity).toInt()
        }

        val nowStr = SimpleDateFormat("dd.MM.yyyy HH:mm", locale).format(Date())
        val rowDateFmt = SimpleDateFormat("dd.MM.yyyy HH:mm", locale)

        // Precompute statistics
        val totalDosesCount = sortedDoses.size
        val totalInsulinUnits = sortedDoses.sumOf { it.doseUnits }
        val basalDoses = sortedDoses.filter { it.type == DoseType.BASAL }
        val bolusDoses = sortedDoses.filter { it.type == DoseType.BOLUS }
        val basalUnits = basalDoses.sumOf { it.doseUnits }
        val bolusUnits = bolusDoses.sumOf { it.doseUnits }

        val fastingList = sortedDoses.mapNotNull { it.bloodGlucose }
        val postList = sortedDoses.mapNotNull { it.postDoseGlucose }

        val avgFasting = if (fastingList.isNotEmpty()) fastingList.average().toInt() else null
        val minFasting = fastingList.minOrNull()
        val maxFasting = fastingList.maxOrNull()

        val avgPost = if (postList.isNotEmpty()) postList.average().toInt() else null
        val minPost = postList.minOrNull()
        val maxPost = postList.maxOrNull()

        // Time in Range calculation (Fasting 70..130, Post 70..180)
        val inRangeFasting = fastingList.count { it in 70..130 }
        val inRangePost = postList.count { it in 70..180 }
        val totalGlucoseMeasurements = fastingList.size + postList.size
        val tirPercent = if (totalGlucoseMeasurements > 0) {
            ((inRangeFasting + inRangePost) * 100) / totalGlucoseMeasurements
        } else null

        var currentIndex = 0
        for (pageIndex in 1..max(1, totalPages)) {
            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageIndex).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            // Page background
            val bgPaint = Paint().apply {
                color = Color.WHITE
                style = Paint.Style.FILL
            }
            canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), PAGE_HEIGHT.toFloat(), bgPaint)

            var yPos = MARGIN

            if (pageIndex == 1) {
                yPos = drawFirstPageHeaderAndSummary(
                    canvas = canvas,
                    startY = yPos,
                    isEn = isEn,
                    periodLabel = periodLabel,
                    nowStr = nowStr,
                    patientName = patientName.trim(),
                    doctorNote = doctorNote.trim(),
                    totalDosesCount = totalDosesCount,
                    totalInsulinUnits = totalInsulinUnits,
                    basalUnits = basalUnits,
                    bolusUnits = bolusUnits,
                    basalCount = basalDoses.size,
                    bolusCount = bolusDoses.size,
                    avgFasting = avgFasting,
                    minFasting = minFasting,
                    maxFasting = maxFasting,
                    avgPost = avgPost,
                    minPost = minPost,
                    maxPost = maxPost,
                    tirPercent = tirPercent,
                    sortedDoses = sortedDoses,
                    lang = lang
                )
            } else {
                yPos = drawContinuationHeader(
                    canvas = canvas,
                    startY = yPos,
                    isEn = isEn,
                    periodLabel = periodLabel,
                    pageIndex = pageIndex,
                    totalPages = totalPages
                )
            }

            // Table Header
            yPos = drawTableHeader(canvas, yPos, isEn)

            val rowsForThisPage = if (pageIndex == 1) firstPageCapacity else nextPageCapacity
            val endIndex = (currentIndex + rowsForThisPage).coerceAtMost(sortedDoses.size)

            if (sortedDoses.isEmpty()) {
                drawEmptyRow(canvas, yPos, isEn)
            } else {
                for (i in currentIndex until endIndex) {
                    val dose = sortedDoses[i]
                    yPos = drawDoseRow(
                        canvas = canvas,
                        y = yPos,
                        rowIndex = i,
                        dose = dose,
                        dateStr = rowDateFmt.format(Date(dose.timestamp)),
                        isEn = isEn,
                        lang = lang
                    )
                }
            }
            currentIndex = endIndex

            // Footer
            drawFooter(canvas, pageIndex, totalPages, isEn, nowStr)

            pdfDocument.finishPage(page)
        }

        val reportsDir = File(context.cacheDir, "reports").apply {
            if (!exists()) mkdirs()
        }
        val fileDateTag = SimpleDateFormat("yyyyMMdd_HHmm", Locale.US).format(Date())
        val fileName = if (isEn) {
            "Insulin_Glucose_Doctor_Report_$fileDateTag.pdf"
        } else {
            "Insulin_Seker_Doktor_Raporu_$fileDateTag.pdf"
        }
        val outFile = File(reportsDir, fileName)
        FileOutputStream(outFile).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()
        return outFile
    }

    private fun drawFirstPageHeaderAndSummary(
        canvas: Canvas,
        startY: Float,
        isEn: Boolean,
        periodLabel: String,
        nowStr: String,
        patientName: String,
        doctorNote: String,
        totalDosesCount: Int,
        totalInsulinUnits: Double,
        basalUnits: Double,
        bolusUnits: Double,
        basalCount: Int,
        bolusCount: Int,
        avgFasting: Int?,
        minFasting: Int?,
        maxFasting: Int?,
        avgPost: Int?,
        minPost: Int?,
        maxPost: Int?,
        tirPercent: Int?,
        sortedDoses: List<DoseRecord>,
        lang: AppLanguage
    ): Float {
        var y = startY
        val contentWidth = PAGE_WIDTH - (MARGIN * 2)

        // 1. Top Clinical Banner
        val bannerHeight = if (patientName.isNotEmpty() || doctorNote.isNotEmpty()) 82f else 66f
        val bannerRect = RectF(MARGIN, y, MARGIN + contentWidth, y + bannerHeight)
        val bannerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0F766E") // Clinical Teal
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(bannerRect, 10f, 10f, bannerPaint)

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 14.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#CCFBF1")
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }

        val mainTitle = if (isEn) {
            "CLINICAL INSULIN & BLOOD GLUCOSE FOLLOW-UP REPORT"
        } else {
            "KLİNİK İNSÜLİN VE KAN ŞEKERİ TAKİP RAPORU (DOKTOR RAPORU)"
        }
        canvas.drawText(mainTitle, MARGIN + 14f, y + 22f, titlePaint)

        val periodLine = if (isEn) {
            "Report Scope: $periodLabel   •   Generated: $nowStr"
        } else {
            "Rapor Kapsamı: $periodLabel   •   Oluşturulma Tarihi: $nowStr"
        }
        canvas.drawText(periodLine, MARGIN + 14f, y + 38f, subPaint)

        if (patientName.isNotEmpty() || doctorNote.isNotEmpty()) {
            val patientPart = if (patientName.isNotEmpty()) {
                if (isEn) "Patient: $patientName" else "Hasta Adı: $patientName"
            } else ""
            val notePart = if (doctorNote.isNotEmpty()) {
                if (isEn) "Note: $doctorNote" else "Not: $doctorNote"
            } else ""
            val combinedInfo = listOf(patientPart, notePart).filter { it.isNotEmpty() }.joinToString("   |   ")
            val infoPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                textSize = 9.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            canvas.drawText(combinedInfo.take(95), MARGIN + 14f, y + 54f, infoPaint)
            canvas.drawText(
                if (isEn) "Prepared for Physician / Endocrinologist Review" else "Hekim / Diyabet Uzmanı Değerlendirmesi İçin Hazırlanmıştır",
                MARGIN + 14f,
                y + 70f,
                subPaint
            )
        } else {
            canvas.drawText(
                if (isEn) "Prepared for Physician / Endocrinologist Review" else "Hekim / Diyabet Uzmanı Değerlendirmesi İçin Hazırlanmıştır",
                MARGIN + 14f,
                y + 54f,
                subPaint
            )
        }

        y += bannerHeight + 12f

        // 2. 4 KPI Summary Boxes
        val boxGap = 8f
        val boxWidth = (contentWidth - (boxGap * 3)) / 4f
        val boxHeight = 62f

        // Box 1: Total Insulin & Dose Count
        drawKpiBox(
            canvas = canvas,
            x = MARGIN,
            y = y,
            w = boxWidth,
            h = boxHeight,
            accentColor = Color.parseColor("#0D9488"),
            title = if (isEn) "TOTAL INSULIN" else "TOPLAM İNSÜLİN",
            mainValue = String.format(Locale.US, "%.1f IU", totalInsulinUnits),
            subValue = if (isEn) "$totalDosesCount recorded doses" else "Toplam $totalDosesCount doz kaydı"
        )

        // Box 2: Basal vs Bolus Breakdown
        drawKpiBox(
            canvas = canvas,
            x = MARGIN + (boxWidth + boxGap) * 1,
            y = y,
            w = boxWidth,
            h = boxHeight,
            accentColor = Color.parseColor("#0284C7"),
            title = if (isEn) "BASAL / BOLUS" else "BAZAL / BOLUS",
            mainValue = String.format(Locale.US, "%.1f / %.1f IU", basalUnits, bolusUnits),
            subValue = if (isEn) "$basalCount Basal • $bolusCount Bolus" else "$basalCount Bazal • $bolusCount Bolus"
        )

        // Box 3: Average Pre-Dose Fasting Glucose
        val fastingMain = if (avgFasting != null) "$avgFasting mg/dL" else (if (isEn) "No Data" else "Veri Yok")
        val fastingSub = if (minFasting != null && maxFasting != null) {
            "Min: $minFasting • Max: $maxFasting"
        } else {
            if (isEn) "Target: 70-130 mg/dL" else "Hedef: 70-130 mg/dL"
        }
        drawKpiBox(
            canvas = canvas,
            x = MARGIN + (boxWidth + boxGap) * 2,
            y = y,
            w = boxWidth,
            h = boxHeight,
            accentColor = Color.parseColor("#16A34A"),
            title = if (isEn) "AVG FASTING BG" else "ORT. AÇLIK ŞEKERİ",
            mainValue = fastingMain,
            subValue = fastingSub
        )

        // Box 4: Average 2-Hour Postprandial Glucose & TIR
        val postMain = if (avgPost != null) "$avgPost mg/dL" else (if (isEn) "No Data" else "Veri Yok")
        val postSub = if (minPost != null && maxPost != null) {
            val tirStr = if (tirPercent != null) " • TIR %$tirPercent" else ""
            "Min: $minPost • Max: $maxPost$tirStr"
        } else {
            if (isEn) "Target: <180 mg/dL" else "Hedef: <180 mg/dL"
        }
        drawKpiBox(
            canvas = canvas,
            x = MARGIN + (boxWidth + boxGap) * 3,
            y = y,
            w = boxWidth,
            h = boxHeight,
            accentColor = Color.parseColor("#E11D48"),
            title = if (isEn) "AVG 2-HR POST BG" else "ORT. TOKLUK ŞEKERİ",
            mainValue = postMain,
            subValue = postSub
        )

        y += boxHeight + 10f

        // 3. Injection Site Rotation Summary Strip
        val siteRect = RectF(MARGIN, y, MARGIN + contentWidth, y + 36f)
        val siteFill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#F1F5F9")
            style = Paint.Style.FILL
        }
        val siteBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#CBD5E1")
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }
        canvas.drawRoundRect(siteRect, 6f, 6f, siteFill)
        canvas.drawRoundRect(siteRect, 6f, 6f, siteBorder)

        val siteTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0F172A")
            textSize = 8.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val siteTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#334155")
            textSize = 8.2f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }

        val siteCounts = sortedDoses.groupingBy { it.injectionSite }.eachCount()
        val siteSummaryStr = if (siteCounts.isEmpty()) {
            if (isEn) "No injection site records in selected period." else "Seçili dönemde kayıtlı enjeksiyon bölgesi bulunmuyor."
        } else {
            siteCounts.entries
                .sortedByDescending { it.value }
                .joinToString("   •   ") { (site, count) ->
                    val sName = if (isEn) Strings.siteDisplayName(site, lang) else site.displayName
                    "$sName: $count"
                }
        }

        canvas.drawText(
            if (isEn) "INJECTION SITE ROTATION DISTRIBUTION:" else "ENJEKSİYON BÖLGESİ ROTASYON DAĞILIMI:",
            MARGIN + 10f,
            y + 14f,
            siteTitlePaint
        )
        canvas.drawText(siteSummaryStr.take(115), MARGIN + 10f, y + 28f, siteTextPaint)

        y += 46f
        return y
    }

    private fun drawKpiBox(
        canvas: Canvas,
        x: Float,
        y: Float,
        w: Float,
        h: Float,
        accentColor: Int,
        title: String,
        mainValue: String,
        subValue: String
    ) {
        val rect = RectF(x, y, x + w, y + h)
        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#F8FAFC")
            style = Paint.Style.FILL
        }
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#CBD5E1")
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }
        canvas.drawRoundRect(rect, 7f, 7f, fillPaint)
        canvas.drawRoundRect(rect, 7f, 7f, borderPaint)

        // Top colored bar inside KPI box
        val accentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = accentColor
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(RectF(x, y, x + w, y + 4f), 4f, 4f, accentPaint)

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = accentColor
            textSize = 7.8f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val mainPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0F172A")
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#475569")
            textSize = 7.4f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }

        canvas.drawText(title, x + 8f, y + 17f, titlePaint)
        canvas.drawText(mainValue, x + 8f, y + 36f, mainPaint)
        canvas.drawText(subValue.take(28), x + 8f, y + 51f, subPaint)
    }

    private fun drawContinuationHeader(
        canvas: Canvas,
        startY: Float,
        isEn: Boolean,
        periodLabel: String,
        pageIndex: Int,
        totalPages: Int
    ): Float {
        val contentWidth = PAGE_WIDTH - (MARGIN * 2)
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0F766E")
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#475569")
            textSize = 8.5f
        }

        val headerText = if (isEn) {
            "CLINICAL INSULIN & BLOOD GLUCOSE REPORT — Continued (Page $pageIndex / $totalPages)"
        } else {
            "KLİNİK İNSÜLİN VE KAN ŞEKERİ TAKİP RAPORU — Devam Sayfası (Sayfa $pageIndex / $totalPages)"
        }
        canvas.drawText(headerText, MARGIN, startY + 12f, titlePaint)
        canvas.drawText(periodLabel, MARGIN, startY + 24f, subPaint)

        val linePaint = Paint().apply {
            color = Color.parseColor("#CBD5E1")
            strokeWidth = 1f
        }
        canvas.drawLine(MARGIN, startY + 30f, MARGIN + contentWidth, startY + 30f, linePaint)
        return startY + 38f
    }

    private fun drawTableHeader(canvas: Canvas, y: Float, isEn: Boolean): Float {
        val contentWidth = PAGE_WIDTH - (MARGIN * 2)
        val headerHeight = 24f
        val rect = RectF(MARGIN, y, MARGIN + contentWidth, y + headerHeight)

        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#1E293B")
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(rect, 4f, 4f, bgPaint)

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 8.2f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        // Columns X offsets (total width = 531)
        // Col 1: Date & Time (0..92)
        // Col 2: Type (95..148)
        // Col 3: Dose IU (150..202)
        // Col 4: Meal / Tag (205..295)
        // Col 5: Injection Site (298..400)
        // Col 6: Fasting BG (403..465)
        // Col 7: 2-Hr Post BG (468..531)
        val textY = y + 15.5f
        canvas.drawText(if (isEn) "DATE & TIME" else "TARİH & SAAT", MARGIN + 6f, textY, textPaint)
        canvas.drawText(if (isEn) "TYPE" else "TÜR", MARGIN + 96f, textY, textPaint)
        canvas.drawText(if (isEn) "DOSE" else "DOZ (IU)", MARGIN + 150f, textY, textPaint)
        canvas.drawText(if (isEn) "MEAL / TAG" else "ÖĞÜN / ETİKET", MARGIN + 205f, textY, textPaint)
        canvas.drawText(if (isEn) "INJECTION SITE" else "ENJEKSİYON BÖLGESİ", MARGIN + 298f, textY, textPaint)
        canvas.drawText(if (isEn) "FASTING BG" else "AÇLIK ŞEKERİ", MARGIN + 402f, textY, textPaint)
        canvas.drawText(if (isEn) "2-HR POST BG" else "2. SAAT TOKLUK", MARGIN + 466f, textY, textPaint)

        return y + headerHeight + 2f
    }

    private fun drawDoseRow(
        canvas: Canvas,
        y: Float,
        rowIndex: Int,
        dose: DoseRecord,
        dateStr: String,
        isEn: Boolean,
        lang: AppLanguage
    ): Float {
        val contentWidth = PAGE_WIDTH - (MARGIN * 2)
        val rowHeight = 25f
        val rect = RectF(MARGIN, y, MARGIN + contentWidth, y + rowHeight)

        if (rowIndex % 2 == 1) {
            val altPaint = Paint().apply {
                color = Color.parseColor("#F8FAFC")
                style = Paint.Style.FILL
            }
            canvas.drawRect(rect, altPaint)
        }

        val dividerPaint = Paint().apply {
            color = Color.parseColor("#E2E8F0")
            strokeWidth = 0.7f
        }
        canvas.drawLine(MARGIN, y + rowHeight, MARGIN + contentWidth, y + rowHeight, dividerPaint)

        val regularPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#1E293B")
            textSize = 8.2f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }
        val boldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0F172A")
            textSize = 8.4f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val textY = y + 16f

        // 1. Date & Time
        canvas.drawText(dateStr, MARGIN + 6f, textY, regularPaint)

        // 2. Type (Basal vs Bolus)
        val typePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (dose.type == DoseType.BASAL) Color.parseColor("#0284C7") else Color.parseColor("#D97706")
            textSize = 8.2f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val typeLabel = if (dose.type == DoseType.BASAL) {
            if (isEn) "Basal" else "Bazal"
        } else {
            "Bolus"
        }
        canvas.drawText(typeLabel, MARGIN + 96f, textY, typePaint)

        // 3. Dose Units
        canvas.drawText(String.format(Locale.US, "%.1f IU", dose.doseUnits), MARGIN + 150f, textY, boldPaint)

        // 4. Meal Tag
        val mealStr = dose.mealTag.ifEmpty { "-" }.take(17)
        canvas.drawText(mealStr, MARGIN + 205f, textY, regularPaint)

        // 5. Injection Site
        val siteStr = (if (isEn) Strings.siteDisplayName(dose.injectionSite, lang) else dose.injectionSite.displayName).take(20)
        canvas.drawText(siteStr, MARGIN + 298f, textY, regularPaint)

        // 6. Fasting BG
        val fastingVal = dose.bloodGlucose
        val fastingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 8.3f
            typeface = Typeface.create(Typeface.DEFAULT, if (fastingVal != null) Typeface.BOLD else Typeface.NORMAL)
            color = when {
                fastingVal == null -> Color.parseColor("#94A3B8")
                fastingVal < 70 || fastingVal > 130 -> Color.parseColor("#DC2626")
                else -> Color.parseColor("#15803D")
            }
        }
        val fastingStr = if (fastingVal != null) "$fastingVal mg/dL" else "-"
        canvas.drawText(fastingStr, MARGIN + 402f, textY, fastingPaint)

        // 7. 2-Hr Post BG
        val postVal = dose.postDoseGlucose
        val postPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 8.3f
            typeface = Typeface.create(Typeface.DEFAULT, if (postVal != null) Typeface.BOLD else Typeface.NORMAL)
            color = when {
                postVal == null -> Color.parseColor("#94A3B8")
                postVal < 70 || postVal > 180 -> Color.parseColor("#DC2626")
                else -> Color.parseColor("#0F766E")
            }
        }
        val postStr = if (postVal != null) "$postVal mg/dL" else "-"
        canvas.drawText(postStr, MARGIN + 466f, textY, postPaint)

        return y + rowHeight
    }

    private fun drawEmptyRow(canvas: Canvas, y: Float, isEn: Boolean) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#64748B")
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
        }
        val msg = if (isEn) {
            "No insulin or blood glucose records found for the selected period."
        } else {
            "Seçili tarih aralığında kayıtlı insülin veya kan şekeri verisi bulunmamaktadır."
        }
        canvas.drawText(msg, MARGIN + 12f, y + 24f, paint)
    }

    private fun drawFooter(
        canvas: Canvas,
        pageIndex: Int,
        totalPages: Int,
        isEn: Boolean,
        nowStr: String
    ) {
        val contentWidth = PAGE_WIDTH - (MARGIN * 2)
        val footerY = PAGE_HEIGHT - 36f

        val linePaint = Paint().apply {
            color = Color.parseColor("#CBD5E1")
            strokeWidth = 0.8f
        }
        canvas.drawLine(MARGIN, footerY - 8f, MARGIN + contentWidth, footerY - 8f, linePaint)

        val disclaimerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#64748B")
            textSize = 7.4f
        }
        val pagePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#334155")
            textSize = 8f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
        }

        val disclaimer = if (isEn) {
            "Medical Note: This report contains patient-logged personal tracking records for clinical review ($nowStr)."
        } else {
            "Tıbbi Bilgi Notu: Bu rapor kullanıcı tarafından girilen kişisel takip kayıtlarından hekim incelemesi için üretilmiştir ($nowStr)."
        }
        canvas.drawText(disclaimer, MARGIN, footerY + 4f, disclaimerPaint)

        val pageLabel = if (isEn) "Page $pageIndex / $totalPages" else "Sayfa $pageIndex / $totalPages"
        canvas.drawText(pageLabel, MARGIN + contentWidth, footerY + 4f, pagePaint)
    }

    /**
     * Shares the generated PDF file via Android's native Share Sheet (WhatsApp, Email, Drive, etc.)
     */
    fun sharePdfFile(context: Context, pdfFile: File, lang: AppLanguage = AppLanguage.TR) {
        val isEn = lang == AppLanguage.EN
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                pdfFile
            )
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(
                    Intent.EXTRA_SUBJECT,
                    if (isEn) "Clinical Insulin & Blood Glucose PDF Report" else "Klinik İnsülin ve Kan Şekeri Takip Raporu (PDF)"
                )
                putExtra(
                    Intent.EXTRA_TEXT,
                    if (isEn)
                        "Attached is my insulin dose, injection rotation, and fasting/postprandial blood glucose report in PDF format."
                    else
                        "Ekte insülin doz geçmişim, enjeksiyon rotasyon bölgelerim ve açlık/tokluk kan şekeri ölçümlerimi içeren PDF doktor raporu yer almaktadır."
                )
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(
                shareIntent,
                if (isEn) "Share PDF Report with Doctor" else "PDF Raporunu Doktorla Paylaş"
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(
                context,
                if (isEn) "Could not open share menu: ${e.localizedMessage}" else "Paylaşım ekranı açılamadı: ${e.localizedMessage}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    /**
     * Saves the generated PDF file directly to the device's Downloads folder (MediaStore on Android 10+).
     */
    fun savePdfToDownloads(context: Context, pdfFile: File, lang: AppLanguage = AppLanguage.TR): Boolean {
        val isEn = lang == AppLanguage.EN
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, pdfFile.name)
                    put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
                    put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                    put(MediaStore.Downloads.IS_PENDING, 1)
                }
                val resolver = context.contentResolver
                val collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                val itemUri = resolver.insert(collection, contentValues) ?: return false

                resolver.openOutputStream(itemUri)?.use { outputStream ->
                    FileInputStream(pdfFile).use { inputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                contentValues.clear()
                contentValues.put(MediaStore.Downloads.IS_PENDING, 0)
                resolver.update(itemUri, contentValues, null, null)
            } else {
                val downloadsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.filesDir
                val targetFile = File(downloadsDir, pdfFile.name)
                pdfFile.copyTo(targetFile, overwrite = true)
            }

            Toast.makeText(
                context,
                if (isEn)
                    "✓ PDF Report saved to Downloads: ${pdfFile.name}"
                else
                    "✓ PDF Raporu İndirilenler klasörüne kaydedildi: ${pdfFile.name}",
                Toast.LENGTH_LONG
            ).show()
            true
        } catch (e: Exception) {
            Toast.makeText(
                context,
                if (isEn) "Failed to save PDF: ${e.localizedMessage}" else "PDF kaydedilemedi: ${e.localizedMessage}",
                Toast.LENGTH_LONG
            ).show()
            false
        }
    }

    /**
     * Opens Android's native Print / Save-as-PDF preview screen so the user can view every page of the PDF
     * directly on the device or emulator and save/print it.
     */
    fun printOrPreviewPdf(context: Context, pdfFile: File, lang: AppLanguage = AppLanguage.TR) {
        val isEn = lang == AppLanguage.EN
        try {
            val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
            if (printManager != null) {
                val jobName = if (isEn) "Insulin_Glucose_Doctor_Report" else "Insulin_Seker_Doktor_Raporu"
                val adapter = object : PrintDocumentAdapter() {
                    override fun onLayout(
                        oldAttributes: PrintAttributes?,
                        newAttributes: PrintAttributes?,
                        cancellationSignal: CancellationSignal?,
                        callback: LayoutResultCallback?,
                        extras: Bundle?
                    ) {
                        if (cancellationSignal?.isCanceled == true) {
                            callback?.onLayoutCancelled()
                            return
                        }
                        val info = PrintDocumentInfo.Builder(pdfFile.name)
                            .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                            .setPageCount(PrintDocumentInfo.PAGE_COUNT_UNKNOWN)
                            .build()
                        callback?.onLayoutFinished(info, true)
                    }

                    override fun onWrite(
                        pages: Array<out PageRange>?,
                        destination: ParcelFileDescriptor?,
                        cancellationSignal: CancellationSignal?,
                        callback: WriteResultCallback?
                    ) {
                        if (destination == null) {
                            callback?.onWriteFailed("No destination")
                            return
                        }
                        try {
                            FileInputStream(pdfFile).use { input ->
                                FileOutputStream(destination.fileDescriptor).use { output ->
                                    input.copyTo(output)
                                }
                            }
                            callback?.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
                        } catch (e: Exception) {
                            callback?.onWriteFailed(e.localizedMessage)
                        }
                    }
                }
                printManager.print(jobName, adapter, PrintAttributes.Builder().build())
            } else {
                sharePdfFile(context, pdfFile, lang)
            }
        } catch (_: Exception) {
            sharePdfFile(context, pdfFile, lang)
        }
    }
}
