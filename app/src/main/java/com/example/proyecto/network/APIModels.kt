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
    val uid: String? = null,
    val userId: String? = null,
    val name: String? = null,
    val fullName: String? = null,
    val displayName: String? = null,
    val username: String? = null,
    val email: String? = null,
    val university: String? = null,
    val initials: String? = null,
    val rating: Double? = null
)

data class UserResponseDto(
    val user: UserDto? = null,
    val data: UserDto? = null,
    val content: UserDto? = null,

    val id: String? = null,
    val uid: String? = null,
    val userId: String? = null,
    val name: String? = null,
    val fullName: String? = null,
    val displayName: String? = null,
    val username: String? = null,
    val email: String? = null,
    val university: String? = null,
    val initials: String? = null,
    val rating: Double? = null
) {
    fun toUserDto(): UserDto {
        return user ?: data ?: content ?: UserDto(
            id = id,
            uid = uid,
            userId = userId,
            name = name,
            fullName = fullName,
            displayName = displayName,
            username = username,
            email = email,
            university = university,
            initials = initials,
            rating = rating
        )
    }
}

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
    val initials: String? = null,
    val name: String? = null,
    val maxDays: Int? = null,
    val rating: Double? = null
)

data class BooksResponseDto(
    val data: List<BookItemDto>? = null,
    val content: List<BookItemDto>? = null,
    val total: Int? = null,
    val page: Int? = null,
    val totalPages: Int? = null,
    val totalElements: Int? = null
)

data class BookItemDto(
    val id: String? = null,
    val title: String? = null,
    val author: String? = null,
    val year: Int? = null,
    val publishYear: Int? = null,
    val pages: Int? = null,
    val pageCount: Int? = null,
    val language: String? = null,
    val genre: String? = null,
    val color: String? = null,
    val condition: String? = null,
    val synopsis: String? = null,
    val description: String? = null,

    val uploadedBy: String? = null,

    @SerializedName("uploaded_by")
    val uploadedBySnake: String? = null,

    val owner: OwnerDto? = null,

    val image: String? = null,
    val images: List<String>? = null,

    @SerializedName("cover_url")
    val coverUrlSnake: String? = null,

    val coverUrl: String? = null,
    val isbn: String? = null,
    val loanDays: Int? = null
)

data class BookDetailDto(
    val id: String? = null,
    val title: String? = null,
    val author: String? = null,
    val year: Int? = null,
    val publishYear: Int? = null,
    val pages: Int? = null,
    val pageCount: Int? = null,
    val language: String? = null,
    val genre: String? = null,
    val color: String? = null,
    val condition: String? = null,
    val synopsis: String? = null,
    val description: String? = null,

    val uploadedBy: String? = null,

    @SerializedName("uploaded_by")
    val uploadedBySnake: String? = null,

    val owner: OwnerDto? = null,

    val image: String? = null,
    val images: List<String>? = null,

    @SerializedName("cover_url")
    val coverUrlSnake: String? = null,

    val coverUrl: String? = null,
    val isbn: String? = null,
    val loanDays: Int? = null
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
    val id: String? = null,
    val title: String? = null,
    val author: String? = null,
    val message: String? = null,
    val status: String? = null
)

data class UpdateBookRequestDto(
    val title: String,
    val author: String
)

data class BasicActionResponseDto(
    val id: String? = null,
    val message: String? = null,
    val status: String? = null
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

// -------------------- CONVERSATIONS OLD --------------------

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

// -------------------- CHATS API --------------------

data class ChatPagedResponseDto(
    val content: List<ChatDto> = emptyList(),
    val page: Int? = 0,
    val totalPages: Int? = 1
)

data class ChatDto(
    val id: String,
    val participants: List<ChatParticipantDto>? = emptyList(),
    val lastMessage: ChatLastMessageDto? = null,
    val unreadCount: Int? = 0
)

data class ChatParticipantDto(
    val userId: String? = null,
    val name: String? = null,
    val initials: String? = null
)

data class ChatLastMessageDto(
    val content: String? = null,
    val sentAt: String? = null
)

data class CreateChatRequestDto(
    val name1: String,
    val initials1: String,
    val userId2: String,
    val name2: String,
    val initials2: String
)

data class MessagePagedResponseDto(
    val content: List<MessageApiDto> = emptyList(),
    val page: Int? = 0,
    val totalPages: Int? = 1
)

data class MessageApiDto(
    val id: String? = null,
    val senderId: String? = null,
    val content: String? = null,
    val sentAt: String? = null
)

data class SendChatMessageRequestDto(
    val senderId: String,
    val content: String
)

data class UpdateFcmTokenRequestDto(
    val token: String
)