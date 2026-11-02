package com.example.util

import com.example.data.model.DoseRecord
import com.example.data.model.DoseType
import com.example.data.model.InjectionSite
import java.util.Calendar

object TrialDataGenerator {

    /**
     * Generates a realistic, clinically structured 1-week (7 days) dataset of insulin dose records,
     * including pre-dose fasting glucose, 2-hour postprandial glucose, realistic meal tags,
     * and anatomical site rotations across abdomen, calves (baldır ön yüzü), arms, and gluteals.
     */
    fun createOneWeekSampleDoses(): List<DoseRecord> {
        val records = mutableListOf<DoseRecord>()

        // 7 days: from 6 days ago up to today (daysAgo 6 down to 0)
        val dayConfigs = listOf(
            // Day -6
            DayPlan(
                daysAgo = 6,
                meals = listOf(
                    MealDose(hour = 8, minute = 15, units = 7.5, type = DoseType.BOLUS, fastingBg = 94, postBg = 135, site = InjectionSite.ABDOMEN_RIGHT, tag = "Kahvaltı", note = "Tam buğday ekmeği ve yumurta"),
                    MealDose(hour = 12, minute = 45, units = 8.5, type = DoseType.BOLUS, fastingBg = 110, postBg = 148, site = InjectionSite.ARM_RIGHT, tag = "Öğle Yemeği", note = "Izgara tavuk ve salata"),
                    MealDose(hour = 19, minute = 20, units = 9.0, type = DoseType.BOLUS, fastingBg = 118, postBg = 156, site = InjectionSite.ABDOMEN_LEFT, tag = "Akşam Yemeği", note = "Sebze yemeği ve yoğurt"),
                    MealDose(hour = 22, minute = 15, units = 16.0, type = DoseType.BASAL, fastingBg = 108, postBg = null, site = InjectionSite.THIGH_RIGHT, tag = "Gece Bazalı", note = "Gece dozu uygulandı")
                )
            ),
            // Day -5
            DayPlan(
                daysAgo = 5,
                meals = listOf(
                    MealDose(hour = 8, minute = 20, units = 8.0, type = DoseType.BOLUS, fastingBg = 102, postBg = 142, site = InjectionSite.ARM_LEFT, tag = "Kahvaltı", note = "Yulaf ezmesi ve ceviz"),
                    MealDose(hour = 13, minute = 0, units = 8.0, type = DoseType.BOLUS, fastingBg = 114, postBg = 150, site = InjectionSite.GLUTEAL_RIGHT, tag = "Öğle Yemeği", note = "Mercimek çorbası"),
                    MealDose(hour = 19, minute = 35, units = 10.0, type = DoseType.BOLUS, fastingBg = 125, postBg = 165, site = InjectionSite.THIGH_LEFT, tag = "Akşam Yemeği", note = "Öğün sonrası 25 dk yürüyüş"),
                    MealDose(hour = 22, minute = 10, units = 16.0, type = DoseType.BASAL, fastingBg = 112, postBg = null, site = InjectionSite.THIGH_RIGHT, tag = "Gece Bazalı", note = "Doku dinlendirildi")
                )
            ),
            // Day -4
            DayPlan(
                daysAgo = 4,
                meals = listOf(
                    MealDose(hour = 8, minute = 30, units = 7.0, type = DoseType.BOLUS, fastingBg = 88, postBg = 126, site = InjectionSite.ABDOMEN_LEFT, tag = "Kahvaltı", note = "Peynirli omlet ve zeytin"),
                    MealDose(hour = 12, minute = 50, units = 9.0, type = DoseType.BOLUS, fastingBg = 106, postBg = 144, site = InjectionSite.ARM_RIGHT, tag = "Öğle Yemeği", note = "Etli kuru fasulye ve ayran"),
                    MealDose(hour = 19, minute = 15, units = 8.5, type = DoseType.BOLUS, fastingBg = 115, postBg = 149, site = InjectionSite.GLUTEAL_LEFT, tag = "Akşam Yemeği", note = "Hafif akşam menüsü"),
                    MealDose(hour = 22, minute = 5, units = 16.0, type = DoseType.BASAL, fastingBg = 104, postBg = null, site = InjectionSite.THIGH_LEFT, tag = "Gece Bazalı", note = "Rahat uygulama")
                )
            ),
            // Day -3
            DayPlan(
                daysAgo = 3,
                meals = listOf(
                    MealDose(hour = 8, minute = 10, units = 8.0, type = DoseType.BOLUS, fastingBg = 112, postBg = 152, site = InjectionSite.ABDOMEN_RIGHT, tag = "Kahvaltı", note = "Haşlanmış yumurta"),
                    MealDose(hour = 13, minute = 10, units = 9.5, type = DoseType.BOLUS, fastingBg = 122, postBg = 168, site = InjectionSite.ARM_LEFT, tag = "Öğle Yemeği", note = "Bulgur pilavı ve et sote"),
                    MealDose(hour = 19, minute = 40, units = 8.0, type = DoseType.BOLUS, fastingBg = 108, postBg = 140, site = InjectionSite.GLUTEAL_RIGHT, tag = "Akşam Yemeği", note = "Zeytinyağlı taze fasulye"),
                    MealDose(hour = 22, minute = 30, units = 17.0, type = DoseType.BASAL, fastingBg = 120, postBg = null, site = InjectionSite.THIGH_RIGHT, tag = "Gece Bazalı", note = "Doz revizyonuna uygun")
                )
            ),
            // Day -2
            DayPlan(
                daysAgo = 2,
                meals = listOf(
                    MealDose(hour = 8, minute = 25, units = 7.0, type = DoseType.BOLUS, fastingBg = 92, postBg = 130, site = InjectionSite.THIGH_RIGHT, tag = "Kahvaltı", note = "Yeşillikli hafif kahvaltı"),
                    MealDose(hour = 12, minute = 40, units = 8.0, type = DoseType.BOLUS, fastingBg = 101, postBg = 138, site = InjectionSite.ABDOMEN_LEFT, tag = "Öğle Yemeği", note = "Ton balıklı salata"),
                    MealDose(hour = 19, minute = 25, units = 9.0, type = DoseType.BOLUS, fastingBg = 116, postBg = 154, site = InjectionSite.ARM_RIGHT, tag = "Akşam Yemeği", note = "Fırında balık"),
                    MealDose(hour = 22, minute = 15, units = 16.0, type = DoseType.BASAL, fastingBg = 107, postBg = null, site = InjectionSite.THIGH_LEFT, tag = "Gece Bazalı", note = "Tam zamanında")
                )
            ),
            // Day -1 (Yesterday)
            DayPlan(
                daysAgo = 1,
                meals = listOf(
                    MealDose(hour = 8, minute = 15, units = 8.0, type = DoseType.BOLUS, fastingBg = 98, postBg = 136, site = InjectionSite.ARM_LEFT, tag = "Kahvaltı", note = "Klasik kahvaltı"),
                    MealDose(hour = 13, minute = 0, units = 9.0, type = DoseType.BOLUS, fastingBg = 119, postBg = 158, site = InjectionSite.GLUTEAL_LEFT, tag = "Öğle Yemeği", note = "Izgara köfte ve ayran"),
                    MealDose(hour = 19, minute = 30, units = 8.5, type = DoseType.BOLUS, fastingBg = 111, postBg = 145, site = InjectionSite.ABDOMEN_RIGHT, tag = "Akşam Yemeği", note = "Sebze yemeği"),
                    MealDose(hour = 22, minute = 20, units = 16.0, type = DoseType.BASAL, fastingBg = 110, postBg = null, site = InjectionSite.THIGH_RIGHT, tag = "Gece Bazalı", note = "Gece şekeri stabil")
                )
            ),
            // Day 0 (Today)
            DayPlan(
                daysAgo = 0,
                meals = listOf(
                    MealDose(hour = 8, minute = 30, units = 8.0, type = DoseType.BOLUS, fastingBg = 95, postBg = 134, site = InjectionSite.ABDOMEN_LEFT, tag = "Kahvaltı", note = "Dengeli kahvaltı"),
                    MealDose(hour = 12, minute = 45, units = 8.5, type = DoseType.BOLUS, fastingBg = 108, postBg = 144, site = InjectionSite.ARM_RIGHT, tag = "Öğle Yemeği", note = "Öğle dozu uygulandı")
                )
            )
        )

        for (plan in dayConfigs) {
            for (meal in plan.meals) {
                val cal = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, -plan.daysAgo)
                    set(Calendar.HOUR_OF_DAY, meal.hour)
                    set(Calendar.MINUTE, meal.minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                records.add(
                    DoseRecord(
                        timestamp = cal.timeInMillis,
                        doseUnits = meal.units,
                        type = meal.type,
                        insulinName = if (meal.type == DoseType.BASAL) "Lantus SoloStar (Bazal)" else "NovoRapid FlexPen (Bolus)",
                        injectionSite = meal.site,
                        mealTag = meal.tag,
                        notes = meal.note,
                        bloodGlucose = meal.fastingBg,
                        postDoseGlucose = meal.postBg
                    )
                )
            }
        }

        return records.sortedByDescending { it.timestamp }
    }

    private data class DayPlan(
        val daysAgo: Int,
        val meals: List<MealDose>
    )

    private data class MealDose(
        val hour: Int,
        val minute: Int,
        val units: Double,
        val type: DoseType,
        val fastingBg: Int,
        val postBg: Int?,
        val site: InjectionSite,
        val tag: String,
        val note: String
    )
}
