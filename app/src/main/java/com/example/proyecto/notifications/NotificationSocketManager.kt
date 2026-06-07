package com.example.proyecto.notifications

import android.content.Context
import android.util.Log
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject

object NotificationSocketManager {

    private const val TAG = "NOTIF_SOCKET"

    private const val NOTIF_URL =
        "https://ms-notifi-e8gpahhfhwb0h4gf.canadacentral-01.azurewebsites.net"

    private var socket: Socket? = null
    private var currentUserId: String = ""

    fun start(
        context: Context,
        userId: String
    ) {
        if (userId.isBlank()) {
            Log.e(TAG, "No se puede iniciar socket: userId vacío")
            return
        }

        val appContext = context.applicationContext

        if (socket != null && currentUserId == userId) {
            Log.d(TAG, "Socket ya estaba iniciado para $userId")
            return
        }

        stop()

        currentUserId = userId

        try {
            val options = IO.Options().apply {
                transports = arrayOf("websocket", "polling")
                reconnection = true
                reconnectionAttempts = Int.MAX_VALUE
                reconnectionDelay = 1000
                forceNew = true
            }

            socket = IO.socket(NOTIF_URL, options)

            socket?.on(Socket.EVENT_CONNECT) {
                Log.d(TAG, "Socket conectado")
                socket?.emit("register", userId)
                Log.d(TAG, "Emit register: $userId")
            }

            socket?.on(Socket.EVENT_DISCONNECT) { args ->
                Log.d(TAG, "Socket desconectado: ${args.joinToString()}")
            }

            socket?.on(Socket.EVENT_CONNECT_ERROR) { args ->
                Log.e(TAG, "Error conectando socket: ${args.joinToString()}")
            }

            socket?.on("notification") { args ->
                Log.d(TAG, "Evento notification recibido: ${args.joinToString()}")

                if (args.isEmpty()) return@on

                val notifJson = when (val first = args[0]) {
                    is JSONObject -> first
                    is String -> JSONObject(first)
                    else -> JSONObject(first.toString())
                }

                handleNotification(appContext, notifJson)
            }

            socket?.connect()

        } catch (e: Exception) {
            Log.e(TAG, "Error iniciando socket: ${e.message}")
        }
    }

    fun stop() {
        try {
            socket?.off()
            socket?.disconnect()
            socket?.close()
        } catch (_: Exception) {
        } finally {
            socket = null
            currentUserId = ""
        }
    }

    fun isConnected(): Boolean {
        return socket?.connected() == true
    }

    private fun handleNotification(
        context: Context,
        notif: JSONObject
    ) {
        try {
            val title = notif.optString("title", "Nuevo mensaje")
            val body = notif.optString("body", "Tienes un mensaje nuevo")

            val data = notif.optJSONObject("data")

            val chatId = data?.optString("chatId").orEmpty()
            val senderName = data?.optString("senderName").orEmpty()

            NotificationHelper.showMessageNotification(
                context = context,
                title = if (senderName.isNotBlank()) senderName else title,
                body = body,
                chatId = chatId,
                senderName = senderName
            )

            Log.d(
                TAG,
                "Notificación Android mostrada. title=$title body=$body chatId=$chatId"
            )

        } catch (e: Exception) {
            Log.e(TAG, "Error mostrando notificación: ${e.message}")
        }
    }
}