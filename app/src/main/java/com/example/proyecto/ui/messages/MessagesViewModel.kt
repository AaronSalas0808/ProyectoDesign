package com.example.proyecto.ui.messages

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto.network.BookRepository
import kotlinx.coroutines.launch

class MessagesViewModel : ViewModel() {

    private val _conversations = MutableLiveData<List<ConversationPreview>>(emptyList())
    val conversations: LiveData<List<ConversationPreview>> = _conversations

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    init {
        loadConversations()
    }

    fun loadConversations() {
        viewModelScope.launch {
            try {
                _conversations.value = BookRepository.getConversations()
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message ?: "Error cargando conversaciones"
            }
        }
    }
}