package com.example.yongbongbookmarket

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.yongbongbookmarket.databinding.ItemCartBinding

/**
 * item_cart.xml 한 줄을 잡아 두는 ViewHolder.
 */
class CartViewHolder(val binding: ItemCartBinding) : RecyclerView.ViewHolder(binding.root)

/**
 * 장바구니 목록을 화면에 보여 주는 Adapter.
 * 실제 저장은 CartRepository가 맡고, 여기서는 버튼 클릭만 Activity로 넘긴다.
 */
class CartAdapter(
    private val cartItems: MutableList<CartItem>,
    private val selectedBookIds: MutableSet<Int>,
    private val onSelectionChange: (CartItem, Boolean) -> Unit,
    private val onQuantityChange: (CartItem, Int) -> Unit,
    private val onDeleteClick: (CartItem) -> Unit
) : RecyclerView.Adapter<CartViewHolder>() {

    override fun getItemCount(): Int {
        // 장바구니에 담긴 줄 수만큼 목록을 만든다.
        return cartItems.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        // item_cart.xml을 장바구니 한 줄 View로 바꾼다.
        val binding = ItemCartBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CartViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        // 현재 줄에 들어갈 책과 수량을 꺼낸다.
        val item = cartItems[position]
        val binding = holder.binding
        val context = binding.root.context

        // 화면에 보이는 값은 CartItem에서 꺼내고, 실제 저장 변경은 Activity로 넘긴다.
        binding.cartBookCover.setImageResource(item.book.imageResId)
        binding.cartBookTitle.text = item.book.title
        binding.cartBookPrice.text = context.getString(R.string.cart_item_price_format, item.book.price)
        binding.cartBookQuantity.text = item.quantity.toString()
        binding.cartBookSubtotal.text = context.getString(R.string.cart_item_subtotal_format, item.subtotal)

        binding.cartItemCheckbox.setOnCheckedChangeListener(null)
        binding.cartItemCheckbox.isChecked = selectedBookIds.contains(item.book.id)
        binding.cartItemCheckbox.setOnCheckedChangeListener { _, isChecked ->
            // 체크 여부가 바뀌면 Activity가 선택 목록과 총액을 다시 계산한다.
            onSelectionChange(item, isChecked)
        }

        binding.minusButton.setOnClickListener {
            // 수량이 0 이하가 되는 처리는 Repository 쪽에서 한 번 더 확인한다.
            onQuantityChange(item, item.quantity - 1)
        }
        binding.plusButton.setOnClickListener {
            onQuantityChange(item, item.quantity + 1)
        }
        binding.deleteButton.setOnClickListener {
            onDeleteClick(item)
        }
    }

    fun updateItems(newItems: List<CartItem>) {
        // 삭제나 수량 변경 뒤 최신 장바구니 목록으로 화면을 새로 그린다.
        cartItems.clear()
        cartItems.addAll(newItems)
        notifyDataSetChanged()
    }
}
