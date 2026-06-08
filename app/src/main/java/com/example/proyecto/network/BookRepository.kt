package com.example.proyecto.network

import android.util.Log
import com.example.proyecto.ui.community.CommunityPost
import com.example.proyecto.ui.discovery.Book
import com.example.proyecto.ui.messages.ConversationPreview

object BookRepository {

    private const val TAG = "BookRepository"

    suspend fun getBooks(): List<Book> {
        val response = RetrofitClient.api.getBooks(size = 100)

        val rawBooks = response.content
            ?: response.data
            ?: emptyList()

        return rawBooks.mapNotNull { dto ->
            val id = dto.id?.trim()
            if (id.isNullOrBlank()) return@mapNotNull null

            dto.toBook(id)
        }
    }

    suspend fun getBookById(id: String): Book {
        val dto = RetrofitClient.api.getBookById(id)
        return dto.toBook(dto.id?.trim().orEmpty().ifBlank { id })
    }

    private suspend fun BookItemDto.toBook(id: String): Book {
        val ownerId = uploadedBy ?: uploadedBySnake
        val ownerProfile = fetchOwnerProfile(ownerId)

        val ownerName = ownerProfile?.name
            ?: ownerProfile?.fullName
            ?: ownerProfile?.displayName
            ?: ownerProfile?.username
            ?: owner?.name
            ?: "Unknown"

        val ownerInitials = ownerProfile?.initials
            ?: owner?.initials
            ?: if (ownerName != "Unknown") getInitials(ownerName) else "??"

        val imageList = buildImageList(
            image = image,
            images = images,
            coverUrl = coverUrl,
            coverUrlSnake = coverUrlSnake,
            isbn = isbn
        )

        Log.d(
            TAG,
            "Book=$title uploadedBy=$ownerId owner=$ownerName image=${imageList.firstOrNull()}"
        )

        return Book(
            id = id,
            title = title.orEmpty().ifBlank { "Untitled" },
            author = author.orEmpty().ifBlank { "Unknown" },
            year = (year ?: publishYear)?.toString().orEmpty(),
            pages = (pages ?: pageCount)?.toString().orEmpty(),
            language = language.orEmpty(),
            ownerName = ownerName,
            genre = genre.orEmpty(),
            color = color.orEmpty().ifBlank { "#7A3728" },
            synopsis = synopsis ?: description ?: "",
            ownerInitials = ownerInitials,
            maxDays = loanDays ?: owner?.maxDays ?: 14,
            images = imageList,
            coverUrl = imageList.firstOrNull(),
            isbn = isbn
        )
    }

    private suspend fun BookDetailDto.toBook(id: String): Book {
        val ownerId = uploadedBy ?: uploadedBySnake
        val ownerProfile = fetchOwnerProfile(ownerId)

        val ownerName = ownerProfile?.name
            ?: ownerProfile?.fullName
            ?: ownerProfile?.displayName
            ?: ownerProfile?.username
            ?: owner?.name
            ?: "Unknown"

        val ownerInitials = ownerProfile?.initials
            ?: owner?.initials
            ?: if (ownerName != "Unknown") getInitials(ownerName) else "??"

        val imageList = buildImageList(
            image = image,
            images = images,
            coverUrl = coverUrl,
            coverUrlSnake = coverUrlSnake,
            isbn = isbn
        )

        Log.d(
            TAG,
            "Detail=$title uploadedBy=$ownerId owner=$ownerName image=${imageList.firstOrNull()}"
        )

        return Book(
            id = id,
            title = title.orEmpty().ifBlank { "Untitled" },
            author = author.orEmpty().ifBlank { "Unknown" },
            year = (year ?: publishYear)?.toString().orEmpty(),
            pages = (pages ?: pageCount)?.toString().orEmpty(),
            language = language.orEmpty(),
            ownerName = ownerName,
            genre = genre.orEmpty(),
            color = color.orEmpty().ifBlank { "#7A3728" },
            synopsis = synopsis ?: description ?: "",
            ownerInitials = ownerInitials,
            maxDays = loanDays ?: owner?.maxDays ?: 14,
            images = imageList,
            coverUrl = imageList.firstOrNull(),
            isbn = isbn
        )
    }

    private suspend fun fetchOwnerProfile(userId: String?): UserDto? {
        val cleanId = userId?.trim().orEmpty()

        if (cleanId.isBlank() || cleanId == "Invitado") {
            return null
        }

        return try {
            RetrofitClient.api.getUserById(cleanId).toUserDto()
        } catch (e: Exception) {
            Log.e(TAG, "Error cargando usuario $cleanId: ${e.message}")
            null
        }
    }

    private fun buildImageList(
        image: String?,
        images: List<String>?,
        coverUrl: String?,
        coverUrlSnake: String?,
        isbn: String?
    ): List<String> {
        val urls = mutableListOf<String>()

        if (!image.isNullOrBlank() && image.startsWith("http", ignoreCase = true)) {
            urls.add(image)
        }

        images
            ?.filter { it.isNotBlank() && it.startsWith("http", ignoreCase = true) }
            ?.forEach { urls.add(it) }

        if (!coverUrlSnake.isNullOrBlank() && coverUrlSnake.startsWith("http", ignoreCase = true)) {
            urls.add(coverUrlSnake)
        }

        if (!coverUrl.isNullOrBlank() && coverUrl.startsWith("http", ignoreCase = true)) {
            urls.add(coverUrl)
        }

        if (!isbn.isNullOrBlank()) {
            urls.add("https://covers.openlibrary.org/b/isbn/${isbn.trim()}-M.jpg")
        }

        return urls
            .map { it.trim().replace("\\/", "/") }
            .distinct()
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

    suspend fun updateBook(
        id: String,
        body: UpdateBookRequestDto
    ): BasicActionResponseDto {
        return RetrofitClient.api.updateBook(id, body)
    }

    suspend fun deleteBook(id: String): BasicActionResponseDto {
        return RetrofitClient.api.deleteBook(id)
    }

    private fun getInitials(name: String): String {
        val parts = name.trim().split(Regex("\\s+"))

        val first = parts.getOrNull(0)
            ?.firstOrNull()
            ?.toString()
            ?: ""

        val second = parts.getOrNull(1)
            ?.firstOrNull()
            ?.toString()
            ?: ""

        return (first + second).uppercase().ifBlank { "??" }
    }
}