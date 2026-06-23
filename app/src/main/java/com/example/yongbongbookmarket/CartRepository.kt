package com.example.yongbongbookmarket

import android.content.Context

/**
 * 장바구니 한 줄에 필요한 책 정보와 수량을 묶어 둔다.
 * 소계는 수량이 바뀔 때마다 다시 계산되도록 getter로 두었다.
 */
data class CartItem(
    val book: Book,
    val quantity: Int
) {
    val subtotal: Int
        get() = book.price * quantity
}

object CartRepository {
    // 여러 화면에서 같은 장바구니를 읽어야 해서 저장 이름을 여기서만 관리한다.
    private const val PREF_NAME = "book_market"
    private const val CART_ITEMS = "cart_items"

    fun addBook(context: Context, bookId: Int) {
        // 이미 담긴 책이면 수량만 1권 늘리고, 처음 담는 책이면 1권으로 시작한다.
        val cart = loadCart(context).toMutableMap()
        cart[bookId] = (cart[bookId] ?: 0) + 1
        saveCart(context, cart)
    }

    fun updateQuantity(context: Context, bookId: Int, quantity: Int) {
        // 수량이 0 이하가 되면 장바구니에서 제거하는 쪽이 자연스럽다.
        val cart = loadCart(context).toMutableMap()
        if (quantity <= 0) {
            cart.remove(bookId)
        } else {
            cart[bookId] = quantity
        }
        saveCart(context, cart)
    }

    fun removeBook(context: Context, bookId: Int) {
        // 개별 삭제 버튼을 눌렀을 때 해당 책 id만 장바구니에서 제거한다.
        val cart = loadCart(context).toMutableMap()
        cart.remove(bookId)
        saveCart(context, cart)
    }

    fun removeBooks(context: Context, bookIds: IntArray) {
        // 주문 완료 후 선택한 책만 지울 때 사용한다.
        val cart = loadCart(context).toMutableMap()
        bookIds.forEach { bookId ->
            cart.remove(bookId)
        }
        saveCart(context, cart)
    }

    fun clear(context: Context) {
        // 전체 장바구니를 비울 때는 저장된 문자열 묶음 자체를 삭제한다.
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(CART_ITEMS).apply()
    }

    fun getItems(context: Context): List<CartItem> {
        // 저장소에는 id와 수량만 있으므로, 화면에 보여 줄 책 정보는 다시 찾아온다.
        val cart = loadCart(context)
        // StringSet은 순서가 고정되지 않아서 화면에 보여 줄 때만 bookId로 정렬한다.
        return cart.toSortedMap().mapNotNull { (bookId, quantity) ->
            val book = BookRepository.getBook(bookId)
            if (book == null) {
                null
            } else {
                CartItem(book, quantity)
            }
        }
    }

    fun getBookCount(context: Context): Int {
        // 책 종류가 아니라 실제 담긴 권수를 보여 주기 위해 수량을 모두 더한다.
        return getItems(context).sumOf { it.quantity }
    }

    private fun loadCart(context: Context): Map<Int, Int> {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val savedItems = prefs.getStringSet(CART_ITEMS, emptySet()) ?: emptySet()

        // SharedPreferences에는 "상품ID:수량"처럼 단순한 문자열로 저장했다.
        // 예: "193444586:2"는 해당 책을 2권 담았다는 뜻이다.
        return savedItems.mapNotNull { item ->
            val parts = item.split(":")
            val bookId = parts.getOrNull(0)?.toIntOrNull()
            val quantity = parts.getOrNull(1)?.toIntOrNull()

            if (bookId == null || quantity == null || quantity <= 0) {
                null
            } else {
                bookId to quantity
            }
        }.toMap()
    }

    private fun saveCart(context: Context, cart: Map<Int, Int>) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        // SharedPreferences는 Map을 바로 저장하지 못해서 문자열 Set으로 바꿔 저장한다.
        val savedItems = cart.map { (bookId, quantity) ->
            "$bookId:$quantity"
        }.toSet()

        prefs.edit().putStringSet(CART_ITEMS, savedItems).apply()
    }
}
