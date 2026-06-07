package com.example.proyecto.ui.messages

data class ConversationPreview(
    val id: String,
    val otherUserId: String = "",
    val initials: String,
    val name: String,
    val preview: String,
    val time: String,
    val unreadCount: Int = 0
) {
    val unread: Boolean
        get() = unreadCount > 0
}