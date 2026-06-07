package com.example.proyecto.ui.messages

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.proyecto.network.ChatDto
import com.example.proyecto.network.RetrofitClient
import com.example.proyecto.session.SessionManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class MessagesViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val CHATS_PAGE_SIZE = 15
    }

    private val sessionManager = SessionManager(application)

    private val _conversations = MutableLiveData<List<ConversationPreview>>(emptyList())
    val conversations: LiveData<List<ConversationPreview>> = _conversations

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    var currentPage: Int = 0
        private set

    var totalPages: Int = 1
        private set

    init {
        loadConversations()
    }

    fun loadConversations(page: Int = 0) {
        val myId = getMyId()

        if (myId.isBlank()) {
            _error.value = "No se encontró el id del usuario en sesión"
            return
        }

        viewModelScope.launch {
            try {
                val response = RetrofitClient.api.getUserChats(
                    userId = myId,
                    page = page,
                    size = CHATS_PAGE_SIZE
                )

                currentPage = response.page ?: page
                totalPages = response.totalPages ?: 1

                _conversations.value = response.content.map {
                    it.toConversationPreview(myId)
                }

                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message ?: "Error cargando conversaciones"
                _conversations.value = emptyList()
            }
        }
    }

    fun markAsRead(chatId: String) {
        val myId = getMyId()

        if (myId.isBlank()) return

        _conversations.value = _conversations.value.orEmpty().map {
            if (it.id == chatId) it.copy(unreadCount = 0) else it
        }

        viewModelScope.launch {
            try {
                RetrofitClient.api.markChatAsRead(
                    chatId = chatId,
                    readerId = myId
                )
            } catch (_: Exception) {
            }
        }
    }

    private fun getMyId(): String {
        val id = sessionManager.getUserId()
        if (id.isNotBlank()) return id

        return sessionManager.getUserEmail()
    }

    private fun ChatDto.toConversationPreview(myId: String): ConversationPreview {
        val other = participants
            ?.firstOrNull { it.userId != myId }
            ?: participants?.firstOrNull()

        val name = other?.name ?: "Usuario"

        return ConversationPreview(
            id = id,
            otherUserId = other?.userId ?: "",
            initials = other?.initials ?: getInitials(name),
            name = name,
            preview = lastMessage?.content ?: "Sin mensajes aún",
            time = formatTime(lastMessage?.sentAt),
            unreadCount = unreadCount ?: 0
        )
    }

    private fun getInitials(name: String): String {
        val parts = name.trim().split(Regex("\\s+"))

        val first = parts.getOrNull(0)
            ?.firstOrNull()
            ?.toString()
            ?: ""

        val second = parts.getOrNull(1)
            ?.firstOrNull()
            ?.toString()
            ?: ""

        return (first + second).uppercase().ifBlank { "??" }
    }

    private fun formatTime(iso: String?): String {
        if (iso.isNullOrBlank()) return ""

        return try {
            val fixedIso = if (
                iso.endsWith("Z", ignoreCase = true) ||
                Regex("[+-]\\d{2}:\\d{2}$").containsMatchIn(iso)
            ) {
                iso
            } else {
                "${iso}Z"
            }

            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX", Locale.getDefault())
            parser.timeZone = TimeZone.getTimeZone("UTC")

            val date = try {
                parser.parse(fixedIso)
            } catch (_: Exception) {
                val parser2 = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX", Locale.getDefault())
                parser2.timeZone = TimeZone.getTimeZone("UTC")
                parser2.parse(fixedIso)
            }

            val out = SimpleDateFormat("h:mm a", Locale.getDefault())

            if (date != null) {
                out.format(date)
            } else {
                ""
            }
        } catch (_: Exception) {
            ""
        }
    }
}