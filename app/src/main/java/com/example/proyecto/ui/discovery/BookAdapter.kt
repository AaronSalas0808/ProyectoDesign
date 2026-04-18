package com.example.proyecto.ui.discovery

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.proyecto.R
import com.example.proyecto.databinding.BookCardBinding

class BookAdapter(
    private val books: List<Book>,
    private val onBookClick: (Book) -> Unit = {},
    private val onOwnerClick: (Book) -> Unit = {}
) : RecyclerView.Adapter<BookAdapter.BookViewHolder>() {

    class BookViewHolder(val binding: BookCardBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val binding = BookCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return BookViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        val book = books[position]

        with(holder.binding) {
            tvBookTitle.text = book.title
            tvBookAuthor.text = book.author
            tvOwnerName.text = book.ownerName

            val imageSource: Any = book.imageUri
                ?: book.getBestRemoteImageUrl()
                ?: R.drawable.placeholder_book_cover

            ivBookCover.load(imageSource) {
                crossfade(true)
                placeholder(R.drawable.placeholder_book_cover)
                error(R.drawable.placeholder_book_cover)
            }

            btnViewBook.setOnClickListener { onBookClick(book) }
            ivOwnerProfile.setOnClickListener { onOwnerClick(book) }
            tvOwnerName.setOnClickListener { onOwnerClick(book) }
            root.setOnClickListener { onBookClick(book) }
        }
    }

    override fun getItemCount() = books.size
}