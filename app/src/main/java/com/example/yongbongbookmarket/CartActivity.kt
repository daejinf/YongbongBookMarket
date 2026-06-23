package com.example.yongbongbookmarket

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.yongbongbookmarket.databinding.ActivityCartBinding
import com.google.android.material.snackbar.Snackbar

/**
 * 장바구니 화면.
 * 저장된 상품을 다시 불러와 수량 변경, 삭제, 주문 흐름 이동을 처리한다.
 * 실제 결제 대신 주문서 확인 화면까지 이어지도록 만들었다.
 */
class CartActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCartBinding

    private lateinit var cartAdapter: CartAdapter
    // 체크된 책 id만 따로 보관한다. 이 값이 주문 대상과 합계 계산 기준이 된다.
    private val selectedBookIds = mutableSetOf<Int>()
    // 새로 장바구니에 들어온 책을 자동 선택하기 위해 이전 목록을 기억한다.
    private var knownBookIds = emptySet<Int>()
    // 전체선택 체크박스를 코드로 바꿀 때 다시 이벤트가 도는 것을 막는다.
    private var updatingSelectionButtons = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 장바구니 화면 XML과 코드를 연결한다.
        binding = ActivityCartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = getString(R.string.cart_title)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Adapter는 버튼 클릭 사실만 알려 주고, 실제 저장 변경은 Activity에서 처리한다.
        cartAdapter = CartAdapter(
            mutableListOf(),
            selectedBookIds = selectedBookIds,
            onSelectionChange = { item, isChecked -> changeSelection(item, isChecked) },
            onQuantityChange = { item, quantity -> changeQuantity(item, quantity) },
            onDeleteClick = { item -> confirmDelete(item) }
        )
        // 장바구니는 수량과 소계를 같이 봐야 해서 한 줄 목록으로 배치했다.
        binding.cartRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.cartRecyclerView.adapter = cartAdapter

        binding.keepShoppingButton.setOnClickListener {
            startActivity(Intent(this, BookListActivity::class.java))
        }

        binding.orderButton.setOnClickListener {
            moveToShipping()
        }

        binding.selectAllRow.setOnClickListener {
            val checked = !binding.selectAllCheckbox.isChecked
            setAllItemsSelected(checked)
        }

        binding.selectAllCheckbox.setOnCheckedChangeListener { _, isChecked ->
            if (!updatingSelectionButtons) {
                setAllItemsSelected(isChecked)
            }
        }

        binding.deliveryCheckbox.setOnCheckedChangeListener { _, isChecked ->
            if (!updatingSelectionButtons) {
                setAllItemsSelected(isChecked)
            }
        }

        loadCart()
    }

    override fun onResume() {
        super.onResume()
        // 다른 화면에서 장바구니가 바뀌었을 수 있으니 돌아올 때마다 다시 읽는다.
        loadCart()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // 장바구니에서도 다른 화면과 같은 Toolbar 메뉴를 그대로 사용한다.
        menuInflater.inflate(R.menu.menu_toolbar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // 메뉴 항목에 따라 화면 이동, 외부 링크, 앱 정보 표시를 나눠 처리한다.
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            R.id.menu_home -> {
                val intent = Intent(this, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
                true
            }
            R.id.menu_book_list -> {
                startActivity(Intent(this, BookListActivity::class.java))
                true
            }
            R.id.menu_cart -> {
                Toast.makeText(this, getString(R.string.current_cart), Toast.LENGTH_SHORT).show()
                true
            }
            R.id.menu_my_page -> {
                startActivity(Intent(this, MyPageActivity::class.java))
                true
            }
            R.id.menu_jnu_site -> {
                openJnuHomePage()
                true
            }
            R.id.menu_aladin_best -> {
                openAladinBestPage()
                true
            }
            R.id.menu_app_info -> {
                showAppInfo()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun loadCart() {
        // 저장된 장바구니를 읽고, 선택 상태와 총액을 한 번에 새로 맞춘다.
        val items = CartRepository.getItems(this)
        syncSelection(items)
        cartAdapter.updateItems(items)
        updateSelectedTotal(items)

        binding.emptyCartText.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
        binding.cartRecyclerView.visibility = if (items.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun syncSelection(items: List<CartItem>) {
        // 장바구니에서 사라진 책은 선택 목록에서도 제거한다.
        val currentBookIds = items.map { it.book.id }.toSet()
        selectedBookIds.retainAll(currentBookIds)

        // 처음 화면을 열었거나 새로 추가된 책은 기본으로 선택해 둔다.
        if (knownBookIds.isEmpty()) {
            selectedBookIds.addAll(currentBookIds)
        } else {
            selectedBookIds.addAll(currentBookIds - knownBookIds)
        }

        knownBookIds = currentBookIds
        updateSelectionButtons(items)
    }

    private fun changeSelection(item: CartItem, isChecked: Boolean) {
        // 개별 체크박스를 누르면 선택 id 목록만 바꾼 뒤 총액을 다시 계산한다.
        if (isChecked) {
            selectedBookIds.add(item.book.id)
        } else {
            selectedBookIds.remove(item.book.id)
        }

        val items = CartRepository.getItems(this)
        updateSelectionButtons(items)
        updateSelectedTotal(items)
    }

    private fun setAllItemsSelected(isChecked: Boolean) {
        // 전체선택은 현재 장바구니의 모든 id를 한 번에 넣거나 비운다.
        val items = CartRepository.getItems(this)
        selectedBookIds.clear()
        if (isChecked) {
            selectedBookIds.addAll(items.map { it.book.id })
        }
        updateSelectionButtons(items)
        updateSelectedTotal(items)
        cartAdapter.notifyDataSetChanged()
    }

    private fun updateSelectionButtons(items: List<CartItem>) {
        // 모든 상품이 선택된 상태일 때만 전체선택과 배송 묶음 체크를 켠다.
        val hasItems = items.isNotEmpty()
        val allChecked = hasItems && items.all { selectedBookIds.contains(it.book.id) }

        updatingSelectionButtons = true
        binding.selectAllCheckbox.isChecked = allChecked
        binding.deliveryCheckbox.isChecked = allChecked
        updatingSelectionButtons = false
    }

    private fun updateSelectedTotal(items: List<CartItem>) {
        // 체크된 책만 골라 합계를 내야 선택 주문 흐름과 맞는다.
        val total = items
            .filter { selectedBookIds.contains(it.book.id) }
            .sumOf { it.subtotal }
        binding.cartTotalText.text = getString(R.string.cart_total_format, total)
    }

    private fun changeQuantity(item: CartItem, quantity: Int) {
        // 마이너스 버튼으로 0권이 되면 바로 지우지 않고 삭제 확인 Dialog를 거친다.
        if (quantity <= 0) {
            confirmDelete(item)
            return
        }

        CartRepository.updateQuantity(this, item.book.id, quantity)
        loadCart()
    }

    private fun confirmDelete(item: CartItem) {
        // 삭제는 실수하기 쉬워서 Dialog로 한 번 더 묻는다.
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.cart_delete_title))
            .setMessage(getString(R.string.cart_delete_message))
            .setPositiveButton(getString(R.string.dialog_ok)) { _, _ ->
                CartRepository.removeBook(this, item.book.id)
                selectedBookIds.remove(item.book.id)
                loadCart()
                Snackbar.make(binding.root, getString(R.string.cart_deleted), Snackbar.LENGTH_SHORT).show()
            }
            .setNegativeButton(getString(R.string.dialog_cancel), null)
            .show()
    }

    private fun moveToShipping() {
        // 주문 버튼은 선택된 책만 배송정보 화면으로 넘긴다.
        val selectedItems = CartRepository.getItems(this)
            .filter { selectedBookIds.contains(it.book.id) }

        if (selectedItems.isEmpty()) {
            Snackbar.make(binding.root, getString(R.string.cart_empty), Snackbar.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(this, ShippingActivity::class.java)
        intent.putExtra("selected_book_ids", selectedItems.map { it.book.id }.toIntArray())
        startActivity(intent)
    }

    private fun openJnuHomePage() {
        // 외부 홈페이지는 앱 안에서 직접 띄우지 않고 브라우저 앱에 맡긴다.
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.jnu_home_url)))
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, getString(R.string.open_external_error), Toast.LENGTH_SHORT).show()
        }
    }

    private fun openAladinBestPage() {
        // 베스트셀러 참고 페이지도 같은 방식으로 기본 브라우저를 연다.
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.aladin_best_url)))
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, getString(R.string.open_external_error), Toast.LENGTH_SHORT).show()
        }
    }

    private fun showAppInfo() {
        // 앱 설명은 화면 이동 없이 간단한 Dialog로 확인하게 했다.
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.app_info_title))
            .setMessage(getString(R.string.app_info_message))
            .setPositiveButton(getString(R.string.dialog_ok), null)
            .show()
    }
}
