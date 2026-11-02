package com.example.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bloodtype
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NotificationsActive
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import com.example.data.model.DoseRecord
import com.example.data.model.DoseType
import com.example.receiver.NotificationHelper
import com.example.ui.theme.AlertPharmacyRuby
import com.example.ui.theme.BasalCyan
import com.example.ui.theme.BioHealingGreen
import com.example.ui.theme.BolusGold
import com.example.ui.theme.ChartSlotBg
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BloodGlucoseEntryDialog(
    dose: DoseRecord,
    onSaveGlucose: (doseId: Long, glucose: Int) -> Unit,
    onSendTestNotification: (doseId: Long, units: Double, type: DoseType) -> Unit,
    onDismiss: () -> Unit,
    lang: com.example.util.AppLanguage = com.example.util.AppLanguage.TR
) {
    val context = LocalContext.current
    val isEn = lang == com.example.util.AppLanguage.EN
    var bgText by remember { mutableStateOf(dose.postDoseGlucose?.toString() ?: "") }
    var inputError by remember { mutableStateOf(false) }

    val bgInt = bgText.toIntOrNull()
    val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(dose.timestamp))

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            onSendTestNotification(dose.id, dose.doseUnits, dose.type)
            Toast.makeText(
                context,
                if (isEn) "📲 2-Hour postprandial glucose notification sent to phone!" else "📲 2. saat tokluk şekeri bildirimi telefonunuza gönderildi!",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            NotificationHelper.openAppNotificationSettings(context)
        }
    }

    val triggerPhoneNotification = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPerm = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!hasPerm) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                onSendTestNotification(dose.id, dose.doseUnits, dose.type)
                Toast.makeText(
                    context,
                    if (isEn) "📲 2-Hour postprandial glucose notification sent to phone!" else "📲 2. saat tokluk şekeri bildirimi telefonunuza gönderildi!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            onSendTestNotification(dose.id, dose.doseUnits, dose.type)
            Toast.makeText(
                context,
                if (isEn) "📲 2-Hour postprandial glucose notification sent to phone!" else "📲 2. saat tokluk şekeri bildirimi telefonunuza gönderildi!",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("blood_glucose_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = HealthSurface),
            border = BorderStroke(1.5.dp, HealthTealBright)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .background(Color(0xFFE11D48).copy(alpha = 0.2f), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bloodtype,
                                contentDescription = null,
                                tint = Color(0xFFFB7185),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isEn) "2-Hour Postprandial Blood Glucose" else "2 Saat Sonraki Tokluk Şekeri Değeri",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextHealthPrimary
                            )
                            Text(
                                text = if (isEn) "2-Hour Post-Dose Glucose Log" else "2 Saat Sonrası Tokluk Ölçüm Kaydı",
                                style = MaterialTheme.typography.bodySmall,
                                color = HealthMint,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = if (isEn) "Close" else "Kapat",
                            tint = TextHealthMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Associated Dose Context Info
                Surface(
                    color = HealthSurfaceVariant,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, HealthBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isEn) "Associated Dose:" else "İlgili İnsülin Dozu:",
                                fontSize = 10.sp,
                                color = TextHealthMuted,
                                fontWeight = FontWeight.Medium
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "%.1f IU %s".format(dose.doseUnits, if (dose.type == DoseType.BOLUS) "Bolus" else (if (isEn) "Basal" else "Bazal")),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (dose.type == DoseType.BOLUS) BolusGold else BasalCyan
                                )
                                Text(
                                    text = if (isEn) " • At $timeStr" else " • Saat $timeStr",
                                    fontSize = 12.sp,
                                    color = TextHealthPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Text(
                                text = if (isEn)
                                    "Site: ${com.example.util.Strings.siteDisplayName(dose.injectionSite, lang)}"
                                else
                                    "Bölge: ${dose.injectionSite.displayName}",
                                fontSize = 11.sp,
                                color = HealthMint
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = ChartSlotBg,
                            border = BorderStroke(0.8.dp, HealthTealBright.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = if (isEn) "+2 Hours" else "+2 Saat",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = HealthCyanBright,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Input Field
                OutlinedTextField(
                    value = bgText,
                    onValueChange = {
                        if (it.length <= 3 && it.all { ch -> ch.isDigit() }) {
                            bgText = it
                            inputError = false
                        }
                    },
                    label = { Text(if (isEn) "Measured Blood Glucose" else "Ölçülen Kan Şekeri") },
                    placeholder = { Text("125") },
                    trailingIcon = {
                        Text(
                            text = "mg/dL",
                            color = HealthCyanBright,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(end = 12.dp)
                        )
                    },
                    isError = inputError,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HealthTealBright,
                        unfocusedBorderColor = HealthBorder,
                        focusedTextColor = TextHealthPrimary,
                        unfocusedTextColor = TextHealthPrimary,
                        cursorColor = HealthTealBright,
                        focusedContainerColor = HealthSurfaceVariant,
                        unfocusedContainerColor = HealthSurfaceVariant
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("glucose_input_field")
                )

                if (inputError) {
                    Text(
                        text = if (isEn)
                            "Please enter a valid blood glucose reading (40 - 500 mg/dL)"
                        else
                            "Lütfen geçerli bir kan şekeri değeri giriniz (40 - 500 mg/dL)",
                        color = Color(0xFFFB7185),
                        fontSize = 10.sp,
                        modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Quick Preset Chips
                Text(
                    text = if (isEn) "Quick Preset:" else "Hızlı Değer Seçimi:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = HealthMint
                )

                Spacer(modifier = Modifier.height(6.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val presets = listOf(95, 110, 125, 140, 160, 180)
                    presets.forEach { value ->
                        val isSelected = bgInt == value
                        Surface(
                            onClick = {
                                bgText = value.toString()
                                inputError = false
                            },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) HealthTeal else HealthSurfaceVariant,
                            border = BorderStroke(1.dp, if (isSelected) HealthTealBright else HealthBorder)
                        ) {
                            Text(
                                text = "$value mg/dL",
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
                                color = if (isSelected) IconOnAccent else TextHealthSecondary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Clinical Assessment Guide
                if (bgInt != null && bgInt in 40..500) {
                    val (statusText, statusColor, advice) = when {
                        bgInt < 70 -> if (isEn) {
                            Triple("⚠️ Hypoglycemia Risk (Low)", Color(0xFFFB7185), "Consume 15g fast-acting carbs (juice/glucose tabs) immediately.")
                        } else {
                            Triple("⚠️ Hipoglisemi Riski (Düşük)", Color(0xFFFB7185), "Acil 15g hızlı karbonhidrat (meyve suyu/kesme şeker) tüketiniz.")
                        }
                        bgInt in 70..140 -> if (isEn) {
                            Triple("✓ Ideal Postprandial Target", BioHealingGreen, "Great job! Insulin dose and meal balance worked perfectly.")
                        } else {
                            Triple("✓ İdeal Tokluk Hedefi", BioHealingGreen, "Tebrikler! İnsülin dozu ve öğün dengesi mükemmel çalıştı.")
                        }
                        bgInt in 141..180 -> if (isEn) {
                            Triple("ℹ️ Acceptable Postprandial", Color(0xFFFBBF24), "In target range; slightly elevated but within safe limits.")
                        } else {
                            Triple("ℹ️ Kabul Edilebilir Tokluk", Color(0xFFFBBF24), "Hedef aralıkta; hafif yüksek ancak güvenli seviyede.")
                        }
                        else -> if (isEn) {
                            Triple("⚠️ High Postprandial Glucose", Color(0xFFF87171), "Drink plenty of water. Follow your doctor's correction guidelines.")
                        } else {
                            Triple("⚠️ Yüksek Tokluk Şekeri", Color(0xFFF87171), "Bol su içiniz. Doktorunuzun önerdiği düzeltme kurallarını takip edin.")
                        }
                    }

                    Surface(
                        color = ChartSlotBg,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, statusColor.copy(alpha = 0.6f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = statusText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = statusColor
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = advice,
                                fontSize = 10.sp,
                                color = TextHealthSecondary,
                                lineHeight = 13.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Action Buttons: Save & Test Notification
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, HealthBorder),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (isEn) "Cancel" else "Vazgeç", color = TextHealthSecondary, fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            val value = bgText.toIntOrNull()
                            if (value == null || value !in 40..500) {
                                inputError = true
                            } else {
                                onSaveGlucose(dose.id, value)
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = HealthTeal),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("save_glucose_button")
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = IconOnAccent)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isEn) "Save Reading" else "Şekeri Kaydet",
                            color = IconOnAccent,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Instant Notification Test Button
                Surface(
                    onClick = { triggerPhoneNotification() },
                    shape = RoundedCornerShape(10.dp),
                    color = ChartSlotBg,
                    border = BorderStroke(0.8.dp, HealthBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = HealthCyanBright,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isEn) "Send Phone Notification Now" else "Telefona Tokluk Şekeri Bildirimi Gönder",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = HealthCyanBright
                        )
                    }
                }
            }
        }
    }
}

/**
 * Modern floating banner shown on HomeScreen when a dose was recently recorded
 * and is awaiting 2-hour postprandial blood glucose measurement.
 */
@Composable
fun BloodGlucoseCheckBanner(
    dose: DoseRecord,
    onOpenDialog: () -> Unit,
    modifier: Modifier = Modifier,
    lang: com.example.util.AppLanguage = com.example.util.AppLanguage.TR
) {
    val context = LocalContext.current
    val isEn = lang == com.example.util.AppLanguage.EN
    val timePassedMinutes = ((System.currentTimeMillis() - dose.timestamp) / 60000).toInt()
    val timePassedStr = if (isEn) {
        if (timePassedMinutes < 60) "$timePassedMinutes min ago" else "${timePassedMinutes / 60} hours ago"
    } else {
        if (timePassedMinutes < 60) "$timePassedMinutes dakika önce" else "${timePassedMinutes / 60} saat önce"
    }

    val bannerPermLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            NotificationHelper.showPostDoseGlucoseNotification(context, dose.id, dose.doseUnits, dose.type)
            Toast.makeText(
                context,
                if (isEn) "📲 2-Hour postprandial notification sent to phone!" else "📲 2. saat tokluk şekeri bildirimi telefonunuza gönderildi!",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            NotificationHelper.openAppNotificationSettings(context)
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("pending_glucose_check_banner"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = PostKpiBg),
        border = BorderStroke(1.2.dp, AlertPharmacyRuby.copy(alpha = 0.7f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header Row: Icon + Title + PENDING badge
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
                            .size(34.dp)
                            .background(AlertPharmacyRuby.copy(alpha = 0.18f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bloodtype,
                            contentDescription = null,
                            tint = AlertPharmacyRuby,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isEn) "2-Hour Postprandial Blood Glucose" else "2 Saat Sonraki Tokluk Şekeri Değeri",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextHealthPrimary,
                            maxLines = 1
                        )
                        Text(
                            text = "%.1f IU %s • %s".format(
                                dose.doseUnits,
                                if (dose.type == DoseType.BOLUS) "Bolus" else "Bazal",
                                timePassedStr
                            ),
                            fontSize = 11.sp,
                            color = TextHealthSecondary,
                            maxLines = 1
                        )
                    }
                }

                Surface(
                    color = AlertPharmacyRuby.copy(alpha = 0.16f),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(0.8.dp, AlertPharmacyRuby.copy(alpha = 0.45f))
                ) {
                    Text(
                        text = if (isEn) "PENDING" else "BEKLENİYOR",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = AlertPharmacyRuby,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons: Enter Glucose + Send Phone Notification
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onOpenDialog,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AlertPharmacyRuby),
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .testTag("banner_enter_glucose_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Bloodtype,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isEn) "Enter 2-Hour Glucose" else "Tokluk Şekerini Gir",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        maxLines = 1
                    )
                }

                Surface(
                    onClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            val hasPerm = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) == PackageManager.PERMISSION_GRANTED
                            if (!hasPerm) {
                                bannerPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                NotificationHelper.showPostDoseGlucoseNotification(context, dose.id, dose.doseUnits, dose.type)
                                Toast.makeText(
                                    context,
                                    if (isEn) "📲 Notification sent to phone!" else "📲 Bildirim telefonunuza gönderildi!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            NotificationHelper.showPostDoseGlucoseNotification(context, dose.id, dose.doseUnits, dose.type)
                            Toast.makeText(
                                context,
                                if (isEn) "📲 Notification sent to phone!" else "📲 Bildirim telefonunuza gönderildi!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    color = ChartSlotBg,
                    border = BorderStroke(1.dp, AlertPharmacyRuby.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .height(40.dp)
                        .testTag("banner_send_phone_notif_btn")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = if (isEn) "Send Phone Notification" else "Telefona Bildirim Gönder",
                            tint = AlertPharmacyRuby,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isEn) "Notify" else "Bildir",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = AlertPharmacyRuby
                        )
                    }
                }
            }
        }
    }
}

