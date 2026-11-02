package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bloodtype
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.BodyAspect
import com.example.data.model.DoseType
import com.example.data.model.InjectionSite
import com.example.ui.theme.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuickDoseDialog(
    initialType: DoseType = DoseType.BOLUS,
    initialUnits: Double = 10.0,
    initialSite: InjectionSite? = null,
    recommendedSite: InjectionSite = InjectionSite.ABDOMEN_RIGHT,
    lastSite: InjectionSite? = null,
    initialMealTag: String = "",
    onDismiss: () -> Unit,
    onSave: (
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
    lang: com.example.util.AppLanguage = com.example.util.AppLanguage.TR
) {
    var selectedType by remember { mutableStateOf(initialType) }
    var doseUnits by remember { mutableDoubleStateOf(initialUnits) }
    var doseUnitsText by remember { mutableStateOf(initialUnits.toString()) }
    var isManualDoseEditing by remember { mutableStateOf(false) }

    var selectedSite by remember { mutableStateOf(initialSite ?: InjectionSite.ABDOMEN_RIGHT) }
    var bodyAspect by remember { mutableStateOf(selectedSite.aspect) }

    val commonTags = if (selectedType == DoseType.BASAL) {
        listOf(
            com.example.util.Strings.mealTagEveningBasal(lang),
            com.example.util.Strings.mealTagMorningBasal(lang),
            com.example.util.Strings.mealTagNightDose(lang)
        )
    } else {
        listOf(
            com.example.util.Strings.mealTagBreakfast(lang),
            com.example.util.Strings.mealTagLunch(lang),
            com.example.util.Strings.mealTagDinner(lang),
            com.example.util.Strings.mealTagSnack(lang),
            com.example.util.Strings.mealTagCorrection(lang)
        )
    }

    var mealTag by remember {
        mutableStateOf(
            initialMealTag.ifEmpty {
                commonTags.firstOrNull() ?: ""
            }
        )
    }
    var trackFastingGlucose by remember { mutableStateOf(true) }
    var trackPostGlucose by remember { mutableStateOf(true) }
    var bloodGlucoseText by remember { mutableStateOf("") }

    val presetDoses = listOf(2.0, 4.0, 6.0, 8.0, 10.0, 12.0, 14.0, 16.0, 18.0, 20.0, 24.0)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(24.dp))
                .testTag("dialog_quick_dose"),
            color = HealthSurface,
            border = BorderStroke(1.5.dp, HealthBorder)
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Dialog Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = com.example.util.Strings.dialogTitle(lang),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = TextHealthPrimary
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("button_dialog_close")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = if (lang == com.example.util.AppLanguage.EN) "Close" else "Kapat",
                            tint = TextHealthSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Type Tab: Bazal vs Bolus
                TabRow(
                    selectedTabIndex = if (selectedType == DoseType.BASAL) 0 else 1,
                    modifier = Modifier.clip(RoundedCornerShape(12.dp)),
                    containerColor = HealthSurfaceVariant
                ) {
                    Tab(
                        selected = selectedType == DoseType.BASAL,
                        onClick = {
                            selectedType = DoseType.BASAL
                            mealTag = com.example.util.Strings.mealTagEveningBasal(lang)
                        },
                        text = {
                            Text(
                                com.example.util.Strings.basalTab(lang),
                                fontWeight = if (selectedType == DoseType.BASAL) FontWeight.ExtraBold else FontWeight.Medium,
                                color = if (selectedType == DoseType.BASAL) BasalCyan else TextHealthSecondary,
                                fontSize = 11.5.sp
                            )
                        },
                        modifier = Modifier.testTag("tab_type_basal")
                    )
                    Tab(
                        selected = selectedType == DoseType.BOLUS,
                        onClick = {
                            selectedType = DoseType.BOLUS
                            mealTag = com.example.util.Strings.mealTagBreakfast(lang)
                        },
                        text = {
                            Text(
                                com.example.util.Strings.bolusTab(lang),
                                fontWeight = if (selectedType == DoseType.BOLUS) FontWeight.ExtraBold else FontWeight.Medium,
                                color = if (selectedType == DoseType.BOLUS) BolusGold else TextHealthSecondary,
                                fontSize = 11.5.sp
                            )
                        },
                        modifier = Modifier.testTag("tab_type_bolus")
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // DOSE AMOUNT STEPPER & DIRECT MANUAL INPUT
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = com.example.util.Strings.doseAmountLabel(lang),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextHealthPrimary
                    )

                    // Toggle manual typing mode button
                    Surface(
                        onClick = { isManualDoseEditing = !isManualDoseEditing },
                        shape = RoundedCornerShape(8.dp),
                        color = if (isManualDoseEditing) HealthTeal else HealthSurfaceVariant,
                        border = BorderStroke(1.dp, if (isManualDoseEditing) HealthTealBright else HealthBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                tint = if (isManualDoseEditing) IconOnAccent else TextHealthPrimary,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isManualDoseEditing) com.example.util.Strings.doneManual(lang) else com.example.util.Strings.enterManually(lang),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isManualDoseEditing) IconOnAccent else TextHealthPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (isManualDoseEditing) {
                    // Direct Keyboard Input Field
                    OutlinedTextField(
                        value = doseUnitsText,
                        onValueChange = { input ->
                            val sanitized = input.replace(',', '.')
                            doseUnitsText = sanitized
                            val parsed = sanitized.toDoubleOrNull()
                            if (parsed != null && parsed >= 0) {
                                doseUnits = parsed
                            }
                        },
                        label = { Text(com.example.util.Strings.exactDosePrompt(lang)) },
                        placeholder = { Text("Örn: 8.5") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_manual_dose_units"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = HealthTealBright,
                            unfocusedBorderColor = HealthBorder,
                            focusedTextColor = TextHealthPrimary,
                            unfocusedTextColor = TextHealthPrimary,
                            focusedContainerColor = ChartSlotBg,
                            unfocusedContainerColor = ChartSlotBg
                        ),
                        singleLine = true
                    )
                } else {
                    // Interactive Clickable Stepper Display
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        IconButton(
                            onClick = {
                                if (doseUnits > 0.5) {
                                    doseUnits = (doseUnits - 1.0).coerceAtLeast(0.5)
                                    doseUnitsText = doseUnits.toString()
                                }
                            },
                            modifier = Modifier
                                .size(46.dp)
                                .background(HealthSurfaceVariant, CircleShape)
                                .border(1.2.dp, HealthBorder, CircleShape)
                                .testTag("button_dose_minus")
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = TextHealthPrimary)
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        // Clickable dose text to directly type custom amount
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(ChartSlotBg)
                                .border(1.5.dp, if (selectedType == DoseType.BASAL) BasalCyan else BolusGold, RoundedCornerShape(12.dp))
                                .clickable { isManualDoseEditing = true }
                                .padding(horizontal = 18.dp, vertical = 6.dp)
                                .testTag("clickable_dose_amount"),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "%.1f IU".format(doseUnits),
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (selectedType == DoseType.BASAL) BasalCyan else BolusGold
                                )
                                Text(
                                    text = com.example.util.Strings.clickToEdit(lang),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextHealthSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        IconButton(
                            onClick = {
                                doseUnits += 1.0
                                doseUnitsText = doseUnits.toString()
                            },
                            modifier = Modifier
                                .size(46.dp)
                                .background(HealthSurfaceVariant, CircleShape)
                                .border(1.2.dp, HealthBorder, CircleShape)
                                .testTag("button_dose_plus")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Increase", tint = TextHealthPrimary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Fast Presets Chips
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    presetDoses.forEach { preset ->
                        FilterChip(
                            selected = doseUnits == preset,
                            onClick = {
                                doseUnits = preset
                                doseUnitsText = preset.toString()
                            },
                            label = {
                                Text(
                                    text = "%.0f IU".format(preset),
                                    fontSize = 11.sp,
                                    fontWeight = if (doseUnits == preset) FontWeight.ExtraBold else FontWeight.SemiBold
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = ChartSlotBg,
                                labelColor = TextHealthPrimary,
                                selectedContainerColor = HealthTeal,
                                selectedLabelColor = IconOnAccent
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = doseUnits == preset,
                                borderColor = HealthBorder,
                                selectedBorderColor = HealthTealBright
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // VISUAL ANATOMICAL INJECTION ROTATION ASSISTANT INSIDE DIALOG
                Text(
                    text = com.example.util.Strings.injectionSiteSectionTitle(lang),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextHealthPrimary
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Status Banner: Theme-aware, high-contrast rotation & last-injected site summary box
                Surface(
                    color = if (lastSite != null) {
                        if (isDayTheme) WarningAmberContainer.copy(alpha = 0.72f) else ChartSlotSelectedBg
                    } else {
                        ChartSlotBg
                    },
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(
                        1.2.dp,
                        if (lastSite != null) WarningAmberHealth.copy(alpha = 0.65f) else HealthBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        if (lastSite != null) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .background(
                                            WarningAmberHealth.copy(alpha = if (isDayTheme) 0.18f else 0.22f),
                                            CircleShape
                                        )
                                        .border(1.dp, WarningAmberHealth.copy(alpha = 0.6f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.History,
                                        contentDescription = null,
                                        tint = WarningAmberHealth,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = com.example.util.Strings.lastInjectedSite(
                                            lang,
                                            com.example.util.Strings.siteDisplayName(lastSite, lang)
                                        ),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = WarningAmberHealth
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = com.example.util.Strings.restSiteWarning(lang),
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextHealthPrimary.copy(alpha = 0.85f)
                                    )
                                }
                            }
                        } else {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(HealthCyanBright, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = com.example.util.Strings.noPriorSite(lang),
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextHealthPrimary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Active selected site pill badge on the right for instant clarity
                        Surface(
                            color = if (selectedSite == lastSite) WarningAmberHealth else HealthTeal,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = com.example.util.Strings.siteDisplayName(selectedSite, lang),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = IconOnAccent,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Body Aspect Switcher (Ön Vücut / Arka Vücut)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(HealthSurfaceVariant)
                        .border(1.dp, HealthBorder, RoundedCornerShape(10.dp))
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Surface(
                        onClick = { bodyAspect = BodyAspect.ANTERIOR },
                        modifier = Modifier.weight(1f).height(34.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = if (bodyAspect == BodyAspect.ANTERIOR) HealthTeal else Color.Transparent
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                com.example.util.Strings.anteriorTab(lang),
                                fontSize = 11.5.sp,
                                fontWeight = if (bodyAspect == BodyAspect.ANTERIOR) FontWeight.ExtraBold else FontWeight.Bold,
                                color = if (bodyAspect == BodyAspect.ANTERIOR) IconOnAccent else TextHealthPrimary
                            )
                        }
                    }
                    Surface(
                        onClick = { bodyAspect = BodyAspect.POSTERIOR },
                        modifier = Modifier.weight(1f).height(34.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = if (bodyAspect == BodyAspect.POSTERIOR) HealthTeal else Color.Transparent
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                com.example.util.Strings.posteriorTab(lang),
                                fontSize = 11.5.sp,
                                fontWeight = if (bodyAspect == BodyAspect.POSTERIOR) FontWeight.ExtraBold else FontWeight.Bold,
                                color = if (bodyAspect == BodyAspect.POSTERIOR) IconOnAccent else TextHealthPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // REAL ANATOMICAL HUMAN BODY FIGURE IN DIALOG (RECOMMENDATION REMOVED - ONLY LAST SITE SHOWN)
                AnatomicalHumanBodyFigure(
                    aspect = bodyAspect,
                    lastSite = lastSite,
                    recommendedSite = null, // Strictly null: Recommendation removed in new dose entry
                    selectedSite = selectedSite,
                    onSiteSelected = { site ->
                        selectedSite = site
                        bodyAspect = site.aspect
                    },
                    lang = lang
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Quick Chips for Selection (Shows ONLY last site badge, NO recommendation badge)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val currentAspectSites = InjectionSite.values().filter { it.aspect == bodyAspect }
                    currentAspectSites.forEach { site ->
                        val isSelected = selectedSite == site
                        val isLast = lastSite == site

                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    selectedSite = site
                                }
                                .testTag("select_site_${site.id}"),
                            shape = RoundedCornerShape(10.dp),
                            color = when {
                                isSelected -> HealthTeal
                                isLast -> WarningAmberContainer
                                else -> ChartSlotBg
                            },
                            border = BorderStroke(
                                1.2.dp,
                                when {
                                    isSelected -> HealthTealBright
                                    isLast -> WarningAmberHealth
                                    else -> HealthBorder
                                }
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = com.example.util.Strings.siteDisplayName(site, lang),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when {
                                        isSelected -> IconOnAccent
                                        isLast -> WarningAmberHealth
                                        else -> TextHealthPrimary
                                    }
                                )
                                if (isLast) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        com.example.util.Strings.tagLast(lang),
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.Black,
                                        color = if (isSelected) IconOnAccent else WarningAmberHealth
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Meal / Occasion Tag
                Text(
                    text = com.example.util.Strings.mealTagLabel(lang),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextHealthPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    commonTags.forEach { tag ->
                        FilterChip(
                            selected = mealTag == tag,
                            onClick = { mealTag = tag },
                            label = {
                                Text(
                                    text = tag,
                                    fontSize = 11.sp,
                                    fontWeight = if (mealTag == tag) FontWeight.ExtraBold else FontWeight.SemiBold
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = ChartSlotBg,
                                labelColor = TextHealthPrimary,
                                selectedContainerColor = HealthTeal,
                                selectedLabelColor = IconOnAccent
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = mealTag == tag,
                                borderColor = HealthBorder,
                                selectedBorderColor = HealthTealBright
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Blood Glucose Tracking & Notification Preference Section
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("card_glucose_tracking_preference"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = HealthSurfaceVariant),
                    border = BorderStroke(
                        1.2.dp,
                        when {
                            trackFastingGlucose && trackPostGlucose -> HealthTealBright
                            trackFastingGlucose -> BioHealingGreen
                            trackPostGlucose -> AlertPharmacyRuby
                            else -> HealthBorder
                        }
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Bloodtype,
                                    contentDescription = null,
                                    tint = if (trackFastingGlucose || trackPostGlucose) AlertPharmacyRuby else TextHealthSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (lang == com.example.util.AppLanguage.EN)
                                        "Blood Glucose Tracking & Alert"
                                    else
                                        "Kan Şekeri Takibi ve Bildirim Tercihi",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = TextHealthPrimary
                                )
                            }
                        }

                        Text(
                            text = if (lang == com.example.util.AppLanguage.EN)
                                "Choose whether you want to track pre-dose fasting glucose, 2-hour postprandial glucose, both, or none:"
                            else
                                "Doz öncesi açlık ve doz sonrası tokluk kan şekeri takibi ile telefon bildirimi tercihinizi seçin:",
                            fontSize = 10.5.sp,
                            color = TextHealthSecondary,
                            modifier = Modifier.padding(top = 4.dp, bottom = 10.dp)
                        )

                        // Row 1: "Sadece Açlık Takibi" & "Sadece Tokluk Takibi"
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val isOnlyFasting = trackFastingGlucose && !trackPostGlucose
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        trackFastingGlucose = true
                                        trackPostGlucose = false
                                    }
                                    .testTag("btn_track_only_fasting"),
                                color = if (isOnlyFasting) BioHealingGreen else ChartSlotBg,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(
                                    1.2.dp,
                                    if (isOnlyFasting) BioHealingGreen else HealthBorder
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = if (lang == com.example.util.AppLanguage.EN)
                                            "🩸 Fasting Only"
                                        else
                                            "🩸 Sadece Açlık Takibi",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (isOnlyFasting) IconOnAccent else TextHealthPrimary,
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        text = if (lang == com.example.util.AppLanguage.EN)
                                            "Pre-dose only"
                                        else
                                            "Doz öncesi açlık",
                                        fontSize = 9.5.sp,
                                        color = if (isOnlyFasting) IconOnAccent.copy(alpha = 0.9f) else TextHealthSecondary,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }

                            val isOnlyPost = !trackFastingGlucose && trackPostGlucose
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        trackFastingGlucose = false
                                        trackPostGlucose = true
                                    }
                                    .testTag("btn_track_only_post"),
                                color = if (isOnlyPost) AlertPharmacyRuby else ChartSlotBg,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(
                                    1.2.dp,
                                    if (isOnlyPost) AlertPharmacyRuby else HealthBorder
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = if (lang == com.example.util.AppLanguage.EN)
                                            "⏱️ Post-Dose Only"
                                        else
                                            "⏱️ Sadece Tokluk Takibi",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (isOnlyPost) Color.White else TextHealthPrimary,
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        text = if (lang == com.example.util.AppLanguage.EN)
                                            "2-hr phone alert"
                                        else
                                            "2. saat bildirimli",
                                        fontSize = 9.5.sp,
                                        color = if (isOnlyPost) Color.White.copy(alpha = 0.9f) else TextHealthSecondary,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Row 2: "Açlık + Tokluk Takibi" & "Takip İstemiyorum (Bildirim Yok)"
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val isBoth = trackFastingGlucose && trackPostGlucose
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        trackFastingGlucose = true
                                        trackPostGlucose = true
                                    }
                                    .testTag("btn_track_both_glucose"),
                                color = if (isBoth) HealthTeal else ChartSlotBg,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(
                                    1.2.dp,
                                    if (isBoth) HealthTealBright else HealthBorder
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = if (lang == com.example.util.AppLanguage.EN)
                                            "✓ Fasting + Post-Dose"
                                        else
                                            "✓ Açlık + Tokluk Takibi",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (isBoth) IconOnAccent else TextHealthPrimary,
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        text = if (lang == com.example.util.AppLanguage.EN)
                                            "Full tracking & alert"
                                        else
                                            "Tam takip ve bildirim",
                                        fontSize = 9.5.sp,
                                        color = if (isBoth) IconOnAccent.copy(alpha = 0.9f) else TextHealthSecondary,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }

                            val isNone = !trackFastingGlucose && !trackPostGlucose
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        trackFastingGlucose = false
                                        trackPostGlucose = false
                                    }
                                    .testTag("btn_track_none_glucose"),
                                color = if (isNone) ChartSlotSelectedBg else ChartSlotBg,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(
                                    1.2.dp,
                                    if (isNone) TextHealthSecondary else HealthBorder
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = if (lang == com.example.util.AppLanguage.EN)
                                            "🔕 No Glucose Tracking"
                                        else
                                            "🔕 Takip İstemiyorum",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = TextHealthPrimary,
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        text = if (lang == com.example.util.AppLanguage.EN)
                                            "No phone alert"
                                        else
                                            "Bildirim gönderilmez",
                                        fontSize = 9.5.sp,
                                        color = TextHealthSecondary,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Notification & Tracking Summary Banner
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = ChartSlotBg,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(0.8.dp, HealthBorder)
                        ) {
                            val statusText = when {
                                trackFastingGlucose && trackPostGlucose ->
                                    if (lang == com.example.util.AppLanguage.EN)
                                        "🔔 Notification Active: Pre-dose fasting glucose will be recorded & you will receive a 2-hour postprandial glucose notification on your phone."
                                    else
                                        "🔔 Bildirim Açık: Doz öncesi açlık şekeri takip edilecek ve 2 saat sonra tokluk kan şekeri ölçümü için telefonunuza bildirim gönderilecektir."
                                trackFastingGlucose && !trackPostGlucose ->
                                    if (lang == com.example.util.AppLanguage.EN)
                                        "🩸 Fasting Tracking Only: Pre-dose fasting glucose is tracked. No 2-hour postprandial notification will be sent."
                                    else
                                        "🩸 Sadece Açlık Takibi Aktif: Doz öncesi açlık şekeriniz takip edilir (girilmezse açlık hatırlatma bildirimi gelir), 2. saat tokluk bildirimi gönderilmez."
                                !trackFastingGlucose && trackPostGlucose ->
                                    if (lang == com.example.util.AppLanguage.EN)
                                        "⏱️ Post-Dose Tracking Only: Pre-dose fasting is skipped. A phone notification will be sent in 2 hours for postprandial glucose."
                                    else
                                        "⏱️ Sadece Tokluk Takibi Aktif: Açlık şekeri sorulmaz. Dozdan 2 saat sonra tokluk kan şekeri ölçümü için telefonunuza bildirim gönderilecektir."
                                else ->
                                    if (lang == com.example.util.AppLanguage.EN)
                                        "🔕 Tracking Disabled: No blood glucose notifications will be sent to your phone for this dose."
                                    else
                                        "🔕 Takip ve Bildirim Kapalı: Bu doz için kan şekeri takibi yapılmayacak ve telefonunuza bildirim gönderilmeyecektir."
                            }
                            Text(
                                text = statusText,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextHealthSecondary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                // Prominent Pre-Dose Fasting Blood Glucose Section (shown when trackFastingGlucose is enabled)
                if (trackFastingGlucose) {
                    Spacer(modifier = Modifier.height(10.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = HealthSurfaceVariant),
                        border = BorderStroke(1.2.dp, if (bloodGlucoseText.isNotEmpty()) BioHealingGreen else HealthTealBright)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Bloodtype,
                                        contentDescription = null,
                                        tint = AlertPharmacyRuby,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (lang == com.example.util.AppLanguage.EN) "Pre-Dose Fasting Blood Glucose:" else "Doz Öncesi Açlık Şekeri:",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = TextHealthPrimary
                                    )
                                }
                                Surface(
                                    color = if (bloodGlucoseText.isNotEmpty()) BioGreenContainer else ChartSlotSelectedBg,
                                    shape = RoundedCornerShape(6.dp),
                                    border = BorderStroke(
                                        0.8.dp,
                                        if (bloodGlucoseText.isNotEmpty()) BioHealingGreen else HealthBorder
                                    )
                                ) {
                                    Text(
                                        text = if (bloodGlucoseText.isNotEmpty()) {
                                            if (lang == com.example.util.AppLanguage.EN) "ENTERED ✓" else "GİRİLDİ ✓"
                                        } else {
                                            if (lang == com.example.util.AppLanguage.EN) "TRACKING ON" else "TAKİP AÇIK"
                                        },
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        color = if (bloodGlucoseText.isNotEmpty()) BioHealingGreen else HealthCyanBright,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Text(
                                text = if (lang == com.example.util.AppLanguage.EN)
                                    "Checking fasting glucose before injection ensures accurate dose adjustment (Target: 70–130 mg/dL)."
                                else
                                    "İnsülin vurmadan önce açlık şekerinizi ölçüp girmeniz dozu güvenle ayarlamanızı sağlar (İdeal Açlık: 70–130 mg/dL).",
                                fontSize = 10.5.sp,
                                color = TextHealthSecondary,
                                modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
                            )

                            // Quick chips for fasting sugar
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                listOf(85, 95, 110, 125, 140, 160).forEach { preset ->
                                    val isPresetSelected = bloodGlucoseText == preset.toString()
                                    Surface(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { bloodGlucoseText = preset.toString() },
                                        color = if (isPresetSelected) BioHealingGreen else ChartSlotBg,
                                        border = BorderStroke(0.8.dp, if (isPresetSelected) BioHealingGreen else HealthBorder)
                                    ) {
                                        Text(
                                            text = "$preset",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isPresetSelected) IconOnAccent else TextHealthPrimary,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = bloodGlucoseText,
                                onValueChange = { bloodGlucoseText = it.filter { char -> char.isDigit() } },
                                label = { Text(com.example.util.Strings.fastingGlucoseInputLabel(lang)) },
                                placeholder = { Text(com.example.util.Strings.fastingGlucoseInputPlaceholder(lang)) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                trailingIcon = {
                                    Text(
                                        text = "mg/dL",
                                        color = HealthCyanBright,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(end = 12.dp)
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_blood_glucose"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = BioHealingGreen,
                                    unfocusedBorderColor = HealthBorder,
                                    focusedTextColor = TextHealthPrimary,
                                    unfocusedTextColor = TextHealthPrimary,
                                    focusedContainerColor = ChartSlotBg,
                                    unfocusedContainerColor = ChartSlotBg
                                ),
                                singleLine = true
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Confirmation / Save Button
                val saveBtnContentColor = if (isDayTheme) IconOnAccent else HealthBg
                Button(
                    onClick = {
                        val bg = if (trackFastingGlucose) bloodGlucoseText.toIntOrNull() else null
                        val brandName = if (selectedType == DoseType.BASAL) {
                            if (lang == com.example.util.AppLanguage.EN) "Basal Insulin" else "Bazal İnsülin"
                        } else {
                            if (lang == com.example.util.AppLanguage.EN) "Bolus Insulin" else "Bolus İnsülin"
                        }
                        onSave(
                            doseUnits,
                            selectedType,
                            selectedSite,
                            mealTag,
                            bg,
                            "",
                            brandName,
                            trackFastingGlucose,
                            trackPostGlucose
                        )
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("button_save_dose_confirm"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedType == DoseType.BASAL) BasalCyan else BolusGold,
                        contentColor = saveBtnContentColor
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp)
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = saveBtnContentColor)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = com.example.util.Strings.saveDoseButton(lang, doseUnits),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = saveBtnContentColor
                    )
                }
            }
        }
    }
}

