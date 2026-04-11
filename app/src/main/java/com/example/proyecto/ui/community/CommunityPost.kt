package com.example.proyecto.ui.community

data class CommunityPost(
    val authorName: String,
    val timestamp: String,
    val content: String,
    val likeCount: Int,
    val commentCount: Int
)