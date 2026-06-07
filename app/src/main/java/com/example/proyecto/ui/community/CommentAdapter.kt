package com.example.proyecto.ui.community

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.proyecto.databinding.ItemCommentBinding
import com.example.proyecto.network.CommentApiDto

class CommentAdapter(
    private var comments: List<CommentApiDto>
) : RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    inner class CommentViewHolder(
        val binding: ItemCommentBinding
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CommentViewHolder {
        val binding = ItemCommentBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return CommentViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: CommentViewHolder,
        position: Int
    ) {
        val comment = comments[position]
        val author = comment.author ?: "Usuario"
        val content = comment.content ?: ""

        holder.binding.tvCommentAvatar.text = getInitials(author)
        holder.binding.tvCommentAuthor.text = author
        holder.binding.tvCommentContent.text = content
    }

    override fun getItemCount(): Int = comments.size

    fun updateComments(newComments: List<CommentApiDto>) {
        comments = newComments
        notifyDataSetChanged()
    }

    private fun getInitials(name: String): String {
        val parts = name.trim().split(Regex("\\s+"))

        val first = parts.getOrNull(0)
            ?.firstOrNull()
            ?.toString()
            ?: ""

        val second = parts.getOrNull(1)
            ?.firstOrNull()
            ?.toString()
            ?: ""

        return (first + second).uppercase().ifBlank { "?" }
    }
}