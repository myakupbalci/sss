package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import com.example.data.model.DoseReminder
import com.example.data.model.DoseType
import com.example.data.model.InjectionSite
import com.example.ui.theme.*

@Composable
fun ActiveReminderBanner(
    reminder: DoseReminder?,
    recommendedSite: InjectionSite,
    onConfirm: (DoseReminder) -> Unit,
    onSnooze15: (DoseReminder) -> Unit,
    onCustomize: (DoseReminder) -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = reminder != null,
        enter = fadeIn() + slideInVertically(),
        exit = fadeOut() + slideOutVertically(),
        modifier = modifier
    ) {
        if (reminder == null) return@AnimatedVisibility

        val isBasal = reminder.type == DoseType.BASAL
        val themeColor = if (isBasal) BasalCyan else BolusGold
        val bgGradient = if (isBasal) {
            Brush.linearGradient(listOf(BasalKpiBg, HealthSurface))
        } else {
            Brush.linearGradient(listOf(BolusKpiBg, HealthSurface))
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(6.dp, RoundedCornerShape(24.dp), spotColor = themeColor.copy(alpha = 0.25f))
                .testTag("active_reminder_banner"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = HealthSurface),
            border = BorderStroke(1.5.dp, themeColor.copy(alpha = 0.8f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(bgGradient)
                    .padding(18.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Header tag & icon
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(themeColor, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = "Aktif Hatırlatıcı",
                                    tint = IconOnAccent,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Surface(
                                    color = themeColor.copy(alpha = 0.20f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = if (isBasal) "SABİT SAATLİ BAZAL" else "YEMEK ÖNCESİ BOLUS",
                                        color = themeColor,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${reminder.label} (${reminder.formattedTime})",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = TextHealthPrimary
                                )
                            }
                        }

                        // Dose amount pill
                        Surface(
                            color = themeColor,
                            shape = RoundedCornerShape(14.dp),
                            shadowElevation = 2.dp
                        ) {
                            Text(
                                text = "%.1f IU".format(reminder.defaultDoseUnits),
                                color = IconOnAccent,
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Glucose request badges if enabled on reminder
                    if (reminder.requestFastingGlucose || reminder.requestPostGlucose) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (reminder.requestFastingGlucose) {
                                Surface(
                                    color = FastingKpiBg,
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(0.8.dp, BioHealingGreen.copy(alpha = 0.6f))
                                ) {
                                    Text(
                                        text = "🩸 Açlık Şekeri İstenecek",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BioHealingGreen,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                            if (reminder.requestPostGlucose) {
                                Surface(
                                    color = PostKpiBg,
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(0.8.dp, AlertPharmacyRuby.copy(alpha = 0.6f))
                                ) {
                                    Text(
                                        text = "⏱️ 2. Saat Tokluk İstenecek",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AlertPharmacyRuby,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // Recommended site chip
                    Surface(
                        color = HealthSurfaceVariant,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, HealthBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "🎯", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Önerilen Rotasyon: ",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = TextHealthSecondary
                            )
                            Text(
                                text = recommendedSite.displayName,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = BioGreenGlow
                            )
                            Text(
                                text = " • ${recommendedSite.absorptionRate.take(16)}...",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextHealthMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Action buttons: "15 Dk Ertele" & "Vuruldum"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onSnooze15(reminder) },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("button_snooze_15"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = AmberGlow
                            ),
                            border = BorderStroke(1.5.dp, AmberGlow)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Alarm,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "15 Dk Ertele",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp
                            )
                        }

                        Button(
                            onClick = { onConfirm(reminder) },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("button_confirm_dose"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BioEmerald,
                                contentColor = HealthBg
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Vuruldum",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp
                            )
                        }
                    }

                    // Customize link
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = { onCustomize(reminder) },
                            modifier = Modifier.testTag("button_customize_reminder_dose")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Dozu / Bölgeyi Özelleştir",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
