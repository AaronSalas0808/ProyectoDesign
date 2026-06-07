package com.example.proyecto.network

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    // -------------------- AUTH --------------------

    @POST("users/login")
    suspend fun loginUser(
        @Body body: LoginRequestDto
    ): Response<ResponseBody>

    @POST("users/register")
    suspend fun registerUser(
        @Body body: RegisterRequestDto
    ): Response<ResponseBody>

    // -------------------- BOOKS --------------------

    @GET("books")
    suspend fun getBooks(): BooksResponseDto

    @GET("books/{id}")
    suspend fun getBookById(
        @Path("id") id: Int
    ): BookDetailDto

    @POST("books")
    suspend fun createBook(
        @Body body: CreateBookRequestDto
    ): CreateBookResponseDto

    @PUT("books/{id}")
    suspend fun updateBook(
        @Path("id") id: Int,
        @Body body: UpdateBookRequestDto
    ): BasicActionResponseDto

    @DELETE("books/{id}")
    suspend fun deleteBook(
        @Path("id") id: Int
    ): BasicActionResponseDto

    // -------------------- COMMUNITY OLD --------------------

    @GET("community/posts")
    suspend fun getCommunityPosts(): CommunityPostsResponseDto

    // -------------------- COMMUNITY POSTS API --------------------

    @GET("posts/{page}/{pageSize}")
    suspend fun getPosts(
        @Path("page") page: Int,
        @Path("pageSize") pageSize: Int
    ): PostsPagedResponseDto

    @POST("posts")
    suspend fun createPost(
        @Body body: CreatePostRequestDto
    ): Response<PostApiDto>

    @DELETE("posts/{postId}")
    suspend fun deletePost(
        @Path("postId") postId: Int
    ): Response<Unit>

    @POST("posts/{postId}/like")
    suspend fun likePost(
        @Path("postId") postId: Int,
        @Query("action") action: String
    ): Response<PostApiDto>

    @GET("posts/{postId}/comments")
    suspend fun getComments(
        @Path("postId") postId: Int
    ): List<CommentApiDto>

    @POST("posts/{postId}/comments")
    suspend fun createComment(
        @Path("postId") postId: Int,
        @Body body: CreateCommentRequestDto
    ): Response<CommentApiDto>

    // -------------------- CONVERSATIONS --------------------

    @GET("conversations")
    suspend fun getConversations(): ConversationsResponseDto

    @GET("conversations/{id}/messages")
    suspend fun getConversationMessages(
        @Path("id") id: Int
    ): ConversationMessagesResponseDto

    @POST("conversations/{id}/messages")
    suspend fun postConversationMessage(
        @Path("id") id: Int,
        @Body body: SendConversationMessageRequestDto
    ): Response<Unit>
}