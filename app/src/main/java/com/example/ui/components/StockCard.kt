package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.example.data.model.DoseType
import com.example.data.model.InsulinStock
import com.example.ui.theme.*

@Composable
fun StockOverviewWidget(
    stocks: List<InsulinStock>,
    onAddPen: (DoseType) -> Unit,
    onEditStock: (InsulinStock) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("stock_overview_widget"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = HealthSurface),
        border = BorderStroke(1.5.dp, HealthBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(HealthTeal, RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Inventory2,
                            contentDescription = "Kalem ve Stok",
                            tint = TextHealthPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Kalem & Doz Stoğu",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextHealthPrimary,
                            letterSpacing = (-0.3).sp
                        )
                        Text(
                            text = "Kalan İnsülin ve Eczane Tedarik Durumu",
                            style = MaterialTheme.typography.bodySmall,
                            color = HealthMint,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Stock Items
            stocks.forEach { stock ->
                StockItemRow(
                    stock = stock,
                    onAddPen = { onAddPen(stock.type) },
                    onEditStock = { onEditStock(stock) }
                )
                Spacer(modifier = Modifier.height(14.dp))
            }
        }
    }
}

@Composable
fun StockItemRow(
    stock: InsulinStock,
    onAddPen: () -> Unit,
    onEditStock: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isBasal = stock.type == DoseType.BASAL
    val themeColor = if (isBasal) BasalIndigo else BolusSunset
    val capacity = (stock.unitsPerPen * 2).toDouble()
    val progress = (stock.totalUnitsRemaining / capacity).coerceIn(0.0, 1.0).toFloat()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(if (stock.isLowStock) AlertRubyContainer else HealthSurfaceVariant)
            .border(
                1.5.dp,
                if (stock.isLowStock) AlertPharmacyRuby else HealthBorder,
                RoundedCornerShape(16.dp)
            )
            .padding(14.dp)
    ) {
        // Stock Low Alert Banner
        AnimatedVisibility(visible = stock.isLowStock) {
            Surface(
                color = AlertPharmacyRuby,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalPharmacy,
                        contentDescription = "Uyarı",
                        tint = TextHealthPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "⚠️ Eczaneden insülin alma zamanı!",
                            color = TextHealthPrimary,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Kritik eşiğin altına indi (Kalan: %.0f IU)".format(stock.totalUnitsRemaining),
                            color = TextHealthPrimary.copy(alpha = 0.9f),
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = themeColor.copy(alpha = 0.25f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = if (isBasal) "BAZAL (UZUN)" else "BOLUS (HIZLI)",
                            color = themeColor,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stock.brandName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextHealthPrimary
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "%.0f IU".format(stock.totalUnitsRemaining),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (stock.isLowStock) AlertPharmacyRuby else themeColor
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "• %.1f Kalem (~%d tam)".format(stock.pensRemaining, stock.fullPensCount),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = HealthMint,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
            }

            IconButton(
                onClick = onEditStock,
                modifier = Modifier
                    .size(40.dp)
                    .background(HealthSurface, CircleShape)
                    .border(1.dp, HealthBorder, CircleShape)
                    .testTag("edit_stock_${stock.type.name}")
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Stok Düzenle",
                    tint = HealthMint,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Visual Fluid Indicator Bar (Deep Teal Container)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
                .clip(RoundedCornerShape(7.dp))
                .background(Color(0xFF06232F))
                .border(1.dp, HealthBorder, RoundedCornerShape(7.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress)
                    .clip(RoundedCornerShape(7.dp))
                    .background(
                        if (stock.isLowStock) {
                            AlertPharmacyRuby
                        } else {
                            HealthTealBright
                        }
                    )
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Threshold & Quick Refill
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Uyarı Eşiği: %.0f IU altı".format(stock.lowStockThresholdUnits),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = HealthMint
            )

            Button(
                onClick = onAddPen,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = HealthTeal,
                    contentColor = TextHealthPrimary
                ),
                border = BorderStroke(1.2.dp, HealthMint),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .height(36.dp)
                    .testTag("add_pen_${stock.type.name}"),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = TextHealthPrimary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "+1 Kalem (300 IU)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextHealthPrimary
                )
            }
        }
    }
}
