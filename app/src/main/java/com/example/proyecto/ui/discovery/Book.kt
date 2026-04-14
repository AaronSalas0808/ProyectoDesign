package com.example.proyecto.ui.discovery

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
    val maxDays: Int? = null
)