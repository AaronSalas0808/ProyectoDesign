package com.example.proyecto.ui.profile

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.proyecto.databinding.ItemEditBookBinding
import com.example.proyecto.ui.discovery.Book

class EditBookAdapter(
    private val books: MutableList<Book>,
    private val onDelete: (Book) -> Unit
) : RecyclerView.Adapter<EditBookAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemEditBookBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemEditBookBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val book = books[position]
        holder.binding.tvBookTitle.text = book.title
        holder.binding.tvBookAuthor.text = book.author
        try {
            holder.binding.viewBookColor.setBackgroundColor(Color.parseColor(book.color))
        } catch (_: Exception) { }
        holder.binding.btnDeleteBook.setOnClickListener { onDelete(book) }
    }

    override fun getItemCount() = books.size

    fun removeBook(book: Book) {
        val idx = books.indexOf(book)
        if (idx >= 0) {
            books.removeAt(idx)
            notifyItemRemoved(idx)
        }
    }
}
