package com.example.proyecto.ui.discovery

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto.network.BookRepository
import com.example.proyecto.network.LocalDataStore
import kotlinx.coroutines.launch

class DiscoveryViewModel : ViewModel() {

    private val _books = MutableLiveData<List<Book>>(emptyList())
    val books: LiveData<List<Book>> = _books

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    init {
        loadBooks()
    }

    fun loadBooks() {
        viewModelScope.launch {
            _loading.value = true

            try {
                val apiBooks = BookRepository.getBooks()

                val mergedBooks = (LocalDataStore.localBooks + apiBooks)
                    .distinctBy { it.id }

                _books.value = mergedBooks
                _error.value = null

            } catch (e: Exception) {
                android.util.Log.e(
                    "DiscoveryViewModel",
                    "Error loading books: ${e.message}",
                    e
                )

                _books.value = LocalDataStore.localBooks
                _error.value = e.message ?: "Error desconocido"

            } finally {
                _loading.value = false
            }
        }
    }
}