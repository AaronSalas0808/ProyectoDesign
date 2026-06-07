package com.example.proyecto.ui.profile

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.proyecto.R
import com.example.proyecto.databinding.FragmentEditProfileBinding
import com.example.proyecto.network.BookRepository
import com.example.proyecto.session.SessionManager
import com.example.proyecto.ui.discovery.Book
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch
import java.io.File

class EditProfileFragment : Fragment() {

    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var sessionManager: SessionManager
    private lateinit var adapter: EditBookAdapter
    private val books = mutableListOf<Book>()

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { savePhotoLocally(it) }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)

        sessionManager = SessionManager(requireContext())

        requireActivity()
            .findViewById<BottomNavigationView>(R.id.nav_view)
            ?.visibility = View.GONE

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        adapter = EditBookAdapter(books) { book ->
            confirmDelete(book)
        }

        binding.rvBooks.layoutManager = LinearLayoutManager(requireContext())
        binding.rvBooks.adapter = adapter

        binding.avatarContainer.setOnClickListener {
            pickImage.launch("image/*")
        }

        binding.btnSave.setOnClickListener {
            saveProfile()
        }

        loadProfile()
        loadBooks()

        return binding.root
    }

    private fun loadProfile() {
        if (!sessionManager.isLoggedIn()) {
            (requireActivity() as? com.example.proyecto.MainActivity)?.logoutUser()
            return
        }

        binding.etName.setText(sessionManager.getDisplayName())

        val photoPath = sessionManager.getProfilePhotoPath()
        if (photoPath.isNotBlank()) {
            val file = File(photoPath)
            if (file.exists()) {
                binding.ivProfilePhoto.setImageURI(Uri.fromFile(file))
            }
        }
    }

    private fun loadBooks() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val userName = sessionManager.getDisplayName()
                val userEmail = sessionManager.getUserEmail()

                val allBooks = BookRepository.getBooks()

                val myBooks = allBooks.filter { book ->
                    book.ownerName.equals(userName, ignoreCase = true) ||
                            book.ownerName.equals(userEmail, ignoreCase = true)
                }

                books.clear()
                books.addAll(myBooks)

                adapter.notifyDataSetChanged()
                updateBooksCount()

                binding.tvEmptyBooks.visibility =
                    if (books.isEmpty()) View.VISIBLE else View.GONE

            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "Error cargando libros: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()

                binding.tvEmptyBooks.visibility =
                    if (books.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }

    private fun confirmDelete(book: Book) {
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar libro")
            .setMessage("¿Eliminar \"${book.title}\" de la app?")
            .setPositiveButton("Eliminar") { _, _ ->
                deleteBook(book)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun deleteBook(book: Book) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                BookRepository.deleteBook(book.id)

                adapter.removeBook(book)
                updateBooksCount()

                binding.tvEmptyBooks.visibility =
                    if (books.isEmpty()) View.VISIBLE else View.GONE

                Toast.makeText(
                    requireContext(),
                    "\"${book.title}\" eliminado",
                    Toast.LENGTH_SHORT
                ).show()

            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "Error al eliminar: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun saveProfile() {
        val newName = binding.etName.text.toString().trim()

        if (newName.isBlank()) {
            binding.etName.error = "El nombre no puede estar vacío"
            return
        }

        binding.btnSave.isEnabled = false

        sessionManager.updateName(newName)

        (requireActivity() as? com.example.proyecto.MainActivity)?.refreshDrawerUser()

        Toast.makeText(
            requireContext(),
            "Perfil actualizado",
            Toast.LENGTH_SHORT
        ).show()

        findNavController().popBackStack()
    }

    private fun savePhotoLocally(uri: Uri) {
        try {
            val userKey = when {
                sessionManager.getUserId().isNotBlank() -> sessionManager.getUserId()
                sessionManager.getUserEmail().isNotBlank() -> sessionManager.getUserEmail().substringBefore("@")
                else -> "default_user"
            }

            val dest = File(requireContext().filesDir, "profile_photo_$userKey.jpg")

            requireContext().contentResolver.openInputStream(uri)?.use { input ->
                dest.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            sessionManager.saveProfilePhotoPath(dest.absolutePath)

            binding.ivProfilePhoto.setImageURI(Uri.fromFile(dest))

            (requireActivity() as? com.example.proyecto.MainActivity)?.refreshDrawerUser()

        } catch (e: Exception) {
            Toast.makeText(
                requireContext(),
                "Error al guardar foto",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun updateBooksCount() {
        binding.tvBooksCount.text =
            "${books.size} libro${if (books.size != 1) "s" else ""}"
    }

    override fun onDestroyView() {
        requireActivity()
            .findViewById<BottomNavigationView>(R.id.nav_view)
            ?.visibility = View.VISIBLE

        super.onDestroyView()
        _binding = null
    }
}