package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DoseType
import com.example.data.model.InsulinStock
import com.example.ui.theme.*
import kotlin.math.sin

/**
 * Compact, minimalist, vertical, side-by-side animated insulin stock widget.
 * Optimized for screen space efficiency with realistic miniature vertical pens,
 * animated fluid waves, micro-bubbles, graduation tick marks, and clean typography.
 */
@Composable
fun AnimatedInsulinStockWidget(
    stocks: List<InsulinStock>,
    onAddPen: (DoseType) -> Unit,
    onEditStock: (InsulinStock) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("animated_stock_widget"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = HealthSurface),
        border = BorderStroke(1.2.dp, HealthBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            // Header Row: Visual Stock Status Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .background(HealthTeal, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MedicalServices,
                            contentDescription = "İnsülin Kalem Stoğu",
                            tint = TextHealthPrimary,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Kalan İnsülin Seviyesi",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextHealthPrimary
                        )
                        Text(
                            text = "Canlı Kalem & Sıvı Rezervi",
                            fontSize = 10.sp,
                            color = HealthMint,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Global low stock banner if any stock is low
                val anyLow = stocks.any { it.isLowStock }
                if (anyLow) {
                    Surface(
                        color = AlertPharmacyRuby,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.testTag("global_low_stock_tag")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalPharmacy,
                                contentDescription = null,
                                tint = TextHealthPrimary,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Eczane Zamanı!",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = TextHealthPrimary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Side-by-Side Dual Vertical Minimalist Insulin Pens (Optimum Screen Space Layout)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                stocks.forEach { stock ->
                    VerticalMinimalistInsulinPenCard(
                        stock = stock,
                        onAddPen = { onAddPen(stock.type) },
                        onEdit = { onEditStock(stock) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

/**
 * Compact minimalist vertical insulin pen card designed for side-by-side display.
 * Includes miniature vertical animated needle & cartridge with moving fluid waves,
 * bubbles, and dosage indicators with an optimized height profile.
 */
@Composable
private fun VerticalMinimalistInsulinPenCard(
    stock: InsulinStock,
    onAddPen: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isBasal = stock.type == DoseType.BASAL
    val primaryColor = if (isBasal) BasalCyan else BolusGold
    val penAccentColor = if (isBasal) Color(0xFF0284C7) else Color(0xFFD97706)
    val fillPercent = (stock.totalUnitsRemaining / 600.0).coerceIn(0.08, 1.0).toFloat()

    // Fluid wave and bubble animations inside vertical cartridge
    val infiniteTransition = rememberInfiniteTransition(label = "vertical_pen_anim")
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "vert_pen_wave"
    )
    val bubbleOffset by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "vert_pen_bubble"
    )

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (stock.isLowStock) AlertRubyContainer.copy(alpha = 0.5f) else HealthSurfaceVariant)
            .border(
                width = 1.dp,
                color = if (stock.isLowStock) AlertPharmacyRuby else primaryColor.copy(alpha = 0.4f),
                shape = RoundedCornerShape(14.dp)
            )
            .padding(horizontal = 8.dp, vertical = 7.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Pen Header (Type Badge + Edit)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = if (stock.isLowStock) AlertPharmacyRuby else primaryColor.copy(alpha = 0.22f),
                shape = RoundedCornerShape(5.dp)
            ) {
                Text(
                    text = if (isBasal) "BAZAL" else "BOLUS",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (stock.isLowStock) TextHealthPrimary else primaryColor,
                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp)
                )
            }

            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(HealthSurface)
                    .clickable(onClick = onEdit),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Düzenle",
                    tint = HealthMint,
                    modifier = Modifier.size(11.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(3.dp))

        // Brand Name (Single line with ellipsis)
        Text(
            text = stock.brandName,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextHealthPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(5.dp))

        // COMPACT VERTICAL MINIMALIST INSULIN PEN CANVAS (70dp height)
        Box(
            modifier = Modifier
                .width(22.dp)
                .height(70.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(Color(0xFF03141B))
                .border(0.8.dp, HealthBorder.copy(alpha = 0.7f), RoundedCornerShape(5.dp)),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Vertical Layout Heights:
                // 1. Top Needle & Hub: 0 to h * 0.16
                val needleTipHeight = h * 0.08f
                val hubHeight = h * 0.08f
                val hubBottom = needleTipHeight + hubHeight

                // 2. Glass Cartridge Window: hubBottom to h * 0.72
                val cartridgeTop = hubBottom
                val cartridgeBottom = h * 0.72f
                val cartridgeHeight = cartridgeBottom - cartridgeTop

                // 3. Pen Plastic Barrel Body: h * 0.72 to h * 0.88
                val barrelTop = cartridgeBottom
                val barrelBottom = h * 0.88f

                // 4. Dose Selector Dial Base & Button: h * 0.88 to h
                val dialTop = barrelBottom

                // -------------------------------------------------------------
                // 1. NEEDLE & HUB (Top)
                // -------------------------------------------------------------
                // Fine steel cannula line
                drawLine(
                    color = Color(0xFFCBD5E1),
                    start = Offset(w / 2f, 1.dp.toPx()),
                    end = Offset(w / 2f, needleTipHeight),
                    strokeWidth = 1.dp.toPx()
                )

                // Needle protective hub (trapezoid cone)
                val hubPath = Path().apply {
                    moveTo(w * 0.38f, needleTipHeight)
                    lineTo(w * 0.62f, needleTipHeight)
                    lineTo(w * 0.82f, hubBottom)
                    lineTo(w * 0.18f, hubBottom)
                    close()
                }
                drawPath(hubPath, color = Color(0xFF0F3A4C))
                drawPath(hubPath, color = HealthTealBright.copy(alpha = 0.6f), style = Stroke(0.8.dp.toPx()))

                // -------------------------------------------------------------
                // 2. GLASS CARTRIDGE RESERVOIR (Middle)
                // -------------------------------------------------------------
                val cartridgeInset = w * 0.12f
                val cartridgeWidth = w - (cartridgeInset * 2)
                val cartridgeRect = Size(cartridgeWidth, cartridgeHeight)
                val cartridgeTopLeft = Offset(cartridgeInset, cartridgeTop)

                // Dark transparent glass background
                drawRoundRect(
                    color = Color(0xFF06222E),
                    topLeft = cartridgeTopLeft,
                    size = cartridgeRect,
                    cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                )
                // Glass border
                drawRoundRect(
                    color = primaryColor.copy(alpha = 0.35f),
                    topLeft = cartridgeTopLeft,
                    size = cartridgeRect,
                    cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()),
                    style = Stroke(0.8.dp.toPx())
                )

                // LIQUID FLUID (Filled from bottom of cartridge upward)
                val activeLiquidHeight = cartridgeHeight * fillPercent
                val liquidTopY = cartridgeBottom - activeLiquidHeight

                val liquidBrush = Brush.verticalGradient(
                    colors = if (stock.isLowStock) {
                        listOf(AlertPharmacyRuby, Color(0xFF9F1239))
                    } else if (isBasal) {
                        listOf(HealthCyanBright, Color(0xFF0284C7))
                    } else {
                        listOf(BolusGold, Color(0xFFD97706))
                    },
                    startY = liquidTopY,
                    endY = cartridgeBottom
                )

                // Animated wave surface across the liquid meniscus
                val liquidPath = Path().apply {
                    val waveAmp = 0.9.dp.toPx()
                    val startX = cartridgeInset + 0.8.dp.toPx()
                    val endX = cartridgeInset + cartridgeWidth - 0.8.dp.toPx()
                    moveTo(startX, liquidTopY)

                    for (x in 0..cartridgeWidth.toInt() step 2) {
                        val currX = cartridgeInset + x
                        val angle = (x / cartridgeWidth) * 2f * Math.PI.toFloat() + wavePhase
                        val currY = liquidTopY + (sin(angle) * waveAmp)
                        lineTo(currX, currY)
                    }

                    lineTo(endX, cartridgeBottom)
                    lineTo(startX, cartridgeBottom)
                    close()
                }
                drawPath(liquidPath, brush = liquidBrush)

                // Rubber Plunger Stopper inside cartridge sitting at the fluid boundary
                if (liquidTopY > cartridgeTop + 3.dp.toPx()) {
                    drawRoundRect(
                        color = Color(0xFF334155),
                        topLeft = Offset(cartridgeInset + 0.8.dp.toPx(), liquidTopY - 2.dp.toPx()),
                        size = Size(cartridgeWidth - 1.6.dp.toPx(), 2.5.dp.toPx()),
                        cornerRadius = CornerRadius(1.dp.toPx(), 1.dp.toPx())
                    )
                }

                // Rising micro bubbles in the moving fluid
                val bubbleY1 = liquidTopY + (activeLiquidHeight * bubbleOffset)
                val bubbleX1 = cartridgeInset + (cartridgeWidth * 0.4f)
                if (bubbleY1 < cartridgeBottom - 1.5.dp.toPx()) {
                    drawCircle(Color.White.copy(alpha = 0.6f), radius = 0.8.dp.toPx(), center = Offset(bubbleX1, bubbleY1))
                }
                val bubbleY2 = liquidTopY + (activeLiquidHeight * ((bubbleOffset + 0.45f) % 1f))
                val bubbleX2 = cartridgeInset + (cartridgeWidth * 0.7f)
                if (bubbleY2 < cartridgeBottom - 1.5.dp.toPx()) {
                    drawCircle(Color.White.copy(alpha = 0.45f), radius = 0.7.dp.toPx(), center = Offset(bubbleX2, bubbleY2))
                }

                // Measurement tick marks (horizontal graduation lines on right side of glass)
                val tickCount = 4
                val stepY = cartridgeHeight / tickCount
                for (i in 1 until tickCount) {
                    val ty = cartridgeTop + (i * stepY)
                    val tickLen = 2.5.dp.toPx()
                    drawLine(
                        color = Color.White.copy(alpha = 0.35f),
                        start = Offset(cartridgeInset + cartridgeWidth - tickLen, ty),
                        end = Offset(cartridgeInset + cartridgeWidth, ty),
                        strokeWidth = 0.7.dp.toPx()
                    )
                }

                // Glass specular shine line along the left edge
                drawLine(
                    color = Color.White.copy(alpha = 0.22f),
                    start = Offset(cartridgeInset + 1.2.dp.toPx(), cartridgeTop + 1.5.dp.toPx()),
                    end = Offset(cartridgeInset + 1.2.dp.toPx(), cartridgeBottom - 1.5.dp.toPx()),
                    strokeWidth = 0.8.dp.toPx()
                )

                // -------------------------------------------------------------
                // 3. PEN PLASTIC BARREL (Lower Section)
                // -------------------------------------------------------------
                val barrelRect = Size(w * 0.84f, barrelBottom - barrelTop)
                val barrelTopLeft = Offset(w * 0.08f, barrelTop)
                drawRoundRect(
                    color = penAccentColor,
                    topLeft = barrelTopLeft,
                    size = barrelRect,
                    cornerRadius = CornerRadius(1.5.dp.toPx(), 1.5.dp.toPx())
                )

                // Grip texture grooves on barrel
                for (k in 1..2) {
                    val gy = barrelTop + (k * (barrelBottom - barrelTop) / 3f)
                    drawLine(
                        color = Color(0xFF0F172A).copy(alpha = 0.45f),
                        start = Offset(w * 0.15f, gy),
                        end = Offset(w * 0.85f, gy),
                        strokeWidth = 0.8.dp.toPx()
                    )
                }

                // -------------------------------------------------------------
                // 4. MECHANICAL DOSE SELECTOR DIAL (Bottom Base)
                // -------------------------------------------------------------
                val dialRect = Size(w * 0.70f, h - dialTop - 2.5.dp.toPx())
                val dialTopLeft = Offset(w * 0.15f, dialTop)
                drawRoundRect(
                    color = Color(0xFF1E293B),
                    topLeft = dialTopLeft,
                    size = dialRect,
                    cornerRadius = CornerRadius(1.5.dp.toPx(), 1.5.dp.toPx())
                )

                // Push button cap at the bottom end
                drawRoundRect(
                    color = penAccentColor,
                    topLeft = Offset(w * 0.25f, h - 2.5.dp.toPx()),
                    size = Size(w * 0.50f, 2.5.dp.toPx()),
                    cornerRadius = CornerRadius(1.dp.toPx(), 1.dp.toPx())
                )
            }
        }

        Spacer(modifier = Modifier.height(5.dp))

        // Remaining Dose IU and Pens
        Text(
            text = "%.0f IU".format(stock.totalUnitsRemaining),
            fontSize = 13.sp,
            fontWeight = FontWeight.Black,
            color = if (stock.isLowStock) AlertPharmacyRuby else primaryColor
        )
        Text(
            text = "(~%.1f Kalem)".format(stock.pensRemaining),
            fontSize = 9.sp,
            fontWeight = FontWeight.SemiBold,
            color = HealthMint
        )

        // Low stock warning badge
        if (stock.isLowStock) {
            Spacer(modifier = Modifier.height(2.dp))
            Surface(
                color = AlertPharmacyRuby,
                shape = RoundedCornerShape(3.dp)
            ) {
                Text(
                    text = "Stok Az!",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Black,
                    color = TextHealthPrimary,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Compact Quick Refill +1 Pen button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(HealthSurface)
                .border(0.9.dp, HealthBorder, RoundedCornerShape(6.dp))
                .clickable(onClick = onAddPen)
                .padding(vertical = 3.5.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = primaryColor,
                modifier = Modifier.size(10.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = "+1 Kalem",
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold,
                color = primaryColor
            )
        }
    }
}
