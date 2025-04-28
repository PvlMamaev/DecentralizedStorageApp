package com.pvlmamaev.decentralizedstorageapp

import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebViewClient
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
    private lateinit var connectToWallet: Button
    private lateinit var sendTransaction: Button
    private lateinit var selectedFileText: TextView
    private lateinit var tonWebView: WebView
    private lateinit var base64Payload: String
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

                        base64Payload = CidSerializer.cidToBase64Boc(cid)

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

        // 0. Находим view
        selectFileButton = findViewById(R.id.selectFileButton)
        connectToWallet = findViewById(R.id.connectToWallet)
        sendTransaction = findViewById(R.id.sendTransaction)
        selectedFileText = findViewById(R.id.selectedFileText)
        tonWebView = findViewById(R.id.tonWebView)

        // 1. Включаем WebView‑дебаг (DevTools)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true)
        }

        // 2. Базовые настройки WebView
        with(tonWebView.settings) {
            javaScriptEnabled = true
            allowFileAccess = true
            allowFileAccessFromFileURLs = true
            allowUniversalAccessFromFileURLs = true
            domStorageEnabled = true
        }

        // 3. Добавляем JS-интерфейс для обратной связи из React
        //    — в React вы вызываете window.AndroidBridge.onTxResult / onTxError(...)
        tonWebView.addJavascriptInterface(object {
            @JavascriptInterface
            fun onTxResult(raw: String) {
                runOnUiThread { selectedFileText.append("\n✅ Tx success: $raw") }
            }
            @JavascriptInterface
            fun onTxError(msg: String) {
                runOnUiThread { selectedFileText.append("\n❌ Tx error: $msg") }
            }
        }, "AndroidBridge")

        // 4. Кастомный WebViewClient для перехвата deep-link’ов ton://…
        tonWebView.webViewClient = object : WebViewClient() {

            // 1) Вот тут, сразу после '{', объявляем handleCustomScheme:
            private fun handleCustomScheme(url: String): Boolean {
                // пропускаем стандартные URL
                if (url.startsWith("http://")
                    || url.startsWith("https://")
                    || url.startsWith("file://")
                    || url.startsWith("about:blank")) {
                    return false
                }
                // все остальные – открываем через Intent
                return try {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    true
                } catch (e: ActivityNotFoundException) {
                    Log.w("WebView", "No handler for scheme in URL: $url")
                    true
                }
            }

            // 2) А теперь override-методы, которые просто дергают handleCustomScheme:
            override fun shouldOverrideUrlLoading(
                view: WebView?, request: WebResourceRequest?
            ): Boolean {
                return handleCustomScheme(request?.url.toString())
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                Log.i("WebView", "📗 загружена страница: $url")
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                Log.e("WebView", "❌ Ошибка при загрузке ${request?.url}: ${error?.description}")
            }
        }


        // 5. Загружаем вашу локальную сборку
        //        tonWebView.loadUrl("https://pvlmamaev.github.io/DecentralizedStorageApp/app/src/main/assets/deploy.html")
        tonWebView.loadUrl("file:///android_asset/tonconnect/index.html")
        tonWebView.visibility = View.GONE


        // 6. Далее ваш filePickerLauncher и отправка base64-пейлоада в JS:
        // Обработка нажатия кнопки выбора файла
        selectFileButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "*/*"
            }
            filePickerLauncher.launch(Intent.createChooser(intent, "Выберите файл"))
        }


        connectToWallet.setOnClickListener {
            // Показываем WebView и передаём payload в JavaScript
            tonWebView.visibility = View.VISIBLE
//            tonWebView.evaluateJavascript(
//                "window.sendCid && window.sendCid('$base64Payload');",
//                null
//            )

//            val js = "pageReady && window.sendCid('$base64Payload')"
//            tonWebView.post { tonWebView.evaluateJavascript(js, null) }
        }

        sendTransaction.setOnClickListener {
            tonWebView.evaluateJavascript("window.checkConnection()", null)
            val testCid = "QmQMFFKqQM7vCJVFUW9zDAfHqtupqjptB5YNUMmE66e1ZP"
            val boc = CidSerializer.cidToBase64Boc(testCid)  // или сразу base64Boc, если у вас есть

            tonWebView.evaluateJavascript(
                "window.dispatchEvent(new Event('tonConnectReturn'));",
                null
            )
            // затем
            tonWebView.evaluateJavascript("window.sendCid('$boc')", null)
        }

    }
}
