package com.example.proyecto.ui.community

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.proyecto.network.CommentApiDto
import com.example.proyecto.network.CreateCommentRequestDto
import com.example.proyecto.network.CreatePostRequestDto
import com.example.proyecto.network.PostApiDto
import com.example.proyecto.network.RetrofitClient
import com.example.proyecto.session.SessionManager
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.temporal.ChronoUnit

class CommunityViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val PAGE_SIZE = 5
    }

    private val sessionManager = SessionManager(application)

    private val _allPosts = MutableLiveData<List<CommunityPost>>(emptyList())
    private val _filteredPosts = MutableLiveData<List<CommunityPost>>(emptyList())
    val filteredPosts: LiveData<List<CommunityPost>> = _filteredPosts

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val likedPosts = mutableSetOf<Int>()

    private var currentTag: String = "All"
    private var currentPage: Int = 1
    private var totalPages: Int = 1

    init {
        loadPosts()
    }

    fun loadPosts(page: Int = currentPage) {
        currentPage = page

        viewModelScope.launch {
            try {
                val response = RetrofitClient.api.getPosts(currentPage, PAGE_SIZE)

                totalPages = response.totalPages.coerceAtLeast(1)

                val mappedPosts = response.content.map { postDto ->
                    postDto.toCommunityPost()
                }

                _allPosts.value = mappedPosts
                _error.value = null
                applyFilter()

            } catch (e: Exception) {
                _error.value = e.message ?: "Error cargando publicaciones"
                _allPosts.value = emptyList()
                applyFilter()
            }
        }
    }

    fun createPost(
        title: String,
        body: String,
        category: String
    ) {
        val author = sessionManager.getDisplayName()

        val cleanTitle = title.trim()
        val cleanBody = body.trim()
        val cleanCategory = category.trim().ifBlank { "Reviews" }

        if (cleanTitle.isBlank()) {
            _error.value = "Escribe un título"
            return
        }

        if (cleanBody.isBlank()) {
            _error.value = "Escribe el contenido"
            return
        }

        viewModelScope.launch {
            try {
                val response = RetrofitClient.api.createPost(
                    CreatePostRequestDto(
                        author = author,
                        category = cleanCategory,
                        title = cleanTitle,
                        content = cleanBody
                    )
                )

                if (response.isSuccessful) {
                    loadPosts(1)
                } else {
                    _error.value = "No se pudo crear la publicación"
                }

            } catch (e: Exception) {
                _error.value = e.message ?: "Error creando publicación"
            }
        }
    }

    fun deletePost(post: CommunityPost) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.api.deletePost(post.id)

                if (response.isSuccessful || response.code() == 204) {
                    loadPosts(currentPage)
                } else {
                    _error.value = "No se pudo eliminar la publicación"
                }

            } catch (e: Exception) {
                _error.value = e.message ?: "Error eliminando publicación"
            }
        }
    }

    fun toggleLike(post: CommunityPost) {
        val alreadyLiked = likedPosts.contains(post.id)
        val action = if (alreadyLiked) "unlike" else "like"

        val previousPosts = _allPosts.value.orEmpty()

        val optimisticPosts = previousPosts.map { item ->
            if (item.id == post.id) {
                item.copy(
                    isLiked = !alreadyLiked,
                    likeCount = if (alreadyLiked) {
                        (item.likeCount - 1).coerceAtLeast(0)
                    } else {
                        item.likeCount + 1
                    }
                )
            } else {
                item
            }
        }

        _allPosts.value = optimisticPosts

        if (alreadyLiked) {
            likedPosts.remove(post.id)
        } else {
            likedPosts.add(post.id)
        }

        applyFilter()

        viewModelScope.launch {
            try {
                val response = RetrofitClient.api.likePost(post.id, action)

                if (response.isSuccessful) {
                    val updatedPost = response.body()

                    if (updatedPost != null) {
                        _allPosts.value = _allPosts.value.orEmpty().map { item ->
                            if (item.id == post.id) {
                                item.copy(
                                    likeCount = updatedPost.likes ?: item.likeCount,
                                    isLiked = likedPosts.contains(post.id)
                                )
                            } else {
                                item
                            }
                        }

                        applyFilter()
                    }
                } else {
                    _allPosts.value = previousPosts

                    if (alreadyLiked) {
                        likedPosts.add(post.id)
                    } else {
                        likedPosts.remove(post.id)
                    }

                    applyFilter()
                    _error.value = "No se pudo actualizar el like"
                }

            } catch (e: Exception) {
                _allPosts.value = previousPosts

                if (alreadyLiked) {
                    likedPosts.add(post.id)
                } else {
                    likedPosts.remove(post.id)
                }

                applyFilter()
                _error.value = e.message ?: "Error actualizando like"
            }
        }
    }

    fun loadComments(
        postId: Int,
        onResult: (List<CommentApiDto>) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val comments = RetrofitClient.api.getComments(postId)
                onResult(comments)

            } catch (e: Exception) {
                _error.value = e.message ?: "Error cargando comentarios"
                onResult(emptyList())
            }
        }
    }


    fun createComment(
        postId: Int,
        content: String,
        onDone: () -> Unit
    ) {
        val author = sessionManager.getDisplayName()
        val cleanContent = content.trim()

        if (cleanContent.isBlank()) {
            _error.value = "Escribe un comentario"
            return
        }

        viewModelScope.launch {
            try {
                val response = RetrofitClient.api.createComment(
                    postId = postId,
                    body = CreateCommentRequestDto(
                        author = author,
                        content = cleanContent
                    )
                )

                if (response.isSuccessful) {
                    _allPosts.value = _allPosts.value.orEmpty().map { item ->
                        if (item.id == postId) {
                            item.copy(commentCount = item.commentCount + 1)
                        } else {
                            item
                        }
                    }

                    applyFilter()
                    onDone()

                } else {
                    _error.value = "No se pudo enviar el comentario"
                }

            } catch (e: Exception) {
                _error.value = e.message ?: "Error enviando comentario"
            }
        }
    }

    fun filterPosts(tag: String) {
        currentTag = tag
        applyFilter()
    }

    fun nextPage() {
        if (currentPage < totalPages) {
            loadPosts(currentPage + 1)
        }
    }

    fun previousPage() {
        if (currentPage > 1) {
            loadPosts(currentPage - 1)
        }
    }

    private fun applyFilter() {
        val currentPosts = _allPosts.value.orEmpty()

        val filteredPosts = if (currentTag == "All") {
            currentPosts
        } else {
            currentPosts.filter { post ->
                post.tag.equals(currentTag, ignoreCase = true)
            }
        }

        _filteredPosts.value = filteredPosts
    }

    private fun PostApiDto.toCommunityPost(): CommunityPost {
        val currentName = sessionManager.getDisplayName()

        return CommunityPost(
            id = id,
            authorName = author ?: "Usuario",
            timestamp = timeAgo(createdAt),
            title = title.orEmpty(),
            content = content.orEmpty(),
            likeCount = likes ?: 0,
            commentCount = commentsCount ?: 0,
            tag = category ?: "Community",
            isLiked = likedPosts.contains(id),
            isMine = author.equals(currentName, ignoreCase = true)
        )
    }

    private fun timeAgo(iso: String?): String {
        if (iso.isNullOrBlank()) return ""

        return try {
            val fixedIso = if (
                iso.endsWith("Z", ignoreCase = true) ||
                Regex("[+-]\\d{2}:\\d{2}$").containsMatchIn(iso)
            ) {
                iso
            } else {
                "${iso}Z"
            }

            val then = Instant.parse(fixedIso)
            val now = Instant.now()

            val seconds = ChronoUnit.SECONDS.between(then, now).coerceAtLeast(0)

            when {
                seconds < 60 -> "Ahora"
                seconds < 3600 -> "${seconds / 60}m"
                seconds < 86400 -> "${seconds / 3600}h"
                seconds < 2592000 -> "${seconds / 86400}d"
                seconds < 31536000 -> "${seconds / 2592000}mo"
                else -> "${seconds / 31536000}a"
            }

        } catch (e: Exception) {
            ""
        }
    }
}