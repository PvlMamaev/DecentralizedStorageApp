package com.pvlmamaev.decentralizedstorageapp

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.WindowCompat
import java.io.File
import javax.crypto.SecretKey

class MainActivity : AppCompatActivity() {

    private var encryptionKey: SecretKey? = null
    private var encryptedFile: File? = null

    private lateinit var selectFileButton: Button
    private lateinit var selectedFileText: TextView
    private var selectedFileUri: Uri? = null

    // Регистрация колбэка на результат выбора файла
    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // Получение URI выбранного файла
            result.data?.data?.let { uri ->
                selectedFileUri = uri
                // Отображение имени выбранного файла
                selectedFileText.text = "Выбран файл: ${uri.lastPathSegment}"

                // Генерируем ключ и шифруем файл
                encryptionKey = FileEncryptor.generateKey()
                encryptedFile = FileEncryptor.encryptFile(this, uri, encryptionKey!!)

                // Показываем путь к зашифрованному файлу (для отладки)
                selectedFileText.append("\nФайл зашифрован: ${encryptedFile?.absolutePath}")

            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Установка поведения, чтобы UI не заходил под статус-бар
        WindowCompat.setDecorFitsSystemWindows(window, true)

        setContentView(R.layout.activity_main)

        // Инициализация кнопки и текста
        selectFileButton = findViewById(R.id.selectFileButton)
        selectedFileText = findViewById(R.id.selectedFileText)

        // Обработка клика по кнопке
        selectFileButton.setOnClickListener {
            // Интент на выбор любого файла
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "*/*" // можно задать "image/*", "application/pdf" и т.д.
            }
            // Запуск выбора файла
            filePickerLauncher.launch(Intent.createChooser(intent, "Выберите файл"))
        }
    }
}
