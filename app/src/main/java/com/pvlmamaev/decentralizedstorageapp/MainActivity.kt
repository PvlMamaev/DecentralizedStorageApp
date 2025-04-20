package com.pvlmamaev.decentralizedstorageapp

import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import java.io.File
import javax.crypto.SecretKey
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private var encryptionKey: SecretKey? = null
    private var encryptedFile: File? = null

    private lateinit var selectFileButton: Button
    private lateinit var selectedFileText: TextView
    private lateinit var tonWebView: WebView
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

                lifecycleScope.launch {
                    try {
                        val cid = PinataUploader.uploadFile(encryptedFile!!)
                        selectedFileText.append("\nCID: $cid")

                        val base64Payload = CidSerializer.cidToBase64Boc(cid)

                        // Показываем WebView и передаём payload в JavaScript
                        tonWebView.visibility = View.VISIBLE
                        tonWebView.evaluateJavascript("window.sendCid('$base64Payload')", null)

                    } catch (e: Exception) {
                        selectedFileText.append("\nОшибка при загрузке: ${e.message}")
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        setContentView(R.layout.activity_main)

        // Инициализация UI
        selectFileButton = findViewById(R.id.selectFileButton)
        selectedFileText = findViewById(R.id.selectedFileText)
        tonWebView = findViewById(R.id.tonWebView)

        tonWebView.settings.javaScriptEnabled = true
        tonWebView.settings.allowFileAccess = true
        tonWebView.settings.domStorageEnabled = true

        // JS-интерфейс для обратной связи из HTML (опционально)
        tonWebView.addJavascriptInterface(object {
            @JavascriptInterface
            fun sendCidFromApp(bocBase64: String) {
                tonWebView.post {
                    tonWebView.evaluateJavascript("window.sendCid('$bocBase64')", null)
                }
            }
        }, "Android")

        tonWebView.loadUrl("file:///android_asset/deploy.html")
        tonWebView.visibility = View.GONE

        // Обработка нажатия кнопки выбора файла
        selectFileButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "*/*"
            }
            filePickerLauncher.launch(Intent.createChooser(intent, "Выберите файл"))
        }
    }
}
