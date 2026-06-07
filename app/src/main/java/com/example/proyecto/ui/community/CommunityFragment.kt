package com.example.proyecto.ui.community

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.proyecto.R
import com.example.proyecto.databinding.FragmentCommunityBinding

class CommunityFragment : Fragment() {

    private var _binding: FragmentCommunityBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CommunityViewModel by activityViewModels()
    private lateinit var adapter: CommunityPostAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCommunityBinding.inflate(inflater, container, false)

        adapter = CommunityPostAdapter(
            posts = emptyList(),

            onAuthorClick = { post ->
                val bundle = Bundle().apply {
                    putString("ownerName", post.authorName)
                }

                findNavController().navigate(
                    R.id.action_community_to_profile_owner,
                    bundle
                )
            },

            onLikeClick = { post ->
                viewModel.toggleLike(post)
            },

            onCommentClick = { post ->
                CommentsDialogFragment
                    .newInstance(post)
                    .show(parentFragmentManager, "comments_dialog")
            },

            onDeleteClick = { post ->
                confirmDeletePost(post)
            }
        )

        binding.rvCommunityPosts.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCommunityPosts.adapter = adapter

        viewModel.filteredPosts.observe(viewLifecycleOwner) { posts ->
            adapter.updatePosts(posts)
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (!error.isNullOrBlank()) {
                Toast.makeText(
                    requireContext(),
                    error,
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        binding.ivMenuBurger.setOnClickListener {
            (requireActivity() as com.example.proyecto.MainActivity).openDrawer()
        }

        binding.fabNewPost.setOnClickListener {
            CreatePublicationDialog().show(
                parentFragmentManager,
                "create_post"
            )
        }

        viewModel.loadPosts()

        return binding.root
    }

    private fun confirmDeletePost(post: CommunityPost) {
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar publicación")
            .setMessage("¿Seguro que deseas eliminar esta publicación?")
            .setPositiveButton("Eliminar") { _, _ ->
                viewModel.deletePost(post)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}