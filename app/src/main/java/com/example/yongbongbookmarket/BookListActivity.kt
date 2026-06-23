package com.example.yongbongbookmarket

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.yongbongbookmarket.databinding.ActivityBookListBinding

// 목록 화면 전체는 ScrollView가 움직이므로, 안쪽 RecyclerView는 자체 스크롤을 끈다.
private class NoScrollGridLayoutManager(context: Context, spanCount: Int) : GridLayoutManager(context, spanCount) {
    override fun canScrollVertically(): Boolean = false
}

/**
 * 도서 상품을 모아 보여 주는 목록 화면.
 * 검색 입력, RecyclerView, 카테고리 필터, Intent 이동을 한 화면에 묶었다.
 */
class BookListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBookListBinding
    private lateinit var bookAdapter: BookAdapter
    private var shouldOpenSearch = false
    private val gridSpanCount = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 목록 화면 XML과 코드를 연결한다.
        binding = ActivityBookListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = ""
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        shouldOpenSearch = intent.getBooleanExtra("open_search", false)

        // 목록 바깥쪽 ScrollView가 전체 화면을 움직이고, RecyclerView는 카드 배치만 맡긴다.
        binding.bookRecyclerView.layoutManager = NoScrollGridLayoutManager(this, gridSpanCount)
        binding.bookRecyclerView.setHasFixedSize(false)
        bookAdapter = BookAdapter(BookRepository.books.toMutableList()) { book ->
            // 카드 한 장을 누르면 해당 책의 상세 화면으로 이동한다.
            openBookDetail(book)
        }
        binding.bookRecyclerView.adapter = bookAdapter
        updateBookListHeight(BookRepository.books.size)

        setupSearchInput()
        setupKeywordButtons()
        setupStatusFilters()
        setupCategoryFilters()
        setupBottomNavigation()

        if (shouldOpenSearch) {
            // 홈의 검색 버튼에서 들어온 경우 키보드가 바로 올라오게 한다.
            binding.searchEditText.post {
                focusSearchInput()
                shouldOpenSearch = false
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // 목록 화면 전용 Toolbar 메뉴를 붙인다.
        menuInflater.inflate(R.menu.menu_book_list, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Toolbar 메뉴에서 누른 항목에 맞춰 화면 이동이나 안내를 처리한다.
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
                Toast.makeText(this, getString(R.string.current_book_list), Toast.LENGTH_SHORT).show()
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
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupSearchInput() {
        // 검색창 주변 영역을 눌러도 EditText에 포커스가 가도록 했다.
        binding.searchSurface.setOnClickListener {
            focusSearchInput()
        }

        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // 글자를 입력할 때마다 목록을 다시 걸러서 보여 준다.
                filterBooks(s?.toString())
            }

            override fun afterTextChanged(s: Editable?) = Unit
        })
    }

    private fun setupKeywordButtons() {
        // 추천 검색어와 바로가기 버튼은 검색어 입력 또는 별도 목록 표시로 연결된다.
        binding.recommendSearchTitle.setOnClickListener {
            Toast.makeText(this, getString(R.string.recommend_search_toast), Toast.LENGTH_SHORT).show()
        }
        binding.keywordAndroid.setOnClickListener {
            setSearchKeyword(getString(R.string.list_keyword_android))
        }
        binding.keywordKotlin.setOnClickListener {
            setSearchKeyword(getString(R.string.list_keyword_kotlin))
        }
        binding.keywordCheap.setOnClickListener {
            clearSearchText()
            val usedSpecialBooks = BookRepository.books
                .filter { book -> isUsedSpecialBook(book) }
                .sortedBy { it.price }
            showBooks("중고 특가", usedSpecialBooks)
        }
        binding.keywordClean.setOnClickListener {
            clearSearchText()
            val cleanBooks = BookRepository.books.filter { book -> isCleanUsedBook(book) }
            showBooks("상태 좋은 책", cleanBooks)
        }
        binding.shortcutRanking.setOnClickListener {
            showRankingBooks()
        }
        binding.shortcutRankingIcon.setOnClickListener {
            showRankingBooks()
        }
        binding.shortcutRandom.setOnClickListener {
            openRandomBook()
        }
        binding.shortcutRandomIcon.setOnClickListener {
            openRandomBook()
        }
    }

    private fun setupStatusFilters() {
        // 상태별 보기와 정렬 버튼은 같은 BookRepository 목록을 다른 기준으로 보여 준다.
        binding.statusAll.setOnClickListener {
            showAllBooks()
        }
        binding.statusNew.setOnClickListener {
            clearSearchText()
            val newBooks = BookRepository.books.filter { book -> isNewCondition(book) }
            showBooks(getString(R.string.status_new), newBooks)
        }
        binding.statusUsed.setOnClickListener {
            clearSearchText()
            val usedBooks = BookRepository.books.filter { book -> !isNewCondition(book) }
            showBooks(getString(R.string.status_used), usedBooks)
        }
        binding.statusMarked.setOnClickListener {
            clearSearchText()
            val markedBooks = BookRepository.books.filter { book ->
                book.condition.contains("필기", ignoreCase = true) ||
                    book.condition.contains("밑줄", ignoreCase = true) ||
                    book.condition.contains("표시", ignoreCase = true)
            }
            showBooks(getString(R.string.status_marked), markedBooks)
        }
        binding.statusReserved.setOnClickListener {
            clearSearchText()
            val reservedBooks = BookRepository.books.filter { book ->
                book.condition.contains("예약", ignoreCase = true) ||
                    book.shippingInfo.contains("예약", ignoreCase = true)
            }
            showBooks(getString(R.string.status_reserved), reservedBooks)
        }
        binding.sortPriceLow.setOnClickListener {
            clearSearchText()
            val sortedBooks = BookRepository.books.sortedBy { book -> book.price }
            showBooks(getString(R.string.sort_price_low), sortedBooks)
        }
        binding.sortPriceHigh.setOnClickListener {
            clearSearchText()
            val sortedBooks = BookRepository.books.sortedByDescending { book -> book.price }
            showBooks(getString(R.string.sort_price_high), sortedBooks)
        }
        binding.sortTitle.setOnClickListener {
            clearSearchText()
            val sortedBooks = BookRepository.books.sortedBy { book -> book.title }
            showBooks(getString(R.string.sort_title), sortedBooks)
        }
    }

    private fun filterBooks(keyword: String?) {
        val searchText = keyword?.trim() ?: ""
        // 검색어가 없으면 전체 목록, 있으면 제목/저자/분야/설명에서 찾는다.
        val filteredBooks = if (searchText.isEmpty()) {
            BookRepository.books
        } else {
            BookRepository.books.filter { book ->
                book.title.contains(searchText, ignoreCase = true) ||
                    book.author.contains(searchText, ignoreCase = true) ||
                    book.major.contains(searchText, ignoreCase = true) ||
                    book.description.contains(searchText, ignoreCase = true)
            }
        }

        bookAdapter.updateBooks(filteredBooks)
        updateBookListHeight(filteredBooks.size)
        // 현재 화면이 전체 목록인지 검색 결과인지 안내 문구를 바꾼다.
        binding.listGuide.text = if (searchText.isEmpty()) {
            getString(R.string.list_guide)
        } else {
            getString(R.string.search_result_format, searchText, filteredBooks.size)
        }
        moveListToTop()
    }

    private fun openRandomBook() {
        // 무작위 책을 고른 뒤 상세 화면에서 추천 이유를 Toast로 보여 준다.
        val randomBook = BookRepository.books.random()
        val reason = BookRecommendationReason.make(this, randomBook)
        openBookDetail(randomBook, reason)
    }

    private fun showRankingBooks() {
        clearSearchText()
        // 판매지수가 같으면 별점이 높은 책이 먼저 보이게 한 번 더 정렬한다.
        val rankedBooks = BookRepository.books.sortedWith(
            compareByDescending<Book> { it.salesIndex }.thenByDescending { it.rating }
        )
        showBooks("판매지수 높은 도서", rankedBooks)
    }

    private fun isUsedSpecialBook(book: Book): Boolean {
        // 중고 특가 검색어에서는 새책을 제외한 상품만 보여 준다.
        return !isNewCondition(book)
    }

    private fun isCleanUsedBook(book: Book): Boolean {
        // 상태가 좋아도 새책이면 중고 추천검색에는 넣지 않는다.
        if (isNewCondition(book)) return false

        return book.condition.contains("상태 좋음", ignoreCase = true) ||
            book.condition.contains("깨끗", ignoreCase = true) ||
            book.condition.contains("낙서 없음", ignoreCase = true) ||
            book.condition.contains("접힘 없음", ignoreCase = true)
    }

    private fun isNewCondition(book: Book): Boolean {
        // 새책 여부는 별도 표시값과 상태 문구를 함께 확인한다.
        return book.isNewBook || book.condition.contains("새책", ignoreCase = true)
    }

    private fun focusSearchInput() {
        // 검색 탭으로 들어왔을 때 사용자가 바로 입력할 수 있게 키보드를 연다.
        binding.searchEditText.requestFocus()
        val inputMethod = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethod.showSoftInput(binding.searchEditText, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun showBooks(label: String, books: List<Book>) {
        // 필터 버튼들이 공통으로 쓰는 목록 갱신 함수다.
        bookAdapter.updateBooks(books)
        updateBookListHeight(books.size)
        binding.listGuide.text = "$label 도서: ${books.size}권"
        moveListToTop()
    }

    private fun updateBookListHeight(bookCount: Int) {
        // RecyclerView 스크롤을 꺼 두었기 때문에, 책 개수에 맞춰 높이를 직접 늘려 준다.
        val rowCount = ((bookCount + gridSpanCount - 1) / gridSpanCount).coerceAtLeast(1)
        // item_book.xml의 카드 높이 380dp와 위아래 margin 7dp를 더해 한 줄 높이를 맞춘다.
        val oneRowHeight = dp(394)
        val verticalPadding = dp(40)
        binding.bookRecyclerView.layoutParams = binding.bookRecyclerView.layoutParams.apply {
            height = rowCount * oneRowHeight + verticalPadding
        }
        binding.bookRecyclerView.requestLayout()
    }

    private fun moveListToTop() {
        // 필터를 누르면 이전 스크롤 위치가 남지 않도록 목록 제목 근처로 올린다.
        binding.bookRecyclerView.scrollToPosition(0)
        binding.bookScrollView.post {
            binding.bookScrollView.smoothScrollTo(0, binding.listGuide.top)
        }
    }

    private fun showAllBooks() {
        // 검색어나 필터가 남아 있으면 전체 목록처럼 보이지 않아서 먼저 비운다.
        clearSearchText()
        showBooks("전체", BookRepository.books)
    }

    private fun setSearchKeyword(keyword: String) {
        // 추천 검색어 버튼을 누르면 직접 입력한 것처럼 EditText에 넣는다.
        binding.searchEditText.setText(keyword)
        binding.searchEditText.setSelection(binding.searchEditText.text.length)
    }

    private fun clearSearchText() {
        // 필터 버튼을 눌렀을 때 이전 검색어가 결과에 섞이지 않게 지운다.
        if (binding.searchEditText.text.isNotEmpty()) {
            binding.searchEditText.setText("")
        }
        binding.searchEditText.clearFocus()
    }

    private fun dp(value: Int): Int {
        // XML이 아닌 코드에서 여백을 줄 때 dp를 px로 바꾸는 helper다.
        return (value * resources.displayMetrics.density).toInt()
    }

    private fun setupCategoryFilters() {
        // 분야 버튼은 major 문자열에 들어 있는 단어를 기준으로 책을 고른다.
        binding.categoryAll.setOnClickListener {
            showAllBooks()
        }
        binding.categoryIt.setOnClickListener {
            clearSearchText()
            val majorBooks = BookRepository.books.filter { book ->
                book.major.contains("Kotlin", ignoreCase = true) ||
                    book.major.contains("Android", ignoreCase = true) ||
                    book.major.contains("프로그래밍", ignoreCase = true) ||
                    book.major.contains("설계", ignoreCase = true)
            }
            showBooks("전공/IT", majorBooks)
        }
        binding.categoryKotlin.setOnClickListener {
            showCategory("Kotlin")
        }
        binding.categoryAndroid.setOnClickListener {
            showCategory("Android")
        }
        binding.categoryComic.setOnClickListener {
            showCategory("만화")
        }
        binding.categoryNovel.setOnClickListener {
            showCategory("소설")
        }
        binding.categoryEconomy.setOnClickListener {
            showCategory("경제경영")
        }
        binding.categoryWorld.setOnClickListener {
            showCategory("세계문학")
        }
        binding.categoryGenre.setOnClickListener {
            showCategory("장르소설")
        }
        binding.categoryScript.setOnClickListener {
            showCategory("대본집")
        }
    }

    private fun showCategory(category: String) {
        clearSearchText()
        // 카테고리 이름이 책의 major에 포함되어 있으면 같은 묶음으로 보여 준다.
        val filteredBooks = BookRepository.books.filter { book ->
            book.major.contains(category, ignoreCase = true)
        }
        showBooks(category, filteredBooks)
    }

    private fun openBookDetail(book: Book, recommendationReason: String = "") {
        // 상세 화면은 Book 객체를 직접 받지 못하므로 필요한 값을 extra로 하나씩 보낸다.
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

    private fun setupBottomNavigation() {
        // 하단 바는 별도 라이브러리 없이 클릭 이벤트와 Intent만으로 처리했다.
        binding.bottomNav.navTodayButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
        }
        binding.bottomNav.navMarketButton.setOnClickListener {
            showAllBooks()
            Toast.makeText(this, getString(R.string.all_books_reset_toast), Toast.LENGTH_SHORT).show()
        }
        binding.bottomNav.navSearchButton.setOnClickListener {
            focusSearchInput()
        }
        binding.bottomNav.navFeedButton.setOnClickListener {
            showMemoDialog()
        }
        binding.bottomNav.navLibraryButton.setOnClickListener {
            startActivity(Intent(this, MyPageActivity::class.java))
        }
    }

    private fun showMemoDialog() {
        // 하단 피드 버튼에서 여는 일반 독서 메모 Dialog다.
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
            // Dialog를 닫지 않고 바로 목록을 갱신해서 여러 메모를 이어서 쓸 수 있다.
            MemoRepository.addMemo(this, memoEdit.text.toString())
            memoEdit.text.clear()
            renderMemoList(memoContainer)
            Toast.makeText(this, getString(R.string.memo_added_toast), Toast.LENGTH_SHORT).show()
        }
    }

    private fun renderMemoList(container: LinearLayout) {
        // Dialog가 다시 열릴 때마다 이전 View를 지우고 저장된 메모를 새로 그린다.
        container.removeAllViews()
        val memos = MemoRepository.getMemoItems(this)

        if (memos.isEmpty()) {
            // 저장된 메모가 없을 때 빈 영역만 보이면 어색해서 안내 문구를 넣는다.
            val emptyText = TextView(this).apply {
                text = getString(R.string.memo_empty)
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
            // 메모 목록은 XML 항목을 따로 만들지 않고 Dialog 안에서 작게 직접 만든다.
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
                    MemoRepository.removeMemo(this@BookListActivity, index)
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

    private fun openJnuHomePage() {
        // 학교 홈페이지는 앱 안 화면이 아니라 브라우저로 열어 준다.
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.jnu_home_url)))
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, getString(R.string.open_external_error), Toast.LENGTH_SHORT).show()
        }
    }

    private fun openAladinBestPage() {
        // 알라딘 베스트 참고 페이지도 외부 브라우저로 연다.
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.aladin_best_url)))
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, getString(R.string.open_external_error), Toast.LENGTH_SHORT).show()
        }
    }

    private fun showAppInfo() {
        // 앱 소개는 짧게 확인할 수 있도록 Dialog로 띄운다.
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.app_info_title))
            .setMessage(getString(R.string.app_info_message))
            .setPositiveButton(getString(R.string.dialog_ok), null)
            .show()
    }
}
