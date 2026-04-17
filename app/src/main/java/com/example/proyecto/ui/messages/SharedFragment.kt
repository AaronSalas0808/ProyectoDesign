package com.example.proyecto.ui.messages

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.proyecto.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.io.File

class SharedFragment : Fragment() {

    private var activePhotoUri: Uri? = null
    private var activePreviewView: ImageView? = null

    private val takePhoto = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            activePreviewView?.setImageURI(activePhotoUri)
            activePreviewView?.visibility = View.VISIBLE
        }
    }

    private val requestCameraPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) launchCamera()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_shared, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().findViewById<BottomNavigationView>(R.id.nav_view)?.visibility = View.GONE

        view.findViewById<View>(R.id.btnClose).setOnClickListener {
            findNavController().popBackStack()
        }

        view.findViewById<View>(R.id.btnConfirmHandoff).setOnClickListener {
            findNavController().previousBackStackEntry
                ?.savedStateHandle
                ?.set("bookShared", true)
            findNavController().popBackStack()
        }

        setupCameraSlot(view, R.id.slotFrontCover, R.id.ivFrontCoverPreview)
        setupCameraSlot(view, R.id.slotBackCover, R.id.ivBackCoverPreview)
        setupCameraSlot(view, R.id.slotSpine, R.id.ivSpinePreview)
    }

    private fun setupCameraSlot(view: View, slotId: Int, previewId: Int) {
        val preview = view.findViewById<ImageView>(previewId)
        view.findViewById<View>(slotId).setOnClickListener {
            activePreviewView = preview
            checkCameraPermissionAndLaunch()
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
        activePhotoUri = createTempPhotoUri()
        takePhoto.launch(activePhotoUri)
    }

    private fun createTempPhotoUri(): Uri {
        val file = File.createTempFile("photo_", ".jpg", requireContext().cacheDir)
        return FileProvider.getUriForFile(requireContext(), "com.example.proyecto.fileprovider", file)
    }

    override fun onDestroyView() {
        requireActivity().findViewById<BottomNavigationView>(R.id.nav_view)?.visibility = View.GONE
        super.onDestroyView()
    }
}
