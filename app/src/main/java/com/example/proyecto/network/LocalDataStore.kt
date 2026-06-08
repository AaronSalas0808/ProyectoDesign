package com.example.proyecto.network

import com.example.proyecto.ui.community.CommunityPost
import com.example.proyecto.ui.discovery.Book

object LocalDataStore {

    val localBooks = mutableListOf<Book>()
    val localPosts = mutableListOf<CommunityPost>()

    private var nextBookId = -1

    fun addBook(book: Book): Book {
        val safeId = if (book.id.isNotBlank()) {
            book.id
        } else {
            "local-${kotlin.math.abs(nextBookId)}"
        }

        nextBookId--

        val withId = book.copy(id = safeId)
        localBooks.add(0, withId)

        return withId
    }

    fun removeBookById(id: String) {
        localBooks.removeAll { it.id == id }
    }

    fun addPost(post: CommunityPost) {
        localPosts.add(0, post)
    }
}