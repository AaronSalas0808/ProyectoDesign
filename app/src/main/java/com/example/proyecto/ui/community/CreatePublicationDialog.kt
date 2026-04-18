package com.example.proyecto.ui.community

import android.graphics.RenderEffect
import android.graphics.Shader
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.example.proyecto.databinding.PublicationBinding

class CreatePublicationDialog : DialogFragment() {

    private var _binding: PublicationBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CommunityViewModel by activityViewModels()
    private var selectedImageUri: Uri? = null

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            binding.llImageUpload.visibility = View.GONE
            binding.ivImagePreview.visibility = View.VISIBLE
            binding.ivImagePreview.setImageURI(uri)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = PublicationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnCloseDialog.setOnClickListener { dismiss() }
        binding.btnCancel.setOnClickListener { dismiss() }
        binding.llImageUpload.setOnClickListener { pickImage.launch("image/*") }

        binding.btnPublish.setOnClickListener {
            val text = binding.etPublicationText.text.toString().trim()
            if (text.isNotEmpty()) {
                val post = CommunityPost(
                    authorName = "Yo",
                    timestamp = "Ahora",
                    content = text,
                    likeCount = 0,
                    commentCount = 0,
                    tag = "General",
                    imageUri = selectedImageUri
                )
                viewModel.addPost(post)
                dismiss()
            } else {
                binding.etPublicationText.error = "Escribe algo antes de publicar"
            }
        }
    }

    override fun onStart() {
        super.onStart()

        val displayMetrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)
        val marginPx = (32 * displayMetrics.density).toInt()

        dialog?.window?.let { win ->
            win.setLayout(
                displayMetrics.widthPixels - marginPx * 2,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            win.setBackgroundDrawableResource(android.R.color.transparent)
            win.setDimAmount(0.4f)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requireActivity().window.decorView.post {
                requireActivity().window.decorView.setRenderEffect(
                    RenderEffect.createBlurEffect(18f, 18f, Shader.TileMode.CLAMP)
                )
            }
        }
    }

    override fun onStop() {
        super.onStop()
        clearBlur()
    }

    override fun onDismiss(dialog: android.content.DialogInterface) {
        super.onDismiss(dialog)
        clearBlur()
    }

    private fun clearBlur() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            activity?.window?.decorView?.setRenderEffect(null)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
