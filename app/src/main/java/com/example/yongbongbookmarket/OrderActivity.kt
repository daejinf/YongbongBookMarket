package com.example.yongbongbookmarket

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.yongbongbookmarket.databinding.ActivityOrderBinding

/**
 * 배송정보와 장바구니 상품을 모아 최종 확인서처럼 보여 주는 화면.
 * 실제 결제 대신 주문 확인 Dialog까지만 구현했다.
 */
class OrderActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOrderBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 주문서 XML을 binding으로 연결하고 화면에 표시할 값을 채운다.
        binding = ActivityOrderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = getString(R.string.order_sheet_title)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        showOrderSheet()

        binding.completeButton.setOnClickListener {
            completeOrder()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        // 주문 확인 화면의 Toolbar 뒤로가기는 주문을 확정하지 않고 화면만 닫는다.
        finish()
        return true
    }

    private fun showOrderSheet() {
        // ShippingActivity에서 넘긴 배송정보를 꺼낸다.
        val name = intent.getStringExtra("shipping_name") ?: ""
        val phone = intent.getStringExtra("shipping_phone") ?: ""
        val zip = intent.getStringExtra("shipping_zip") ?: ""
        val address = intent.getStringExtra("shipping_address") ?: ""
        val selectedBookIds = getSelectedBookIds()
        // 선택 id가 비어 있으면 장바구니 전체, 값이 있으면 선택한 책만 주문서에 올린다.
        val items = CartRepository.getItems(this).filter { item ->
            selectedBookIds.isEmpty() || selectedBookIds.contains(item.book.id)
        }

        binding.shippingInfoText.text = "$name\n$phone\n$zip\n$address"

        binding.orderItemsText.text = items.joinToString(separator = "\n") { item ->
            getString(R.string.order_item_line, item.book.title, item.quantity, item.subtotal)
        }
        binding.orderTotalText.text = getString(R.string.order_total_format, items.sumOf { it.subtotal })
    }

    private fun completeOrder() {
        // 주문 확인 후에는 선택한 상품을 장바구니에서 비운다.
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.order_dialog_title))
            .setMessage(getString(R.string.order_dialog_message))
            .setPositiveButton(getString(R.string.dialog_ok)) { _, _ ->
                val selectedBookIds = getSelectedBookIds()
                // 장바구니 전체 주문과 바로구매 주문을 같은 화면에서 처리한다.
                if (selectedBookIds.isEmpty()) {
                    CartRepository.clear(this)
                } else {
                    CartRepository.removeBooks(this, selectedBookIds)
                }
                val intent = Intent(this, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
                finish()
            }
            .show()
    }

    private fun getSelectedBookIds(): IntArray {
        // Intent extra가 없으면 빈 배열로 처리해서 null 확인을 줄인다.
        return intent.getIntArrayExtra("selected_book_ids") ?: intArrayOf()
    }
}
