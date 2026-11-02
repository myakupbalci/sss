package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory
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
import com.example.data.model.DoseType
import com.example.data.model.InsulinStock
import com.example.ui.components.EditStockDialog
import com.example.ui.theme.AlertRed
import com.example.ui.theme.AlertRedContainer
import com.example.ui.theme.BasalBlue
import com.example.ui.theme.BolusOrange
import com.example.ui.theme.SkyPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StockScreen(
    stocks: List<InsulinStock>,
    allDoses: List<DoseRecord>,
    onAddPens: (DoseType, Int) -> Unit,
    onUpdateStock: (InsulinStock) -> Unit,
    modifier: Modifier = Modifier,
    lang: com.example.util.AppLanguage = com.example.util.AppLanguage.TR
) {
    var editingStock by remember { mutableStateOf<InsulinStock?>(null) }
    val anyLowStock = stocks.any { it.isLowStock }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (lang == com.example.util.AppLanguage.EN) "Pen & Dose Inventory" else "Kalem & Doz Stok Takibi",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = com.example.ui.theme.TextHealthPrimary
            )
            Text(
                text = if (lang == com.example.util.AppLanguage.EN) "Automatic tracking of insulin pens and remaining IU doses" else "İnsülin kalemlerinizin ve toplam IU miktarınızın otomatik takibi",
                style = MaterialTheme.typography.bodyMedium,
                color = com.example.ui.theme.TextHealthSecondary
            )
        }

        // Global Low Stock Warning Banner
        if (anyLowStock) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("banner_global_pharmacy_warning"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = com.example.ui.theme.AlertRubyContainer),
                    border = BorderStroke(1.5.dp, com.example.ui.theme.AlertPharmacyRuby)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(com.example.ui.theme.AlertPharmacyRuby, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalPharmacy,
                                contentDescription = if (lang == com.example.util.AppLanguage.EN) "Pharmacy" else "Eczane",
                                tint = com.example.ui.theme.TextHealthPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = if (lang == com.example.util.AppLanguage.EN) "⚠️ Time to refill from pharmacy!" else "⚠️ Eczaneden insülin alma zamanı!",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = com.example.ui.theme.AlertPharmacyRuby
                            )
                            Text(
                                text = if (lang == com.example.util.AppLanguage.EN)
                                    "Your supply has dropped below the critical threshold. Please obtain a refill to prevent interruptions to your therapy."
                                else
                                    "Stoklarınız kritik seviyenin altına indi. İnsülin tedavinizin kesintiye uğramaması için lütfen yeni reçete yazdırınız veya eczaneden temin ediniz.",
                                style = MaterialTheme.typography.bodySmall,
                                color = com.example.ui.theme.TextHealthPrimary
                            )
                        }
                    }
                }
            }
        }

        // Stock Cards
        items(stocks, key = { it.id }) { stock ->
            val isBasal = stock.type == DoseType.BASAL
            val themeColor = if (isBasal) com.example.ui.theme.BasalCyan else com.example.ui.theme.BolusGold

            // Calculate estimated days left based on past 7 days usage
            val sevenDaysAgo = System.currentTimeMillis() - (7 * 24 * 3600 * 1000L)
            val pastDoses = allDoses.filter { it.type == stock.type && it.timestamp >= sevenDaysAgo }
            val dailyAvg = if (pastDoses.isNotEmpty()) pastDoses.sumOf { it.doseUnits } / 7.0 else 18.0
            val daysRemaining = if (dailyAvg > 0) (stock.totalUnitsRemaining / dailyAvg).toInt() else 30

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("stock_card_${stock.type.name}"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = com.example.ui.theme.HealthSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = BorderStroke(
                    if (stock.isLowStock) 1.5.dp else 1.2.dp,
                    if (stock.isLowStock) com.example.ui.theme.AlertPharmacyRuby else com.example.ui.theme.HealthBorder
                )
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = if (isBasal) com.example.ui.theme.BasalContainerDark else com.example.ui.theme.BolusContainerDark,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = if (isBasal) (if (lang == com.example.util.AppLanguage.EN) "BASAL" else "BAZAL") else "BOLUS",
                                    color = themeColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = stock.brandName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = com.example.ui.theme.TextHealthPrimary
                            )
                        }

                        IconButton(
                            onClick = { editingStock = stock },
                            modifier = Modifier.testTag("button_edit_stock_${stock.type.name}")
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = if (lang == com.example.util.AppLanguage.EN) "Edit" else "Düzenle", tint = com.example.ui.theme.HealthTealBright)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Remaining Units and Pens Hero
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text(
                                text = if (lang == com.example.util.AppLanguage.EN) "Remaining Dose" else "Kalan Doz",
                                style = MaterialTheme.typography.labelMedium,
                                color = com.example.ui.theme.TextHealthSecondary
                            )
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "%.0f".format(stock.totalUnitsRemaining),
                                    fontSize = 36.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (stock.isLowStock) com.example.ui.theme.AlertPharmacyRuby else themeColor
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "IU",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = com.example.ui.theme.TextHealthSecondary,
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = if (lang == com.example.util.AppLanguage.EN) "Pens on Hand" else "Eldeki Kalem",
                                style = MaterialTheme.typography.labelMedium,
                                color = com.example.ui.theme.TextHealthSecondary
                            )
                            Text(
                                text = if (lang == com.example.util.AppLanguage.EN) "%.1f Pens".format(stock.pensRemaining) else "%.1f Kalem".format(stock.pensRemaining),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = com.example.ui.theme.TextHealthPrimary
                            )
                            Text(
                                text = if (lang == com.example.util.AppLanguage.EN) "(1 Pen = ${stock.unitsPerPen} IU)" else "(1 Kalem = ${stock.unitsPerPen} IU)",
                                fontSize = 11.sp,
                                color = com.example.ui.theme.TextHealthSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Progress
                    val maxCapacity = (stock.unitsPerPen * 3).toDouble()
                    val progress = (stock.totalUnitsRemaining / maxCapacity).coerceIn(0.0, 1.0).toFloat()
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = if (stock.isLowStock) com.example.ui.theme.AlertPharmacyRuby else themeColor,
                        trackColor = com.example.ui.theme.HealthSurfaceVariant,
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Estimated Days & Threshold info
                    Surface(
                        color = com.example.ui.theme.HealthSurfaceVariant,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, com.example.ui.theme.HealthBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (lang == com.example.util.AppLanguage.EN) "⏳ Estimated Time:" else "⏳ Tahmini Süre:",
                                    fontSize = 11.sp,
                                    color = com.example.ui.theme.TextHealthSecondary
                                )
                                Text(
                                    text = if (lang == com.example.util.AppLanguage.EN) "~$daysRemaining days remaining" else "~$daysRemaining günlük stok kaldı",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (daysRemaining <= 3) com.example.ui.theme.AlertPharmacyRuby else com.example.ui.theme.TextHealthPrimary
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = if (lang == com.example.util.AppLanguage.EN) "⚠️ Alert Threshold:" else "⚠️ Uyarı Eşiği:",
                                    fontSize = 11.sp,
                                    color = com.example.ui.theme.TextHealthSecondary
                                )
                                Text(
                                    text = if (lang == com.example.util.AppLanguage.EN) "< %.0f IU".format(stock.lowStockThresholdUnits) else "%.0f IU altı".format(stock.lowStockThresholdUnits),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = com.example.ui.theme.TextHealthPrimary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Quick Add Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onAddPens(stock.type, 1) },
                            modifier = Modifier.weight(1f).testTag("quick_add_1_pen_${stock.type.name}"),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.2.dp, com.example.ui.theme.HealthTealBright.copy(alpha = 0.7f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = com.example.ui.theme.HealthTealBright)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (lang == com.example.util.AppLanguage.EN) "+1 Pen (300 IU)" else "+1 Kalem (300 IU)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { onAddPens(stock.type, 2) },
                            modifier = Modifier.weight(1f).testTag("quick_add_2_pens_${stock.type.name}"),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.2.dp, com.example.ui.theme.HealthTealBright.copy(alpha = 0.7f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = com.example.ui.theme.HealthTealBright)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (lang == com.example.util.AppLanguage.EN) "+2 Pens (600 IU)" else "+2 Kalem (600 IU)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    editingStock?.let { stock ->
        EditStockDialog(
            stock = stock,
            onDismiss = { editingStock = null },
            onSave = { updated ->
                onUpdateStock(updated)
                editingStock = null
            },
            lang = lang
        )
    }
}
