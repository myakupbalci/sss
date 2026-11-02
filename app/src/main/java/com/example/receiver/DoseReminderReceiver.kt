package com.example.receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.db.AppDatabase
import com.example.data.model.DoseRecord
import com.example.data.model.DoseType
import com.example.data.model.InjectionSite
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class DoseReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val db = AppDatabase.getInstance(context)
        val dao = db.doseDao()
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val reminderId = intent.getLongExtra(NotificationHelper.EXTRA_REMINDER_ID, -1L)

        when (action) {
            NotificationHelper.ACTION_TRIGGER_REMINDER -> {
                CoroutineScope(Dispatchers.IO).launch {
                    val reminder = if (reminderId > 0) dao.getReminderById(reminderId) else null
                    if (reminder != null && reminder.isEnabled) {
                        val lastDose = dao.getLastDose()
                        val recommendedSite = InjectionSite.getRecommendedRotation(lastDose?.injectionSite, reminder.type)

                        NotificationHelper.showDoseReminderNotification(
                            context = context,
                            reminderId = reminder.id,
                            label = reminder.label,
                            type = reminder.type,
                            units = reminder.defaultDoseUnits,
                            recommendedSite = recommendedSite,
                            requestFastingGlucose = reminder.requestFastingGlucose,
                            requestPostGlucose = reminder.requestPostGlucose
                        )

                        // Schedule next day's recurring alarm
                        val tomorrow = Calendar.getInstance().apply {
                            set(Calendar.HOUR_OF_DAY, reminder.hour)
                            set(Calendar.MINUTE, reminder.minute)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                            add(Calendar.DAY_OF_YEAR, 1)
                        }
                        NotificationHelper.scheduleReminderAlarm(context, reminder, tomorrow.timeInMillis)
                    }
                }
            }

            NotificationHelper.ACTION_DOSE_CONFIRMED -> {
                val label = intent.getStringExtra(NotificationHelper.EXTRA_REMINDER_LABEL) ?: "İnsülin Dozu"
                val typeStr = intent.getStringExtra(NotificationHelper.EXTRA_REMINDER_TYPE) ?: DoseType.BOLUS.name
                val type = runCatching { DoseType.valueOf(typeStr) }.getOrDefault(DoseType.BOLUS)
                val units = intent.getDoubleExtra(NotificationHelper.EXTRA_REMINDER_UNITS, 10.0)
                val siteStr = intent.getStringExtra(NotificationHelper.EXTRA_RECOMMENDED_SITE) ?: InjectionSite.ABDOMEN_RIGHT.name
                val site = runCatching { InjectionSite.valueOf(siteStr) }.getOrDefault(InjectionSite.ABDOMEN_RIGHT)
                val extraFasting = intent.getBooleanExtra(NotificationHelper.EXTRA_REQUEST_FASTING, true)
                val extraPost = intent.getBooleanExtra(NotificationHelper.EXTRA_REQUEST_POST, true)

                val notifId = (1000 + reminderId.coerceAtLeast(1L)).toInt()
                notificationManager.cancel(notifId)

                CoroutineScope(Dispatchers.IO).launch {
                    val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                    val r = if (reminderId > 0) dao.getReminderById(reminderId) else null
                    val shouldRequestFasting = r?.requestFastingGlucose ?: extraFasting
                    val shouldRequestPost = r?.requestPostGlucose ?: extraPost

                    // 1. Direct Data Record
                    val newDoseId = dao.insertDose(
                        DoseRecord(
                            timestamp = System.currentTimeMillis(),
                            doseUnits = units,
                            type = type,
                            insulinName = if (type == DoseType.BASAL) "Bazal İnsülin" else "Bolus İnsülin",
                            injectionSite = site,
                            trackFastingGlucose = shouldRequestFasting,
                            trackPostGlucose = shouldRequestPost,
                            mealTag = label,
                            notes = "Hatırlatıcı onayı ile kaydedildi (Önerilen bölge: ${site.displayName})"
                        )
                    )

                    // 2. Schedule 2-hour postprandial blood glucose check alarm & notify phone
                    if (shouldRequestPost) {
                        NotificationHelper.schedulePostDoseGlucoseAlarm(context, newDoseId, units, type)
                        NotificationHelper.showPostDoseTrackingStartedNotification(context, newDoseId, units, type)
                    }

                    // 3. Decrement Stock
                    dao.decrementStock(type, units)

                    // 4. Mark reminder confirmed today
                    if (r != null) {
                        dao.updateReminder(r.copy(lastConfirmedDate = todayStr, snoozeUntil = null))
                    }
                }
            }

            NotificationHelper.ACTION_DOSE_SNOOZE_15 -> {
                val notifId = (1000 + reminderId.coerceAtLeast(1L)).toInt()
                notificationManager.cancel(notifId)

                val snoozeTime = System.currentTimeMillis() + (15 * 60 * 1000L) // 15 minutes

                CoroutineScope(Dispatchers.IO).launch {
                    if (reminderId > 0) {
                        val r = dao.getReminderById(reminderId)
                        if (r != null) {
                            dao.updateReminder(r.copy(snoozeUntil = snoozeTime))
                            NotificationHelper.scheduleReminderAlarm(context, r, snoozeTime)
                        }
                    }
                }
            }

            NotificationHelper.ACTION_CHECK_BLOOD_GLUCOSE -> {
                val doseId = intent.getLongExtra(NotificationHelper.EXTRA_DOSE_ID, -1L)
                val units = intent.getDoubleExtra(NotificationHelper.EXTRA_REMINDER_UNITS, 10.0)
                val typeStr = intent.getStringExtra(NotificationHelper.EXTRA_REMINDER_TYPE) ?: DoseType.BOLUS.name
                val type = runCatching { DoseType.valueOf(typeStr) }.getOrDefault(DoseType.BOLUS)

                if (doseId > 0) {
                    CoroutineScope(Dispatchers.IO).launch {
                        val dose = dao.getDoseById(doseId)
                        if (dose != null && dose.trackPostGlucose && dose.postDoseGlucose == null) {
                            NotificationHelper.showPostDoseGlucoseNotification(
                                context = context,
                                doseId = doseId,
                                units = units,
                                type = type
                            )
                        }
                    }
                }
            }
        }
    }
}
