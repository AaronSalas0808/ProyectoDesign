package com.example.proyecto.ui.messages

data class ChatMessage(
    val text: String,
    val time: String,
    val isSent: Boolean,
    val type: Int = TYPE_NORMAL
) {
    companion object {
        const val TYPE_NORMAL = 0
        const val TYPE_BOOK_SHARED = 1
    }
}
