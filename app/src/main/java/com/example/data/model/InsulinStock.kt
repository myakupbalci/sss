package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlin.math.ceil

@Entity(tableName = "insulin_stocks")
data class InsulinStock(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: DoseType,
    val brandName: String,
    val unitsPerPen: Int = 300,
    val totalUnitsRemaining: Double,
    val lowStockThresholdUnits: Double = 90.0,
    val lastUpdated: Long = System.currentTimeMillis()
) {
    val pensRemaining: Double
        get() = if (unitsPerPen > 0) totalUnitsRemaining / unitsPerPen.toDouble() else 0.0

    val fullPensCount: Int
        get() = if (unitsPerPen > 0) (totalUnitsRemaining / unitsPerPen).toInt() else 0

    val isLowStock: Boolean
        get() = totalUnitsRemaining <= lowStockThresholdUnits
}
