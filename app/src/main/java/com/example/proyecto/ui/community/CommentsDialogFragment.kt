package com.example.proyecto.ui.community

import android.app.Dialog
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.proyecto.databinding.DialogCommentsBinding

class CommentsDialogFragment : DialogFragment() {

    private var _binding: DialogCommentsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CommunityViewModel by activityViewModels()

    private lateinit var adapter: CommentAdapter

    private var postId: Int = 0
    private var postAuthor: String = ""
    private var postTitle: String = ""
    private var postContent: String = ""

    companion object {
        private const val ARG_POST_ID = "post_id"
        private const val ARG_POST_AUTHOR = "post_author"
        private const val ARG_POST_TITLE = "post_title"
        private const val ARG_POST_CONTENT = "post_content"

        fun newInstance(post: CommunityPost): CommentsDialogFragment {
            return CommentsDialogFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_POST_ID, post.id)
                    putString(ARG_POST_AUTHOR, post.authorName)
                    putString(ARG_POST_TITLE, post.title)
                    putString(ARG_POST_CONTENT, post.content)
                }
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogCommentsBinding.inflate(LayoutInflater.from(requireContext()))

        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(binding.root)

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        readArgs()
        setupUi()
        loadComments()

        return dialog
    }

    override fun onStart() {
        super.onStart()

        val displayMetrics = DisplayMetrics()

        @Suppress("DEPRECATION")
        requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)

        val width = (displayMetrics.widthPixels * 0.92).toInt()

        dialog?.window?.setLayout(
            width,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    private fun readArgs() {
        postId = arguments?.getInt(ARG_POST_ID) ?: 0
        postAuthor = arguments?.getString(ARG_POST_AUTHOR).orEmpty()
        postTitle = arguments?.getString(ARG_POST_TITLE).orEmpty()
        postContent = arguments?.getString(ARG_POST_CONTENT).orEmpty()
    }

    private fun setupUi() {
        adapter = CommentAdapter(emptyList())

        binding.rvComments.layoutManager = LinearLayoutManager(requireContext())
        binding.rvComments.adapter = adapter

        binding.tvCommentsSubtitle.text = if (postAuthor.isNotBlank()) {
            "Publicación de $postAuthor"
        } else {
            "Publicación"
        }

        binding.tvPostPreview.text = when {
            postTitle.isNotBlank() -> postTitle
            postContent.isNotBlank() -> postContent.take(90)
            else -> "Publicación"
        }

        binding.btnCloseComments.setOnClickListener {
            dismiss()
        }

        binding.btnCancelComment.setOnClickListener {
            dismiss()
        }

        binding.btnSendComment.setOnClickListener {
            sendComment()
        }
    }

    private fun loadComments() {
        binding.tvCommentsEmpty.visibility = android.view.View.VISIBLE
        binding.tvCommentsEmpty.text = "Cargando comentarios..."

        viewModel.loadComments(postId) { comments ->
            if (_binding == null) return@loadComments

            adapter.updateComments(comments)

            if (comments.isEmpty()) {
                binding.tvCommentsEmpty.visibility = android.view.View.VISIBLE
                binding.tvCommentsEmpty.text = "Todavía no hay comentarios.\nSé la primera persona en comentar."
            } else {
                binding.tvCommentsEmpty.visibility = android.view.View.GONE
            }
        }
    }

    private fun sendComment() {
        val text = binding.etComment.text.toString().trim()

        if (text.isBlank()) {
            binding.etComment.error = "Escribe un comentario"
            return
        }

        binding.btnSendComment.isEnabled = false
        binding.btnSendComment.text = "Enviando..."

        viewModel.createComment(postId, text) {
            if (_binding == null) return@createComment

            binding.etComment.setText("")
            binding.btnSendComment.isEnabled = true
            binding.btnSendComment.text = "Enviar"

            Toast.makeText(
                requireContext(),
                "Comentario enviado",
                Toast.LENGTH_SHORT
            ).show()

            loadComments()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}