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
    private const val CHANNEL_ID = "prayer_daily_times_channel"
    private const val NOTIFICATION_ID = 5005

    fun showNotification(context: Context, data: PrayerTimesData) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "أوقات الصلاة اليومية",
                NotificationManager.IMPORTANCE_LOW // Low priority so it doesn't make sound/popup, just sits in center
            ).apply {
                description = "إشعار يعرض أوقات الصلوات لليوم"
                setShowBadge(false)
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
            .setAutoCancel(false) // stay there or dismiss? Let's make it dismissible but visible
            .setOngoing(true) // User wants it to show up. A sticky notification implies ongoing? "اشعار عند فتح التطبيق يظهر هذا الأشعار في مركز الاشعارات...". Let's make it non-ongoing so they can swipe it if they want.
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setColor(0xFF14B8A6.toInt())
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}
