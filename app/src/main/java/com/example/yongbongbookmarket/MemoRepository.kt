package com.example.yongbongbookmarket

import android.content.Context

/**
 * 메모 한 개를 표현하는 값.
 * bookId가 -1이면 특정 책에 묶이지 않은 일반 메모로 본다.
 */
data class ReadingMemo(
    val bookId: Int,
    val bookTitle: String,
    val text: String
) {
    val titleLabel: String
        get() = if (bookId == -1 || bookTitle.isBlank()) "일반 메모" else bookTitle
}

/**
 * 일반 메모와 책별 메모를 SharedPreferences에 저장한다.
 *
 * DB를 쓰지 않는 대신 여러 메모를 하나의 문자열로 묶어서 보관한다.
 */
object MemoRepository {
    private const val PREFS_NAME = "book_market"
    private const val MEMO_LIST = "feed_memo_list"
    private const val OLD_MEMO = "feed_memo"
    private const val SEPARATOR = "\u001E"
    private const val FIELD_SEPARATOR = "\u001F"
    private const val NO_BOOK_ID = -1

    fun getMemos(context: Context): MutableList<String> {
        // 예전 코드와 호환되도록 문자열 목록만 필요한 곳에서 쓰는 함수다.
        return getMemoItems(context).map { it.text }.toMutableList()
    }

    fun getMemoItems(context: Context): MutableList<ReadingMemo> {
        // 저장된 문자열을 읽어서 화면에서 쓰기 좋은 ReadingMemo 목록으로 바꾼다.
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val saved = prefs.getString(MEMO_LIST, "") ?: ""
        val memos = saved.split(SEPARATOR)
            .mapNotNull { parseMemo(it) }
            .toMutableList()

        val oldMemo = prefs.getString(OLD_MEMO, "")?.trim().orEmpty()
        if (oldMemo.isNotEmpty() && memos.none { it.text == oldMemo }) {
            // 예전 저장값이 남아 있으면 새 목록 구조로 한 번만 옮긴다.
            memos.add(0, ReadingMemo(NO_BOOK_ID, "", oldMemo))
            saveMemos(context, memos)
            prefs.edit().remove(OLD_MEMO).apply()
        }

        return memos
    }

    fun addMemo(context: Context, memo: String) {
        // 일반 메모는 특정 책 id 없이 저장한다.
        val cleanMemo = memo.trim()
        if (cleanMemo.isEmpty()) return

        val memos = getMemoItems(context)
        memos.add(0, ReadingMemo(NO_BOOK_ID, "", cleanMemo))
        saveMemos(context, memos)
    }

    fun addBookMemo(context: Context, bookId: Int, bookTitle: String, memo: String) {
        // 책별 메모는 나중에 필터링할 수 있도록 책 id와 제목을 함께 저장한다.
        val cleanMemo = memo.trim()
        if (cleanMemo.isEmpty()) return

        val memos = getMemoItems(context)
        memos.add(0, ReadingMemo(bookId, bookTitle.trim(), cleanMemo))
        saveMemos(context, memos)
    }

    fun removeMemo(context: Context, index: Int) {
        // 일반 메모 Dialog에서는 화면 순서(index)로 삭제한다.
        val memos = getMemoItems(context)
        if (index in memos.indices) {
            memos.removeAt(index)
            saveMemos(context, memos)
        }
    }

    fun removeMemo(context: Context, memo: ReadingMemo) {
        // 책별 메모 Dialog에서는 같은 내용의 메모 객체를 찾아 삭제한다.
        val memos = getMemoItems(context)
        val removeIndex = memos.indexOfFirst { item ->
            item.bookId == memo.bookId &&
                item.bookTitle == memo.bookTitle &&
                item.text == memo.text
        }
        if (removeIndex != -1) {
            memos.removeAt(removeIndex)
            saveMemos(context, memos)
        }
    }

    fun clear(context: Context) {
        // 전체 삭제는 현재 구조와 예전 구조의 저장값을 둘 다 비운다.
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .remove(MEMO_LIST)
            .remove(OLD_MEMO)
            .apply()
    }

    private fun parseMemo(rawMemo: String): ReadingMemo? {
        // 저장 문자열 한 조각을 bookId, bookTitle, text로 다시 나눈다.
        val raw = rawMemo.trim()
        if (raw.isEmpty()) return null

        val fields = raw.split(FIELD_SEPARATOR, limit = 3)
        if (fields.size == 3) {
            return ReadingMemo(
                fields[0].toIntOrNull() ?: NO_BOOK_ID,
                fields[1].trim(),
                fields[2].trim()
            ).takeIf { it.text.isNotEmpty() }
        }

        // 예전 버전에서 저장한 메모는 책 정보가 없으므로 일반 메모로 보여 준다.
        return ReadingMemo(NO_BOOK_ID, "", raw)
    }

    private fun saveMemos(context: Context, memos: List<ReadingMemo>) {
        // 메모 내용에는 쉼표나 줄바꿈이 들어갈 수 있어 잘 쓰지 않는 구분 문자를 사용했다.
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(MEMO_LIST, memos.joinToString(SEPARATOR) { memo ->
                listOf(memo.bookId.toString(), memo.bookTitle.cleanMemoField(), memo.text.cleanMemoField())
                    .joinToString(FIELD_SEPARATOR)
            })
            .apply()
    }

    private fun String.cleanMemoField(): String {
        // 저장용 구분 문자가 메모 안에 섞이면 복원이 꼬이므로 공백으로 바꿔 둔다.
        return replace(SEPARATOR, " ")
            .replace(FIELD_SEPARATOR, " ")
            .trim()
    }
}
