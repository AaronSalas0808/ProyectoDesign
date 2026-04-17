package com.example.proyecto.network

import com.example.proyecto.ui.community.CommunityPost
import com.example.proyecto.ui.discovery.Book
import com.example.proyecto.ui.messages.ChatMessage
import com.example.proyecto.ui.messages.ConversationPreview
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object BookRepository {

    suspend fun getBooks(): List<Book> {
        return RetrofitClient.api.getBooks().data.map { dto ->
            Book(
                id = dto.id,
                title = dto.title,
                author = dto.author,
                year = dto.year.toString(),
                pages = dto.pages.toString(),
                language = dto.language,
                ownerName = dto.owner.name,
                genre = dto.genre,
                color = dto.color,
                ownerInitials = dto.owner.initials
            )
        }
    }

    suspend fun getBookById(id: Int): Book {
        val dto = RetrofitClient.api.getBookById(id)
        return Book(
            id = dto.id,
            title = dto.title,
            author = dto.author,
            year = dto.year.toString(),
            pages = dto.pages.toString(),
            language = dto.language,
            ownerName = dto.owner.name,
            genre = dto.genre,
            color = dto.color,
            synopsis = dto.synopsis,
            ownerInitials = dto.owner.initials,
            maxDays = dto.owner.maxDays
        )
    }

    suspend fun getCommunityPosts(): List<CommunityPost> {
        return RetrofitClient.api.getCommunityPosts().data.map { dto ->
            CommunityPost(
                authorName = dto.name,
                timestamp = dto.time,
                content = "${dto.title}\n\n${dto.body}",
                likeCount = dto.likes,
                commentCount = dto.comments,
                tag = dto.tag
            )
        }
    }

    suspend fun getConversations(): List<ConversationPreview> {
        return RetrofitClient.api.getConversations().data.map { dto ->
            ConversationPreview(
                id = dto.id,
                initials = dto.initials,
                name = dto.name,
                preview = dto.preview,
                time = dto.time,
                unread = dto.unread
            )
        }
    }

    suspend fun getConversationMessages(id: Int): List<ChatMessage> {
        return RetrofitClient.api.getConversationMessages(id).data.map { dto ->
            ChatMessage(
                text = dto.text,
                time = formatTimestamp(dto.timestamp),
                isSent = dto.sender.equals("me", ignoreCase = true)
            )
        }
    }

    suspend fun postConversationMessage(id: Int, text: String): Boolean {
        val response = RetrofitClient.api.postConversationMessage(
            id,
            SendConversationMessageRequestDto(text = text)
        )
        return response.isSuccessful
    }

    suspend fun createBook(body: CreateBookRequestDto): CreateBookResponseDto {
        return RetrofitClient.api.createBook(body)
    }

    suspend fun updateBook(id: Int, body: UpdateBookRequestDto): BasicActionResponseDto {
        return RetrofitClient.api.updateBook(id, body)
    }

    suspend fun deleteBook(id: Int): BasicActionResponseDto {
        return RetrofitClient.api.deleteBook(id)
    }

    private fun formatTimestamp(raw: String): String {
        return try {
            val parsed = OffsetDateTime.parse(raw)
            parsed.format(DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault()))
        } catch (_: Exception) {
            raw
        }
    }
}