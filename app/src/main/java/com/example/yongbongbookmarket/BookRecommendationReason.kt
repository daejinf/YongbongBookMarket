package com.example.yongbongbookmarket

import android.content.Context

/**
 * 랜덤 추천 버튼에서 보여 줄 짧은 이유를 만든다.
 *
 * 추천 책은 무작위로 고르지만, 안내 문구는 책의 가격과 상태를 보고 붙인다.
 */
object BookRecommendationReason {

    fun make(context: Context, book: Book): String {
        // 책의 상태와 가격을 차례로 확인해서 가장 먼저 맞는 추천 문장을 고른다.
        return when {
            book.isNewBook -> context.getString(R.string.recommend_reason_new)
            book.price <= 10000 -> context.getString(R.string.recommend_reason_low_price)
            // 필기와 밑줄은 중고책에서 실제로 확인할 만한 정보라 추천 이유에 넣었다.
            book.condition.contains("필기", ignoreCase = true) ||
                book.condition.contains("밑줄", ignoreCase = true) ->
                context.getString(R.string.recommend_reason_marked)
            book.rating >= 9.3 -> context.getString(R.string.recommend_reason_rating)
            book.major.contains("Kotlin", ignoreCase = true) ||
                book.major.contains("Android", ignoreCase = true) ->
                context.getString(R.string.recommend_reason_develop)
            else -> context.getString(R.string.recommend_reason_major_format, book.major)
        }
    }
}
