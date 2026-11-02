package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.DoseRecord
import com.example.data.model.DoseReminder
import com.example.data.model.DoseType
import com.example.data.model.InjectionSite
import com.example.data.model.InsulinStock
import com.example.receiver.NotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

sealed class UiEvent {
    data class ShowToast(val message: String) : UiEvent()
    data class StockLowAlert(val brand: String, val remainingUnits: Double) : UiEvent()
}

data class DailySummary(
    val totalUnits: Double = 0.0,
    val basalUnits: Double = 0.0,
    val bolusUnits: Double = 0.0,
    val doseCount: Int = 0
)

class InsulinViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getInstance(application)
    private val dao = db.doseDao()

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    // All doses
    val allDoses: StateFlow<List<DoseRecord>> = dao.getAllDosesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All reminders
    val reminders: StateFlow<List<DoseReminder>> = dao.getAllRemindersFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All stocks
    val stocks: StateFlow<List<InsulinStock>> = dao.getAllStocksFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Last dose
    val lastDose: StateFlow<DoseRecord?> = dao.getLastDoseFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Today's doses and daily summary
    val todayDoses: StateFlow<List<DoseRecord>> = allDoses.map { doses ->
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val startOfDay = cal.timeInMillis

        doses.filter { it.timestamp >= startOfDay }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todaySummary: StateFlow<DailySummary> = todayDoses.map { doses ->
        val total = doses.sumOf { it.doseUnits }
        val basal = doses.filter { it.type == DoseType.BASAL }.sumOf { it.doseUnits }
        val bolus = doses.filter { it.type == DoseType.BOLUS }.sumOf { it.doseUnits }
        DailySummary(
            totalUnits = total,
            basalUnits = basal,
            bolusUnits = bolus,
            doseCount = doses.size
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DailySummary())

    // Active in-app reminder (due or snoozed)
    val activeReminderDue: StateFlow<DoseReminder?> = reminders.combine(todayDoses) { reminderList, todayList ->
        val now = Calendar.getInstance()
        val currentHour = now.get(Calendar.HOUR_OF_DAY)
        val currentMinute = now.get(Calendar.MINUTE)
        val currentTotalMinutes = currentHour * 60 + currentMinute
        val nowMillis = System.currentTimeMillis()
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        // Find reminder that is snoozed and ready, OR within active window and not confirmed today
        reminderList.firstOrNull { reminder ->
            if (!reminder.isEnabled) return@firstOrNull false

            // Check if snoozed
            if (reminder.snoozeUntil != null && reminder.snoozeUntil <= nowMillis) {
                return@firstOrNull true
            }

            // If already confirmed today and not snoozed, skip
            if (reminder.lastConfirmedDate == todayStr) return@firstOrNull false

            // Check if reminder time is now or slightly overdue (within 120 mins)
            val reminderTotalMinutes = reminder.hour * 60 + reminder.minute
            val diffMinutes = currentTotalMinutes - reminderTotalMinutes
            diffMinutes in 0..120
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Selected App Language (TR / EN)
    private val _appLanguage = MutableStateFlow(com.example.util.AppLanguage.TR)
    val appLanguage: StateFlow<com.example.util.AppLanguage> = _appLanguage.asStateFlow()

    fun setLanguage(language: com.example.util.AppLanguage) {
        _appLanguage.value = language
    }

    // Most recent dose within 6 hours that is awaiting a 2-hour postprandial blood glucose check
    val pendingGlucoseCheckDose: StateFlow<DoseRecord?> = allDoses.map { list ->
        val sixHoursAgo = System.currentTimeMillis() - (6 * 60 * 60 * 1000L)
        list.firstOrNull { it.timestamp >= sixHoursAgo && it.trackPostGlucose && it.postDoseGlucose == null }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val notifiedReminderKeys = mutableSetOf<String>()

    init {
        NotificationHelper.createNotificationChannels(application)
        viewModelScope.launch(Dispatchers.IO) {
            val existing = dao.getAllDosesList()
            if (existing.size < 10) {
                seedOneWeekTrialData(force = false)
            }
        }
        // Automatically schedule all active reminders and trigger phone notification when a reminder becomes due
        viewModelScope.launch(Dispatchers.IO) {
            reminders.collect { list ->
                list.forEach { r ->
                    if (r.isEnabled) {
                        NotificationHelper.scheduleReminderAlarm(application, r)
                    }
                }
            }
        }
        viewModelScope.launch(Dispatchers.IO) {
            activeReminderDue.collect { dueReminder ->
                if (dueReminder != null) {
                    val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                    val key = "${dueReminder.id}_${todayStr}_${dueReminder.snoozeUntil ?: 0L}"
                    if (notifiedReminderKeys.add(key)) {
                        NotificationHelper.showDoseReminderNotification(
                            context = application,
                            reminderId = dueReminder.id,
                            label = dueReminder.label,
                            type = dueReminder.type,
                            units = dueReminder.defaultDoseUnits,
                            recommendedSite = getRecommendedSite(dueReminder.type),
                            requestFastingGlucose = dueReminder.requestFastingGlucose,
                            requestPostGlucose = dueReminder.requestPostGlucose
                        )
                    }
                }
            }
        }
    }

    /**
     * Seeds 1 week (7 days) of realistic trial data with rotating injection sites,
     * basal and bolus doses, pre-dose fasting glucose, and 2-hour postprandial glucose.
     */
    fun seedOneWeekTrialData(force: Boolean = true) {
        viewModelScope.launch(Dispatchers.IO) {
            val sampleRecords = com.example.util.TrialDataGenerator.createOneWeekSampleDoses()
            dao.insertDoses(sampleRecords)
            if (force) {
                _eventFlow.emit(
                    UiEvent.ShowToast(
                        if (_appLanguage.value == com.example.util.AppLanguage.EN)
                            "✓ 1 week of trial insulin & glucose data loaded!"
                        else
                            "✓ 1 haftalık deneme insülin ve şeker verisi yüklendi!"
                    )
                )
            }
        }
    }

    // Recommendation logic
    fun getRecommendedSite(targetType: DoseType? = null): InjectionSite {
        val last = lastDose.value
        return InjectionSite.getRecommendedRotation(last?.injectionSite, targetType)
    }

    fun getRotationAdvice(): String {
        val last = lastDose.value
        val next = getRecommendedSite(last?.type)
        return InjectionSite.getRotationAdviceText(last?.injectionSite, next)
    }

    // Direct Confirmation from Reminder / In-App Notification
    fun confirmDose(
        reminder: DoseReminder,
        customSite: InjectionSite? = null,
        customUnits: Double? = null
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val site = customSite ?: getRecommendedSite(reminder.type)
            val units = customUnits ?: reminder.defaultDoseUnits
            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            val stock = dao.getStockByType(reminder.type)
            val brand = stock?.brandName ?: if (reminder.type == DoseType.BASAL) "Bazal İnsülin" else "Bolus İnsülin"

            // 1. Insert Dose
            val newDoseId = dao.insertDose(
                DoseRecord(
                    timestamp = System.currentTimeMillis(),
                    doseUnits = units,
                    type = reminder.type,
                    insulinName = brand,
                    injectionSite = site,
                    trackFastingGlucose = reminder.requestFastingGlucose,
                    trackPostGlucose = reminder.requestPostGlucose,
                    mealTag = reminder.label,
                    notes = "Onaylandı (Önerilen bölge: ${site.displayName})"
                )
            )

            // Schedule 2-hour postprandial blood glucose check alarm & send phone notification if requested
            if (reminder.requestPostGlucose) {
                NotificationHelper.schedulePostDoseGlucoseAlarm(getApplication(), newDoseId, units, reminder.type)
                NotificationHelper.showPostDoseTrackingStartedNotification(getApplication(), newDoseId, units, reminder.type)
            } else if (reminder.requestFastingGlucose) {
                NotificationHelper.showFastingGlucoseTrackingNotification(getApplication(), newDoseId, units, reminder.type, null)
            }

            // 2. Decrement stock
            dao.decrementStock(reminder.type, units)

            // 3. Update reminder state
            dao.updateReminder(
                reminder.copy(
                    lastConfirmedDate = todayStr,
                    snoozeUntil = null
                )
            )

            val glucoseNote = when {
                reminder.requestFastingGlucose && reminder.requestPostGlucose ->
                    "Açlık ve 2. saat tokluk şekeri telefon bildirimi aktif."
                reminder.requestFastingGlucose ->
                    "Açlık şekeri takibi aktif."
                reminder.requestPostGlucose ->
                    "2 saat sonra tokluk şeker ölçüm telefon bildirimi kuruldu."
                else ->
                    "Kan şekeri hatırlatıcısı kapalı."
            }
            _eventFlow.emit(UiEvent.ShowToast("✓ %.1f IU %s dozu kaydedildi. %s".format(units, reminder.type.title, glucoseNote)))
        }
    }

    // 15-Minute Snooze Mechanism
    fun snoozeReminder(reminder: DoseReminder) {
        viewModelScope.launch(Dispatchers.IO) {
            val snoozeMillis = System.currentTimeMillis() + (15 * 60 * 1000L)
            dao.updateReminder(reminder.copy(snoozeUntil = snoozeMillis))
            NotificationHelper.scheduleReminderAlarm(getApplication(), reminder, snoozeMillis)
            _eventFlow.emit(UiEvent.ShowToast("⏰ Hatırlatıcı 15 dakika ertelendi."))
        }
    }

    // Manual Dose Entry
    fun recordManualDose(
        units: Double,
        type: DoseType,
        site: InjectionSite,
        mealTag: String,
        bloodGlucose: Int?,
        notes: String,
        insulinName: String,
        trackFastingGlucose: Boolean = true,
        trackPostGlucose: Boolean = true
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val savedFasting = if (trackFastingGlucose) bloodGlucose else null
            val newDoseId = dao.insertDose(
                DoseRecord(
                    timestamp = System.currentTimeMillis(),
                    doseUnits = units,
                    type = type,
                    insulinName = insulinName,
                    injectionSite = site,
                    bloodGlucose = savedFasting,
                    trackFastingGlucose = trackFastingGlucose,
                    trackPostGlucose = trackPostGlucose,
                    mealTag = mealTag,
                    notes = notes
                )
            )

            // Trigger phone notifications ONLY according to user's selected tracking preference
            if (trackPostGlucose) {
                NotificationHelper.schedulePostDoseGlucoseAlarm(getApplication(), newDoseId, units, type)
                NotificationHelper.showPostDoseTrackingStartedNotification(getApplication(), newDoseId, units, type)
            } else if (trackFastingGlucose) {
                NotificationHelper.showFastingGlucoseTrackingNotification(
                    getApplication(),
                    newDoseId,
                    units,
                    type,
                    savedFasting
                )
            }

            val isEn = _appLanguage.value == com.example.util.AppLanguage.EN
            val toastMsg = when {
                trackFastingGlucose && trackPostGlucose ->
                    if (isEn) {
                        "✓ %.1f IU insulin recorded. Fasting & 2-hour postprandial glucose notification active.".format(units)
                    } else {
                        "✓ %.1f IU insülin kaydedildi. Açlık takibi ve 2 saat sonrası tokluk şekeri bildirimi kuruldu.".format(units)
                    }
                trackFastingGlucose && !trackPostGlucose ->
                    if (isEn) {
                        "✓ %.1f IU insulin & fasting glucose tracking recorded (Post-dose notification off).".format(units)
                    } else {
                        "✓ %.1f IU insülin ve sadece açlık şekeri takibi kaydedildi (Tokluk bildirimi kapalı).".format(units)
                    }
                !trackFastingGlucose && trackPostGlucose ->
                    if (isEn) {
                        "✓ %.1f IU insulin recorded. 2-hour postprandial glucose notification scheduled.".format(units)
                    } else {
                        "✓ %.1f IU insülin kaydedildi. Sadece 2 saat sonrası tokluk şekeri telefon bildirimi kuruldu.".format(units)
                    }
                else ->
                    if (isEn) {
                        "✓ %.1f IU insulin recorded (Blood glucose tracking & notifications disabled).".format(units)
                    } else {
                        "✓ %.1f IU insülin kaydedildi (Kan şekeri takibi ve bildirimi kapalı).".format(units)
                    }
            }
            _eventFlow.emit(UiEvent.ShowToast(toastMsg))
        }
    }

    // Record / Update 2-Hour Postprandial Blood Glucose Result
    fun updateDosePostGlucose(doseId: Long, glucose: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.updateDosePostGlucose(doseId, glucose)
            val msg = if (_appLanguage.value == com.example.util.AppLanguage.EN) {
                "✓ 2-Hour postprandial glucose (%d mg/dL) saved.".format(glucose)
            } else {
                "✓ 2 Saat sonraki tokluk şekeri (%d mg/dL) başarıyla kaydedildi.".format(glucose)
            }
            _eventFlow.emit(UiEvent.ShowToast(msg))
        }
    }

    // Record / Update Pre-Dose Fasting Blood Glucose Result
    fun updateDoseBloodGlucose(doseId: Long, glucose: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.updateDoseBloodGlucose(doseId, glucose)
            val msg = if (_appLanguage.value == com.example.util.AppLanguage.EN) {
                "✓ Pre-dose blood glucose (%d mg/dL) saved.".format(glucose)
            } else {
                "✓ Doz öncesi açlık kan şekeri (%d mg/dL) kaydedildi.".format(glucose)
            }
            _eventFlow.emit(UiEvent.ShowToast(msg))
        }
    }

    // Fast Test Helper: triggers the 2-hour phone notification immediately for demonstration/testing
    fun triggerInstant2HourGlucoseNotification(doseId: Long, units: Double, type: DoseType) {
        NotificationHelper.showPostDoseGlucoseNotification(getApplication(), doseId, units, type)
    }

    fun deleteDose(record: DoseRecord) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.deleteDose(record)
            _eventFlow.emit(UiEvent.ShowToast("Doz kaydı silindi."))
        }
    }

    // Reminders Management
    fun saveReminder(reminder: DoseReminder) {
        viewModelScope.launch(Dispatchers.IO) {
            val saved = if (reminder.id == 0L) {
                val newId = dao.insertReminder(reminder)
                reminder.copy(id = newId)
            } else {
                dao.updateReminder(reminder)
                reminder
            }
            NotificationHelper.scheduleReminderAlarm(getApplication(), saved)
            if (saved.isEnabled) {
                NotificationHelper.showReminderScheduledNotification(getApplication(), saved)
            }
            _eventFlow.emit(
                UiEvent.ShowToast(
                    if (_appLanguage.value == com.example.util.AppLanguage.EN)
                        "✓ Reminder saved and phone notification alarm set."
                    else
                        "✓ Hatırlatıcı kaydedildi ve telefon bildirim alarmı kuruldu."
                )
            )
        }
    }

    fun toggleReminder(reminder: DoseReminder, isEnabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = reminder.copy(isEnabled = isEnabled)
            dao.updateReminder(updated)
            NotificationHelper.scheduleReminderAlarm(getApplication(), updated)
            if (isEnabled) {
                NotificationHelper.showReminderScheduledNotification(getApplication(), updated)
            }
        }
    }

    fun deleteReminder(reminder: DoseReminder) {
        viewModelScope.launch(Dispatchers.IO) {
            val disabled = reminder.copy(isEnabled = false)
            NotificationHelper.scheduleReminderAlarm(getApplication(), disabled)
            dao.deleteReminder(reminder)
            _eventFlow.emit(UiEvent.ShowToast("Hatırlatıcı kaldırıldı."))
        }
    }

    // Stock Management
    fun addPensToStock(type: DoseType, pensCount: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val current = dao.getStockByType(type)
            if (current != null) {
                val addedUnits = pensCount * current.unitsPerPen
                val updated = current.copy(
                    totalUnitsRemaining = current.totalUnitsRemaining + addedUnits,
                    lastUpdated = System.currentTimeMillis()
                )
                dao.updateStock(updated)
                _eventFlow.emit(UiEvent.ShowToast("+%d Kalem (%d IU) stoğa eklendi.".format(pensCount, addedUnits)))
            }
        }
    }

    fun updateStockDirectly(stock: InsulinStock) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.updateStock(stock.copy(lastUpdated = System.currentTimeMillis()))
            _eventFlow.emit(UiEvent.ShowToast("Stok bilgileri güncellendi."))
        }
    }
}
