package com.example.proyecto.ui.community

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.proyecto.databinding.FragmentCommunityBinding

class CommunityFragment : Fragment() {

    private var _binding: FragmentCommunityBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCommunityBinding.inflate(inflater, container, false)

        val samplePosts = listOf(
            CommunityPost(
                authorName = "Melany Arrieta",
                timestamp = "2 hours ago",
                content = "Just finished reading this amazing book. The atmosphere is just unmatched! Has anyone else found themselves romanticizing their library hours because of this book?",
                likeCount = 24,
                commentCount = 8
            ),
            CommunityPost(
                authorName = "Carlos Mendez",
                timestamp = "5 hours ago",
                content = "Looking for someone to swap 'The Alchemist' for 'Sapiens'. Both in excellent condition. DM me if interested!",
                likeCount = 15,
                commentCount = 3
            ),
            CommunityPost(
                authorName = "Sofia Vargas",
                timestamp = "1 day ago",
                content = "Just listed 3 new books available for loan: Dune, 1984, and Brave New World. Check my profile if you want to borrow any of them!",
                likeCount = 42,
                commentCount = 12
            ),
            CommunityPost(
                authorName = "Luis Torres",
                timestamp = "2 days ago",
                content = "Book club meeting this Friday at 6pm. We'll be discussing 'One Hundred Years of Solitude'. Everyone is welcome!",
                likeCount = 31,
                commentCount = 19
            )
        )

        binding.rvCommunityPosts.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCommunityPosts.adapter = CommunityPostAdapter(samplePosts)

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
