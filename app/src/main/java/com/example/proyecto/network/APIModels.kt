package com.example.proyecto.network

import com.google.gson.annotations.SerializedName

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
    val owner: OwnerDto,
    val images: List<String>? = null,
    @SerializedName("cover_url")
    val coverUrl: String? = null,
    val isbn: String? = null
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
    val owner: OwnerDto,
    val images: List<String>? = null,
    @SerializedName("cover_url")
    val coverUrl: String? = null,
    val isbn: String? = null
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

data class ConversationItemDto(
    val id: Int,
    val initials: String,
    val name: String,
    val preview: String,
    val time: String,
    val unread: Boolean
)

data class ConversationsResponseDto(
    val data: List<ConversationItemDto>
)

data class ConversationMessageDto(
    val sender: String,
    val text: String,
    val timestamp: String
)

data class ConversationMessagesResponseDto(
    val data: List<ConversationMessageDto>
)

data class SendConversationMessageRequestDto(
    val text: String
)