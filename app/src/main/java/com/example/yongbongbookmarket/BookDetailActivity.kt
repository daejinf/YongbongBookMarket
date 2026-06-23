package com.example.yongbongbookmarket

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.yongbongbookmarket.databinding.ActivityBookDetailBinding

/**
 * 목록에서 고른 책을 자세히 보여 주는 화면.
 * Intent extra로 받은 값을 꺼내 표시하고, 장바구니/바로구매 흐름으로 이어 준다.
 */
class BookDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBookDetailBinding

    // 장바구니와 최근 본 상품 저장에 같이 쓰는 현재 책 id.
    private var selectedBookId: Int = -1
    private var selectedBookTitle: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 상세 화면 XML의 View들을 binding으로 연결한다.
        binding = ActivityBookDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = getString(R.string.book_detail_title)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // 목록에서 넘긴 값을 꺼낸다. 값이 빠진 경우도 앱이 바로 죽지 않게 기본값을 둔다.
        selectedBookId = intent.getIntExtra("book_id", -1)
        val title = intent.getStringExtra("book_title") ?: ""
        selectedBookTitle = title
        val author = intent.getStringExtra("book_author") ?: ""
        val originalPrice = intent.getIntExtra("book_original_price", 0)
        val price = intent.getIntExtra("book_price", 0)
        val publishedDate = intent.getStringExtra("book_date") ?: ""
        val condition = intent.getStringExtra("book_condition") ?: ""
        val shippingInfo = intent.getStringExtra("book_shipping") ?: ""
        val major = intent.getStringExtra("book_major") ?: ""
        val description = intent.getStringExtra("book_description") ?: ""
        val rating = intent.getDoubleExtra("book_rating", 0.0)
        val reviewCount = intent.getIntExtra("book_review_count", 0)
        val salesIndex = intent.getIntExtra("book_sales_index", 0)
        val imageResId = intent.getIntExtra("book_image", R.drawable.book_cover_01)
        val recommendationReason = intent.getStringExtra("recommendation_reason").orEmpty()

        // 위에서 꺼낸 값을 실제 화면 요소에 하나씩 넣는다.
        binding.detailCover.setImageResource(imageResId)
        binding.detailTitle.text = title
        binding.detailAuthor.text = getString(R.string.detail_author_format, author)
        binding.detailOriginalPrice.text = getString(R.string.detail_original_price_format, originalPrice)
        binding.detailPrice.text = getString(R.string.detail_price_format, price)
        binding.detailDate.text = getString(R.string.detail_date_format, publishedDate)
        binding.detailCondition.text = getString(R.string.detail_condition_format, condition)
        binding.detailPlace.text = getString(R.string.detail_shipping_format, shippingInfo)
        binding.detailMajor.text = major
        binding.detailDescription.text = description
        binding.detailRatingLine.text = getString(R.string.detail_rating_format, rating, reviewCount, salesIndex)

        saveLastBook(selectedBookId, title, imageResId)
        showRecommendationReason(recommendationReason)

        // 구매 안내 버튼은 상세 설명 Dialog만 띄운다.
        binding.buyInfoButton.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle(getString(R.string.buy_dialog_title))
                .setMessage(getString(R.string.buy_dialog_message))
                .setPositiveButton(getString(R.string.dialog_ok), null)
                .show()
        }

        binding.buyNowButton.setOnClickListener {
            buyNow()
        }

        binding.favoriteButton.setOnClickListener {
            addToCart()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // 공통 Toolbar 메뉴에 책별 메모 항목만 추가로 붙인다.
        menuInflater.inflate(R.menu.menu_toolbar, menu)
        menu?.add(0, MENU_BOOK_MEMO_ID, 3, getString(R.string.menu_book_memo))
            ?.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // 상세 화면의 Toolbar 메뉴 이동을 한곳에서 처리한다.
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
                startActivity(Intent(this, CartActivity::class.java))
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
            MENU_BOOK_MEMO_ID -> {
                showBookMemoDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun saveLastBook(bookId: Int, title: String, imageResId: Int) {
        // 최근 본 책은 서버 없이 SharedPreferences에 간단히 남긴다.
        val prefs = getSharedPreferences("book_market", MODE_PRIVATE)
        prefs.edit()
            .putInt("last_book_id", bookId)
            .putString("last_book_title", title)
            .putInt("last_book_image", imageResId)
            .apply()
        // 홈과 내서재의 최근 본 상품 영역은 이 저장소에서 목록을 다시 읽는다.
        RecentBookRepository.saveViewedBook(this, bookId)
    }

    private fun showRecommendationReason(reason: String) {
        // 랜덤 추천으로 들어온 경우에만 추천 이유 Toast를 보여 준다.
        if (reason.isBlank()) return

        Toast.makeText(
            this,
            getString(R.string.random_reason_format, reason),
            Toast.LENGTH_LONG
        ).show()
    }

    private fun addToCart() {
        // 상세 화면에서 담기 버튼을 눌렀을 때 현재 책 한 권을 장바구니에 추가한다.
        if (selectedBookId == -1) {
            Toast.makeText(this, getString(R.string.cart_empty), Toast.LENGTH_SHORT).show()
            return
        }

        // 저장은 CartRepository에 맡기고, 화면에서는 이동 여부만 묻는다.
        CartRepository.addBook(this, selectedBookId)
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.saved_favorite))
            .setMessage(getString(R.string.cart_move_question))
            .setPositiveButton(getString(R.string.dialog_yes)) { _, _ ->
                startActivity(Intent(this, CartActivity::class.java))
            }
            .setNegativeButton(getString(R.string.dialog_no), null)
            .show()
    }

    private fun buyNow() {
        // 바로구매도 같은 장바구니 저장 방식을 쓰고, 주문인 정보는 다음 화면에서 직접 받는다.
        if (selectedBookId == -1) {
            Toast.makeText(this, getString(R.string.cart_empty), Toast.LENGTH_SHORT).show()
            return
        }

        CartRepository.addBook(this, selectedBookId)
        Toast.makeText(this, getString(R.string.buy_now_added), Toast.LENGTH_SHORT).show()
        val intent = Intent(this, ShippingActivity::class.java)
        intent.putExtra("selected_book_ids", intArrayOf(selectedBookId))
        startActivity(intent)
    }

    private fun showBookMemoDialog() {
        // 같은 dialog_memo.xml을 쓰되, 여기서는 현재 책에 묶인 메모만 보여 준다.
        val dialogView = layoutInflater.inflate(R.layout.dialog_memo, null)
        val memoContainer = dialogView.findViewById<LinearLayout>(R.id.memo_list_container)
        val memoEdit = dialogView.findViewById<EditText>(R.id.memo_edit)
        memoEdit.hint = getString(R.string.book_memo_hint)
        renderBookMemoList(memoContainer)

        val dialog = AlertDialog.Builder(this)
            .setTitle(getString(R.string.book_memo_title))
            .setView(dialogView)
            .setPositiveButton(getString(R.string.memo_add_button), null)
            .setNegativeButton(getString(R.string.memo_close_button), null)
            .show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            // selectedBookId와 제목을 같이 저장해야 나중에 어떤 책 메모인지 구분할 수 있다.
            MemoRepository.addBookMemo(this, selectedBookId, selectedBookTitle, memoEdit.text.toString())
            memoEdit.text.clear()
            renderBookMemoList(memoContainer)
            Toast.makeText(this, getString(R.string.book_memo_added), Toast.LENGTH_SHORT).show()
        }
    }

    private fun renderBookMemoList(container: LinearLayout) {
        container.removeAllViews()
        // 전체 메모 중에서 현재 책 id와 같은 메모만 골라 보여 준다.
        val memos = MemoRepository.getMemoItems(this)
            .filter { memo -> memo.bookId == selectedBookId }

        if (memos.isEmpty()) {
            val emptyText = TextView(this).apply {
                text = getString(R.string.book_memo_empty)
                setTextColor(getColor(R.color.book_gray))
                textSize = 14f
                gravity = android.view.Gravity.CENTER
                setBackgroundResource(R.drawable.panel_soft_round)
                setPadding(dp(14), dp(22), dp(14), dp(22))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }
            container.addView(emptyText)
            return
        }

        memos.forEachIndexed { index, memo ->
            // Dialog 안에 넣을 메모 한 줄을 코드로 만든다.
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = android.view.Gravity.CENTER_VERTICAL
                setBackgroundResource(R.drawable.panel_soft_round)
                setPadding(dp(12), dp(10), dp(10), dp(10))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomMargin = dp(8)
                }
            }
            val numberText = TextView(this).apply {
                text = "%02d".format(index + 1)
                gravity = android.view.Gravity.CENTER
                setBackgroundResource(R.drawable.logo_badge)
                setTextColor(getColor(R.color.campus_yellow))
                textSize = 12f
                typeface = android.graphics.Typeface.DEFAULT_BOLD
                layoutParams = LinearLayout.LayoutParams(dp(34), dp(34))
            }
            val memoText = TextView(this).apply {
                text = memo.text
                setTextColor(getColor(R.color.book_ink))
                textSize = 14f
                maxLines = 4
                ellipsize = android.text.TextUtils.TruncateAt.END
                setPadding(dp(12), 0, dp(8), 0)
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }
            val deleteText = TextView(this).apply {
                text = getString(R.string.memo_delete_button)
                gravity = android.view.Gravity.CENTER
                setBackgroundResource(R.drawable.panel_feed_card)
                setTextColor(getColor(R.color.book_gray))
                textSize = 13f
                setPadding(dp(12), dp(7), dp(12), dp(7))
                setOnClickListener {
                    MemoRepository.removeMemo(this@BookDetailActivity, memo)
                    renderBookMemoList(container)
                }
            }
            row.addView(numberText)
            row.addView(memoText)
            row.addView(deleteText)
            container.addView(row)
        }
    }

    private fun dp(value: Int): Int {
        // 코드에서 만든 View는 XML처럼 dp를 바로 못 쓰기 때문에 px로 바꿔 준다.
        return (value * resources.displayMetrics.density).toInt()
    }

    private fun openJnuHomePage() {
        // 외부 홈페이지는 브라우저 앱으로 넘긴다.
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.jnu_home_url)))
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, getString(R.string.open_external_error), Toast.LENGTH_SHORT).show()
        }
    }

    private fun openAladinBestPage() {
        // 책 정보 확인용 외부 페이지를 열 때 사용한다.
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.aladin_best_url)))
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, getString(R.string.open_external_error), Toast.LENGTH_SHORT).show()
        }
    }

    private fun showAppInfo() {
        // 현재 앱에 대한 짧은 설명을 Dialog로 보여 준다.
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.app_info_title))
            .setMessage(getString(R.string.app_info_message))
            .setPositiveButton(getString(R.string.dialog_ok), null)
            .show()
    }

    companion object {
        private const val MENU_BOOK_MEMO_ID = 4001
    }
}
