package com.pvlmamaev.decentralizedstorageapp

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.activityViewModels

// флаг, чтобы загружать WebView только один раз
private var webViewLoaded = false

class WalletFragment : Fragment(R.layout.fragment_wallet) {

    // Здесь мы что-то делаем с объектом состояния
    private val vm: MainViewModel by activityViewModels()
    // Объявляем вьюху webview
    private lateinit var tonWebView: WebView
    // Объявляем кнопку отправки транзакции
    private lateinit var sendButton: Button

    // Эта функция вызывается при создании экрана
    override fun onViewCreated(v: View, savedInstanceState: Bundle?) {
        // Находим представление для webview
        tonWebView = v.findViewById(R.id.tonWebView)
        // Находим представление для кнопки отправки транзакции
        sendButton = v.findViewById(R.id.sendTransaction)
        // Основная функция работы. Представлена ниже
        prepareWebView()

        // Обработчик нажатия на кнопку подключения кошелька
        v.findViewById<Button>(R.id.connectToWallet).setOnClickListener {
            // Делаем webview видимым чтобы стала доступна кнопка подключения
            // кошелька из react
            tonWebView.visibility = View.VISIBLE
            // Делаем фон webview прозрачным
            tonWebView.setBackgroundColor(Color.TRANSPARENT)
            tonWebView.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        }

        // Обработчик нажатия на кнопку отправки транзакции
        sendButton.setOnClickListener {
            // Создаем переменную которая берет значение из сохраненного
            // состояния MainViewModel. Этим значением является... хз что
            // Если же значения нет, мы вызываем обработчик нажатия
            // странно. Зачем его вызывать еще раз. Что такое @setOnClickListener
//            val boc = vm.base64Payload.value ?: return@setOnClickListener
            val testCid = "QmQMFFKqQM7vCJVFUW9zDAfHqtupqjptB5YNUMmE66e1ZP"
            val boc = vm.base64Payload.value ?: CidSerializer.cidToBase64Boc(testCid)
            // Вызываем из react функцию checkConnection()
            // А ее у нас вроде бы и нет
            tonWebView.evaluateJavascript("window.checkConnection()", null)
            // Вызываем из react функцию dispatchEvent()
            // А ее у нас вроде бы и нет
            tonWebView.evaluateJavascript(
                "window.dispatchEvent(new Event('tonConnectReturn'));", null
            )
            // Вызываем из react функцию sendCid в которую передаем значение
            // которое достали из MainViewModel
            tonWebView.evaluateJavascript("window.sendCid('$boc')", null)
        }

        // хз че тут происходит
        vm.base64Payload.observe(viewLifecycleOwner) { payload ->
            sendButton.isEnabled = !payload.isNullOrEmpty()
        }
    }

    // Основная функция работы
    private fun prepareWebView() {
        // Включаем WebView‑дебаг (DevTools)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true)
        }
        // Здесь настраиваем наш webview т.к. изначально нам доступны из
        // него не все функции
        with(tonWebView.settings) {
            javaScriptEnabled = true
            allowFileAccess = true
            allowFileAccessFromFileURLs = true
            allowUniversalAccessFromFileURLs = true
            domStorageEnabled = true
        }
        // Объявляем функции которые может запустить react
        // В нашем случае эти функции отвечают за состояние
        // отправки транзакции
        tonWebView.addJavascriptInterface(object {
            @JavascriptInterface
            fun onTxResult(raw: String) =
                Toast.makeText(requireContext(), "✅ $raw", Toast.LENGTH_SHORT).show()

            @JavascriptInterface
            fun onTxError(msg: String) =
                Toast.makeText(requireContext(), "❌ $msg", Toast.LENGTH_SHORT).show()
        }, "AndroidBridge")

        // 4. Кастомный WebViewClient для перехвата deep-link’ов ton://…
        tonWebView.webViewClient = object : WebViewClient() {

            override fun shouldOverrideUrlLoading(
                view: WebView?, request: WebResourceRequest?
            ): Boolean {

                val url = request?.url.toString()

                // --- (A) Tonkeeper ---
                if (url.startsWith("https://app.tonkeeper.com") ||
                    url.startsWith("https://wallet.tonkeeper.com")
                ) {

                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    // пробуем адресовать именно Tonkeeper;
                    // если не установлен – не задаём пакет и дадим шанс другим
                    if (isInstalled("com.ton_keeper")) {
                        intent.setPackage("com.ton_keeper")
                        intent.addFlags(Intent.FLAG_ACTIVITY_REQUIRE_NON_BROWSER)
                    }
                    startActivity(intent)
                    return true
                }

                // --- (B) все остальные deep-links (https://t.me/wallet…, ton:// …) ---
                if (!url.startsWith("http://") && !url.startsWith("https://")
                    || url.startsWith("https://t.me/")          // Telegram Wallet
                ) {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    return true
                }

                return false
            }

            // Это часть которая проверяет установлен ли на телефоне
            // Tonkeeper и получает название пакета. Иначе null
            private fun isInstalled(pkg: String): Boolean =
                requireContext().packageManager.getLaunchIntentForPackage(pkg) != null
        }

        // При открытии webview будет загружаться страница index.html
        // из файла по указанному пути
        // Мб нужно будет раскомментрировать эти строки ------------
//        if (webViewLoaded) return
//        webViewLoaded = true
        // чтобы не создавался webview несколько раз ---------------
        tonWebView.loadUrl("file:///android_asset/tonconnect/index.html")
    }

    override fun onDestroyView() {
        // Очищаем память от webview
        tonWebView.loadUrl("about:blank")
        tonWebView.clearHistory()
        tonWebView.removeAllViews()
        tonWebView.destroy()
        super.onDestroyView()
    }
}
