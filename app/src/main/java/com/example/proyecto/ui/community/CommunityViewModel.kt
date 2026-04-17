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
        _filteredPosts.value =
            if (currentTag == "All") currentPosts
            else currentPosts.filter { it.tag.equals(currentTag, ignoreCase = true) }
    }
}