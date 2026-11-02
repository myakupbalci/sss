package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DoseRecord
import com.example.data.model.InjectionSite
import com.example.ui.components.BodyRotationAssistant
import com.example.ui.theme.EmeraldSecondary
import com.example.ui.theme.SkyPrimary
import com.example.ui.theme.SkyPrimaryContainer
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningAmber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RotationScreen(
    lastDose: DoseRecord?,
    recommendedSite: InjectionSite,
    allDoses: List<DoseRecord>,
    modifier: Modifier = Modifier,
    lang: com.example.util.AppLanguage = com.example.util.AppLanguage.TR
) {
    var selectedSite by remember { mutableStateOf(recommendedSite) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(com.example.ui.theme.HealthTeal, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.RotateRight,
                        contentDescription = null,
                        tint = com.example.ui.theme.IconOnAccent,
                        modifier = Modifier.size(25.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = if (lang == com.example.util.AppLanguage.EN) "Visual Rotation Assistant" else "Görsel Rotasyon Asistanı",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = com.example.ui.theme.TextHealthPrimary
                    )
                    Text(
                        text = if (lang == com.example.util.AppLanguage.EN) "Injection sites, absorption kinetics, and tissue health guide" else "Enjeksiyon bölgeleri, emilim hızları ve doku sağlığı rehberi",
                        style = MaterialTheme.typography.bodySmall,
                        color = com.example.ui.theme.HealthMint,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Full Body Rotation Component
        item {
            BodyRotationAssistant(
                lastSite = lastDose?.injectionSite,
                recommendedSite = recommendedSite,
                selectedSite = selectedSite,
                onSiteSelected = { site -> selectedSite = site },
                showDetailedGuide = true,
                lang = lang
            )
        }

        // Injection Site Statistics & Usage
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("card_site_statistics"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = com.example.ui.theme.HealthSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = BorderStroke(1.5.dp, com.example.ui.theme.HealthBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .background(com.example.ui.theme.HealthTeal, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = com.example.ui.theme.IconOnAccent,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (lang == com.example.util.AppLanguage.EN) "Site-Based Injection History" else "Bölge Bazlı Enjeksiyon Geçmişi",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = com.example.ui.theme.TextHealthPrimary
                            )
                            Text(
                                text = if (lang == com.example.util.AppLanguage.EN) "When was each site last used?" else "Hangi bölgeye en son ne zaman vuruldu?",
                                style = MaterialTheme.typography.bodySmall,
                                color = com.example.ui.theme.TextHealthSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    InjectionSite.values().forEach { site ->
                        val siteDoses = allDoses.filter { it.injectionSite == site }
                        val lastUsedDose = siteDoses.maxByOrNull { it.timestamp }
                        val isLastUsedOverall = lastDose?.injectionSite == site
                        val isRecommendedOverall = recommendedSite == site

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(10.dp),
                            color = when {
                                isRecommendedOverall -> com.example.ui.theme.BioGreenContainer
                                isLastUsedOverall -> com.example.ui.theme.WarningAmberContainer
                                else -> com.example.ui.theme.HealthSurfaceVariant
                            },
                            border = BorderStroke(
                                1.2.dp,
                                when {
                                    isRecommendedOverall -> com.example.ui.theme.BioHealingGreen
                                    isLastUsedOverall -> com.example.ui.theme.WarningAmberHealth
                                    else -> com.example.ui.theme.HealthBorder
                                }
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = com.example.util.Strings.siteDisplayName(site, lang),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = com.example.ui.theme.TextHealthPrimary
                                        )
                                        if (isRecommendedOverall) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = if (lang == com.example.util.AppLanguage.EN) "★ Recommended" else "★ Önerilen",
                                                color = com.example.ui.theme.BioHealingGreen,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                        } else if (isLastUsedOverall) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = if (lang == com.example.util.AppLanguage.EN) "● Last Used" else "● En Son",
                                                color = com.example.ui.theme.WarningAmberHealth,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                        }
                                    }
                                    Text(
                                        text = com.example.util.Strings.siteAbsorptionRate(site, lang),
                                        fontSize = 11.sp,
                                        color = com.example.ui.theme.TextHealthSecondary
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    if (lastUsedDose != null) {
                                        val timeStr = SimpleDateFormat(
                                            if (lang == com.example.util.AppLanguage.EN) "MMM d, HH:mm" else "d MMM, HH:mm",
                                            if (lang == com.example.util.AppLanguage.EN) Locale("en") else Locale("tr")
                                        ).format(Date(lastUsedDose.timestamp))
                                        Text(
                                            text = timeStr,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = com.example.ui.theme.TextHealthPrimary
                                        )
                                        Text(
                                            text = if (lang == com.example.util.AppLanguage.EN) "${siteDoses.size} injections" else "${siteDoses.size} kez vuruldu",
                                            fontSize = 10.sp,
                                            color = com.example.ui.theme.TextHealthSecondary
                                        )
                                    } else {
                                        Text(
                                            text = if (lang == com.example.util.AppLanguage.EN) "Not used yet" else "Henüz kullanılmadı",
                                            fontSize = 11.sp,
                                            color = com.example.ui.theme.TextHealthMuted
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Clinical Guide on Lipohypertrophy
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("card_lipohypertrophy_guide"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = com.example.ui.theme.BioGreenContainer),
                border = BorderStroke(1.2.dp, com.example.ui.theme.BioHealingGreen)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.HealthAndSafety,
                            contentDescription = null,
                            tint = com.example.ui.theme.BioGreenGlow,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (lang == com.example.util.AppLanguage.EN) "Lipohypertrophy & Site Rotation Guide" else "Lipohipertrofi ve Rotasyon Rehberi",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = com.example.ui.theme.BioGreenGlow
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = if (lang == com.example.util.AppLanguage.EN)
                            "• What is Lipohypertrophy?\nRepeated injections into the same area cause fatty lumps and tissue hardening under the skin."
                        else
                            "• Lipohipertrofi Nedir?\nAynı vücut bölgesine sürekli insülin yapıldığında deri altında yağ dokusu kalınlaşması ve sert yumrular oluşur.",
                        style = MaterialTheme.typography.bodySmall,
                        color = com.example.ui.theme.TextHealthPrimary
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (lang == com.example.util.AppLanguage.EN)
                            "• Why is it Dangerous?\nHardened tissue impairs predictable insulin absorption, causing unexplained high glucose (hyperglycemia) followed by sudden hypoglycemic crashes."
                        else
                            "• Neden Tehlikelidir?\nSertleşmiş dokuya yapılan insülin düzensiz emilir. Bu durum beklenmedik yüksek şekerlere (hiperglisemi) ve sonrasında ani hipoglisemi ataklarına yol açar.",
                        style = MaterialTheme.typography.bodySmall,
                        color = com.example.ui.theme.TextHealthPrimary
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (lang == com.example.util.AppLanguage.EN)
                            "• Proper Rotation Technique:\nKeep at least 1–2 cm (one finger width) between consecutive injection points. Alternate right and left abdomen evenly. Use thighs or buttocks for evening basal insulin."
                        else
                            "• Doğru Rotasyon Kuralı:\nHer enjeksiyon arasında en az 1-2 cm (1 parmak) mesafe bırakın. Karın bölgesinde sağ ve sol tarafları sırayla kullanın. Gece bazalını bacak veya kalçaya yapmayı tercih edin.",
                        style = MaterialTheme.typography.bodySmall,
                        color = com.example.ui.theme.TextHealthSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
