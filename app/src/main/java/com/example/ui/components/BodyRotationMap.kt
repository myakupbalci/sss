package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.R
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.RotateRight
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BodyAspect
import com.example.data.model.InjectionSite
import com.example.ui.theme.BioGreenContainer
import com.example.ui.theme.BioGreenGlow
import com.example.ui.theme.BioHealingGreen
import com.example.ui.theme.ChartCanvasBg
import com.example.ui.theme.ChartSlotBg
import com.example.ui.theme.ChartSlotSelectedBg
import com.example.ui.theme.HealthBorder
import com.example.ui.theme.isDayTheme
import com.example.ui.theme.HealthCyanBright
import com.example.ui.theme.HealthMint
import com.example.ui.theme.HealthSurface
import com.example.ui.theme.HealthSurfaceVariant
import com.example.ui.theme.HealthTeal
import com.example.ui.theme.HealthTealBright
import com.example.ui.theme.IconOnAccent
import com.example.ui.theme.TextHealthPrimary
import com.example.ui.theme.TextHealthSecondary
import com.example.ui.theme.WarningAmberContainer
import com.example.ui.theme.WarningAmberHealth

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BodyRotationAssistant(
    lastSite: InjectionSite?,
    recommendedSite: InjectionSite? = null,
    selectedSite: InjectionSite,
    onSiteSelected: (InjectionSite) -> Unit,
    modifier: Modifier = Modifier,
    showDetailedGuide: Boolean = true,
    lang: com.example.util.AppLanguage = com.example.util.AppLanguage.TR
) {
    var currentAspect by remember { mutableStateOf(BodyAspect.ANTERIOR) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("body_rotation_assistant_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = HealthSurface),
        border = BorderStroke(1.5.dp, HealthBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(HealthTeal, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.RotateRight,
                        contentDescription = com.example.util.Strings.rotationAssistantTitle(lang),
                        tint = IconOnAccent,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = com.example.util.Strings.rotationAssistantTitle(lang),
                        fontSize = 19.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextHealthPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = com.example.util.Strings.rotationAssistantSubtitle(lang),
                        fontSize = 13.sp,
                        color = HealthMint,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Advice Banner (Vibrant Healthcare Contrast with larger readable text)
            val adviceText = if (recommendedSite != null) {
                com.example.util.Strings.rotationAdviceTitle(lastSite, recommendedSite, lang)
            } else {
                if (lastSite != null) com.example.util.Strings.lastInjectedSite(lang, com.example.util.Strings.siteDisplayName(lastSite, lang))
                else com.example.util.Strings.noPriorSite(lang)
            }
            val adviceSub = if (recommendedSite != null) {
                com.example.util.Strings.rotationAdviceSubtitle(lang)
            } else {
                if (lastSite != null) com.example.util.Strings.restSiteWarning(lang) else ""
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = if (lastSite != null) BioGreenContainer else HealthSurfaceVariant,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(
                    1.5.dp,
                    if (lastSite != null) BioHealingGreen else HealthCyanBright
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (lastSite != null) Icons.Default.CheckCircle else Icons.Default.Info,
                        contentDescription = null,
                        tint = if (lastSite != null) BioGreenGlow else HealthCyanBright,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = adviceText,
                            fontSize = 14.5.sp,
                            lineHeight = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextHealthPrimary
                        )
                        if (adviceSub.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = adviceSub,
                                fontSize = 12.5.sp,
                                lineHeight = 17.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextHealthSecondary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Anterior / Posterior Segment Switcher (Spacious 2-line tabs so text never cramps)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(HealthSurfaceVariant)
                    .border(1.dp, HealthBorder, RoundedCornerShape(14.dp))
                    .padding(4.dp)
                    .testTag("body_aspect_tabs"),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                AspectSegmentButton(
                    title = if (lang == com.example.util.AppLanguage.EN) "Anterior Body" else "Ön Beden",
                    subtitle = if (lang == com.example.util.AppLanguage.EN) "Abdomen, Thigh, Arm" else "Karın, Baldır, Kol",
                    isSelected = currentAspect == BodyAspect.ANTERIOR,
                    onClick = { currentAspect = BodyAspect.ANTERIOR },
                    modifier = Modifier.weight(1f).testTag("tab_anterior")
                )
                AspectSegmentButton(
                    title = if (lang == com.example.util.AppLanguage.EN) "Posterior Body" else "Arka Beden",
                    subtitle = if (lang == com.example.util.AppLanguage.EN) "Gluteal Zone" else "Kalça Bölgesi",
                    isSelected = currentAspect == BodyAspect.POSTERIOR,
                    onClick = { currentAspect = BodyAspect.POSTERIOR },
                    modifier = Modifier.weight(1f).testTag("tab_posterior")
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Real Anatomical Human Body Silhouette Component (Clean 3-Column Non-Overlapping Layout)
            AnatomicalHumanBodyFigure(
                aspect = currentAspect,
                lastSite = lastSite,
                recommendedSite = recommendedSite,
                selectedSite = selectedSite,
                onSiteSelected = onSiteSelected,
                lang = lang
            )

            if (showDetailedGuide) {
                Spacer(modifier = Modifier.height(16.dp))
                // Selected Site Details with larger, clear clinical text
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = HealthSurfaceVariant,
                    border = BorderStroke(1.2.dp, HealthBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = com.example.util.Strings.selectedSiteLabel(lang, com.example.util.Strings.siteDisplayName(selectedSite, lang)),
                                fontSize = 15.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextHealthPrimary
                            )
                            if (recommendedSite != null && selectedSite == recommendedSite) {
                                Surface(
                                    color = BioHealingGreen,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = com.example.util.Strings.rotationBadge(lang),
                                        color = IconOnAccent,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = com.example.util.Strings.absorptionSpeed(lang, com.example.util.Strings.siteAbsorptionRate(selectedSite, lang)),
                            fontSize = 13.5.sp,
                            lineHeight = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = HealthMint
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = com.example.util.Strings.clinicalRule(lang, com.example.util.Strings.siteRotationTips(selectedSite, lang)),
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextHealthSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AspectSegmentButton(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) HealthTeal else Color.Transparent,
        border = if (isSelected) BorderStroke(1.2.dp, HealthMint) else null
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (isSelected) IconOnAccent else TextHealthPrimary,
                maxLines = 1
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isSelected) IconOnAccent.copy(alpha = 0.88f) else TextHealthSecondary,
                maxLines = 1
            )
        }
    }
}

/**
 * Balanced Anatomical Human Body Figure Stage:
 * - Large, prominent central anatomical silhouette (330.dp height, 1.08x scale)
 * - Balanced left & right callout boxes with generous 80.dp vertical spacing (y = 56.dp, 136.dp, 216.dp)
 * - Clear arrow pointers with arrowheads connecting each box directly to its anatomical zone pin on the body
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AnatomicalHumanBodyFigure(
    aspect: BodyAspect,
    lastSite: InjectionSite?,
    recommendedSite: InjectionSite? = null,
    selectedSite: InjectionSite,
    onSiteSelected: (InjectionSite) -> Unit,
    modifier: Modifier = Modifier,
    lang: com.example.util.AppLanguage = com.example.util.AppLanguage.TR
) {
    val pulseTransition = rememberInfiniteTransition(label = "body_zone_pulse")
    val pulseScale by pulseTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(ChartCanvasBg)
            .border(1.5.dp, HealthBorder, RoundedCornerShape(20.dp))
            .padding(vertical = 14.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(330.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            HealthTeal.copy(alpha = if (isDayTheme) 0.16f else 0.24f),
                            ChartCanvasBg
                        )
                    )
                ),
            contentAlignment = Alignment.TopCenter
        ) {
            // Subtle Full-Width Medical Grid & Ambient Aura so background is 100% unified edge-to-edge
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cx = size.width / 2f
                val cy = size.height / 2f
                drawCircle(
                    color = HealthTealBright.copy(alpha = if (isDayTheme) 0.10f else 0.12f),
                    radius = size.minDimension * 0.44f,
                    center = Offset(cx, cy)
                )
                drawCircle(
                    color = HealthTeal.copy(alpha = if (isDayTheme) 0.18f else 0.22f),
                    radius = size.minDimension * 0.34f,
                    center = Offset(cx, cy),
                    style = Stroke(width = 1.dp.toPx())
                )
            }

            // Large, Prominent Medical Anatomical Human Body Silhouette (100% Transparent Background PNG)
            Image(
                painter = painterResource(
                    id = if (aspect == BodyAspect.ANTERIOR) R.drawable.img_body_anterior_clean else R.drawable.img_body_posterior_clean
                ),
                contentDescription = if (aspect == BodyAspect.ANTERIOR) "Ön Beden Anatomisi" else "Arka Beden Anatomisi",
                modifier = Modifier
                    .fillMaxSize()
                    .scale(1.06f),
                contentScale = ContentScale.Fit
            )

            // Subtle Glowing Navel Safe Zone Overlay for Anterior Aspect
            if (aspect == BodyAspect.ANTERIOR) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val cx = size.width / 2f
                    val navelCenter = Offset(cx, 150.dp.toPx())
                    drawCircle(color = HealthMint, radius = 3.5.dp.toPx(), center = navelCenter)
                    drawCircle(
                        color = HealthTealBright.copy(alpha = 0.5f),
                        radius = 19.dp.toPx(),
                        center = navelCenter,
                        style = Stroke(
                            width = 1.2.dp.toPx(),
                            pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
                        )
                    )
                }
            }

            // Canvas for Arrow Pointers from Callout Boxes to Anatomical Body Pins
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cx = size.width / 2f
                val leftBoxEdge = 78.dp.toPx()
                val rightBoxEdge = size.width - 78.dp.toPx()

                fun siteArrowColor(site: InjectionSite): Color = when {
                    selectedSite == site -> HealthCyanBright
                    recommendedSite == site -> BioHealingGreen
                    lastSite == site -> WarningAmberHealth
                    else -> HealthTealBright.copy(alpha = 0.8f)
                }

                if (aspect == BodyAspect.ANTERIOR) {
                    // Right Arm: Left Top Box (y=26dp..66dp) -> Diagonal arrow to Arm Pin (cx - 48dp, y=110dp)
                    drawCalloutArrow(
                        from = Offset(leftBoxEdge, 54.dp.toPx()),
                        to = Offset(cx - 55.dp.toPx(), 97.dp.toPx()),
                        color = siteArrowColor(InjectionSite.ARM_RIGHT)
                    )

                    // Left Arm: Right Top Box (y=26dp..66dp) -> Diagonal arrow to Arm Pin (cx + 48dp, y=110dp)
                    drawCalloutArrow(
                        from = Offset(rightBoxEdge, 54.dp.toPx()),
                        to = Offset(cx + 55.dp.toPx(), 97.dp.toPx()),
                        color = siteArrowColor(InjectionSite.ARM_LEFT)
                    )

                    // Right Abdomen: Left Middle Box (y=152dp) -> Abdomen Pin (cx - 26dp, y=152dp)
                    drawCalloutArrow(
                        from = Offset(leftBoxEdge, 152.dp.toPx()),
                        to = Offset(cx - 41.dp.toPx(), 152.dp.toPx()),
                        color = siteArrowColor(InjectionSite.ABDOMEN_RIGHT)
                    )

                    // Left Abdomen: Right Middle Box (y=152dp) -> Abdomen Pin (cx + 26dp, y=152dp)
                    drawCalloutArrow(
                        from = Offset(rightBoxEdge, 152.dp.toPx()),
                        to = Offset(cx + 41.dp.toPx(), 152.dp.toPx()),
                        color = siteArrowColor(InjectionSite.ABDOMEN_LEFT)
                    )

                    // Right Thigh: Left Bottom Box (y=242dp) -> Thigh Pin (cx - 23dp, y=196dp)
                    drawCalloutArrow(
                        from = Offset(leftBoxEdge, 240.dp.toPx()),
                        to = Offset(cx - 37.dp.toPx(), 204.dp.toPx()),
                        color = siteArrowColor(InjectionSite.THIGH_RIGHT)
                    )

                    // Left Thigh: Right Bottom Box (y=242dp) -> Thigh Pin (cx + 23dp, y=196dp)
                    drawCalloutArrow(
                        from = Offset(rightBoxEdge, 240.dp.toPx()),
                        to = Offset(cx + 37.dp.toPx(), 204.dp.toPx()),
                        color = siteArrowColor(InjectionSite.THIGH_LEFT)
                    )
                } else {
                    // POSTERIOR
                    // Right Gluteal: Left Box (y=172dp) -> Gluteal Pin (cx - 26dp, y=172dp)
                    drawCalloutArrow(
                        from = Offset(leftBoxEdge, 172.dp.toPx()),
                        to = Offset(cx - 41.dp.toPx(), 172.dp.toPx()),
                        color = siteArrowColor(InjectionSite.GLUTEAL_RIGHT)
                    )

                    // Left Gluteal: Right Box (y=172dp) -> Gluteal Pin (cx + 26dp, y=172dp)
                    drawCalloutArrow(
                        from = Offset(rightBoxEdge, 172.dp.toPx()),
                        to = Offset(cx + 41.dp.toPx(), 172.dp.toPx()),
                        color = siteArrowColor(InjectionSite.GLUTEAL_LEFT)
                    )
                }
            }

            // Circular Target Pins on the Character Figure
            if (aspect == BodyAspect.ANTERIOR) {
                // Right Arm Pin (center at cx - 48dp, y = 110dp)
                AnatomicalBodyPin(
                    site = InjectionSite.ARM_RIGHT,
                    isLast = lastSite == InjectionSite.ARM_RIGHT,
                    isRecommended = recommendedSite == InjectionSite.ARM_RIGHT,
                    isSelected = selectedSite == InjectionSite.ARM_RIGHT,
                    pulseScale = pulseScale,
                    onClick = { onSiteSelected(InjectionSite.ARM_RIGHT) },
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(x = (-48).dp, y = 97.dp)
                )

                // Left Arm Pin (center at cx + 48dp, y = 110dp)
                AnatomicalBodyPin(
                    site = InjectionSite.ARM_LEFT,
                    isLast = lastSite == InjectionSite.ARM_LEFT,
                    isRecommended = recommendedSite == InjectionSite.ARM_LEFT,
                    isSelected = selectedSite == InjectionSite.ARM_LEFT,
                    pulseScale = pulseScale,
                    onClick = { onSiteSelected(InjectionSite.ARM_LEFT) },
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(x = 48.dp, y = 97.dp)
                )

                // Right Abdomen Pin (center at cx - 26dp, y = 152dp)
                AnatomicalBodyPin(
                    site = InjectionSite.ABDOMEN_RIGHT,
                    isLast = lastSite == InjectionSite.ABDOMEN_RIGHT,
                    isRecommended = recommendedSite == InjectionSite.ABDOMEN_RIGHT,
                    isSelected = selectedSite == InjectionSite.ABDOMEN_RIGHT,
                    pulseScale = pulseScale,
                    onClick = { onSiteSelected(InjectionSite.ABDOMEN_RIGHT) },
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(x = (-26).dp, y = 139.dp)
                )

                // Left Abdomen Pin (center at cx + 26dp, y = 152dp)
                AnatomicalBodyPin(
                    site = InjectionSite.ABDOMEN_LEFT,
                    isLast = lastSite == InjectionSite.ABDOMEN_LEFT,
                    isRecommended = recommendedSite == InjectionSite.ABDOMEN_LEFT,
                    isSelected = selectedSite == InjectionSite.ABDOMEN_LEFT,
                    pulseScale = pulseScale,
                    onClick = { onSiteSelected(InjectionSite.ABDOMEN_LEFT) },
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(x = 26.dp, y = 139.dp)
                )

                // Right Thigh Pin (center at cx - 23dp, y = 196dp)
                AnatomicalBodyPin(
                    site = InjectionSite.THIGH_RIGHT,
                    isLast = lastSite == InjectionSite.THIGH_RIGHT,
                    isRecommended = recommendedSite == InjectionSite.THIGH_RIGHT,
                    isSelected = selectedSite == InjectionSite.THIGH_RIGHT,
                    pulseScale = pulseScale,
                    onClick = { onSiteSelected(InjectionSite.THIGH_RIGHT) },
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(x = (-23).dp, y = 183.dp)
                )

                // Left Thigh Pin (center at cx + 23dp, y = 196dp)
                AnatomicalBodyPin(
                    site = InjectionSite.THIGH_LEFT,
                    isLast = lastSite == InjectionSite.THIGH_LEFT,
                    isRecommended = recommendedSite == InjectionSite.THIGH_LEFT,
                    isSelected = selectedSite == InjectionSite.THIGH_LEFT,
                    pulseScale = pulseScale,
                    onClick = { onSiteSelected(InjectionSite.THIGH_LEFT) },
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(x = 23.dp, y = 183.dp)
                )

                // Balanced Left Callout Boxes (y = 26.dp, 132.dp, 224.dp)
                AnatomicalCalloutLabel(
                    site = InjectionSite.ARM_RIGHT,
                    label = com.example.util.Strings.calloutRightArm(lang),
                    isLast = lastSite == InjectionSite.ARM_RIGHT,
                    isRecommended = recommendedSite != null && recommendedSite == InjectionSite.ARM_RIGHT,
                    isSelected = selectedSite == InjectionSite.ARM_RIGHT,
                    onClick = { onSiteSelected(InjectionSite.ARM_RIGHT) },
                    lang = lang,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset(x = 0.dp, y = 26.dp)
                        .width(76.dp)
                )

                AnatomicalCalloutLabel(
                    site = InjectionSite.ABDOMEN_RIGHT,
                    label = com.example.util.Strings.calloutRightAbdomen(lang),
                    isLast = lastSite == InjectionSite.ABDOMEN_RIGHT,
                    isRecommended = recommendedSite != null && recommendedSite == InjectionSite.ABDOMEN_RIGHT,
                    isSelected = selectedSite == InjectionSite.ABDOMEN_RIGHT,
                    onClick = { onSiteSelected(InjectionSite.ABDOMEN_RIGHT) },
                    lang = lang,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset(x = 0.dp, y = 132.dp)
                        .width(76.dp)
                )

                AnatomicalCalloutLabel(
                    site = InjectionSite.THIGH_RIGHT,
                    label = if (lang == com.example.util.AppLanguage.EN) "Right Thigh" else "Sağ Baldır",
                    isLast = lastSite == InjectionSite.THIGH_RIGHT,
                    isRecommended = recommendedSite != null && recommendedSite == InjectionSite.THIGH_RIGHT,
                    isSelected = selectedSite == InjectionSite.THIGH_RIGHT,
                    onClick = { onSiteSelected(InjectionSite.THIGH_RIGHT) },
                    lang = lang,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset(x = 0.dp, y = 224.dp)
                        .width(76.dp)
                )

                // Balanced Right Callout Boxes (y = 26.dp, 132.dp, 224.dp)
                AnatomicalCalloutLabel(
                    site = InjectionSite.ARM_LEFT,
                    label = com.example.util.Strings.calloutLeftArm(lang),
                    isLast = lastSite == InjectionSite.ARM_LEFT,
                    isRecommended = recommendedSite != null && recommendedSite == InjectionSite.ARM_LEFT,
                    isSelected = selectedSite == InjectionSite.ARM_LEFT,
                    onClick = { onSiteSelected(InjectionSite.ARM_LEFT) },
                    lang = lang,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 0.dp, y = 26.dp)
                        .width(76.dp)
                )

                AnatomicalCalloutLabel(
                    site = InjectionSite.ABDOMEN_LEFT,
                    label = com.example.util.Strings.calloutLeftAbdomen(lang),
                    isLast = lastSite == InjectionSite.ABDOMEN_LEFT,
                    isRecommended = recommendedSite != null && recommendedSite == InjectionSite.ABDOMEN_LEFT,
                    isSelected = selectedSite == InjectionSite.ABDOMEN_LEFT,
                    onClick = { onSiteSelected(InjectionSite.ABDOMEN_LEFT) },
                    lang = lang,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 0.dp, y = 132.dp)
                        .width(76.dp)
                )

                AnatomicalCalloutLabel(
                    site = InjectionSite.THIGH_LEFT,
                    label = if (lang == com.example.util.AppLanguage.EN) "Left Thigh" else "Sol Baldır",
                    isLast = lastSite == InjectionSite.THIGH_LEFT,
                    isRecommended = recommendedSite != null && recommendedSite == InjectionSite.THIGH_LEFT,
                    isSelected = selectedSite == InjectionSite.THIGH_LEFT,
                    onClick = { onSiteSelected(InjectionSite.THIGH_LEFT) },
                    lang = lang,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 0.dp, y = 224.dp)
                        .width(76.dp)
                )
            } else {
                // POSTERIOR
                // Right Gluteal Pin (center at cx - 26dp, y = 172dp)
                AnatomicalBodyPin(
                    site = InjectionSite.GLUTEAL_RIGHT,
                    isLast = lastSite == InjectionSite.GLUTEAL_RIGHT,
                    isRecommended = recommendedSite != null && recommendedSite == InjectionSite.GLUTEAL_RIGHT,
                    isSelected = selectedSite == InjectionSite.GLUTEAL_RIGHT,
                    pulseScale = pulseScale,
                    onClick = { onSiteSelected(InjectionSite.GLUTEAL_RIGHT) },
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(x = (-26).dp, y = 159.dp)
                )

                // Left Gluteal Pin (center at cx + 26dp, y = 172dp)
                AnatomicalBodyPin(
                    site = InjectionSite.GLUTEAL_LEFT,
                    isLast = lastSite == InjectionSite.GLUTEAL_LEFT,
                    isRecommended = recommendedSite != null && recommendedSite == InjectionSite.GLUTEAL_LEFT,
                    isSelected = selectedSite == InjectionSite.GLUTEAL_LEFT,
                    pulseScale = pulseScale,
                    onClick = { onSiteSelected(InjectionSite.GLUTEAL_LEFT) },
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(x = 26.dp, y = 159.dp)
                )

                // Left Callout
                AnatomicalCalloutLabel(
                    site = InjectionSite.GLUTEAL_RIGHT,
                    label = com.example.util.Strings.calloutRightGluteal(lang),
                    isLast = lastSite == InjectionSite.GLUTEAL_RIGHT,
                    isRecommended = recommendedSite != null && recommendedSite == InjectionSite.GLUTEAL_RIGHT,
                    isSelected = selectedSite == InjectionSite.GLUTEAL_RIGHT,
                    onClick = { onSiteSelected(InjectionSite.GLUTEAL_RIGHT) },
                    lang = lang,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset(x = 0.dp, y = 152.dp)
                        .width(76.dp)
                )

                // Right Callout
                AnatomicalCalloutLabel(
                    site = InjectionSite.GLUTEAL_LEFT,
                    label = com.example.util.Strings.calloutLeftGluteal(lang),
                    isLast = lastSite == InjectionSite.GLUTEAL_LEFT,
                    isRecommended = recommendedSite != null && recommendedSite == InjectionSite.GLUTEAL_LEFT,
                    isSelected = selectedSite == InjectionSite.GLUTEAL_LEFT,
                    onClick = { onSiteSelected(InjectionSite.GLUTEAL_LEFT) },
                    lang = lang,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 0.dp, y = 152.dp)
                        .width(76.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Health Legend Pill row (Readable 12.sp font)
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (recommendedSite != null) {
                HealthLegendPill(color = BioHealingGreen, label = com.example.util.Strings.legendRecommended(lang))
            }
            HealthLegendPill(color = WarningAmberHealth, label = com.example.util.Strings.legendLast(lang))
            HealthLegendPill(color = HealthCyanBright, label = com.example.util.Strings.legendSelected(lang))
        }
    }
}

/**
 * Draws a medical pointer line with an origin dot on the box edge and a sharp arrowhead pointing at the body pin.
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawCalloutArrow(
    from: Offset,
    to: Offset,
    color: Color
) {
    // Origin dot on the callout box edge
    drawCircle(
        color = color,
        radius = 2.8.dp.toPx(),
        center = from
    )

    // Pointer shaft
    drawLine(
        color = color,
        start = from,
        end = to,
        strokeWidth = 1.8.dp.toPx()
    )

    // Filled triangular arrowhead at `to`
    val dx = to.x - from.x
    val dy = to.y - from.y
    val len = kotlin.math.sqrt(dx * dx + dy * dy).coerceAtLeast(1f)
    val ux = dx / len
    val uy = dy / len
    val px = -uy
    val py = ux

    val headLength = 7.dp.toPx()
    val headHalfWidth = 4.2.dp.toPx()

    val baseCenter = Offset(to.x - ux * headLength, to.y - uy * headLength)
    val leftWing = Offset(baseCenter.x + px * headHalfWidth, baseCenter.y + py * headHalfWidth)
    val rightWing = Offset(baseCenter.x - px * headHalfWidth, baseCenter.y - py * headHalfWidth)

    val arrowPath = Path().apply {
        moveTo(to.x, to.y)
        lineTo(leftWing.x, leftWing.y)
        lineTo(rightWing.x, rightWing.y)
        close()
    }
    drawPath(path = arrowPath, color = color)
}

/**
 * An anatomical circular hotspot pin placed directly on the human body contour.
 */
@Composable
private fun AnatomicalBodyPin(
    site: InjectionSite,
    isLast: Boolean,
    isRecommended: Boolean,
    isSelected: Boolean,
    pulseScale: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pinColor = when {
        isSelected -> HealthCyanBright
        isRecommended -> BioHealingGreen
        isLast -> WarningAmberHealth
        else -> HealthTeal
    }

    val scaleModifier = if (isRecommended) Modifier.scale(pulseScale) else Modifier

    Box(
        modifier = modifier
            .then(scaleModifier)
            .size(26.dp)
            .clip(CircleShape)
            .background(HealthSurface)
            .border(
                width = if (isRecommended || isSelected || isLast) 2.5.dp else 1.5.dp,
                color = pinColor,
                shape = CircleShape
            )
            .clickable(onClick = onClick)
            .testTag("pin_${site.id}"),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(pinColor, CircleShape)
        )
    }
}

/**
 * Balanced, readable callout box positioned on the left/right margins with arrow pointer to the body zone.
 */
@Composable
private fun AnatomicalCalloutLabel(
    site: InjectionSite,
    label: String,
    isLast: Boolean,
    isRecommended: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    lang: com.example.util.AppLanguage = com.example.util.AppLanguage.TR
) {
    val borderColor = when {
        isSelected -> HealthCyanBright
        isRecommended -> BioHealingGreen
        isLast -> WarningAmberHealth
        else -> HealthBorder
    }

    val bgColor = when {
        isSelected -> ChartSlotSelectedBg
        isRecommended -> BioGreenContainer
        isLast -> WarningAmberContainer
        else -> ChartSlotBg
    }

    val textColor = when {
        isSelected -> HealthCyanBright
        isRecommended -> BioGreenGlow
        isLast -> WarningAmberHealth
        else -> TextHealthPrimary
    }

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(11.dp),
        color = bgColor,
        border = BorderStroke(if (isSelected || isRecommended || isLast) 1.6.dp else 1.dp, borderColor),
        modifier = modifier
            .testTag("label_${site.id}")
            .testTag("chip_${site.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 7.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = textColor,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
            if (isRecommended) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (lang == com.example.util.AppLanguage.EN) "★ Rec" else "★ Önerilen",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = BioGreenGlow,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            } else if (isLast) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (lang == com.example.util.AppLanguage.EN) "● Last" else "● Son",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = WarningAmberHealth,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            } else if (isSelected) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (lang == com.example.util.AppLanguage.EN) "✓ Selected" else "✓ Seçili",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = HealthCyanBright,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun HealthLegendPill(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = TextHealthPrimary
        )
    }
}
