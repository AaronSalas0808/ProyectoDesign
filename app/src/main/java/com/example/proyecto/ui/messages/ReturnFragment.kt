package com.example.proyecto.ui.messages

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.proyecto.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.io.File

class ReturnFragment : Fragment() {

    private var activePhotoUri: Uri? = null
    private var activePreviewView: ImageView? = null

    private var checkDamage: CheckBox? = null
    private var checkEdition: CheckBox? = null
    private var checkAgree: CheckBox? = null
    private var btnConfirm: View? = null

    private var rating = 0
    private val stars = mutableListOf<TextView>()

    private val takePhoto =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                activePreviewView?.setImageURI(activePhotoUri)
                activePreviewView?.visibility = View.VISIBLE
            }
        }

    private val requestCameraPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                launchCamera()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Permiso de cámara denegado",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_return, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        requireActivity()
            .findViewById<BottomNavigationView>(R.id.nav_view)
            ?.visibility = View.GONE

        view.findViewById<View>(R.id.btnClose).setOnClickListener {
            findNavController().popBackStack()
        }

        btnConfirm = view.findViewById(R.id.btnConfirmReturn)

        checkDamage = findCheckBoxByName(view, "cbDamage")
        checkEdition = findCheckBoxByName(view, "cbEdition")
        checkAgree = findCheckBoxByName(view, "cbAgree")

        val listener = View.OnClickListener {
            updateConfirmButton()
        }

        checkDamage?.setOnClickListener(listener)
        checkEdition?.setOnClickListener(listener)
        checkAgree?.setOnClickListener(listener)

        setupStars(view)
        updateConfirmButton()

        btnConfirm?.setOnClickListener {
            if (!allChecked()) {
                Toast.makeText(
                    requireContext(),
                    "Debes confirmar todas las condiciones",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            findNavController().previousBackStackEntry
                ?.savedStateHandle
                ?.set("bookReturned", true)

            findNavController().popBackStack()
        }

        setupCameraSlot(view, R.id.slotFrontCover, R.id.ivFrontCoverPreview)
        setupCameraSlot(view, R.id.slotBackCover, R.id.ivBackCoverPreview)
        setupCameraSlot(view, R.id.slotSpine, R.id.ivSpinePreview)

        setupOptionalCameraSlot(view, "slotInterior", "ivInteriorPreview")
    }

    private fun setupCameraSlot(view: View, slotId: Int, previewId: Int) {
        val preview = view.findViewById<ImageView>(previewId)

        view.findViewById<View>(slotId).setOnClickListener {
            activePreviewView = preview
            checkCameraPermissionAndLaunch()
        }
    }

    private fun setupOptionalCameraSlot(view: View, slotName: String, previewName: String) {
        val slotId = resources.getIdentifier(slotName, "id", requireContext().packageName)
        val previewId = resources.getIdentifier(previewName, "id", requireContext().packageName)

        if (slotId != 0 && previewId != 0) {
            setupCameraSlot(view, slotId, previewId)
        }
    }

    private fun setupStars(view: View) {
        stars.clear()

        val ids = listOf("star1", "star2", "star3", "star4", "star5")

        ids.forEachIndexed { index, name ->
            val id = resources.getIdentifier(name, "id", requireContext().packageName)

            if (id != 0) {
                val star = view.findViewById<TextView>(id)
                stars.add(star)

                star.setOnClickListener {
                    rating = index + 1
                    paintStars()
                }
            }
        }

        paintStars()
    }

    private fun paintStars() {
        stars.forEachIndexed { index, star ->
            star.text = "★"
            star.setTextColor(
                if (index < rating) {
                    0xFFFFB300.toInt()
                } else {
                    0xFFD8C8B3.toInt()
                }
            )
        }
    }

    private fun checkCameraPermissionAndLaunch() {
        if (
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
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

        return FileProvider.getUriForFile(
            requireContext(),
            "com.example.proyecto.fileprovider",
            file
        )
    }

    private fun allChecked(): Boolean {
        val damage = checkDamage?.isChecked ?: true
        val edition = checkEdition?.isChecked ?: true
        val agree = checkAgree?.isChecked ?: true

        return damage && edition && agree
    }

    private fun updateConfirmButton() {
        btnConfirm?.isEnabled = allChecked()
        btnConfirm?.alpha = if (allChecked()) 1f else 0.5f
    }

    private fun findCheckBoxByName(view: View, idName: String): CheckBox? {
        val id = resources.getIdentifier(idName, "id", requireContext().packageName)

        return if (id != 0) {
            view.findViewById(id)
        } else {
            null
        }
    }

    override fun onDestroyView() {
        requireActivity()
            .findViewById<BottomNavigationView>(R.id.nav_view)
            ?.visibility = View.VISIBLE

        super.onDestroyView()
    }
}