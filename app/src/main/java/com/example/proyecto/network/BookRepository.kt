package com.example.proyecto.network

import com.example.proyecto.ui.community.CommunityPost
import com.example.proyecto.ui.discovery.Book

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