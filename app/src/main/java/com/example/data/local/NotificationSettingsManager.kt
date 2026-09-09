package com.example.data.local

import android.app.NotificationManager
import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.worker.AyahWorker
import com.example.worker.DailyAyahWorker
import com.example.worker.EventNotificationWorker
import com.example.worker.ReminderWorker
import java.util.concurrent.TimeUnit

object NotificationSettingsManager {
    private const val PREF_NAME = "quran_notification_prefs"
    private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
    private const val KEY_QURAN_NOTIFICATIONS = "quran_notifications_enabled"
    private const val KEY_UPCOMING_EVENTS = "upcoming_events_enabled"
    private const val KEY_AZKAR_NOTIFICATIONS = "azkar_notifications_enabled"
    private const val KEY_PRAYER_NOTIFICATIONS = "prayer_notifications_enabled"

    fun areNotificationsEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)
    }

    fun setNotificationsEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled).apply()

        val ahlPrefs = context.getSharedPreferences("AhlAlBaytPrefs", Context.MODE_PRIVATE)
        ahlPrefs.edit().putBoolean("notifications_enabled", enabled).apply()

        if (enabled) {
            scheduleAllNotifications(context)
        } else {
            cancelAllNotifications(context)
        }
    }

    fun isQuranEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_QURAN_NOTIFICATIONS, true)
    }

    fun setQuranEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_QURAN_NOTIFICATIONS, enabled).apply()
        if (enabled && areNotificationsEnabled(context)) {
            scheduleAllNotifications(context)
        } else {
            cancelAyahNotifications(context)
        }
    }

    fun isUpcomingEventsEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_UPCOMING_EVENTS, true)
    }

    fun setUpcomingEventsEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_UPCOMING_EVENTS, enabled).apply()
        if (enabled && areNotificationsEnabled(context)) {
            scheduleAllNotifications(context)
        } else {
            cancelEventNotifications(context)
        }
    }

    fun isAzkarEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val ahlPrefs = context.getSharedPreferences("AhlAlBaytPrefs", Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_AZKAR_NOTIFICATIONS, ahlPrefs.getBoolean("notifications_enabled", true))
    }

    fun setAzkarEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_AZKAR_NOTIFICATIONS, enabled).apply()
        val ahlPrefs = context.getSharedPreferences("AhlAlBaytPrefs", Context.MODE_PRIVATE)
        ahlPrefs.edit().putBoolean("notifications_enabled", enabled).apply()

        if (enabled && areNotificationsEnabled(context)) {
            scheduleAllNotifications(context)
        } else {
            cancelAzkarNotifications(context)
        }
    }

    fun isPrayerNotificationsEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_PRAYER_NOTIFICATIONS, true)
    }

    fun setPrayerNotificationsEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_PRAYER_NOTIFICATIONS, enabled).apply()
        if (enabled && areNotificationsEnabled(context)) {
            try {
                com.example.utils.PrayerNotificationScheduler.scheduleAllPrayerNotifications(context)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            try {
                com.example.utils.PrayerNotificationScheduler.cancelAllPrayerAlarms(context)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun scheduleAllNotifications(context: Context) {
        try {
            val workManager = WorkManager.getInstance(context)
            val masterEnabled = areNotificationsEnabled(context)
            if (!masterEnabled) {
                cancelAllNotifications(context)
                return
            }

            // 1. Ayah Notifications (Hourly with KEEP policy)
            if (isQuranEnabled(context)) {
                val periodicAyahRequest = PeriodicWorkRequestBuilder<AyahWorker>(1, TimeUnit.HOURS).build()
                workManager.enqueueUniquePeriodicWork(
                    "AyahNotificationPeriodic",
                    ExistingPeriodicWorkPolicy.KEEP,
                    periodicAyahRequest
                )

                val dailyAyahWorkRequest = PeriodicWorkRequestBuilder<DailyAyahWorker>(12, TimeUnit.HOURS).build()
                workManager.enqueueUniquePeriodicWork(
                    "DailyAyahWorker",
                    ExistingPeriodicWorkPolicy.KEEP,
                    dailyAyahWorkRequest
                )
            } else {
                cancelAyahNotifications(context)
            }

            // 2. Worship and Azkar Reminders
            if (isAzkarEnabled(context)) {
                val reminderRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
                    .setInitialDelay(60, TimeUnit.MINUTES)
                    .build()
                workManager.enqueueUniqueWork(
                    "ReminderWorkerChain",
                    ExistingWorkPolicy.KEEP,
                    reminderRequest
                )
            } else {
                cancelAzkarNotifications(context)
            }

            // 3. Religious Events & Occasions
            if (isUpcomingEventsEnabled(context)) {
                val eventRequest = PeriodicWorkRequestBuilder<EventNotificationWorker>(1, TimeUnit.HOURS).build()
                workManager.enqueueUniquePeriodicWork(
                    "EventNotificationWorker",
                    ExistingPeriodicWorkPolicy.KEEP,
                    eventRequest
                )
            } else {
                cancelEventNotifications(context)
            }

            // 4. Exact AlarmManager Scheduling
            com.example.receiver.NotificationAlarmScheduler.scheduleNextAlarm(context)

            // 5. Mawaqit Al-Salah (Prayer Times & Adhan Notifications)
            if (isPrayerNotificationsEnabled(context)) {
                try {
                    com.example.utils.PrayerNotificationHelper.createNotificationChannel(context)
                    com.example.utils.PrayerNotificationScheduler.scheduleAllPrayerNotifications(context)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                com.example.utils.PrayerNotificationScheduler.cancelAllPrayerAlarms(context)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun cancelAyahNotifications(context: Context) {
        try {
            val workManager = WorkManager.getInstance(context)
            workManager.cancelUniqueWork("AyahNotificationPeriodic")
            workManager.cancelUniqueWork("AyahNotificationWork")
            workManager.cancelUniqueWork("DailyAyahWorker")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun cancelAzkarNotifications(context: Context) {
        try {
            val workManager = WorkManager.getInstance(context)
            workManager.cancelUniqueWork("ReminderWorkerChain")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun cancelEventNotifications(context: Context) {
        try {
            val workManager = WorkManager.getInstance(context)
            workManager.cancelUniqueWork("EventNotificationWorker")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun cancelAllNotifications(context: Context) {
        try {
            val workManager = WorkManager.getInstance(context)
            workManager.cancelUniqueWork("AyahNotificationPeriodic")
            workManager.cancelUniqueWork("AyahNotificationWork")
            workManager.cancelUniqueWork("DailyAyahWorker")
            workManager.cancelUniqueWork("ReminderWorkerChain")
            workManager.cancelUniqueWork("EventNotificationWorker")
            com.example.receiver.NotificationAlarmScheduler.cancelAlarm(context)
            com.example.utils.PrayerNotificationScheduler.cancelAllPrayerAlarms(context)

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.cancelAll()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
