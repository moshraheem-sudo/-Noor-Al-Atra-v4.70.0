package com.example.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.model.PrayerTimesData

object PrayerStickyNotification {
    private const val CHANNEL_ID = "prayer_daily_times_card_isolated_v2"
    private const val NOTIFICATION_ID = 5005
    private const val GROUP_PRAYER_CARD = "GROUP_PRAYER_CARD_ISOLATED"

    fun showNotification(context: Context, data: PrayerTimesData) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "بطاقة مواقيت الصلاة المستقلة",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "عرض بطاقة مواقيت الصلاة بشكل منفصل ودائم في مركز الإشعارات"
                setShowBadge(false)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }
            notificationManager.createNotificationChannel(channel)
        }

        val remoteViews = RemoteViews(context.packageName, R.layout.notification_prayer_times)
        remoteViews.setTextViewText(R.id.tv_fajr_time, PrayerCalculator.formatTo12h(data.fajir, com.example.data.model.AppLanguage.ARABIC))
        remoteViews.setTextViewText(R.id.tv_sunrise_time, PrayerCalculator.formatTo12h(data.sunrise, com.example.data.model.AppLanguage.ARABIC))
        remoteViews.setTextViewText(R.id.tv_dhuhr_time, PrayerCalculator.formatTo12h(data.doher, com.example.data.model.AppLanguage.ARABIC))
        remoteViews.setTextViewText(R.id.tv_maghrib_time, PrayerCalculator.formatTo12h(data.maghrib, com.example.data.model.AppLanguage.ARABIC))

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val appIconBitmap = try {
            BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher)
        } catch (e: Exception) {
            null
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_prayer)
            .setLargeIcon(appIconBitmap)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomContentView(remoteViews)
            .setContentIntent(pendingIntent)
            .setAutoCancel(false)
            .setOngoing(true) // Keeps prayer times card permanently visible and protected from accidental dismissal
            .setGroup(GROUP_PRAYER_CARD) // Isolated from general app notification bundle
            .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_ALL)
            .setSortKey("00_PRAYER_CARD_TOP") // Pin to top
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setShowWhen(false)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setColor(0xFF14B8A6.toInt())
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}
