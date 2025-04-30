package com.pvlmamaev.decentralizedstorageapp

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Говорим системе: - Не трогай паддинги!!! Я сам :3
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_main)

        // Берём корневой контейнер (NavHost)
        val navHost = findViewById<View>(R.id.nav_host)

        // И при каждом изменении системных панелей задаём паддинги
        ViewCompat.setOnApplyWindowInsetsListener(navHost) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            WindowInsetsCompat.CONSUMED   // сообщаем, что сами всё обработали
        }

    }
}
