package com.example.proyecto.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.proyecto.MainActivity
import com.example.proyecto.R
import com.example.proyecto.network.RetrofitClient
import com.example.proyecto.network.UpdateFcmTokenRequestDto
import com.example.proyecto.session.SessionManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BookLoopMessagingService : FirebaseMessagingService() {

    companion object {
        private const val CHANNEL_ID = "bookloop_messages"
        private const val CHANNEL_NAME = "Mensajes"
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        Log.d("FCM_TOKEN", "Nuevo token desde service: $token")

        val sessionManager = SessionManager(this)
        sessionManager.saveFcmToken(token)

        val userId = sessionManager.getUserId()

        if (userId.isNotBlank()) {
            sendTokenToBackend(userId, token)
        } else {
            Log.e("FCM_TOKEN", "onNewToken: No hay userId en sesión")
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        Log.d("FCM_MESSAGE", "Push recibido")
        Log.d("FCM_MESSAGE", "Notification: ${message.notification}")
        Log.d("FCM_MESSAGE", "Data: ${message.data}")

        val title = message.notification?.title
            ?: message.data["title"]
            ?: message.data["senderName"]
            ?: "Nuevo mensaje"

        val body = message.notification?.body
            ?: message.data["body"]
            ?: message.data["message"]
            ?: message.data["content"]
            ?: "Tienes un mensaje nuevo"

        val chatId = message.data["chatId"].orEmpty()

        showNotification(
            title = title,
            body = body,
            chatId = chatId
        )
    }

    private fun showNotification(
        title: String,
        body: String,
        chatId: String
    ) {
        createNotificationChannel()

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("openChatId", chatId)
        }

        val requestCode = if (chatId.isNotBlank()) {
            chatId.hashCode()
        } else {
            System.currentTimeMillis().toInt()
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .build()

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        manager.notify(
            requestCode,
            notification
        )

        Log.d("FCM_MESSAGE", "Notificación mostrada: $title - $body")
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones de mensajes de BookLoop"
            }

            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun sendTokenToBackend(userId: String, token: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.api.updateFcmToken(
                    userId = userId,
                    body = UpdateFcmTokenRequestDto(token = token)
                )

                Log.d(
                    "FCM_TOKEN",
                    "Token enviado desde service. Código: ${response.code()}"
                )
            } catch (e: Exception) {
                Log.e(
                    "FCM_TOKEN",
                    "Error enviando token desde service: ${e.message}"
                )
            }
        }
    }
}