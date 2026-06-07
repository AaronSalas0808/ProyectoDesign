package com.example.proyecto.network

import com.google.gson.annotations.SerializedName

// -------------------- AUTH --------------------

data class LoginRequestDto(
    val email: String,
    val password: String
)

data class RegisterRequestDto(
    val name: String,
    val email: String,
    val university: String,
    val password: String
)

data class UserDto(
    val id: String? = null,
    val name: String? = null,
    val email: String? = null,
    val university: String? = null
)

data class AuthResponseDto(
    val user: UserDto? = null,
    val message: String? = null,
    val status: String? = null
)

data class ErrorResponseDto(
    val message: String? = null,
    val error: String? = null
)

// -------------------- BOOKS --------------------

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

// -------------------- COMMUNITY OLD DTO --------------------

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

// -------------------- COMMUNITY POSTS API --------------------

data class PostsPagedResponseDto(
    val content: List<PostApiDto> = emptyList(),

    @SerializedName("total_pages")
    val totalPages: Int = 1,

    @SerializedName("total_elements")
    val totalElements: Int = 0
)

data class PostApiDto(
    val id: Int,
    val author: String? = null,

    @SerializedName("created_at")
    val createdAt: String? = null,

    val title: String? = null,
    val content: String? = null,
    val category: String? = null,
    val likes: Int? = 0,

    @SerializedName("comments_count")
    val commentsCount: Int? = 0
)

data class CreatePostRequestDto(
    val author: String,
    val category: String,
    val title: String,
    val content: String
)

data class CommentApiDto(
    val id: Int? = null,
    val author: String? = null,
    val content: String? = null,

    @SerializedName("created_at")
    val createdAt: String? = null
)

data class CreateCommentRequestDto(
    val author: String,
    val content: String
)

// -------------------- CONVERSATIONS --------------------

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