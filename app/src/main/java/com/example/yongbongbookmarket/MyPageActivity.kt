package com.example.yongbongbookmarket

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.yongbongbookmarket.databinding.ActivityMyPageBinding

/**
 * 로그인 상태와 장바구니 요약을 한 번에 확인하는 마이페이지 화면.
 * SharedPreferences로 저장한 로그인 상태와 최근 본 상품을 보여 준다.
 */
class MyPageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyPageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 내서재 화면 XML의 View들을 binding으로 연결한다.
        binding = ActivityMyPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = getString(R.string.my_page_title)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        loadMyPage()
    }

    override fun onResume() {
        super.onResume()
        // 로그인이나 장바구니 화면을 다녀오면 내용이 바뀔 수 있어 다시 불러온다.
        loadMyPage()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // 내서재 화면에서도 공통 Toolbar 메뉴를 사용할 수 있게 붙인다.
        menuInflater.inflate(R.menu.menu_toolbar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // 현재 화면은 Toast로 알려 주고, 나머지는 해당 화면으로 이동한다.
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
                Toast.makeText(this, getString(R.string.current_my_page), Toast.LENGTH_SHORT).show()
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

    private fun loadMyPage() {
        // 한 번 읽은 장바구니 목록을 화면의 여러 영역에서 함께 사용한다.
        val cartItems = CartRepository.getItems(this)

        showLoginInfo()
        showCartInfo(cartItems)
        renderViewedBooks()

        binding.loginButton.setOnClickListener {
            handleLoginButton()
        }

        binding.orderButton.setOnClickListener {
            startActivity(Intent(this, CartActivity::class.java))
        }
    }

    private fun renderViewedBooks() {
        // 홈과 같은 방식으로 마지막 상품과 최근 상품 목록을 나눠 그린다.
        renderLastViewedBook()
        renderRecentViewedBooks()
    }

    private fun renderLastViewedBook() {
        // 마지막으로 본 책이 있으면 카드에 채우고, 없으면 목록 화면으로 가는 안내 카드로 둔다.
        val book = RecentBookRepository.getLastBook(this)
        if (book == null) {
            // 아직 본 상품이 없으면 도서 목록으로 이동할 수 있는 기본 카드를 둔다.
            binding.myLastViewedCover.setImageResource(R.drawable.book_cover_01)
            binding.myLastViewedTitle.text = getString(R.string.home_no_last_viewed)
            binding.myLastViewedMeta.text = getString(R.string.home_last_viewed_empty_meta)
            binding.myLastViewedCard.setOnClickListener {
                startActivity(Intent(this, BookListActivity::class.java))
            }
            return
        }

        binding.myLastViewedCover.setImageResource(book.imageResId)
        binding.myLastViewedTitle.text = book.title
        binding.myLastViewedMeta.text = "${book.author} · ${book.major}"
        binding.myLastViewedCard.setOnClickListener {
            openBookDetail(book)
        }
    }

    private fun renderRecentViewedBooks() {
        // 최근 본 책 목록은 매번 비운 뒤 새로 만들어야 중복 View가 쌓이지 않는다.
        binding.myRecentBooksContainer.removeAllViews()
        val recentBooks = RecentBookRepository.getRecentBooks(this)

        if (recentBooks.isEmpty()) {
            // 최근 본 상품이 없을 때도 섹션 높이가 갑자기 사라지지 않게 안내 문구를 넣는다.
            val emptyText = TextView(this).apply {
                text = getString(R.string.home_recent_viewed_empty)
                setTextColor(getColor(R.color.book_gray))
                textSize = 14f
                setPadding(4, 10, 4, 16)
            }
            binding.myRecentBooksContainer.addView(emptyText)
            return
        }

        recentBooks.forEach { book ->
            binding.myRecentBooksContainer.addView(createRecentBookCard(book))
        }
    }

    private fun createRecentBookCard(book: Book): LinearLayout {
        // 내서재의 최근 본 상품 가로 목록에 들어갈 카드 한 장을 만든다.
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundResource(R.drawable.panel_feed_card)
            setPadding(dp(10), dp(10), dp(10), dp(10))
            isClickable = true
            isFocusable = true
            layoutParams = LinearLayout.LayoutParams(dp(118), LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                marginEnd = dp(12)
            }
            setOnClickListener {
                openBookDetail(book)
            }
        }

        val cover = ImageView(this).apply {
            setImageResource(book.imageResId)
            scaleType = ImageView.ScaleType.CENTER_CROP
            setBackgroundResource(R.drawable.cover_shadow)
            setPadding(1, 1, 1, 1)
            layoutParams = LinearLayout.LayoutParams(dp(98), dp(136))
        }

        val title = TextView(this).apply {
            text = book.title
            setTextColor(getColor(R.color.book_ink))
            textSize = 14f
            maxLines = 2
            ellipsize = android.text.TextUtils.TruncateAt.END
            setPadding(0, dp(8), 0, 0)
        }

        card.addView(cover)
        card.addView(title)
        return card
    }

    private fun openBookDetail(book: Book) {
        // 상세 화면이 필요한 값을 extra로 모두 넘긴다.
        val intent = Intent(this, BookDetailActivity::class.java)
        intent.putExtra("book_id", book.id)
        intent.putExtra("book_title", book.title)
        intent.putExtra("book_author", book.author)
        intent.putExtra("book_original_price", book.originalPrice)
        intent.putExtra("book_price", book.price)
        intent.putExtra("book_date", book.publishedDate)
        intent.putExtra("book_condition", book.condition)
        intent.putExtra("book_shipping", book.shippingInfo)
        intent.putExtra("book_major", book.major)
        intent.putExtra("book_description", book.description)
        intent.putExtra("book_rating", book.rating)
        intent.putExtra("book_review_count", book.reviewCount)
        intent.putExtra("book_sales_index", book.salesIndex)
        intent.putExtra("book_image", book.imageResId)
        startActivity(intent)
    }

    private fun dp(value: Int): Int {
        // 코드로 만든 카드 크기를 dp 기준으로 맞추기 위한 변환 함수다.
        return (value * resources.displayMetrics.density).toInt()
    }

    private fun showLoginInfo() {
        // 저장된 로그인 상태에 따라 프로필 문구와 버튼 이름을 바꾼다.
        val prefs = getSharedPreferences("book_market", MODE_PRIVATE)
        val isLoggedIn = prefs.getBoolean("is_logged_in", false)
        val memberName = prefs.getString("member_name", "")
        val memberId = prefs.getString("member_id", "")

        if (isLoggedIn) {
            binding.profileNameText.text = getString(R.string.my_profile_logged_in_format, memberName)
            binding.loginStatusText.text = getString(R.string.login_status_format, memberName, memberId)
            binding.loginButton.text = getString(R.string.logout_button)
        } else {
            binding.profileNameText.text = getString(R.string.my_profile_login_required)
            binding.loginStatusText.text = getString(R.string.my_profile_login_required_message)
            binding.loginButton.text = getString(R.string.login_button)
        }
    }

    private fun handleLoginButton() {
        // 로그인 상태라면 로그아웃, 아니라면 로그인 화면 이동으로 같은 버튼을 나눠 쓴다.
        val prefs = getSharedPreferences("book_market", MODE_PRIVATE)
        val isLoggedIn = prefs.getBoolean("is_logged_in", false)

        if (isLoggedIn) {
            // 로그아웃은 저장된 로그인 값만 지우면 된다.
            prefs.edit()
                .putBoolean("is_logged_in", false)
                .remove("member_id")
                .remove("member_name")
                .apply()
            Toast.makeText(this, getString(R.string.logout_message), Toast.LENGTH_SHORT).show()
            showLoginInfo()
        } else {
            // 로그인하지 않은 상태에서는 로그인 화면으로 이동한다.
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    private fun showCartInfo(cartItems: List<CartItem>) {
        // 장바구니 요약은 권수와 상품 줄 목록을 따로 보여 준다.
        binding.cartCountText.text = getString(R.string.cart_count_format, cartItems.sumOf { it.quantity })
        binding.cartSummaryText.text = if (cartItems.isEmpty()) {
            getString(R.string.cart_empty)
        } else {
            cartItems.joinToString(separator = "\n") { item ->
                "${item.book.title} / ${item.quantity}개 / ${item.subtotal}원"
            }
        }
    }

    private fun openJnuHomePage() {
        // 외부 홈페이지는 기본 브라우저로 열고, 열 앱이 없으면 안내한다.
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.jnu_home_url)))
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, getString(R.string.open_external_error), Toast.LENGTH_SHORT).show()
        }
    }

    private fun openAladinBestPage() {
        // 참고용 도서 사이트 링크도 외부 브라우저로 보낸다.
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.aladin_best_url)))
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, getString(R.string.open_external_error), Toast.LENGTH_SHORT).show()
        }
    }

    private fun showAppInfo() {
        // 앱 정보는 내서재에서도 같은 문구의 Dialog를 사용한다.
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.app_info_title))
            .setMessage(getString(R.string.app_info_message))
            .setPositiveButton(getString(R.string.dialog_ok), null)
            .show()
    }
}
