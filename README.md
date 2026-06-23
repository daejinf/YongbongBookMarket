====================================================================
[모바일 응용 소프트웨어(2) 기말 프로젝트 제출]
전대진
====================================================================

### 1. 앱 기획 및 테마 설명

- 앱 이름: 용봉문고
- 테마: 중고 도서와 신간 도서를 함께 다루는 모바일 책 쇼핑몰
- 기획 의도: 전공서만 판매하는 앱이 아니라, Kotlin/Android 도서부터 만화, 소설, 경제경영, 세계문학, 장르소설, 대본집, 에세이까지 여러 분야의 책을 한곳에서 둘러볼 수 있는 도서 쇼핑몰 앱으로 기획했습니다. 사용자는 홈 화면에서 추천 도서와 최근 본 상품을 확인하고, 목록 화면에서 검색/필터/정렬 기능으로 책을 찾은 뒤 상세 화면에서 장바구니 담기 또는 바로구매 흐름으로 주문할 수 있습니다.

### 2. 개발 및 실행 환경

- IDE: Android Studio
- 언어: Kotlin
- 화면 구성: XML + ViewBinding
- 저장 방식: SharedPreferences
- Min SDK: 26
- Target SDK: 36
- Android Gradle Plugin: 9.0.1
- Kotlin: 2.2.10
- 사용 라이브러리: AppCompat, Material, ConstraintLayout, RecyclerView, CardView, DrawerLayout

현재 프로젝트는 Android Gradle Plugin 9.0.1의 built-in Kotlin 설정을 사용합니다. 따라서 app 모듈에 `org.jetbrains.kotlin.android` 플러그인을 따로 추가하지 않았습니다.

### 3. 주요 Activity 및 XML 구성

[시작 및 홈]
- `SplashActivity`: 앱 실행 후 2.5초 동안 스플래시 화면 표시 후 홈으로 이동
- `MainActivity`: 로고, 추천 도서, 랜덤 추천, 마지막에 본 상품, 최근 본 상품, Toolbar, NavigationDrawer, 하단 바 구성
- 관련 XML: `activity_splash.xml`, `activity_main.xml`, `bottom_nav_home.xml`, `nav_header.xml`, `menu_navigation.xml`

[도서 목록 및 상세]
- `BookListActivity`: RecyclerView + CardView로 도서 25권 표시, 검색어/추천 검색어/상태/가격순/이름순/카테고리 필터 처리
- `BookAdapter`: 도서 카드 목록 Adapter
- `BookDetailActivity`: Intent로 전달받은 표지, 제목, 저자, 가격, 출판일, 상태, 설명 표시
- 관련 XML: `activity_book_list.xml`, `item_book.xml`, `activity_book_detail.xml`, `menu_book_list.xml`, `menu_toolbar.xml`

[장바구니 및 주문 흐름]
- `CartActivity`: 장바구니 상품 목록, 개별 체크박스, 전체선택, 수량 변경, 삭제, 선택 상품 주문 처리
- `CartAdapter`, `CartRepository`: 장바구니 목록 표시와 SharedPreferences 저장 담당
- `ShippingActivity`: 주문인 이름, 연락처, 우편번호, 주소 입력
- `OrderActivity`: 주문 상품과 총액 확인 후 주문 완료 Dialog 표시
- 관련 XML: `activity_cart.xml`, `item_cart.xml`, `activity_shipping.xml`, `activity_order.xml`

[내서재, 로그인, 메모]
- `LoginActivity`: 서버 없이 SharedPreferences에 로그인 상태 저장
- `MyPageActivity`: 로그인 상태, 장바구니 요약, 마지막에 본 상품, 최근 본 상품 표시
- `MemoRepository`: 일반 메모와 책별 독서 메모 저장
- `RecentBookRepository`: 마지막에 본 상품과 최근 본 상품 저장
- 관련 XML: `activity_login.xml`, `activity_my_page.xml`, `dialog_memo.xml`, `bottom_nav_search.xml`

### 4. 앱 실행 및 조작 방법

1. 압축을 푼 폴더를 Android Studio에서 `[File] -> [Open]`으로 엽니다.
2. Gradle Sync가 끝날 때까지 기다립니다.
3. 실행하면 `AndroidManifest.xml`의 Launcher 설정에 따라 `SplashActivity`부터 시작됩니다.
4. 홈 화면에서 추천 도서, 마지막에 본 상품, 최근 본 상품 카드를 누르면 상세 화면으로 이동합니다.
5. 하단 바의 `도서` 또는 `검색`을 누르면 도서 목록 화면으로 이동합니다.
6. 도서 목록에서는 검색창, 추천 검색어, 상태 필터, 가격순, 이름순, 카테고리 버튼으로 책을 찾을 수 있습니다.
7. 도서 상세 화면에서 `장바구니 담기`, `바로구매`, `책 메모` 기능을 사용할 수 있습니다.
8. 장바구니에서는 개별 체크박스 또는 전체선택으로 주문할 상품을 고른 뒤 주문 흐름으로 이동합니다.
9. 주문서에서는 사용자가 입력한 주문인 정보와 선택한 상품의 총액을 확인합니다.

### 5. 참고 사항 및 주의사항

- 장바구니, 주문서, 로그인은 필수 구현 범위는 아니지만 앱 흐름을 자연스럽게 만들기 위해 직접 확장한 기능입니다.
- DB는 사용하지 않고, 장바구니/로그인/메모/최근 본 상품은 SharedPreferences로 저장합니다.
- `Unresolved reference`가 보이면 `[File] -> [Sync Project with Gradle Files]`를 먼저 실행하면 됩니다.

## 주요 기능

- 스플래시 화면 후 홈 화면으로 이동
- 홈 화면에서 큐레이터 추천, 랜덤 추천, 마지막에 본 상품, 최근 본 상품 표시
- 상품 목록에서 검색어, 추천 검색어, 상태, 가격순, 이름순, 카테고리 기준으로 도서 보기
- 도서 카드를 누르면 상세 화면으로 이동
- 상세 화면에서 장바구니 담기, 바로구매, 책별 독서 메모 작성
- 장바구니에서 상품별 체크박스 선택, 전체선택, 수량 변경, 삭제, 선택 상품 주문
- 배송정보 화면에서 주문인 정보를 직접 입력
- 주문서 화면에서 주문 상품과 총액 확인
- 로그인 상태, 장바구니, 메모, 최근 본 상품을 SharedPreferences에 저장

## 지시서 충족 체크리스트

| 구분 | 지시서 내용 | 구현 여부 |
| --- | --- | --- |
| 필수 | Kotlin 전용 구현 | `.kt` 파일만 사용, `.java` 파일 없음 |
| 필수 | minSdk 26, targetSdk 36 | `app/build.gradle.kts`에 반영 |
| 필수 | 스플래시 화면 | `SplashActivity`에서 2.5초 후 홈 화면 이동 |
| 필수 | 메인 화면 | 로고, 홈 콘텐츠, Toolbar, NavigationDrawer 구성 |
| 필수 | 도서 목록 화면 | RecyclerView + CardView로 25권 표시 |
| 필수 | 도서 상세 화면 | Intent extra로 표지, 제목, 저자, 가격, 출판일 전달 |
| 필수 | 옵션 메뉴 | Toolbar 메뉴 항목으로 홈, 상품 목록, 장바구니, 마이페이지, 앱 정보 이동/안내 |
| 필수 | 리소스 분리 | `strings.xml`, `colors.xml`, `themes.xml` 사용 |
| 가산점 | NavigationDrawer | 홈 화면 왼쪽 메뉴로 주요 화면 이동 |
| 가산점 | SharedPreferences | 장바구니, 로그인 상태, 메모, 최근 본 상품 저장 |
| 가산점 | AlertDialog / Snackbar | 장바구니 확인, 주문 완료, 삭제 확인, 입력 누락 안내에 사용 |
| 직접 확장 | 장바구니, 주문서, 로그인 | 필수 항목은 아니지만 앱 완성도를 위해 직접 구현 |

## 화면 흐름

```text
SplashActivity
  -> MainActivity
      -> BookListActivity
          -> BookDetailActivity
              -> CartActivity
              -> ShippingActivity
                  -> OrderActivity
      -> CartActivity
      -> LoginActivity
      -> MyPageActivity
```

## 파일 구성

### Kotlin 파일

| 파일 | 역할 |
| --- | --- |
| `Book.kt` | 도서 데이터 클래스와 25권의 상품 데이터 저장 |
| `BookAdapter.kt` | RecyclerView에 도서 카드를 표시 |
| `BookListActivity.kt` | 검색, 추천 검색어, 상태 필터, 가격순/이름순, 카테고리 처리 |
| `BookDetailActivity.kt` | 도서 상세 정보, 장바구니 담기, 바로구매, 책별 메모 처리 |
| `BookRecommendationReason.kt` | 랜덤 추천 시 책 데이터에 맞는 추천 사유 생성 |
| `CartRepository.kt` | 장바구니 데이터를 SharedPreferences에 저장 |
| `CartActivity.kt` | 장바구니 목록, 체크박스 선택, 수량 변경, 삭제, 주문 이동 |
| `CartAdapter.kt` | 장바구니 상품 목록 표시 |
| `MemoRepository.kt` | 일반 메모와 책별 메모를 여러 개 저장 |
| `RecentBookRepository.kt` | 마지막에 본 상품, 최근 본 상품 저장 |
| `MainActivity.kt` | 홈 화면, Drawer 메뉴, 하단 바, 추천/최근 본 상품 연결 |
| `MyPageActivity.kt` | 내서재, 로그인 상태, 장바구니 요약, 최근 본 상품 표시 |
| `LoginActivity.kt` | 간단 로그인 상태 저장 |
| `ShippingActivity.kt` | 주문인 정보 입력 후 주문서로 전달 |
| `OrderActivity.kt` | 주문서 확인과 주문 완료 처리 |
| `SplashActivity.kt` | 앱 시작 스플래시 화면 |

### XML / 리소스 파일

| 파일 | 역할 |
| --- | --- |
| `activity_main.xml` | 홈 화면 |
| `activity_book_list.xml` | 상품 목록, 검색, 필터 화면 |
| `activity_book_detail.xml` | 상품 상세 화면 |
| `activity_cart.xml` | 장바구니 화면 |
| `activity_shipping.xml` | 배송정보 입력 화면 |
| `activity_order.xml` | 주문서 화면 |
| `activity_my_page.xml` | 내서재 화면 |
| `activity_login.xml` | 로그인 화면 |
| `bottom_nav_home.xml`, `bottom_nav_search.xml` | 하단 메뉴 |
| `dialog_memo.xml` | 메모 입력 Dialog |
| `item_book.xml`, `item_cart.xml` | RecyclerView 항목 |
| `menu_toolbar.xml`, `menu_book_list.xml`, `menu_navigation.xml` | Toolbar와 Drawer 메뉴 |
| `strings.xml` | 화면 문구, URL, 메뉴 이름 |
| `colors.xml`, `themes.xml` | 색상과 앱 테마 |

## 수업 내용 활용

이 앱은 수업에서 다룬 Android 기본 기능을 중심으로 만들었습니다.

- Activity 전환
- Intent와 `putExtra`
- ViewBinding
- RecyclerView와 Adapter
- Toolbar 옵션 메뉴
- NavigationDrawer
- AlertDialog
- Toast
- Snackbar
- SharedPreferences
- XML 레이아웃 구성
- drawable, string, color 리소스 분리

## 수업 내용에서 확장한 부분

아래 기능은 수업 내용을 바탕으로 만들었지만, 예제보다 구조가 조금 더 복잡해진 부분입니다.

| 기능 | 확장한 이유 |
| --- | --- |
| 장바구니 | 과제 필수 항목은 아니지만 도서 쇼핑몰 흐름을 완성하기 위해 직접 구현 |
| 주문서 | 배송정보 입력 후 주문 내용을 확인하는 흐름을 보여 주기 위해 직접 구현 |
| 로그인 | 서버 인증 없이 `SharedPreferences`로 로그인 상태만 저장하는 간단한 방식으로 직접 구현 |
| 상태/가격/이름/카테고리 필터 | 상품 목록에서 실제 쇼핑몰처럼 여러 기준으로 책을 볼 수 있게 하기 위해 |
| 전체 도서 높이 조정 | ScrollView 안 RecyclerView가 잘리거나 빈 공간이 생기는 문제를 줄이기 위해 |
| 책별 독서 메모 | 단순 메모가 아니라 특정 책과 연결된 메모를 남기기 위해 |
| 선택 상품 주문 | 장바구니 전체가 아니라 체크한 책만 주문하기 위해 |
| 최근 본 상품 | 홈과 내서재에서 사용자가 본 책을 다시 열 수 있게 하기 위해 |
| 랜덤 추천 사유 | 랜덤 추천이 단순 이동으로 끝나지 않게 책 상태와 가격을 바탕으로 이유를 표시하기 위해 |

## 저장 방식

DB는 사용하지 않았습니다. 수업 범위 안에서 구현하기 위해 `SharedPreferences`를 사용했습니다.

- 장바구니: `상품ID:수량`
- 최근 본 상품: 최근에 본 도서 id 목록
- 로그인 상태: 로그인 여부, 사용자 이름
- 메모: 메모 내용, 연결된 책 id, 책 제목

메모는 여러 개 저장되며, 책 상세 화면에서 작성한 메모는 해당 책 제목과 함께 저장됩니다.

## 출처 및 참고 자료

| 항목 | 출처 |
| --- | --- |
| 용봉문고 로고 | 사용자가 제공한 YB BOOKS / 용봉문고 로고 |
| 도서 표지 | 프로젝트 `res/drawable`, `res/drawable-nodpi` 리소스 |
| 알라딘 베스트 참고 | https://www.aladin.co.kr/shop/common/wbest.aspx?BranchType=1 |
| 알라딘 베스트 2페이지 참고 | https://www.aladin.co.kr/shop/common/wbest.aspx?BestType=Bestseller&BranchType=1&CID=0&page=2&cnt=1000&SortOrder=1 |
| 전남대 홈페이지 링크 | https://www.jnu.ac.kr |

앱은 전남대 학생 전용이 아니며, 전남대 링크는 메뉴에서 이동 가능한 외부 사이트 중 하나로 넣었습니다.

## 빌드 확인

리소스와 Kotlin 코드 연결 확인에 사용한 명령입니다.

```powershell
.\gradlew.bat :app:mergeDebugResources :app:compileDebugKotlin
```

2026년 6월 22일 기준 위 명령으로 `BUILD SUCCESSFUL`을 확인했습니다.  
현재 프로젝트는 Android Gradle Plugin 9.0.1의 built-in Kotlin 설정으로 `compileDebugKotlin` 태스크가 정상 생성됩니다. 그래서 app 모듈에 `org.jetbrains.kotlin.android` 플러그인을 별도로 적용하지 않았습니다. 해당 플러그인을 중복 적용하면 `kotlin` extension 중복 오류가 발생합니다.


## 어려웠던 점

| 어려웠던 점 | 해결 방법 |
| --- | --- |
| 전체 도서를 눌렀을 때 RecyclerView가 잘리거나 아래 공간이 남는 문제 | RecyclerView 자체 스크롤을 막고, 책 개수에 맞춰 높이를 다시 계산하도록 수정 |
| 장바구니에서 전체선택과 개별 선택 상태를 맞추는 문제 | 선택된 책 id를 따로 관리하고, 선택 상품만 주문서로 넘기도록 구현 |
| 메모를 하나가 아니라 여러 개 저장해야 하는 문제 | `SharedPreferences`에 메모 목록을 문자열로 저장하고, 책 id와 책 제목을 함께 저장 |
| 화면마다 하단 바와 메뉴 이동이 끊기는 문제 | 각 Activity에서 Intent 이동을 직접 연결하고, 현재 화면에서는 Toast로 안내 |
| 글자 잘림과 버튼 깨짐 문제 | XML의 폭, 높이, padding, ScrollView/HorizontalScrollView 구성을 여러 번 조정 |

## AI 사용 내역

이 프로젝트에서는 ChatGPT를 보조 도구로 사용했습니다.  
수업에서 배운 Activity, Intent, ViewBinding, RecyclerView, Toolbar, Dialog, SharedPreferences 기본 구현은 직접 작성했습니다. AI는 단순 코드 작성이 아니라, 화면이 깨지거나 저장 구조가 복잡해진 부분처럼 혼자 원인을 바로 찾기 어려웠던 항목을 점검하는 데 사용했습니다.

### 사용 원칙

1. 수업에서 배운 기능은 수업 방식에 맞춰 직접 구현했습니다.
2. 기존 수업 내용을 응용하면 만들 수 있는 부분은 Kotlin/XML 기본 구조 안에서 처리했습니다.
3. 수업 예제보다 복잡하고 오류 원인을 바로 찾기 어려웠던 부분만 AI 사용 내역에 적었습니다.
4. AI가 만든 문장을 그대로 붙여 넣기보다, 현재 코드와 맞는지 확인한 뒤 문장을 고쳤습니다.

### 상세 사용 내역

| 위치 | 무엇을 AI에 물어봤는가 | 어떻게 반영했는가 | 왜 사용했는가 |
| --- | --- | --- | --- |
| `BookListActivity.kt`의 `NoScrollGridLayoutManager`, `updateBookListHeight()` | `ScrollView` 안에 `RecyclerView`를 넣었을 때 전체 도서가 잘리거나 아래 공간이 남는 원인 | RecyclerView 자체 스크롤을 막고, 책 개수와 그리드 줄 수에 맞춰 높이를 다시 계산 | 단순 RecyclerView 예제보다 화면 구조가 복잡해서 원인 파악에 도움이 필요했기 때문 |
| `MemoRepository.kt`의 메모 저장 구조 | 여러 메모와 책별 메모를 DB 없이 저장할 때 문자열이 깨지지 않게 하는 방법 | `ReadingMemo`에 책 id/제목/내용을 넣고, `\u001E`, `\u001F` 구분자로 저장 | 메모 내용에 쉼표나 줄바꿈이 들어가도 저장값이 깨지지 않게 하기 위해 |
| `BookDetailActivity.kt`, `MainActivity.kt`의 메모 목록 렌더링 | 저장된 메모 개수가 바뀔 때 Dialog 안 목록을 어떻게 다시 그릴지 | `LinearLayout`에 TextView 행을 코드로 생성하고, 삭제 후 `renderMemoList()`를 다시 호출 | XML에 고정된 메모 1개가 아니라 여러 개를 보여 줘야 했기 때문 |
| `RecentBookRepository.kt`, `MainActivity.kt`, `MyPageActivity.kt` | 마지막에 본 상품과 최근 본 상품을 중복 없이 순서대로 보여 주는 방법 | 기존 id를 제거한 뒤 맨 앞에 추가하고, 홈/내서재에서 동적 카드로 표시 | 같은 책이 반복해서 보이면 최근 본 상품 기능이 어색해지기 때문 |
| `BookRecommendationReason.kt` | 랜덤 추천을 눌렀을 때 단순 이동이 아니라 자연스러운 추천 사유를 만드는 기준 | 새책, 가격, 필기/밑줄, 별점, 분야를 기준으로 `when` 조건 작성 | 랜덤 추천 기능에 작은 독창성을 넣되, AI 추천처럼 과장되지 않게 하기 위해 |
| `MainActivity.kt`, `MyPageActivity.kt`의 동적 도서 카드 | 최근 본 상품과 큐레이터 카드가 책 데이터에 따라 바뀌게 만드는 방법 | `createRecentBookCard()`, `createCuratorMiniCard()`에서 ImageView/TextView를 코드로 생성 | XML에 모든 카드를 고정하면 책 데이터가 바뀔 때 화면도 일일이 수정해야 했기 때문 |
| XML 레이아웃 전반 | 검색창, 하단 바, 필터 칩 글자가 작게 깨지는 원인 | `ScrollView`, `HorizontalScrollView`, 고정 높이, padding, 버튼 폭을 조정 | 실제 폰 화면에서 글자 잘림과 버튼 깨짐이 반복되어 레이아웃 점검이 필요했기 때문 |

### AI 사용 없이 직접 처리한 부분

아래 내용은 수업 자료와 실습 예제에서 배운 범위 안에서 직접 구현했습니다.
장바구니, 주문서, 로그인은 수업 필수 항목은 아니지만 앱 완성도를 높이기 위해 직접 확장 구현했습니다.

| 수업 내용 | 앱에서 직접 적용한 부분 |
| --- | --- |
| 05 코틀린의 유용한 기법 | `listOf`, `mapNotNull`, `filter`, `sortedBy`, `take`, `when` 사용 |
| 06 뷰를 이용한 화면 구성 | TextView, ImageView, Button, EditText, CheckBox 배치 |
| 07 뷰를 배치하는 레이아웃 | LinearLayout, ConstraintLayout, ScrollView, HorizontalScrollView 조합 |
| 08 사용자 이벤트 처리하기 | `setOnClickListener`, Toast, 버튼 활성/비활성 흐름 직접 작성 |
| 13 액티비티 컴포넌트 | Intent로 화면 이동, `putExtra`/`getExtra`로 도서 정보 전달 |
| 14 메뉴와 앱바 | Toolbar 옵션 메뉴 XML 작성, 메뉴 클릭 시 화면 이동 처리 |
| 15 대화상자 다루기 | AlertDialog로 장바구니 확인, 주문 완료, 메모 입력 Dialog 구성 |

직접 작성한 대표 구현은 다음과 같습니다.
- `SplashActivity`에서 2.5초 뒤 `MainActivity`로 이동
- `MainActivity`에서 Drawer 메뉴와 하단 바 클릭 이벤트 연결
- `BookListActivity`에서 검색어 입력, 추천 검색어 버튼, 카테고리 버튼 클릭 처리
- `BookAdapter`에서 RecyclerView 항목을 표시하고 클릭된 책을 Activity로 전달
- `BookDetailActivity`에서 Intent로 받은 책 정보를 화면에 표시
- `CartActivity`에서 장바구니 수량 변경, 삭제, 전체선택 버튼 처리
- `CartActivity`에서 선택된 책 id를 직접 관리해 선택 상품만 주문서로 전달
- `ShippingActivity`에서 이름, 연락처, 주소 미입력 시 Snackbar 안내
- `OrderActivity`에서 주문 완료 AlertDialog 표시 후 홈으로 이동
- `LoginActivity`에서 입력값을 받아 SharedPreferences에 로그인 상태 저장
- `strings.xml`, `colors.xml`, `themes.xml`로 문구, 색상, 테마 분리
