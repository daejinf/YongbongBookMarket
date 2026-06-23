package com.example.yongbongbookmarket

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.yongbongbookmarket.databinding.ActivitySplashBinding

/**
 * 앱을 처음 실행했을 때 잠깐 보이는 스플래시 화면.
 * 앱 로고를 잠깐 보여 준 뒤 MainActivity로 넘어간다.
 */
class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 스플래시 화면도 XML로 만들었기 때문에 binding으로 연결한다.
        val binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 앱 이름을 읽을 정도의 짧은 시간만 스플래시를 보여 준다.
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            // 시간이 지나면 홈 화면으로 이동하고, 뒤로가기로 스플래시에 돌아오지 않게 finish를 호출한다.
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }, 2500)
    }
}
