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

    @GET("users/{id}")
    suspend fun getUserById(
        @Path("id") id: String
    ): UserResponseDto

    @POST("users/{userId}/fcm-token")
    suspend fun updateFcmToken(
        @Path("userId") userId: String,
        @Body body: UpdateFcmTokenRequestDto
    ): Response<Unit>

    // -------------------- BOOKS --------------------

    @GET("books")
    suspend fun getBooks(
        @Query("size") size: Int = 100
    ): BooksResponseDto

    @GET("books/{id}")
    suspend fun getBookById(
        @Path("id") id: String
    ): BookDetailDto

    @POST("books")
    suspend fun createBook(
        @Body body: CreateBookRequestDto
    ): CreateBookResponseDto

    @PUT("books/{id}")
    suspend fun updateBook(
        @Path("id") id: String,
        @Body body: UpdateBookRequestDto
    ): BasicActionResponseDto

    @DELETE("books/{id}")
    suspend fun deleteBook(
        @Path("id") id: String
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

    // -------------------- CONVERSATIONS OLD --------------------

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

    // -------------------- CHATS API --------------------

    @GET("chats/user/{userId}")
    suspend fun getUserChats(
        @Path("userId") userId: String,
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Query("sort") sort: String = "createdAt,desc"
    ): ChatPagedResponseDto

    @POST("chats/user/{userId}")
    suspend fun openOrCreateChat(
        @Path("userId") userId: String,
        @Body body: CreateChatRequestDto
    ): Response<ChatDto>

    @GET("chats/{chatId}/messages")
    suspend fun getChatMessages(
        @Path("chatId") chatId: String,
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Query("sort") sort: String = "sentAt,asc"
    ): MessagePagedResponseDto

    @POST("chats/{chatId}/messages")
    suspend fun sendChatMessage(
        @Path("chatId") chatId: String,
        @Body body: SendChatMessageRequestDto
    ): Response<MessageApiDto>

    @POST("chats/{chatId}/messages/read")
    suspend fun markChatAsRead(
        @Path("chatId") chatId: String,
        @Query("readerId") readerId: String
    ): Response<Unit>
}