package com.example.yongbongbookmarket

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.yongbongbookmarket.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar

/**
 * 앱을 켠 뒤 가장 먼저 만나는 홈 화면.
 * Toolbar, NavigationDrawer, Intent 이동을 한 화면에서 연결한다.
 */
class MainActivity : AppCompatActivity() {

    companion object {
        private const val MENU_MEMO_ID = 3001
    }

    private data class CuratorPick(
        val label: String,
        val book: Book,
        val oneLineReview: String
    )

    private lateinit var binding: ActivityMainBinding
    private lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 홈 화면 XML의 View들을 binding으로 연결한다.
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = getString(R.string.app_name)

        // Toolbar 왼쪽 버튼으로 Drawer 메뉴를 열 수 있게 묶었다.
        toggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            R.string.drawer_opened,
            R.string.drawer_closed
        )
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        binding.goListButton.setOnClickListener {
            startActivity(Intent(this, BookListActivity::class.java))
        }

        binding.cartButton.setOnClickListener {
            startActivity(Intent(this, CartActivity::class.java))
        }

        binding.curatorBookArea.setOnClickListener {
            openBookDetail(BookRepository.books[1])
        }

        binding.homeRandomTab.setOnClickListener {
            openRandomBook()
        }

        binding.lastBookButton.setOnClickListener {
            openLastBook()
        }

        binding.loginShortcutButton.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        renderCuratorMoreBooks()
        renderViewedBooks()
        setupBottomNavigation()
        setupDrawerMenu()
    }

    override fun onResume() {
        super.onResume()
        // 상세 화면을 보고 돌아오면 최근 본 상품이 바뀌었을 수 있어 다시 그린다.
        renderViewedBooks()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // 공통 메뉴를 붙이고, 홈에서도 메모를 바로 열 수 있게 항목을 하나 더한다.
        menuInflater.inflate(R.menu.menu_toolbar, menu)
        menu?.add(0, MENU_MEMO_ID, 3, getString(R.string.menu_book_memo))
            ?.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // 왼쪽 Drawer 버튼과 오른쪽 Toolbar 메뉴가 같은 함수로 들어오므로 먼저 Drawer인지 확인한다.
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return when (item.itemId) {
            R.id.menu_home -> {
                Toast.makeText(this, getString(R.string.current_home), Toast.LENGTH_SHORT).show()
                true
            }
            R.id.menu_book_list -> {
                showMenuMoveToast(getString(R.string.menu_book_list))
                startActivity(Intent(this, BookListActivity::class.java))
                true
            }
            R.id.menu_cart -> {
                showMenuMoveToast(getString(R.string.menu_cart))
                startActivity(Intent(this, CartActivity::class.java))
                true
            }
            R.id.menu_my_page -> {
                showMenuMoveToast(getString(R.string.menu_my_page))
                startActivity(Intent(this, MyPageActivity::class.java))
                true
            }
            R.id.menu_jnu_site -> {
                showMenuMoveToast(getString(R.string.menu_jnu_site))
                openJnuHomePage()
                true
            }
            R.id.menu_aladin_best -> {
                showMenuMoveToast(getString(R.string.menu_aladin_best))
                openAladinBestPage()
                true
            }
            R.id.menu_app_info -> {
                Toast.makeText(this, getString(R.string.option_app_info_toast), Toast.LENGTH_SHORT).show()
                showAppInfo()
                true
            }
            MENU_MEMO_ID -> {
                showMemoDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupDrawerMenu() {
        // 왼쪽 Drawer 메뉴의 각 항목을 실제 화면 이동과 연결한다.
        binding.mainDrawerView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_home -> {
                    Toast.makeText(this, getString(R.string.current_home), Toast.LENGTH_SHORT).show()
                }
                R.id.menu_book_list -> {
                    showMenuMoveToast(getString(R.string.menu_book_list))
                    startActivity(Intent(this, BookListActivity::class.java))
                }
                R.id.menu_cart -> {
                    showMenuMoveToast(getString(R.string.menu_cart))
                    startActivity(Intent(this, CartActivity::class.java))
                }
                R.id.menu_memo -> {
                    showMemoDialog()
                }
                R.id.menu_my_page -> {
                    showMenuMoveToast(getString(R.string.menu_my_page))
                    startActivity(Intent(this, MyPageActivity::class.java))
                }
                R.id.menu_jnu_site -> {
                    showMenuMoveToast(getString(R.string.menu_jnu_site))
                    openJnuHomePage()
                }
                R.id.menu_aladin_best -> {
                    showMenuMoveToast(getString(R.string.menu_aladin_best))
                    openAladinBestPage()
                }
                R.id.menu_app_info -> {
                    Toast.makeText(this, getString(R.string.option_app_info_toast), Toast.LENGTH_SHORT).show()
                    showAppInfo()
                }
            }
            binding.drawerLayout.closeDrawers()
            true
        }
    }

    private fun openLastBook() {
        // 상세 화면에서 저장한 마지막 책 id를 읽어 다시 상세 화면으로 보낸다.
        val book = RecentBookRepository.getLastBook(this)

        if (book == null) {
            Snackbar.make(binding.root, getString(R.string.no_last_book), Snackbar.LENGTH_SHORT).show()
        } else {
            openBookDetail(book)
        }
    }

    private fun openRandomBook() {
        // 추천 탭을 누르면 임의의 책을 하나 고르고, 상세 화면에 추천 이유도 같이 넘긴다.
        val randomBook = BookRepository.books.random()
        val reason = BookRecommendationReason.make(this, randomBook)
        openBookDetail(randomBook, reason)
    }

    private fun showMenuMoveToast(menuName: String) {
        // 메뉴를 눌렀을 때 사용자가 이동 동작을 바로 알아볼 수 있게 짧게 알린다.
        Toast.makeText(this, getString(R.string.menu_move_format, menuName), Toast.LENGTH_SHORT).show()
    }

    private fun renderViewedBooks() {
        // 마지막에 본 상품 카드와 최근 본 상품 가로 목록을 함께 갱신한다.
        renderLastViewedBook()
        renderRecentViewedBooks()
    }

    private fun renderCuratorMoreBooks() {
        // 홈의 큐레이터 추천 영역은 실행할 때마다 후보 중 일부를 골라 만든다.
        binding.curatorMoreContainer.removeAllViews()
        val picks = listOfNotNull(
            pickBook(10, 11, 12)?.let {
                CuratorPick("만화 추천", it, "짧게 읽어도 캐릭터의 결이 바로 살아나는 책.")
            },
            pickBook(13, 16)?.let {
                CuratorPick("경제경영 추천", it, "숫자보다 먼저 흐름을 읽게 해 주는 책.")
            }
        )

        picks.forEach { pick ->
            binding.curatorMoreContainer.addView(createCuratorMiniCard(pick))
        }
    }

    private fun pickBook(vararg indexes: Int): Book? {
        // 지정한 후보 중 실제로 존재하는 책만 모은 뒤 하나를 고른다.
        return indexes.toList().mapNotNull { index ->
            BookRepository.books.getOrNull(index)
        }.shuffled().firstOrNull()
    }

    private fun createCuratorMiniCard(pick: CuratorPick): LinearLayout {
        // XML 파일을 따로 만들지 않고 코드에서 작은 추천 카드를 만든다.
        val book = pick.book
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
            setBackgroundResource(R.drawable.panel_feed_card)
            setPadding(dp(12), dp(12), dp(12), dp(12))
            isClickable = true
            isFocusable = true
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dp(10)
            }
            setOnClickListener {
                openBookDetail(pick.book)
            }
        }

        val cover = ImageView(this).apply {
            setImageResource(pick.book.imageResId)
            scaleType = ImageView.ScaleType.CENTER_CROP
            setBackgroundResource(R.drawable.cover_shadow)
            setPadding(1, 1, 1, 1)
            layoutParams = LinearLayout.LayoutParams(dp(58), dp(82))
        }

        val textBox = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                marginStart = dp(12)
            }
        }

        val label = TextView(this).apply {
            text = "${book.major} 추천"
            setTextColor(getColor(R.color.book_gray))
            textSize = 12f
            maxLines = 1
            ellipsize = android.text.TextUtils.TruncateAt.END
        }

        val title = TextView(this).apply {
            text = book.title
            setTextColor(getColor(R.color.book_ink))
            textSize = 17f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            maxLines = 2
            ellipsize = android.text.TextUtils.TruncateAt.END
            setPadding(0, dp(4), 0, 0)
        }

        val author = TextView(this).apply {
            text = book.author
            setTextColor(getColor(R.color.book_gray))
            textSize = 13f
            maxLines = 1
            ellipsize = android.text.TextUtils.TruncateAt.END
            setPadding(0, dp(5), 0, 0)
        }

        val review = TextView(this).apply {
            text = "“${pick.oneLineReview}”"
            setTextColor(getColor(R.color.book_ink))
            textSize = 13f
            maxLines = 2
            ellipsize = android.text.TextUtils.TruncateAt.END
            setPadding(0, dp(7), 0, 0)
        }

        textBox.addView(label)
        textBox.addView(title)
        textBox.addView(author)
        textBox.addView(review)
        card.addView(cover)
        card.addView(textBox)
        return card
    }

    private fun renderLastViewedBook() {
        val book = RecentBookRepository.getLastBook(this)
        if (book == null) {
            // 아직 본 책이 없으면 목록 화면으로 안내하는 기본 카드를 보여 준다.
            binding.lastViewedCover.setImageResource(R.drawable.book_cover_01)
            binding.lastViewedTitle.text = getString(R.string.home_no_last_viewed)
            binding.lastViewedMeta.text = getString(R.string.home_last_viewed_empty_meta)
            binding.lastViewedCard.setOnClickListener {
                startActivity(Intent(this, BookListActivity::class.java))
            }
            return
        }

        binding.lastViewedCover.setImageResource(book.imageResId)
        binding.lastViewedTitle.text = book.title
        binding.lastViewedMeta.text = "${book.author} · ${book.major}"
        binding.lastViewedCard.setOnClickListener {
            openBookDetail(book)
        }
    }

    private fun renderRecentViewedBooks() {
        // 최근 본 상품 목록은 저장된 순서를 기준으로 가로 카드들을 다시 만든다.
        binding.recentBooksContainer.removeAllViews()
        val recentBooks = RecentBookRepository.getRecentBooks(this)

        if (recentBooks.isEmpty()) {
            // 최근 본 상품이 없을 때도 화면이 비어 보이지 않도록 안내 문구를 넣는다.
            val emptyText = TextView(this).apply {
                gravity = android.view.Gravity.CENTER
                setBackgroundResource(R.drawable.panel_soft_round)
                text = getString(R.string.home_recent_viewed_empty)
                gravity = android.view.Gravity.CENTER
                setBackgroundResource(R.drawable.panel_soft_round)
                setTextColor(getColor(R.color.book_gray))
                textSize = 14f
                setPadding(4, 10, 4, 16)
            }
            binding.recentBooksContainer.addView(emptyText)
            return
        }

        recentBooks.forEach { book ->
            binding.recentBooksContainer.addView(createRecentBookCard(book))
        }
    }

    private fun createRecentBookCard(book: Book): LinearLayout {
        // 최근 본 상품 가로 목록에 들어갈 작은 책 카드 한 장을 만든다.
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundResource(R.drawable.panel_feed_card)
            setPadding(dp(10), dp(10), dp(10), dp(10))
            isClickable = true
            isFocusable = true
            layoutParams = LinearLayout.LayoutParams(dp(124), LinearLayout.LayoutParams.WRAP_CONTENT).apply {
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
            layoutParams = LinearLayout.LayoutParams(dp(104), dp(144))
        }

        val title = TextView(this).apply {
            text = book.title
            setTextColor(getColor(R.color.book_ink))
            textSize = 14f
            maxLines = 2
            ellipsize = android.text.TextUtils.TruncateAt.END
            setPadding(0, dp(8), 0, 0)
        }

        val author = TextView(this).apply {
            text = book.author
            setTextColor(getColor(R.color.book_gray))
            textSize = 12f
            maxLines = 1
            ellipsize = android.text.TextUtils.TruncateAt.END
            setPadding(0, dp(4), 0, 0)
        }

        card.addView(cover)
        card.addView(title)
        card.addView(author)
        return card
    }

    private fun dp(value: Int): Int {
        // 코드로 만든 View는 dp 단위를 직접 못 쓰므로 화면 밀도에 맞춰 px로 바꾼다.
        return (value * resources.displayMetrics.density).toInt()
    }

    private fun setupBottomNavigation() {
        // 하단 바의 다섯 영역을 각각 홈/목록/검색/메모/내서재 동작에 연결한다.
        binding.bottomNav.navTodayButton.setOnClickListener {
            Toast.makeText(this, getString(R.string.current_home), Toast.LENGTH_SHORT).show()
        }
        binding.bottomNav.navMarketButton.setOnClickListener {
            startActivity(Intent(this, BookListActivity::class.java))
        }
        binding.bottomNav.navSearchButton.setOnClickListener {
            val intent = Intent(this, BookListActivity::class.java)
            intent.putExtra("open_search", true)
            startActivity(intent)
        }
        binding.bottomNav.navFeedButton.setOnClickListener {
            showMemoDialog()
        }
        binding.bottomNav.navLibraryButton.setOnClickListener {
            startActivity(Intent(this, MyPageActivity::class.java))
        }
    }

    private fun showMemoDialog() {
        // 홈에서도 바로 독서 메모를 확인하고 추가할 수 있게 Dialog를 띄운다.
        val dialogView = layoutInflater.inflate(R.layout.dialog_memo, null)
        val memoContainer = dialogView.findViewById<LinearLayout>(R.id.memo_list_container)
        val memoEdit = dialogView.findViewById<EditText>(R.id.memo_edit)
        renderMemoList(memoContainer)

        val dialog = AlertDialog.Builder(this)
            .setTitle(getString(R.string.memo_dialog_title))
            .setView(dialogView)
            .setPositiveButton(getString(R.string.memo_add_button), null)
            .setNegativeButton(getString(R.string.memo_close_button), null)
            .setNeutralButton(getString(R.string.memo_delete_all_button)) { _, _ ->
                MemoRepository.clear(this)
                Toast.makeText(this, getString(R.string.memo_clear_all_toast), Toast.LENGTH_SHORT).show()
            }
            .show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            // 저장 버튼을 눌러도 Dialog는 유지해서 여러 문장을 이어서 적을 수 있다.
            MemoRepository.addMemo(this, memoEdit.text.toString())
            memoEdit.text.clear()
            renderMemoList(memoContainer)
            Toast.makeText(this, getString(R.string.memo_added_toast), Toast.LENGTH_SHORT).show()
        }
    }

    private fun renderMemoList(container: LinearLayout) {
        // 메모 Dialog 안의 목록은 저장소 내용을 기준으로 매번 다시 구성한다.
        container.removeAllViews()
        val memos = MemoRepository.getMemoItems(this)

        if (memos.isEmpty()) {
            // 빈 목록 대신 안내 문구를 보여 주면 사용자가 무엇을 해야 할지 알기 쉽다.
            val emptyText = TextView(this).apply {
                text = getString(R.string.memo_empty)
                setTextColor(getColor(R.color.book_gray))
                textSize = 14f
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
            // Dialog 안에 들어가는 작은 메모 행을 코드로 만든다.
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
            val textColumn = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(12), 0, dp(8), 0)
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }
            val titleText = TextView(this).apply {
                text = memo.titleLabel
                setTextColor(getColor(R.color.jnu_green_dark))
                textSize = 12f
                typeface = android.graphics.Typeface.DEFAULT_BOLD
                maxLines = 1
                ellipsize = android.text.TextUtils.TruncateAt.END
            }
            val memoText = TextView(this).apply {
                text = memo.text
                setTextColor(getColor(R.color.book_ink))
                textSize = 14f
                maxLines = 4
                ellipsize = android.text.TextUtils.TruncateAt.END
                setPadding(0, dp(3), 0, 0)
            }
            val deleteText = TextView(this).apply {
                text = getString(R.string.memo_delete_button)
                gravity = android.view.Gravity.CENTER
                setBackgroundResource(R.drawable.panel_feed_card)
                setTextColor(getColor(R.color.book_gray))
                textSize = 13f
                setPadding(dp(12), dp(7), dp(12), dp(7))
                setOnClickListener {
                    MemoRepository.removeMemo(this@MainActivity, index)
                    renderMemoList(container)
                }
            }
            textColumn.addView(titleText)
            textColumn.addView(memoText)
            row.addView(numberText)
            row.addView(textColumn)
            row.addView(deleteText)
            container.addView(row)
        }
    }

    private fun openBookDetail(book: Book, recommendationReason: String = "") {
        // 홈의 여러 카드에서 공통으로 쓰는 상세 화면 이동 함수다.
        // 상세 화면에서 바로 그릴 수 있도록 필요한 값만 넘긴다.
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
        intent.putExtra("recommendation_reason", recommendationReason)
        startActivity(intent)
    }

    private fun openJnuHomePage() {
        // 외부 사이트는 ACTION_VIEW Intent로 기본 브라우저에 맡긴다.
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.jnu_home_url)))
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, getString(R.string.open_external_error), Toast.LENGTH_SHORT).show()
        }
    }

    private fun openAladinBestPage() {
        // 도서 정보 출처로 참고한 베스트 페이지를 열 때 사용한다.
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.aladin_best_url)))
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, getString(R.string.open_external_error), Toast.LENGTH_SHORT).show()
        }
    }

    private fun showAppInfo() {
        // 앱 이름과 성격을 간단히 보여 주는 안내 Dialog다.
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.app_info_title))
            .setMessage(getString(R.string.app_info_message))
            .setPositiveButton(getString(R.string.dialog_ok), null)
            .show()
    }
}
