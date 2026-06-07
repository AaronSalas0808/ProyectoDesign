package com.example.proyecto.ui.messages

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.proyecto.R

class ChatAdapter(
    private val messages: MutableList<ChatMessage>,
    private val onShareBookClick: () -> Unit = {},
    private val onDeliverBookClick: () -> Unit = {}
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_RECEIVED = 0
        private const val VIEW_SENT = 1
        private const val VIEW_BOOK_SHARED = 2
        private const val VIEW_DELIVER = 3
    }

    override fun getItemViewType(position: Int): Int {
        return when (messages[position].type) {
            ChatMessage.TYPE_BOOK_SHARED -> VIEW_BOOK_SHARED
            ChatMessage.TYPE_DELIVER -> VIEW_DELIVER
            else -> if (messages[position].isSent) VIEW_SENT else VIEW_RECEIVED
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        return when (viewType) {
            VIEW_SENT -> {
                val view = inflater.inflate(R.layout.item_message_sent, parent, false)
                SentViewHolder(
                    view,
                    view.findViewById(R.id.tvMessage),
                    view.findViewById(R.id.tvTime)
                )
            }

            VIEW_BOOK_SHARED -> {
                val view = inflater.inflate(R.layout.item_message_book_shared, parent, false)
                BookSharedViewHolder(
                    view,
                    view.findViewById(R.id.tvTime)
                )
            }

            VIEW_DELIVER -> {
                val view = inflater.inflate(R.layout.item_message_deliver_book, parent, false)
                DeliverViewHolder(
                    view,
                    view.findViewById(R.id.tvTime)
                )
            }

            else -> {
                val view = inflater.inflate(R.layout.item_message_received, parent, false)
                ReceivedViewHolder(
                    view,
                    view.findViewById(R.id.tvMessage),
                    view.findViewById(R.id.tvTime)
                )
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val msg = messages[position]

        when (holder) {
            is SentViewHolder -> {
                holder.tvMessage.text = msg.text
                holder.tvTime.text = msg.time
            }

            is ReceivedViewHolder -> {
                holder.tvMessage.text = msg.text
                holder.tvTime.text = msg.time
            }

            is BookSharedViewHolder -> {
                holder.tvTime.text = msg.time
                holder.card.setOnClickListener {
                    onShareBookClick()
                }
            }

            is DeliverViewHolder -> {
                holder.tvTime.text = msg.time
                holder.card.setOnClickListener {
                    onDeliverBookClick()
                }
            }
        }
    }

    override fun getItemCount(): Int = messages.size

    fun setMessages(newMessages: List<ChatMessage>) {
        messages.clear()
        messages.addAll(newMessages)
        notifyDataSetChanged()
    }

    fun addMessage(message: ChatMessage) {
        messages.add(message)
        notifyItemInserted(messages.size - 1)
    }

    fun removeMessageById(id: String) {
        val index = messages.indexOfFirst { it.id == id }

        if (index >= 0) {
            messages.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    fun replaceMessage(tempId: String, newMessage: ChatMessage) {
        val index = messages.indexOfFirst { it.id == tempId }

        if (index >= 0) {
            messages[index] = newMessage
            notifyItemChanged(index)
        }
    }

    class SentViewHolder(
        itemView: View,
        val tvMessage: TextView,
        val tvTime: TextView
    ) : RecyclerView.ViewHolder(itemView)

    class ReceivedViewHolder(
        itemView: View,
        val tvMessage: TextView,
        val tvTime: TextView
    ) : RecyclerView.ViewHolder(itemView)

    class BookSharedViewHolder(
        itemView: View,
        val tvTime: TextView
    ) : RecyclerView.ViewHolder(itemView) {
        val card: View = itemView.findViewById(R.id.cardShareBook)
    }

    class DeliverViewHolder(
        itemView: View,
        val tvTime: TextView
    ) : RecyclerView.ViewHolder(itemView) {
        val card: View = itemView.findViewById(R.id.cardDeliverBook)
    }
}