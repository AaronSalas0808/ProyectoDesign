package com.example.proyecto.ui.add

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.example.proyecto.R
import com.example.proyecto.databinding.FragmentAddBinding
import java.io.File

class AddFragment : Fragment() {

    private var _binding: FragmentAddBinding? = null
    private val binding get() = _binding!!

    private var coverPhotoUri: Uri? = null
    private var selectedCondition: String = "Excellent"

    private val takeCoverPhoto = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            binding.ivBookCoverPreview.setImageURI(coverPhotoUri)
            binding.ivBookCoverPreview.visibility = View.VISIBLE
        }
    }

    private val requestCameraPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) launchCamera()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddBinding.inflate(inflater, container, false)

        requireActivity().findViewById<BottomNavigationView>(R.id.nav_view).visibility = View.GONE

        binding.btnClose.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.coverUploadContainer.setOnClickListener {
            checkCameraPermissionAndLaunch()
        }

        setupConditionChips()

        val genres = resources.getStringArray(R.array.book_genres).toMutableList()
        genres.add(0, "Selecciona un género")
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            genres
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        binding.spinnerGenre.adapter = adapter

        return binding.root
    }

    private fun setupConditionChips() {
        val chips = listOf(
            binding.chipExcellent to "Excellent",
            binding.chipGood      to "Good",
            binding.chipFair      to "Fair"
        )
        chips.forEach { (chip, condition) ->
            chip.setOnClickListener {
                selectedCondition = condition
                chips.forEach { (c, _) ->
                    c.setBackgroundResource(R.drawable.bg_condition_unselected)
                }
                chip.setBackgroundResource(R.drawable.bg_condition_selected)
            }
        }
    }

    private fun checkCameraPermissionAndLaunch() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED) {
            launchCamera()
        } else {
            requestCameraPermission.launch(Manifest.permission.CAMERA)
        }
    }

    private fun launchCamera() {
        coverPhotoUri = createTempPhotoUri()
        takeCoverPhoto.launch(coverPhotoUri)
    }

    private fun createTempPhotoUri(): Uri {
        val file = File.createTempFile("photo_", ".jpg", requireContext().cacheDir)
        return FileProvider.getUriForFile(requireContext(), "com.example.proyecto.fileprovider", file)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().findViewById<BottomNavigationView>(R.id.nav_view).visibility = View.VISIBLE
        _binding = null
    }
}
