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
import com.example.proyecto.databinding.FragmentEditProfileBinding
import com.example.proyecto.network.BookRepository
import com.example.proyecto.ui.discovery.Book
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.proyecto.R
import kotlinx.coroutines.launch
import java.io.File

class EditProfileFragment : Fragment() {

    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: EditBookAdapter
    private val books = mutableListOf<Book>()

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { savePhotoLocally(it) }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        requireActivity().findViewById<BottomNavigationView>(R.id.nav_view)?.visibility = View.GONE

        binding.btnBack.setOnClickListener { findNavController().popBackStack() }

        adapter = EditBookAdapter(books) { book -> confirmDelete(book) }
        binding.rvBooks.layoutManager = LinearLayoutManager(requireContext())
        binding.rvBooks.adapter = adapter

        binding.avatarContainer.setOnClickListener { pickImage.launch("image/*") }

        binding.btnSave.setOnClickListener { saveProfile() }

        loadProfile()
        loadBooks()

        return binding.root
    }

    private fun loadProfile() {
        val uid = auth.currentUser?.uid ?: return

        val photoPath = requireContext().getSharedPreferences("profile", 0)
            .getString("photo_path_$uid", null)
        if (photoPath != null) {
            val file = File(photoPath)
            if (file.exists()) binding.ivProfilePhoto.setImageURI(Uri.fromFile(file))
        }

        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                val name = doc.getString("name").orEmpty().ifBlank {
                    auth.currentUser?.displayName.orEmpty()
                }
                binding.etName.setText(name)
            }
    }

    private fun loadBooks() {
        val uid = auth.currentUser?.uid ?: return
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val userName = getUserName(uid)
                val allBooks = BookRepository.getBooks()
                val myBooks = allBooks.filter { it.ownerName.equals(userName, ignoreCase = true) }
                books.clear()
                books.addAll(myBooks)
                adapter.notifyDataSetChanged()
                updateBooksCount()
                binding.tvEmptyBooks.visibility = if (books.isEmpty()) View.VISIBLE else View.GONE
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error cargando libros: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private suspend fun getUserName(uid: String): String {
        return kotlinx.coroutines.suspendCancellableCoroutine { cont ->
            db.collection("users").document(uid).get()
                .addOnSuccessListener { doc ->
                    val name = doc.getString("name").orEmpty().ifBlank {
                        auth.currentUser?.displayName.orEmpty()
                    }
                    cont.resume(name) {}
                }
                .addOnFailureListener {
                    cont.resume(auth.currentUser?.displayName.orEmpty()) {}
                }
        }
    }

    private fun confirmDelete(book: Book) {
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar libro")
            .setMessage("¿Eliminar \"${book.title}\" de la app?")
            .setPositiveButton("Eliminar") { _, _ -> deleteBook(book) }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun deleteBook(book: Book) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                BookRepository.deleteBook(book.id)
                adapter.removeBook(book)
                updateBooksCount()
                binding.tvEmptyBooks.visibility = if (books.isEmpty()) View.VISIBLE else View.GONE
                Toast.makeText(requireContext(), "\"${book.title}\" eliminado", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error al eliminar: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveProfile() {
        val uid = auth.currentUser?.uid ?: return
        val newName = binding.etName.text.toString().trim()
        if (newName.isBlank()) {
            binding.etName.error = "El nombre no puede estar vacío"
            return
        }

        binding.btnSave.isEnabled = false
        db.collection("users").document(uid)
            .update("name", newName)
            .addOnSuccessListener {
                (requireActivity() as? com.example.proyecto.MainActivity)?.refreshDrawerUser()
                Toast.makeText(requireContext(), "Perfil actualizado", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
            .addOnFailureListener { e ->
                binding.btnSave.isEnabled = true
                Toast.makeText(requireContext(), "Error al guardar: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun savePhotoLocally(uri: Uri) {
        val uid = auth.currentUser?.uid ?: return
        try {
            val dest = File(requireContext().filesDir, "profile_photo_$uid.jpg")
            requireContext().contentResolver.openInputStream(uri)?.use { input ->
                dest.outputStream().use { output -> input.copyTo(output) }
            }
            requireContext().getSharedPreferences("profile", 0)
                .edit().putString("photo_path_$uid", dest.absolutePath).apply()
            binding.ivProfilePhoto.setImageURI(Uri.fromFile(dest))
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error al guardar foto", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateBooksCount() {
        binding.tvBooksCount.text = "${books.size} libro${if (books.size != 1) "s" else ""}"
    }

    override fun onDestroyView() {
        requireActivity().findViewById<BottomNavigationView>(R.id.nav_view)?.visibility = View.VISIBLE
        super.onDestroyView()
        _binding = null
    }
}
