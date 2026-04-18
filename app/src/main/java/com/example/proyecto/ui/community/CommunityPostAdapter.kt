package com.example.proyecto.ui.community

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.widget.ImageViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.proyecto.R

class CommunityPostAdapter(
    private var posts: List<CommunityPost>,
    private val onAuthorClick: (CommunityPost) -> Unit = {},
    private val onLikeClick: (CommunityPost) -> Unit = {},
    private val onCommentClick: (CommunityPost) -> Unit = {}
) : RecyclerView.Adapter<CommunityPostAdapter.PostViewHolder>() {

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivPostAvatar: ImageView = itemView.findViewById(R.id.ivPostAvatar)
        val tvAuthorName: TextView = itemView.findViewById(R.id.tvPostAuthorName)
        val tvTimestamp: TextView = itemView.findViewById(R.id.tvPostTimestamp)
        val tvContent: TextView = itemView.findViewById(R.id.tvPostContent)
        val tvLikeCount: TextView = itemView.findViewById(R.id.tvLikeCount)
        val tvCommentCount: TextView = itemView.findViewById(R.id.tvCommentCount)
        val ivLikeIcon: ImageView = itemView.findViewById(R.id.ivLikeIcon)
        val ivCommentIcon: ImageView = itemView.findViewById(R.id.ivCommentIcon)
        val ivPostImage: ImageView = itemView.findViewById(R.id.ivPostImage)
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

        if (post.imageUri != null) {
            holder.ivPostImage.visibility = View.VISIBLE
            holder.ivPostImage.setImageURI(post.imageUri)
        } else {
            holder.ivPostImage.visibility = View.GONE
        }

        holder.ivPostAvatar.setOnClickListener { onAuthorClick(post) }
        holder.tvAuthorName.setOnClickListener { onAuthorClick(post) }

        val likeColor = if (post.isLiked) "#E53935" else "#888888"
        ImageViewCompat.setImageTintList(
            holder.ivLikeIcon,
            ColorStateList.valueOf(Color.parseColor(likeColor))
        )

        holder.ivLikeIcon.setOnClickListener { onLikeClick(post) }
        holder.tvLikeCount.setOnClickListener { onLikeClick(post) }

        holder.ivCommentIcon.setOnClickListener { onCommentClick(post) }
        holder.tvCommentCount.setOnClickListener { onCommentClick(post) }
    }

    override fun getItemCount() = posts.size

    fun updatePosts(newPosts: List<CommunityPost>) {
        posts = newPosts
        notifyDataSetChanged()
    }
}