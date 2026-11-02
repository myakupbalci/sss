package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dose_records")
data class DoseRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val doseUnits: Double,
    val type: DoseType,
    val insulinName: String,
    val injectionSite: InjectionSite,
    val bloodGlucose: Int? = null,
    val postDoseGlucose: Int? = null,
    val trackFastingGlucose: Boolean = true,
    val trackPostGlucose: Boolean = true,
    val mealTag: String = "",
    val notes: String = ""
)
