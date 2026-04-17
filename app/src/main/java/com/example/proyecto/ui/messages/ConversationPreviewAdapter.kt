package com.example.proyecto.ui.messages

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.proyecto.R

class ConversationPreviewAdapter(
    private var items: List<ConversationPreview>,
    private val onClick: (ConversationPreview) -> Unit
) : RecyclerView.Adapter<ConversationPreviewAdapter.ConversationViewHolder>() {

    inner class ConversationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvChatName: TextView = itemView.findViewById(R.id.tvChatName)
        val tvChatLastMessage: TextView = itemView.findViewById(R.id.tvChatLastMessage)
        val tvChatTimestamp: TextView = itemView.findViewById(R.id.tvChatTimestamp)
        val viewUnreadBadge: View = itemView.findViewById(R.id.viewUnreadBadge)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_preview, parent, false)
        return ConversationViewHolder(view)
    }

    override fun onBindViewHolder(holder: ConversationViewHolder, position: Int) {
        val item = items[position]

        holder.tvChatName.text = item.name
        holder.tvChatLastMessage.text = item.preview
        holder.tvChatTimestamp.text = item.time
        holder.viewUnreadBadge.visibility = if (item.unread) View.VISIBLE else View.GONE

        holder.itemView.setOnClickListener { onClick(item) }
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<ConversationPreview>) {
        items = newItems
        notifyDataSetChanged()
    }
}