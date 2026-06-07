package com.example.proyecto.network

import com.example.proyecto.ui.community.CommunityPost
import com.example.proyecto.ui.discovery.Book
import com.example.proyecto.ui.messages.ConversationPreview

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
                ownerInitials = dto.owner.initials,
                images = dto.images ?: emptyList(),
                coverUrl = dto.coverUrl,
                isbn = dto.isbn
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
            maxDays = dto.owner.maxDays,
            images = dto.images ?: emptyList(),
            coverUrl = dto.coverUrl,
            isbn = dto.isbn
        )
    }

    suspend fun getCommunityPosts(): List<CommunityPost> {
        val response = RetrofitClient.api.getCommunityPosts()

        return response.data.map { item ->
            CommunityPost(
                id = item.id,
                authorName = item.name,
                timestamp = item.time,
                title = item.title,
                content = item.body,
                likeCount = item.likes,
                commentCount = item.comments,
                tag = item.tag,
                comments = emptyList(),
                isLiked = false,
                isMine = false,
                imageUri = null
            )
        }
    }

    suspend fun getConversations(): List<ConversationPreview> {
        return RetrofitClient.api.getConversations().data.map { dto ->
            ConversationPreview(
                id = dto.id.toString(),
                initials = dto.initials,
                name = dto.name,
                preview = dto.preview,
                time = dto.time,
                unreadCount = if (dto.unread) 1 else 0
            )
        }
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
}