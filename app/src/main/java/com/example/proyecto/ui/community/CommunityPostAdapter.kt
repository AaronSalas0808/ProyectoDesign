package com.example.proyecto.ui.community

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.proyecto.R

class CommunityPostAdapter(
    private var posts: List<CommunityPost>,
    private val onAuthorClick: (CommunityPost) -> Unit = {}
) : RecyclerView.Adapter<CommunityPostAdapter.PostViewHolder>() {

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivPostAvatar: android.widget.ImageView = itemView.findViewById(R.id.ivPostAvatar)
        val tvAuthorName: TextView = itemView.findViewById(R.id.tvPostAuthorName)
        val tvTimestamp: TextView = itemView.findViewById(R.id.tvPostTimestamp)
        val tvContent: TextView = itemView.findViewById(R.id.tvPostContent)
        val tvLikeCount: TextView = itemView.findViewById(R.id.tvLikeCount)
        val tvCommentCount: TextView = itemView.findViewById(R.id.tvCommentCount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_community_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]
        holder.tvAuthorName.text = post.authorName
        holder.tvTimestamp.text = post.timestamp
        holder.tvContent.text = post.content
        holder.tvLikeCount.text = post.likeCount.toString()
        holder.tvCommentCount.text = post.commentCount.toString()
        holder.ivPostAvatar.setOnClickListener { onAuthorClick(post) }
        holder.tvAuthorName.setOnClickListener { onAuthorClick(post) }
    }

    override fun getItemCount() = posts.size

    fun updatePosts(newPosts: List<CommunityPost>) {
        posts = newPosts
        notifyDataSetChanged()
    }
}