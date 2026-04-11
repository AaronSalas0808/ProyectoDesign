package com.example.proyecto.network

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {

    @GET("books")
    suspend fun getBooks(): BooksResponseDto

    @GET("books/{id}")
    suspend fun getBookById(@Path("id") id: Int): BookDetailDto

    @POST("books")
    suspend fun createBook(@Body body: CreateBookRequestDto): CreateBookResponseDto

    @PUT("books/{id}")
    suspend fun updateBook(
        @Path("id") id: Int,
        @Body body: UpdateBookRequestDto
    ): BasicActionResponseDto

    @DELETE("books/{id}")
    suspend fun deleteBook(@Path("id") id: Int): BasicActionResponseDto

    @GET("community/posts")
    suspend fun getCommunityPosts(): CommunityPostsResponseDto
}