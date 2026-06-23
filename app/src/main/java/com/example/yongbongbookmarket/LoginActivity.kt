package com.example.yongbongbookmarket

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.yongbongbookmarket.databinding.ActivityLoginBinding

/**
 * 주문 흐름에서 사용할 간단한 로그인 화면.
 * 서버 인증 대신 SharedPreferences에 아이디와 이름만 저장한다.
 */
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // ViewBinding을 쓰면 findViewById 없이 XML의 View를 바로 사용할 수 있다.
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = getString(R.string.login_title)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.loginButton.setOnClickListener {
            saveLoginInfo()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        // Toolbar의 뒤로가기 버튼도 기기 뒤로가기처럼 현재 화면만 닫는다.
        finish()
        return true
    }

    private fun saveLoginInfo() {
        // 입력창 앞뒤 공백은 사용자가 실수로 넣을 수 있어 먼저 정리한다.
        val studentId = binding.studentIdEdit.text.toString().trim()
        val name = binding.nameEdit.text.toString().trim()
        val password = binding.passwordEdit.text.toString().trim()

        // 하나라도 비어 있으면 저장하지 않고 화면에 안내만 보여 준다.
        if (studentId.isEmpty() || name.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, getString(R.string.login_empty_message), Toast.LENGTH_SHORT).show()
            return
        }

        // 입력한 정보는 앱 안에서 로그인 상태를 구분하는 데만 사용한다.
        val prefs = getSharedPreferences("book_market", MODE_PRIVATE)
        prefs.edit()
            .putBoolean("is_logged_in", true)
            .putString("member_id", studentId)
            .putString("member_name", name)
            .apply()

        Toast.makeText(this, getString(R.string.login_success_message, name), Toast.LENGTH_SHORT).show()
        finish()
    }
}
