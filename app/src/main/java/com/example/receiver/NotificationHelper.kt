package com.example.receiver

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.data.model.DoseReminder
import com.example.data.model.DoseType
import com.example.data.model.InjectionSite
import java.util.Calendar
import java.util.Locale

object NotificationHelper {
    const val CHANNEL_REMINDERS_ID = "insulin_dose_reminders"
    const val CHANNEL_REMINDERS_NAME = "İnsülin Hatırlatıcıları"
    const val CHANNEL_STOCK_ID = "insulin_stock_alerts"
    const val CHANNEL_STOCK_NAME = "İnsülin Stok Uyarıları"
    const val CHANNEL_GLUCOSE_CHECK_ID = "blood_glucose_checks"
    const val CHANNEL_GLUCOSE_CHECK_NAME = "Açlık ve 2. Saat Tokluk Kan Şekeri Bildirimleri"

    const val ACTION_TRIGGER_REMINDER = "com.example.ACTION_TRIGGER_REMINDER"
    const val ACTION_DOSE_CONFIRMED = "com.example.ACTION_DOSE_CONFIRMED"
    const val ACTION_DOSE_SNOOZE_15 = "com.example.ACTION_DOSE_SNOOZE_15"
    const val ACTION_CHECK_BLOOD_GLUCOSE = "com.example.ACTION_CHECK_BLOOD_GLUCOSE"

    const val EXTRA_REMINDER_ID = "extra_reminder_id"
    const val EXTRA_REMINDER_LABEL = "extra_reminder_label"
    const val EXTRA_REMINDER_TYPE = "extra_reminder_type"
    const val EXTRA_REMINDER_UNITS = "extra_reminder_units"
    const val EXTRA_RECOMMENDED_SITE = "extra_recommended_site"
    const val EXTRA_REQUEST_FASTING = "extra_request_fasting"
    const val EXTRA_REQUEST_POST = "extra_request_post"
    const val EXTRA_DOSE_ID = "extra_dose_id"
    const val EXTRA_OPEN_BG_DIALOG = "extra_open_bg_dialog"
    const val EXTRA_OPEN_QUICK_DOSE_DIALOG = "extra_open_quick_dose_dialog"

    fun areNotificationsGranted(context: Context): Boolean {
        val managerEnabled = NotificationManagerCompat.from(context).areNotificationsEnabled()
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permGranted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            permGranted && managerEnabled
        } else {
            managerEnabled
        }
    }

    fun openAppNotificationSettings(context: Context) {
        try {
            val intent = Intent().apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                } else {
                    action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    data = Uri.fromParts("package", context.packageName, null)
                }
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            try {
                val fallback = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(fallback)
            } catch (_: Exception) {
            }
        }
    }

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val reminderChannel = NotificationChannel(
                CHANNEL_REMINDERS_ID,
                CHANNEL_REMINDERS_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Zamanı gelen insülin dozu ve açlık/tokluk kan şekeri hatırlatıcıları"
                enableVibration(true)
            }

            val stockChannel = NotificationChannel(
                CHANNEL_STOCK_ID,
                CHANNEL_STOCK_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Kritik seviyeye inen insülin kalem stok bildirimleri"
                enableVibration(true)
            }

            val glucoseChannel = NotificationChannel(
                CHANNEL_GLUCOSE_CHECK_ID,
                CHANNEL_GLUCOSE_CHECK_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "İnsülin enjeksiyonundan 2 saat sonraki tokluk kan şekeri ölçüm bildirimleri"
                enableVibration(true)
            }

            notificationManager.createNotificationChannel(reminderChannel)
            notificationManager.createNotificationChannel(stockChannel)
            notificationManager.createNotificationChannel(glucoseChannel)
        }
    }

    fun showDoseReminderNotification(
        context: Context,
        reminderId: Long,
        label: String,
        type: DoseType,
        units: Double,
        recommendedSite: InjectionSite,
        requestFastingGlucose: Boolean = true,
        requestPostGlucose: Boolean = true
    ) {
        createNotificationChannels(context)
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = (1000 + reminderId.coerceAtLeast(1L)).toInt()

        // Content Intent: Open app directly into dose & fasting glucose entry if requested
        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_OPEN_QUICK_DOSE_DIALOG, true)
            putExtra(EXTRA_REMINDER_ID, reminderId)
            putExtra(EXTRA_REMINDER_LABEL, label)
            putExtra(EXTRA_REMINDER_TYPE, type.name)
            putExtra(EXTRA_REMINDER_UNITS, units)
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action 1: "Vuruldum" (Confirm Dose)
        val confirmIntent = Intent(context, DoseReminderReceiver::class.java).apply {
            action = ACTION_DOSE_CONFIRMED
            putExtra(EXTRA_REMINDER_ID, reminderId)
            putExtra(EXTRA_REMINDER_LABEL, label)
            putExtra(EXTRA_REMINDER_TYPE, type.name)
            putExtra(EXTRA_REMINDER_UNITS, units)
            putExtra(EXTRA_RECOMMENDED_SITE, recommendedSite.name)
            putExtra(EXTRA_REQUEST_FASTING, requestFastingGlucose)
            putExtra(EXTRA_REQUEST_POST, requestPostGlucose)
        }
        val confirmPendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId * 10 + 1,
            confirmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action 2: "15 Dk Ertele" (Snooze 15 minutes)
        val snoozeIntent = Intent(context, DoseReminderReceiver::class.java).apply {
            action = ACTION_DOSE_SNOOZE_15
            putExtra(EXTRA_REMINDER_ID, reminderId)
            putExtra(EXTRA_REMINDER_LABEL, label)
            putExtra(EXTRA_REMINDER_TYPE, type.name)
            putExtra(EXTRA_REMINDER_UNITS, units)
            putExtra(EXTRA_RECOMMENDED_SITE, recommendedSite.name)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId * 10 + 2,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val typeText = if (type == DoseType.BASAL) "Bazal İnsülin" else "Bolus İnsülin"
        val rotationAdvice = "Önerilen Bölge: ${recommendedSite.displayName}"

        val glucoseRequestSummary = when {
            requestFastingGlucose && requestPostGlucose ->
                "🩸 Açlık Şekeri: Lütfen doz öncesi ölçüp girin • ⏱️ Dozdan 2 saat sonra Tokluk Şekeri bildirimi gönderilecektir."
            requestFastingGlucose ->
                "🩸 Açlık Şekeri Talebi: Lütfen insülin vurmadan önce açlık kan şekerinizi ölçüp uygulamaya girin."
            requestPostGlucose ->
                "⏱️ Tokluk Şekeri Talebi: Doz onaylandıktan 2 saat sonra tokluk kan şekeri ölçüm bildirimi gönderilecektir."
            else ->
                "Kan şekeri hatırlatması bu alarm için kapalıdır."
        }

        val shortGlucoseTag = when {
            requestFastingGlucose && requestPostGlucose -> " • 🩸 Açlık + Tokluk Şekeri"
            requestFastingGlucose -> " • 🩸 Açlık Şekeri Ölçümü"
            requestPostGlucose -> " • ⏱️ 2. Saat Tokluk Takibi"
            else -> ""
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_REMINDERS_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("💉 İnsülin ve Şeker Vakti: $label")
            .setContentText("%.1f IU $typeText • $rotationAdvice$shortGlucoseTag".format(units))
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(
                        "Saat geldi! %.1f IU $typeText ($label) uygulanmalı.\n" +
                            "$glucoseRequestSummary\n" +
                            "🔄 Rotasyon Önerisi: ${recommendedSite.displayName} (${recommendedSite.absorptionRate})".format(units)
                    )
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)
            .addAction(R.drawable.ic_launcher_foreground, "✓ Vuruldum", confirmPendingIntent)
            .addAction(R.drawable.ic_launcher_foreground, "⏰ 15 Dk Ertele", snoozePendingIntent)

        if (requestFastingGlucose) {
            builder.addAction(R.drawable.ic_launcher_foreground, "🩸 Açlık Şekeri Gir", contentPendingIntent)
        }

        try {
            notificationManager.notify(notificationId, builder.build())
        } catch (_: SecurityException) {
        }
    }

    /**
     * Immediately notifies the phone when a reminder is created/updated so the user gets instant confirmation
     * on their device that the reminder and blood glucose alerts are active.
     */
    fun showReminderScheduledNotification(
        context: Context,
        reminder: DoseReminder
    ) {
        if (!reminder.isEnabled) return
        createNotificationChannels(context)
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = (3000 + reminder.id.coerceAtLeast(1L)).toInt()

        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val timeFormatted = String.format(Locale.getDefault(), "%02d:%02d", reminder.hour, reminder.minute)
        val glucoseModeText = when {
            reminder.requestFastingGlucose && reminder.requestPostGlucose -> "Açlık + 2. Saat Tokluk Şekeri Bildirimi Açık"
            reminder.requestFastingGlucose -> "Sadece Açlık Kan Şekeri Bildirimi Açık"
            reminder.requestPostGlucose -> "Sadece 2. Saat Tokluk Kan Şekeri Bildirimi Açık"
            else -> "Kan Şekeri Talebi Kapalı"
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_REMINDERS_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("⏰ Hatırlatıcı Kuruldu: ${reminder.label} ($timeFormatted)")
            .setContentText("%.1f IU • $glucoseModeText".format(reminder.defaultDoseUnits))
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Her gün saat $timeFormatted için '${reminder.label}' (%.1f IU) telefon bildirimi aktif edildi.\n🩸 Kan Şekeri Ayarı: $glucoseModeText".format(reminder.defaultDoseUnits))
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)

        try {
            notificationManager.notify(notificationId, builder.build())
        } catch (_: SecurityException) {
        }
    }

    /**
     * Immediately posts a phone notification when a dose is recorded and 2-hour postprandial glucose check is scheduled,
     * allowing the user to see the active 2-hour tracking on their phone and tap to enter postprandial glucose at any time.
     */
    fun showPostDoseTrackingStartedNotification(
        context: Context,
        doseId: Long,
        units: Double,
        type: DoseType
    ) {
        createNotificationChannels(context)
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = (15000 + doseId.coerceAtLeast(1L)).toInt()

        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_OPEN_BG_DIALOG, true)
            putExtra(EXTRA_DOSE_ID, doseId)
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val cal = Calendar.getInstance().apply {
            add(Calendar.HOUR_OF_DAY, 2)
        }
        val targetTimeStr = String.format(
            Locale.getDefault(),
            "%02d:%02d",
            cal.get(Calendar.HOUR_OF_DAY),
            cal.get(Calendar.MINUTE)
        )
        val typeText = if (type == DoseType.BASAL) "Bazal" else "Bolus"

        val builder = NotificationCompat.Builder(context, CHANNEL_GLUCOSE_CHECK_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("⏱️ 2. Saat Tokluk Şekeri Alarmı Kuruldu ($targetTimeStr)")
            .setContentText("%.1f IU %s kaydedildi. Saat %s'de tokluk şekeri bildirimi gelecek.".format(units, typeText, targetTimeStr))
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(
                        "%.1f IU %s insülin dozunuz kaydedildi.\n\n" +
                            "🩸 2 saat sonra (Saat $targetTimeStr) telefonunuza Tokluk Kan Şekeri ölçüm bildirimi gönderilecektir. " +
                            "Ölçümünüzü hemen girmek isterseniz aşağıdaki butona dokunabilirsiniz.".format(units, typeText)
                    )
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)
            .addAction(R.drawable.ic_launcher_foreground, "🩸 Tokluk Şekerini Gir", contentPendingIntent)

        try {
            notificationManager.notify(notificationId, builder.build())
        } catch (_: SecurityException) {
        }
    }

    /**
     * Posts a phone notification when the user selects "Fasting Blood Glucose Tracking Only" (Sadece Açlık Takibi)
     * during manual dose entry, without scheduling a 2-hour postprandial alarm.
     */
    fun showFastingGlucoseTrackingNotification(
        context: Context,
        doseId: Long,
        units: Double,
        type: DoseType,
        fastingGlucose: Int?
    ) {
        createNotificationChannels(context)
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = (14000 + doseId.coerceAtLeast(1L)).toInt()

        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val typeText = if (type == DoseType.BASAL) "Bazal" else "Bolus"
        val titleText = if (fastingGlucose != null) {
            "🩸 Açlık Şekeri Takibi Kaydedildi ($fastingGlucose mg/dL)"
        } else {
            "🩸 Doz Öncesi Açlık Kan Şekeri Takibi"
        }
        val shortText = if (fastingGlucose != null) {
            "%.1f IU %s • Açlık: %d mg/dL (Tokluk bildirimi kapalı)".format(units, typeText, fastingGlucose)
        } else {
            "%.1f IU %s kaydedildi. Lütfen doz öncesi açlık kan şekerinizi kontrol edin.".format(units, typeText)
        }
        val bigText = if (fastingGlucose != null) {
            "%.1f IU %s insülin dozunuz ve doz öncesi açlık kan şekeriniz (%d mg/dL) kaydedildi.\nTercihiniz doğrultusunda 2 saat sonrası tokluk bildirimi kapalıdır.".format(units, typeText, fastingGlucose)
        } else {
            "%.1f IU %s insülin dozunuz 'Sadece Açlık Takibi' seçilerek kaydedildi.\nLütfen doz öncesi açlık kan şekerinizi ölçüp takip edin. Tercihiniz doğrultusunda 2. saat tokluk bildirimi gönderilmeyecektir.".format(units, typeText)
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_GLUCOSE_CHECK_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(titleText)
            .setContentText(shortText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)

        try {
            notificationManager.notify(notificationId, builder.build())
        } catch (_: SecurityException) {
        }
    }

    fun showLowStockNotification(
        context: Context,
        brandName: String,
        unitsLeft: Double
    ) {
        createNotificationChannels(context)
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = 9999

        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_STOCK_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("⚠️ Eczaneden insülin alma zamanı!")
            .setContentText("$brandName stoğunuz azaldı (Kalan: %.0f IU)".format(unitsLeft))
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Eczaneden insülin alma zamanı!\n$brandName için elinizde sadece %.0f IU insülin kaldı. Tedavinizin aksamaması için lütfen reçetenizi yenileyiniz.".format(unitsLeft))
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)

        try {
            notificationManager.notify(notificationId, builder.build())
        } catch (_: SecurityException) {
        }
    }

    fun scheduleReminderAlarm(context: Context, reminder: DoseReminder, triggerMillis: Long? = null) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, DoseReminderReceiver::class.java).apply {
            action = ACTION_TRIGGER_REMINDER
            putExtra(EXTRA_REMINDER_ID, reminder.id)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (!reminder.isEnabled) {
            alarmManager.cancel(pendingIntent)
            return
        }

        val targetMillis = triggerMillis ?: run {
            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, reminder.hour)
                set(Calendar.MINUTE, reminder.minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                if (before(Calendar.getInstance())) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }
            calendar.timeInMillis
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, targetMillis, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, targetMillis, pendingIntent)
            }
        } catch (e: SecurityException) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, targetMillis, pendingIntent)
        }
    }

    /**
     * Schedules a reminder 2 hours (or custom delay) after a dose is recorded
     * to check postprandial/control blood glucose.
     */
    fun schedulePostDoseGlucoseAlarm(
        context: Context,
        doseId: Long,
        units: Double,
        type: DoseType,
        delayMillis: Long = 2 * 60 * 60 * 1000L
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, DoseReminderReceiver::class.java).apply {
            action = ACTION_CHECK_BLOOD_GLUCOSE
            putExtra(EXTRA_DOSE_ID, doseId)
            putExtra(EXTRA_REMINDER_UNITS, units)
            putExtra(EXTRA_REMINDER_TYPE, type.name)
        }
        val requestCode = (50000 + doseId).toInt()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerMillis = System.currentTimeMillis() + delayMillis

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
            }
        } catch (e: SecurityException) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
        }
    }

    /**
     * Shows phone notification 2 hours after dose, requesting blood glucose measurement.
     */
    fun showPostDoseGlucoseNotification(
        context: Context,
        doseId: Long,
        units: Double,
        type: DoseType
    ) {
        createNotificationChannels(context)
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = (20000 + doseId.coerceAtLeast(1L)).toInt()

        // Content intent: Opens MainActivity and triggers blood glucose entry
        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_OPEN_BG_DIALOG, true)
            putExtra(EXTRA_DOSE_ID, doseId)
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val typeText = if (type == DoseType.BASAL) "Bazal" else "Bolus"

        val builder = NotificationCompat.Builder(context, CHANNEL_GLUCOSE_CHECK_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("🩸 2 Saat Sonrası: Tokluk Kan Şekeri Ölçüm Vakti")
            .setContentText("%.1f IU %s dozunuzdan 2 saat geçti. Lütfen tokluk şekerinizi ölçün.".format(units, typeText))
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("2 saat önce vurulan %.1f IU %s dozunun kontrol zamanı geldi.\n\nTakibinizi kayıt altına almak için lütfen 2 saat sonraki tokluk kan şekerinizi ölçüp uygulamaya kaydedin.".format(units, typeText))
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)
            .addAction(R.drawable.ic_launcher_foreground, "🩸 Tokluk Şekerini Gir", contentPendingIntent)

        try {
            notificationManager.notify(notificationId, builder.build())
        } catch (_: SecurityException) {
        }
    }
}
