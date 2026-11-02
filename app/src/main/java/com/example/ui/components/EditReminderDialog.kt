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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bloodtype
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.DoseReminder
import com.example.data.model.DoseType
import com.example.ui.theme.AlertPharmacyRuby
import com.example.ui.theme.AlertRed
import com.example.ui.theme.BasalCyan
import com.example.ui.theme.BioHealingGreen
import com.example.ui.theme.BolusGold
import com.example.ui.theme.ChartSlotBg
import com.example.ui.theme.FastingKpiBg
import com.example.ui.theme.HealthBorder
import com.example.ui.theme.HealthMint
import com.example.ui.theme.HealthSurface
import com.example.ui.theme.HealthSurfaceVariant
import com.example.ui.theme.HealthTeal
import com.example.ui.theme.HealthTealBright
import com.example.ui.theme.IconOnAccent
import com.example.ui.theme.PostKpiBg
import com.example.ui.theme.TextHealthPrimary
import com.example.ui.theme.TextHealthSecondary
import java.util.Locale

@Composable
fun EditReminderDialog(
    initialReminder: DoseReminder? = null,
    onDismiss: () -> Unit,
    onSave: (DoseReminder) -> Unit,
    onDelete: ((DoseReminder) -> Unit)? = null,
    lang: com.example.util.AppLanguage = com.example.util.AppLanguage.TR
) {
    val isEn = lang == com.example.util.AppLanguage.EN
    val isEditing = initialReminder != null
    var label by remember {
        mutableStateOf(initialReminder?.label ?: if (isEn) "Evening Basal Dose" else "Akşam Bazal Dozu")
    }
    var selectedType by remember { mutableStateOf(initialReminder?.type ?: DoseType.BASAL) }
    var hour by remember { mutableIntStateOf(initialReminder?.hour ?: 21) }
    var minute by remember { mutableIntStateOf(initialReminder?.minute ?: 0) }
    var doseUnitsText by remember { mutableStateOf(initialReminder?.defaultDoseUnits?.toInt()?.toString() ?: "16") }
    var isEnabled by remember { mutableStateOf(initialReminder?.isEnabled ?: true) }
    var requestFastingGlucose by remember { mutableStateOf(initialReminder?.requestFastingGlucose ?: true) }
    var requestPostGlucose by remember { mutableStateOf(initialReminder?.requestPostGlucose ?: true) }

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = HealthTealBright,
        unfocusedBorderColor = HealthBorder,
        focusedTextColor = TextHealthPrimary,
        unfocusedTextColor = TextHealthPrimary,
        focusedLabelColor = HealthTealBright,
        unfocusedLabelColor = TextHealthSecondary,
        cursorColor = HealthTealBright,
        focusedContainerColor = HealthSurfaceVariant,
        unfocusedContainerColor = HealthSurfaceVariant
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .testTag("dialog_edit_reminder"),
            color = HealthSurface,
            border = BorderStroke(1.5.dp, HealthBorder)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isEditing) {
                            if (isEn) "⏰ Edit Reminder" else "⏰ Hatırlatıcıyı Düzenle"
                        } else {
                            if (isEn) "⏰ Add New Reminder" else "⏰ Yeni Hatırlatıcı Ekle"
                        },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextHealthPrimary
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("button_reminder_dialog_close")) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = if (isEn) "Close" else "Kapat",
                            tint = TextHealthSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Type Tab: Bazal vs Bolus
                TabRow(
                    selectedTabIndex = if (selectedType == DoseType.BASAL) 0 else 1,
                    modifier = Modifier.clip(RoundedCornerShape(12.dp)),
                    containerColor = HealthSurfaceVariant,
                    contentColor = HealthTealBright
                ) {
                    Tab(
                        selected = selectedType == DoseType.BASAL,
                        onClick = {
                            selectedType = DoseType.BASAL
                            if (label == "Öğle Bolusu" || label == "Kahvaltı Bolusu" || label == "Öğün Öncesi Bolus") {
                                label = if (isEn) "Evening Basal Dose" else "Akşam Bazal Dozu"
                            }
                        },
                        text = {
                            Text(
                                text = if (isEn) "Fixed Time (Basal)" else "Sabit Saatli (Bazal)",
                                fontWeight = FontWeight.Bold,
                                color = if (selectedType == DoseType.BASAL) BasalCyan else TextHealthSecondary
                            )
                        },
                        modifier = Modifier.testTag("reminder_tab_basal")
                    )
                    Tab(
                        selected = selectedType == DoseType.BOLUS,
                        onClick = {
                            selectedType = DoseType.BOLUS
                            if (label == "Akşam Bazal Dozu" || label == "Evening Basal Dose") {
                                label = if (isEn) "Pre-Meal Bolus" else "Öğün Öncesi Bolus"
                            }
                        },
                        text = {
                            Text(
                                text = if (isEn) "Mealtime (Bolus)" else "Yemek Öncesi (Bolus)",
                                fontWeight = FontWeight.Bold,
                                color = if (selectedType == DoseType.BOLUS) BolusGold else TextHealthSecondary
                            )
                        },
                        modifier = Modifier.testTag("reminder_tab_bolus")
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text(if (isEn) "Reminder Name" else "Hatırlatıcı Adı") },
                    placeholder = { Text(if (isEn) "e.g. Evening Basal Insulin" else "Örn: Akşam Bazal İnsülini") },
                    colors = textFieldColors,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_reminder_label")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Time picker inputs (Hour & Minute)
                Text(
                    text = if (isEn) "Alarm Time:" else "Alarm Saati:",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextHealthPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = String.format(Locale.getDefault(), "%02d", hour),
                        onValueChange = { input ->
                            val h = input.filter { it.isDigit() }.toIntOrNull() ?: 0
                            if (h in 0..23) hour = h
                        },
                        label = { Text(if (isEn) "Hour (00-23)" else "Saat (00-23)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = textFieldColors,
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("input_reminder_hour")
                    )

                    Text(":", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextHealthPrimary)

                    OutlinedTextField(
                        value = String.format(Locale.getDefault(), "%02d", minute),
                        onValueChange = { input ->
                            val m = input.filter { it.isDigit() }.toIntOrNull() ?: 0
                            if (m in 0..59) minute = m
                        },
                        label = { Text(if (isEn) "Minute (00-59)" else "Dakika (00-59)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = textFieldColors,
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("input_reminder_minute")
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = doseUnitsText,
                    onValueChange = { doseUnitsText = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text(if (isEn) "Default Dose Units (IU)" else "Varsayılan Doz Miktarı (IU)") },
                    placeholder = { Text("14") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = textFieldColors,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_reminder_units")
                )

                Spacer(modifier = Modifier.height(16.dp))

                // NEW: Blood Glucose Request Options (Fasting & Postprandial)
                Text(
                    text = if (isEn) "Blood Glucose Request Preferences" else "Kan Şekeri Ölçüm Talebi (Açlık / Tokluk)",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = HealthMint
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isEn)
                        "Select whether you want to be asked for Fasting blood glucose, 2-hour Postprandial blood glucose, or both."
                    else
                        "İsterseniz sadece açlık, sadece tokluk veya her iki kan şekeri ölçüm talebini aktif edebilirsiniz.",
                    fontSize = 11.sp,
                    color = TextHealthSecondary,
                    lineHeight = 14.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                // 1) Fasting Blood Glucose Request Card
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (requestFastingGlucose) FastingKpiBg else ChartSlotBg,
                    border = BorderStroke(
                        width = if (requestFastingGlucose) 1.5.dp else 1.dp,
                        color = if (requestFastingGlucose) BioHealingGreen else HealthBorder
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { requestFastingGlucose = !requestFastingGlucose }
                        .testTag("card_reminder_fasting_glucose")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(BioHealingGreen.copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bloodtype,
                                    contentDescription = null,
                                    tint = BioHealingGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (isEn) "Request Fasting Glucose" else "Açlık Kan Şekeri Talebi",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = TextHealthPrimary
                                )
                                Text(
                                    text = if (isEn) "Ask for pre-dose fasting reading" else "Doz öncesi açlık kan şekeri ölçümü sorulsun",
                                    fontSize = 10.sp,
                                    color = TextHealthSecondary
                                )
                            }
                        }

                        Switch(
                            checked = requestFastingGlucose,
                            onCheckedChange = { requestFastingGlucose = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = IconOnAccent,
                                checkedTrackColor = BioHealingGreen
                            ),
                            modifier = Modifier.testTag("switch_reminder_fasting_glucose")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 2) Postprandial (2-Hour) Blood Glucose Request Card
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (requestPostGlucose) PostKpiBg else ChartSlotBg,
                    border = BorderStroke(
                        width = if (requestPostGlucose) 1.5.dp else 1.dp,
                        color = if (requestPostGlucose) AlertPharmacyRuby else HealthBorder
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { requestPostGlucose = !requestPostGlucose }
                        .testTag("card_reminder_post_glucose")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(AlertPharmacyRuby.copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = AlertPharmacyRuby,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (isEn) "Request 2-Hour Postprandial Glucose" else "Tokluk Kan Şekeri Talebi (2. Saat)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = TextHealthPrimary
                                )
                                Text(
                                    text = if (isEn) "Notify 2 hours after dose for post-meal reading" else "Dozdan 2 saat sonra tokluk şekeri bildirimi gönderilsin",
                                    fontSize = 10.sp,
                                    color = TextHealthSecondary
                                )
                            }
                        }

                        Switch(
                            checked = requestPostGlucose,
                            onCheckedChange = { requestPostGlucose = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = IconOnAccent,
                                checkedTrackColor = AlertPharmacyRuby
                            ),
                            modifier = Modifier.testTag("switch_reminder_post_glucose")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isEn) "Reminder Enabled" else "Hatırlatıcı Aktif",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextHealthPrimary
                    )
                    Switch(
                        checked = isEnabled,
                        onCheckedChange = { isEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = IconOnAccent,
                            checkedTrackColor = HealthTeal
                        ),
                        modifier = Modifier.testTag("switch_reminder_enabled")
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (isEditing && onDelete != null && initialReminder != null) {
                        OutlinedButton(
                            onClick = {
                                onDelete(initialReminder)
                                onDismiss()
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = AlertRed),
                            border = BorderStroke(1.dp, AlertRed),
                            modifier = Modifier.testTag("button_delete_reminder")
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = AlertRed)
                        }
                    }

                    Button(
                        onClick = {
                            val units = doseUnitsText.toDoubleOrNull() ?: 10.0
                            val reminder = (initialReminder ?: DoseReminder(
                                label = label,
                                type = selectedType,
                                hour = hour,
                                minute = minute,
                                defaultDoseUnits = units,
                                isEnabled = isEnabled,
                                requestFastingGlucose = requestFastingGlucose,
                                requestPostGlucose = requestPostGlucose
                            )).copy(
                                label = label,
                                type = selectedType,
                                hour = hour,
                                minute = minute,
                                defaultDoseUnits = units,
                                isEnabled = isEnabled,
                                requestFastingGlucose = requestFastingGlucose,
                                requestPostGlucose = requestPostGlucose
                            )
                            onSave(reminder)
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f).height(48.dp).testTag("button_save_reminder"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = HealthTeal,
                            contentColor = IconOnAccent
                        )
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, tint = IconOnAccent)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isEn) "Save Reminder" else "Hatırlatıcıyı Kaydet",
                            fontWeight = FontWeight.ExtraBold,
                            color = IconOnAccent
                        )
                    }
                }
            }
        }
    }
}
