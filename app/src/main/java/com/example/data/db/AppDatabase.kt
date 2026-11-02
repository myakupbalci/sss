package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.DoseRecord
import com.example.data.model.DoseReminder
import com.example.data.model.DoseType
import com.example.data.model.InjectionSite
import com.example.data.model.InsulinStock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [DoseRecord::class, DoseReminder::class, InsulinStock::class],
    version = 4,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun doseDao(): DoseDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "insulin_assistant.db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialData(database.doseDao())
                    }
                }
            }

            private suspend fun populateInitialData(dao: DoseDao) {
                // Initial Default Reminders:
                // 1) Evening Basal
                dao.insertReminder(
                    DoseReminder(
                        label = "Akşam Bazal İnsülin",
                        type = DoseType.BASAL,
                        hour = 22,
                        minute = 0,
                        defaultDoseUnits = 16.0,
                        isEnabled = true
                    )
                )
                // 2) Breakfast Bolus
                dao.insertReminder(
                    DoseReminder(
                        label = "Kahvaltı Bolusu",
                        type = DoseType.BOLUS,
                        hour = 8,
                        minute = 30,
                        defaultDoseUnits = 6.0,
                        isEnabled = true
                    )
                )
                // 3) Lunch Bolus
                dao.insertReminder(
                    DoseReminder(
                        label = "Öğle Yemeği Bolusu",
                        type = DoseType.BOLUS,
                        hour = 13,
                        minute = 0,
                        defaultDoseUnits = 8.0,
                        isEnabled = true
                    )
                )
                // 4) Dinner Bolus
                dao.insertReminder(
                    DoseReminder(
                        label = "Akşam Yemeği Bolusu",
                        type = DoseType.BOLUS,
                        hour = 19,
                        minute = 30,
                        defaultDoseUnits = 8.0,
                        isEnabled = true
                    )
                )

                // Initial Stock: 1 full Basal pen (300 IU) and 2 full Bolus pens (600 IU)
                dao.insertStock(
                    InsulinStock(
                        type = DoseType.BASAL,
                        brandName = "Lantus SoloStar (Bazal)",
                        unitsPerPen = 300,
                        totalUnitsRemaining = 300.0,
                        lowStockThresholdUnits = 90.0
                    )
                )
                dao.insertStock(
                    InsulinStock(
                        type = DoseType.BOLUS,
                        brandName = "NovoRapid FlexPen (Bolus)",
                        unitsPerPen = 300,
                        totalUnitsRemaining = 600.0,
                        lowStockThresholdUnits = 100.0
                    )
                )

                // Add 1 sample initial dose record from yesterday so rotation assistant has realistic context
                val yesterdayMillis = System.currentTimeMillis() - (18 * 3600 * 1000L)
                dao.insertDose(
                    DoseRecord(
                        timestamp = yesterdayMillis,
                        doseUnits = 16.0,
                        type = DoseType.BASAL,
                        insulinName = "Lantus SoloStar",
                        injectionSite = InjectionSite.ABDOMEN_LEFT,
                        mealTag = "Gece Bazalı",
                        notes = "Rahat geçti"
                    )
                )
            }
        }
    }
}
