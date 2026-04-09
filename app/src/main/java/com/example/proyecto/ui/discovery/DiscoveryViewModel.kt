package com.example.proyecto.ui.discovery

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class DiscoveryViewModel : ViewModel() {

    private val _books = MutableLiveData<List<Book>>().apply {
        value = listOf(
            Book("La casa de los espíritus", "Isabel Allende", "1982", "450", "Spanish", "Aaron Salas", 4.9f),
            Book("The Song of Achilles", "Madeline Miller", "2011", "378", "English", "María López", 4.7f),
            Book("Cien años de soledad", "Gabriel García Márquez", "1967", "417", "Spanish", "Carlos Ruiz", 4.8f),
            Book("El amor en los tiempos del cólera", "Gabriel García Márquez", "1985", "348", "Spanish", "Sofía Méndez", 4.6f)
        )
    }
    val books: LiveData<List<Book>> = _books
}
