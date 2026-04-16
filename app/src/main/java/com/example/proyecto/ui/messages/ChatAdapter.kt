package com.example.proyecto.ui.messages

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.proyecto.R

class ChatAdapter(private val messages: List<ChatMessage>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_RECEIVED    = 0
        private const val TYPE_SENT        = 1
        private const val TYPE_BOOK_SHARED = 2
    }

    override fun getItemViewType(position: Int): Int {
        val msg = messages[position]
        return when {
            msg.type == ChatMessage.TYPE_BOOK_SHARED -> TYPE_BOOK_SHARED
            msg.isSent -> TYPE_SENT
            else -> TYPE_RECEIVED
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_SENT -> {
                val view = inflater.inflate(R.layout.item_message_sent, parent, false)
                SentViewHolder(view.findViewById(R.id.tvMessage), view.findViewById(R.id.tvTime))
            }
            TYPE_BOOK_SHARED -> {
                val view = inflater.inflate(R.layout.item_message_book_shared, parent, false)
                BookSharedViewHolder(view.findViewById(R.id.tvTime))
            }
            else -> {
                val view = inflater.inflate(R.layout.item_message_received, parent, false)
                ReceivedViewHolder(view.findViewById(R.id.tvMessage), view.findViewById(R.id.tvTime))
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val msg = messages[position]
        when (holder) {
            is SentViewHolder      -> { holder.tvMessage.text = msg.text; holder.tvTime.text = msg.time }
            is ReceivedViewHolder  -> { holder.tvMessage.text = msg.text; holder.tvTime.text = msg.time }
            is BookSharedViewHolder -> { holder.tvTime.text = msg.time }
        }
    }

    override fun getItemCount() = messages.size

    class SentViewHolder(val tvMessage: TextView, val tvTime: TextView) :
        RecyclerView.ViewHolder(tvMessage.rootView)

    class ReceivedViewHolder(val tvMessage: TextView, val tvTime: TextView) :
        RecyclerView.ViewHolder(tvMessage.rootView)

    class BookSharedViewHolder(val tvTime: TextView) :
        RecyclerView.ViewHolder(tvTime.rootView)
}
