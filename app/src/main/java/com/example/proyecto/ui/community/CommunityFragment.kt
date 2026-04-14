package com.example.proyecto.ui.community

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.proyecto.R
import com.example.proyecto.databinding.FragmentCommunityBinding

class CommunityFragment : Fragment() {

    private var _binding: FragmentCommunityBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CommunityViewModel by viewModels()
    private lateinit var adapter: CommunityPostAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCommunityBinding.inflate(inflater, container, false)

        adapter = CommunityPostAdapter(emptyList()) { post ->
            val bundle = android.os.Bundle().apply {
                putString("ownerName", post.authorName)
            }
            findNavController().navigate(R.id.action_community_to_profile_owner, bundle)
        }

        binding.rvCommunityPosts.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCommunityPosts.adapter = adapter

        viewModel.filteredPosts.observe(viewLifecycleOwner) { posts ->
            adapter.updatePosts(posts)
        }

        binding.ivMenuBurger.setOnClickListener {
            (requireActivity() as com.example.proyecto.MainActivity).openDrawer()
        }

        setupFilters()

        return binding.root
    }

    private fun setupFilters() {
        binding.chipAll.setOnClickListener { viewModel.filterPosts("All") }
        binding.chipReviews.setOnClickListener { viewModel.filterPosts("Reviews") }
        binding.chipRecommendations.setOnClickListener { viewModel.filterPosts("Recommendations") }
        binding.chipAuthors.setOnClickListener { viewModel.filterPosts("Authors") }
        binding.chipCommunity.setOnClickListener { viewModel.filterPosts("Community") }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}