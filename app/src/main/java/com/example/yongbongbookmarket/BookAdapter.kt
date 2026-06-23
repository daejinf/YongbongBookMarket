package com.example.yongbongbookmarket

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.yongbongbookmarket.databinding.ItemBookBinding

/**
 * item_book.xml 한 장을 잡아 두는 ViewHolder.
 */
class BookViewHolder(val binding: ItemBookBinding) : RecyclerView.ViewHolder(binding.root)

/**
 * Book 데이터를 카드 목록으로 바꿔 주는 RecyclerView Adapter.
 * 화면 이동은 Activity가 맡고, Adapter는 클릭된 책만 알려 준다.
 */
class BookAdapter(
    private val books: MutableList<Book>,
    private val onBookClick: (Book) -> Unit
) : RecyclerView.Adapter<BookViewHolder>() {

    override fun getItemCount(): Int {
        // RecyclerView는 이 개수만큼 onBindViewHolder를 호출해서 카드를 그린다.
        return books.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        // XML로 만든 item_book 레이아웃을 실제 View 객체로 바꾸는 단계다.
        val binding = ItemBookBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BookViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        // position은 현재 화면에 그려질 카드의 순서다.
        val book = books[position]
        val binding = holder.binding
        val context = binding.root.context

        // Book 객체의 값을 카드 안의 TextView와 ImageView에 하나씩 꽂아 넣는다.
        binding.bookCover.setImageResource(book.imageResId)
        binding.bookTitle.text = book.title
        binding.bookAuthor.text = context.getString(R.string.book_author_format, book.author)
        binding.bookMajor.text = book.major
        binding.bookOriginalPrice.text = context.getString(R.string.book_original_price_format, book.originalPrice)
        binding.bookPrice.text = context.getString(R.string.book_price_format, book.price)
        binding.bookCondition.text = book.condition
        binding.root.setOnClickListener {
            // 상세 화면으로 이동하는 일은 Activity가 맡도록 클릭된 책만 넘긴다.
            onBookClick(book)
        }
    }

    fun updateBooks(filteredBooks: List<Book>) {
        // 검색이나 필터 결과가 바뀌면 기존 목록을 지우고 새 목록으로 다시 그린다.
        books.clear()
        books.addAll(filteredBooks)
        notifyDataSetChanged()
    }
}
