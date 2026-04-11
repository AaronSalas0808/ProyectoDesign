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

    init {
        loadPosts()
    }

    private fun loadPosts() {
        viewModelScope.launch {
            try {
                val posts = BookRepository.getCommunityPosts()
                _allPosts.value = posts
                _filteredPosts.value = posts
            } catch (e: Exception) {
                _allPosts.value = emptyList()
                _filteredPosts.value = emptyList()
            }
        }
    }

    fun filterPosts(tag: String) {
        val currentPosts = _allPosts.value.orEmpty()
        _filteredPosts.value =
            if (tag == "All") currentPosts
            else currentPosts.filter { it.tag.equals(tag, ignoreCase = true) }
    }
}