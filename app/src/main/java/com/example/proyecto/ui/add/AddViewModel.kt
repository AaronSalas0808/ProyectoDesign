package com.example.proyecto.ui.add

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.proyecto.network.BookRepository
import com.example.proyecto.network.CreateBookRequestDto
import com.example.proyecto.network.LocalDataStore
import com.example.proyecto.session.SessionManager
import com.example.proyecto.ui.discovery.Book
import kotlinx.coroutines.launch

class AddViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionManager = SessionManager(application)

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _success = MutableLiveData<Boolean?>()
    val success: LiveData<Boolean?> = _success

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun publishBook(
        title: String,
        author: String,
        pages: Int,
        language: String,
        genre: String,
        synopsis: String,
        condition: String,
        localImageUri: Uri?
    ) {
        if (_loading.value == true) return

        _loading.value = true

        viewModelScope.launch {
            try {
                val request = CreateBookRequestDto(
                    title = title,
                    author = author,
                    year = 2024,
                    pages = pages,
                    language = language,
                    genre = genre,
                    color = "#9C2F1F"
                )

                val response = BookRepository.createBook(request)

                /*
                 * Si la API responde bien, usamos el ID que devuelva.
                 * Si no devuelve ID, generamos uno temporal local.
                 */
                val createdId = response.id?.toString()
                    ?: "local-${System.currentTimeMillis()}"

                val ownerName = sessionManager.getDisplayName()
                    .ifBlank { "Yo" }

                val ownerInitials = getInitials(ownerName)

                val localBook = Book(
                    id = createdId,
                    title = response.title ?: title,
                    author = response.author ?: author,
                    year = "2024",
                    pages = pages.toString(),
                    language = language,
                    ownerName = ownerName,
                    genre = genre,
                    color = "#9C2F1F",
                    synopsis = synopsis,
                    ownerInitials = ownerInitials,
                    imageUri = localImageUri
                )

                /*
                 * Esto actualiza la app localmente para que el libro aparezca rápido.
                 * Si Discovery carga directo de API, también aparecerá al recargar.
                 */
                LocalDataStore.addBook(localBook)

                _success.value = true
                _error.value = null

            } catch (e: Exception) {
                _error.value = e.message ?: "No se pudo publicar el libro"
            } finally {
                _loading.value = false
            }
        }
    }

    fun clearSuccess() {
        _success.value = null
    }

    fun clearError() {
        _error.value = null
    }

    private fun getInitials(name: String): String {
        val parts = name.trim().split(Regex("\\s+"))

        val first = parts.getOrNull(0)
            ?.firstOrNull()
            ?.toString()
            ?: ""

        val second = parts.getOrNull(1)
            ?.firstOrNull()
            ?.toString()
            ?: ""

        return (first + second).uppercase().ifBlank { "YO" }
    }
}