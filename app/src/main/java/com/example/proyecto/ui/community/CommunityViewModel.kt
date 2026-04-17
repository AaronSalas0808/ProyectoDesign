package com.example.proyecto.ui.community

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto.network.BookRepository
import kotlinx.coroutines.launch

class CommunityViewModel : ViewModel() {

    private val _allPosts = MutableLiveData<List<CommunityPost>>(emptyList())
    private val _filteredPosts = MutableLiveData<List<CommunityPost>>(emptyList())
    val filteredPosts: LiveData<List<CommunityPost>> = _filteredPosts

    private var currentTag: String = "All"

    init {
        loadPosts()
    }

    private fun loadPosts() {
        viewModelScope.launch {
            try {
                val posts = BookRepository.getCommunityPosts()
                _allPosts.value = posts
                applyFilter()
            } catch (e: Exception) {
                android.util.Log.e("CommunityViewModel", "Error loading posts: ${e.message}", e)
            }
        }
    }

    fun filterPosts(tag: String) {
        currentTag = tag
        applyFilter()
    }

    fun toggleLike(post: CommunityPost) {
        val updatedPosts = _allPosts.value.orEmpty().map {
            if (it.authorName == post.authorName &&
                it.timestamp == post.timestamp &&
                it.content == post.content
            ) {
                if (it.isLiked) {
                    it.copy(
                        isLiked = false,
                        likeCount = (it.likeCount - 1).coerceAtLeast(0)
                    )
                } else {
                    it.copy(
                        isLiked = true,
                        likeCount = it.likeCount + 1
                    )
                }
            } else {
                it
            }
        }

        _allPosts.value = updatedPosts
        applyFilter()
    }

    private fun applyFilter() {
        val currentPosts = _allPosts.value.orEmpty()
        val filtered = if (currentTag == "All") currentPosts
            else currentPosts.filter { it.tag.equals(currentTag, ignoreCase = true) }
        _filteredPosts.value = filtered.sortedBy { parseRelativeTime(it.timestamp) }
    }

    private fun parseRelativeTime(time: String): Long {
        val lower = time.lowercase().trim()
        val number = Regex("\\d+").find(lower)?.value?.toLongOrNull() ?: 1L
        return when {
            Regex("\\d+\\s*m(in|inuto|inute)?\\b").containsMatchIn(lower) -> number * 60
            Regex("\\d+\\s*h(r|our|ora)?\\b").containsMatchIn(lower) -> number * 3600
            Regex("\\d+\\s*d(ay|ía|ia)?\\b").containsMatchIn(lower) -> number * 86400
            Regex("\\d+\\s*w(k|eek|semana)?\\b").containsMatchIn(lower) -> number * 604800
            Regex("\\d+\\s*mo(nth|s)?\\b").containsMatchIn(lower) || "mes" in lower -> number * 2592000
            Regex("\\d+\\s*y(r|ear|año)?\\b").containsMatchIn(lower) || "año" in lower -> number * 31536000
            else -> 0L
        }
    }
}