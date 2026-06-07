package com.example.proyecto.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.proyecto.MainActivity
import com.example.proyecto.R

object NotificationHelper {

    private const val CHANNEL_ID = "bookloop_messages"
    private const val CHANNEL_NAME = "Mensajes"

    fun showMessageNotification(
        context: Context,
        title: String,
        body: String,
        chatId: String = "",
        senderName: String = ""
    ) {
        createChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("openChatId", chatId)
            putExtra("senderName", senderName)
        }

        val requestCode = if (chatId.isNotBlank()) {
            chatId.hashCode()
        } else {
            System.currentTimeMillis().toInt()
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title.ifBlank { "Nuevo mensaje" })
            .setContentText(body.ifBlank { "Tienes un mensaje nuevo" })
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(body.ifBlank { "Tienes un mensaje nuevo" })
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val manager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        manager.notify(requestCode, notification)
    }

    fun showTestNotification(context: Context) {
        showMessageNotification(
            context = context,
            title = "Prueba BookLoop",
            body = "Si ves esto, Android sí puede mostrar notificaciones.",
            chatId = "test"
        )
    }

    private fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones de mensajes de BookLoop"
            }

            val manager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            manager.createNotificationChannel(channel)
        }
    }
}