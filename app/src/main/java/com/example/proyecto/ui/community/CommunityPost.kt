package com.example.proyecto.ui.community

import android.net.Uri

data class CommunityPost(
    val authorName: String,
    val timestamp: String,
    val content: String,
    val likeCount: Int,
    val commentCount: Int,
    val tag: String,
    val comments: List<String> = emptyList(),
    val isLiked: Boolean = false,
    val imageUri: Uri? = null
)