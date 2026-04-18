package com.example.proyecto.ui.discovery

import android.net.Uri

data class Book(
    val id: Int,
    val title: String,
    val author: String,
    val year: String,
    val pages: String,
    val language: String,
    val ownerName: String,
    val genre: String = "",
    val color: String = "",
    val synopsis: String = "",
    val ownerInitials: String = "",
    val maxDays: Int? = null,
    val imageUri: Uri? = null,
    val images: List<String> = emptyList(),
    val coverUrl: String? = null,
    val isbn: String? = null
) {
    fun getBestRemoteImageUrl(): String? {
        val firstImage = images.firstOrNull { it.isNotBlank() }?.trim()
        if (!firstImage.isNullOrEmpty()) return firstImage

        if (!coverUrl.isNullOrBlank()) return coverUrl.trim()

        if (!isbn.isNullOrBlank()) {
            return "https://covers.openlibrary.org/b/isbn/${isbn.trim()}-M.jpg?default=false"
        }

        return null
    }
}