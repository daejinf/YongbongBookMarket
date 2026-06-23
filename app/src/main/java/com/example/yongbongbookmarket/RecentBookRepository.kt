package com.example.yongbongbookmarket

import android.content.Context

/**
 * 최근 본 책과 마지막으로 본 책을 저장해 두는 저장소.
 *
 * 화면에 다시 보여 줄 때는 저장된 id로 BookRepository에서 책 정보를 찾아온다.
 */
object RecentBookRepository {
    private const val PREFS_NAME = "book_market"
    private const val LAST_BOOK_ID = "last_book_id"
    private const val RECENT_BOOK_IDS = "recent_book_ids"
    private const val SEPARATOR = ","
    private const val MAX_RECENT_COUNT = 8

    fun saveViewedBook(context: Context, bookId: Int) {
        // 잘못 넘어온 id는 저장하지 않는다.
        if (bookId == -1) return

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        // 같은 책을 다시 열면 중복으로 남기지 않고 맨 앞으로 올린다.
        val ids = getRecentBookIds(context)
            .filter { it != bookId }
            .toMutableList()

        ids.add(0, bookId)

        prefs.edit()
            .putInt(LAST_BOOK_ID, bookId)
            .putString(RECENT_BOOK_IDS, ids.take(MAX_RECENT_COUNT).joinToString(SEPARATOR))
            .apply()
    }

    fun getLastBook(context: Context): Book? {
        // 마지막으로 열어 본 책 id를 읽고 실제 Book 객체로 바꾼다.
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return BookRepository.getBook(prefs.getInt(LAST_BOOK_ID, -1))
    }

    fun getRecentBooks(context: Context): List<Book> {
        // 최근 id 목록 중 현재 BookRepository에 남아 있는 책만 화면에 보여 준다.
        return getRecentBookIds(context)
            .mapNotNull { id -> BookRepository.getBook(id) }
    }

    private fun getRecentBookIds(context: Context): List<Int> {
        // "id,id,id" 형태로 저장한 값을 다시 숫자 목록으로 바꾼다.
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedIds = prefs.getString(RECENT_BOOK_IDS, "").orEmpty()
            .split(SEPARATOR)
            .mapNotNull { value -> value.trim().toIntOrNull() }

        val oldLastBookId = prefs.getInt(LAST_BOOK_ID, -1)
        // 예전에는 마지막 책 하나만 저장했기 때문에, 그 값도 최근 목록에 합쳐 준다.
        return if (oldLastBookId != -1 && oldLastBookId !in savedIds) {
            listOf(oldLastBookId) + savedIds
        } else {
            savedIds
        }
    }
}
