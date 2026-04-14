package com.example.proyecto.ui.messages

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.proyecto.R

class ChatAdapter(private val messages: List<ChatMessage>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_RECEIVED = 0
        private const val TYPE_SENT     = 1
    }

    override fun getItemViewType(position: Int) =
        if (messages[position].isSent) TYPE_SENT else TYPE_RECEIVED

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_SENT) {
            val view = inflater.inflate(R.layout.item_message_sent, parent, false)
            SentViewHolder(view.findViewById(R.id.tvMessage), view.findViewById(R.id.tvTime))
        } else {
            val view = inflater.inflate(R.layout.item_message_received, parent, false)
            ReceivedViewHolder(view.findViewById(R.id.tvMessage), view.findViewById(R.id.tvTime))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val msg = messages[position]
        when (holder) {
            is SentViewHolder     -> { holder.tvMessage.text = msg.text; holder.tvTime.text = msg.time }
            is ReceivedViewHolder -> { holder.tvMessage.text = msg.text; holder.tvTime.text = msg.time }
        }
    }

    override fun getItemCount() = messages.size

    class SentViewHolder(val tvMessage: TextView, val tvTime: TextView) :
        RecyclerView.ViewHolder(tvMessage.rootView)

    class ReceivedViewHolder(val tvMessage: TextView, val tvTime: TextView) :
        RecyclerView.ViewHolder(tvMessage.rootView)
}
