package com.example.proyecto.network

import com.example.proyecto.ui.community.CommunityPost
import com.example.proyecto.ui.discovery.Book

object LocalDataStore {
    val localBooks = mutableListOf<Book>()
    val localPosts = mutableListOf<CommunityPost>()
    private var nextBookId = -1

    fun addBook(book: Book): Book {
        val withId = book.copy(id = nextBookId--)
        localBooks.add(0, withId)
        return withId
    }

    fun addPost(post: CommunityPost) {
        localPosts.add(0, post)
    }
}
