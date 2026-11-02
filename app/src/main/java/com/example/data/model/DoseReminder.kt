package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Locale

@Entity(tableName = "dose_reminders")
data class DoseReminder(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val label: String,
    val type: DoseType,
    val hour: Int,
    val minute: Int,
    val defaultDoseUnits: Double,
    val isEnabled: Boolean = true,
    val snoozeUntil: Long? = null,
    val lastConfirmedDate: String? = null, // yyyy-MM-dd
    val requestFastingGlucose: Boolean = true, // Doz öncesi Açlık Kan Şekeri talebi
    val requestPostGlucose: Boolean = true     // Dozdan 2 saat sonra Tokluk Kan Şekeri talebi
) {
    val formattedTime: String
        get() = String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
}
