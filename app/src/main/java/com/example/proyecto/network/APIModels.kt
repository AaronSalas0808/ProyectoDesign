package com.example.proyecto.network

data class OwnerDto(
    val initials: String,
    val name: String,
    val maxDays: Int? = null
)

data class BookItemDto(
    val id: Int,
    val title: String,
    val author: String,
    val year: Int,
    val pages: Int,
    val language: String,
    val genre: String,
    val color: String,
    val owner: OwnerDto
)

data class BooksResponseDto(
    val data: List<BookItemDto>,
    val total: Int,
    val page: Int
)

data class BookDetailDto(
    val id: Int,
    val title: String,
    val author: String,
    val year: Int,
    val pages: Int,
    val language: String,
    val genre: String,
    val color: String,
    val synopsis: String,
    val owner: OwnerDto
)

data class CommunityPostItemDto(
    val id: Int,
    val initials: String,
    val name: String,
    val time: String,
    val title: String,
    val body: String,
    val likes: Int,
    val comments: Int,
    val tag: String
)

data class CommunityPostsResponseDto(
    val data: List<CommunityPostItemDto>
)

data class CreateBookRequestDto(
    val title: String,
    val author: String,
    val year: Int,
    val pages: Int,
    val language: String,
    val genre: String,
    val color: String
)

data class CreateBookResponseDto(
    val id: Int,
    val title: String,
    val author: String,
    val message: String,
    val status: String
)

data class UpdateBookRequestDto(
    val title: String,
    val author: String
)

data class BasicActionResponseDto(
    val id: Int,
    val message: String,
    val status: String
)