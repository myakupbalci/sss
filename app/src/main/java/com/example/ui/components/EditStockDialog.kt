package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.DoseType
import com.example.data.model.InsulinStock
import com.example.ui.theme.BasalBlue
import com.example.ui.theme.BolusOrange

@Composable
fun EditStockDialog(
    stock: InsulinStock,
    onDismiss: () -> Unit,
    onSave: (InsulinStock) -> Unit,
    lang: com.example.util.AppLanguage = com.example.util.AppLanguage.TR
) {
    var brandName by remember { mutableStateOf(stock.brandName) }
    var unitsPerPenText by remember { mutableStateOf(stock.unitsPerPen.toString()) }
    var totalUnitsText by remember { mutableStateOf(stock.totalUnitsRemaining.toInt().toString()) }
    var penCountText by remember { mutableStateOf(stock.fullPensCount.toString()) }
    var thresholdText by remember { mutableStateOf(stock.lowStockThresholdUnits.toInt().toString()) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .testTag("dialog_edit_stock"),
            color = com.example.ui.theme.HealthSurface,
            border = androidx.compose.foundation.BorderStroke(1.5.dp, com.example.ui.theme.HealthBorder)
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
                        text = if (lang == com.example.util.AppLanguage.EN) "📦 Update Stock & Pens" else "📦 Stok & Kalem Güncelle",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = com.example.ui.theme.TextHealthPrimary
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("button_stock_dialog_close")) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = if (lang == com.example.util.AppLanguage.EN) "Close" else "Kapat", tint = com.example.ui.theme.TextHealthSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = brandName,
                    onValueChange = { brandName = it },
                    label = { Text(if (lang == com.example.util.AppLanguage.EN) "Insulin Brand Name" else "İnsülin Kalem Markası") },
                    modifier = Modifier.fillMaxWidth().testTag("input_brand_name")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = totalUnitsText,
                    onValueChange = { input ->
                        totalUnitsText = input.filter { it.isDigit() }
                        val u = totalUnitsText.toDoubleOrNull() ?: 0.0
                        val upp = unitsPerPenText.toDoubleOrNull() ?: 300.0
                        if (upp > 0) {
                            penCountText = (u / upp).toString().take(4)
                        }
                    },
                    label = { Text(if (lang == com.example.util.AppLanguage.EN) "Total Dose on Hand (IU)" else "Toplam Eldeki Doz (IU)") },
                    placeholder = { Text("600") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("input_total_units")
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = penCountText,
                        onValueChange = { input ->
                            penCountText = input
                            val p = input.toDoubleOrNull() ?: 0.0
                            val upp = unitsPerPenText.toDoubleOrNull() ?: 300.0
                            totalUnitsText = (p * upp).toInt().toString()
                        },
                        label = { Text(if (lang == com.example.util.AppLanguage.EN) "Pen Count" else "Kalem Sayısı") },
                        placeholder = { Text("2") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f).testTag("input_pen_count")
                    )

                    OutlinedTextField(
                        value = unitsPerPenText,
                        onValueChange = { unitsPerPenText = it.filter { c -> c.isDigit() } },
                        label = { Text(if (lang == com.example.util.AppLanguage.EN) "IU Per Pen" else "Kalem Başı IU") },
                        placeholder = { Text("300") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f).testTag("input_units_per_pen")
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = thresholdText,
                    onValueChange = { thresholdText = it.filter { c -> c.isDigit() } },
                    label = { Text(if (lang == com.example.util.AppLanguage.EN) "Low Stock Alert Threshold (IU)" else "Kritik Stok Uyarısı Eşiği (IU)") },
                    supportingText = {
                        Text(
                            if (lang == com.example.util.AppLanguage.EN)
                                "When stock drops to this level, pharmacy refill warning is triggered."
                            else
                                "Stok bu seviyeye indiğinde 'Eczaneden insülin alma zamanı!' uyarısı verilir."
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("input_low_stock_threshold")
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        val units = totalUnitsText.toDoubleOrNull() ?: stock.totalUnitsRemaining
                        val upp = unitsPerPenText.toIntOrNull() ?: stock.unitsPerPen
                        val threshold = thresholdText.toDoubleOrNull() ?: stock.lowStockThresholdUnits
                        onSave(
                            stock.copy(
                                brandName = brandName,
                                unitsPerPen = upp,
                                totalUnitsRemaining = units,
                                lowStockThresholdUnits = threshold
                            )
                        )
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("button_save_stock"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (stock.type == DoseType.BASAL) BasalBlue else BolusOrange
                    )
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (lang == com.example.util.AppLanguage.EN) "Save Changes" else "Değişiklikleri Kaydet", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
