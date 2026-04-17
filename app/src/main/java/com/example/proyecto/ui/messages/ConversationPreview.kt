package com.example.proyecto.ui.messages

data class ConversationPreview(
    val id: Int,
    val initials: String,
    val name: String,
    val preview: String,
    val time: String,
    val unread: Boolean
)