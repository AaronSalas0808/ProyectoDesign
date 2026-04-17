package com.example.proyecto.ui.profile

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.proyecto.databinding.ItemOwnerBookBinding
import com.example.proyecto.ui.discovery.Book

class OwnerBookAdapter(
    private val books: List<Book>,
    private val onClick: (Book) -> Unit
) : RecyclerView.Adapter<OwnerBookAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemOwnerBookBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemOwnerBookBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val book = books[position]
        holder.binding.tvBookTitle.text = book.title
        holder.binding.tvBookAuthor.text = book.author
        try {
            holder.binding.viewBookColor.setBackgroundColor(Color.parseColor(book.color))
        } catch (_: Exception) { }
        holder.binding.root.setOnClickListener { onClick(book) }
    }

    override fun getItemCount() = books.size
}
