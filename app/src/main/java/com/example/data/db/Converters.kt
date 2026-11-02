package com.example.data.db

import androidx.room.TypeConverter
import com.example.data.model.DoseType
import com.example.data.model.InjectionSite

class Converters {
    @TypeConverter
    fun fromDoseType(value: DoseType): String = value.name

    @TypeConverter
    fun toDoseType(value: String): DoseType = runCatching { DoseType.valueOf(value) }.getOrDefault(DoseType.BOLUS)

    @TypeConverter
    fun fromInjectionSite(value: InjectionSite): String = value.name

    @TypeConverter
    fun toInjectionSite(value: String): InjectionSite = runCatching { InjectionSite.valueOf(value) }.getOrDefault(InjectionSite.ABDOMEN_RIGHT)
}
