package com.example.proyecto.ui.community

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto.network.BookRepository
import kotlinx.coroutines.launch

class CommunityViewModel : ViewModel() {

    private val _posts = MutableLiveData<List<CommunityPost>>()
    val posts: LiveData<List<CommunityPost>> = _posts

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    init {
        loadPosts()
    }

    fun loadPosts() {
        viewModelScope.launch {
            try {
                _posts.value = BookRepository.getCommunityPosts()
                _error.value = null
            } catch (e: Exception) {
                Log.e("CommunityViewModel", "Error cargando publicaciones", e)
                _error.value = "${e.javaClass.simpleName}: ${e.message}"
            }
        }
    }
}