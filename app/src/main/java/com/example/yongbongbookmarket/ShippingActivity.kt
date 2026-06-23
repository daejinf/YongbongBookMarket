package com.example.yongbongbookmarket

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.yongbongbookmarket.databinding.ActivityShippingBinding
import com.google.android.material.snackbar.Snackbar

/**
 * 주문서에 들어갈 배송정보를 직접 입력받는 화면.
 * 입력한 값은 Intent extra로 OrderActivity에 전달한다.
 */
class ShippingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityShippingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 배송정보 화면 XML을 binding으로 연결한다.
        binding = ActivityShippingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = getString(R.string.shipping_title)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.nextButton.setOnClickListener {
            moveToOrderSheet()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        // 배송정보 입력 중 뒤로가기를 누르면 이전 화면으로 돌아간다.
        finish()
        return true
    }

    private fun moveToOrderSheet() {
        // 주문서에 그대로 보여 줄 값이므로 입력 직후 공백을 제거한다.
        val name = binding.nameEdit.text.toString().trim()
        val phone = binding.phoneEdit.text.toString().trim()
        val zip = binding.zipEdit.text.toString().trim()
        val address = binding.addressEdit.text.toString().trim()

        // 배송정보는 네 항목이 모두 있어야 주문서로 넘어가게 했다.
        if (name.isEmpty() || phone.isEmpty() || zip.isEmpty() || address.isEmpty()) {
            Snackbar.make(binding.root, getString(R.string.shipping_empty_message), Snackbar.LENGTH_SHORT).show()
            return
        }

        // 주문서 화면에서 다시 꺼내 쓸 값이라 extra 이름을 명확하게 적었다.
        val intent = Intent(this, OrderActivity::class.java)
        intent.putExtra("shipping_name", name)
        intent.putExtra("shipping_phone", phone)
        intent.putExtra("shipping_zip", zip)
        intent.putExtra("shipping_address", address)
        // 장바구니에서 고른 상품만 주문서에 보이도록 선택 id도 이어서 넘긴다.
        intent.putExtra("selected_book_ids", getSelectedBookIds())
        startActivity(intent)
    }

    private fun getSelectedBookIds(): IntArray {
        // 바로구매처럼 한 권만 넘어온 경우도 같은 이름의 extra로 처리한다.
        return intent.getIntArrayExtra("selected_book_ids") ?: intArrayOf()
    }
}
