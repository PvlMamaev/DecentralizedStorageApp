package com.pvlmamaev.decentralizedstorageapp

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.ValueCallback
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class TonConnectBottomSheet : BottomSheetDialogFragment() {

    private lateinit var tonWebView: WebView

    override fun getTheme(): Int = R.style.TransparentBottomSheet

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.bottom_sheet_wallet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        tonWebView = view.findViewById(R.id.tonWebView)
        val params = tonWebView.layoutParams
        params.height = 1300
        tonWebView.layoutParams = params

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

        // Делаем фон webview прозрачным
        tonWebView.setBackgroundColor(Color.TRANSPARENT)
        tonWebView.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        WebView.setWebContentsDebuggingEnabled(true)


        // Объявляем функции которые может запустить react
        // В нашем случае эти функции отвечают за состояние
        tonWebView.addJavascriptInterface(object {
            @JavascriptInterface
            fun onTxResult(raw: String) {
                Toast.makeText(context, "✅ $raw", Toast.LENGTH_SHORT).show()
                // Закрываем BottomSheet после удачной транзакции
                activity?.runOnUiThread {
                    dismiss()
                }
            }

            @JavascriptInterface
            fun onTxError(msg: String) {
                Toast.makeText(context, "❌ $msg", Toast.LENGTH_SHORT).show()
                // Закрываем BottomSheet после неудачной транзакции
                activity?.runOnUiThread {
                    dismiss()
                }
            }

            @JavascriptInterface
            fun onTxComplete() {
                // Закрываем BottomSheet после окна подключения кошелька
                Toast.makeText(context, "Закрываем BottomSheet", Toast.LENGTH_SHORT).show()
                activity?.runOnUiThread {
                    dismiss()
                    findNavController().navigate(
                        R.id.action_walletFragment_to_fileFragment, null,
                        NavOptions.Builder()
                            .setEnterAnim(androidx.navigation.ui.R.anim.nav_default_enter_anim)
                            .setExitAnim(androidx.navigation.ui.R.anim.nav_default_exit_anim)
                            .setPopUpTo(R.id.walletFragment, true)
                            .build()
                    )
                }
            }
        }, "AndroidBridge")

        // Кастомный WebViewClient для перехвата deep-link’ов ton://…
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

    fun sendTransaction(boc: String) {
        val params = tonWebView.layoutParams
        params.height = 900
        tonWebView.layoutParams = params
        // Вызываем из react функцию checkConnection()
        // А ее у нас вроде бы и нет
        tonWebView.evaluateJavascript("window.checkConnection()", null)
        // Вызываем из react функцию dispatchEvent()
        // А ее у нас вроде бы и нет
        tonWebView.evaluateJavascript(
            "window.dispatchEvent(new Event('tonConnectReturn'))", null
        )
        // Вызываем из react функцию sendCid в которую передаем значение
        // которое достали из MainViewModel
        tonWebView.evaluateJavascript("window.sendCid('$boc')", null)
    }

    fun connectWallet() {
        val handler = Handler(Looper.getMainLooper())

        // Т.к. если сразу вызвать метод из react после создания webview
        // то метод просто не выполнится т.к. react еще будет не готов
        // по этому мы вводим функцию после выполнения которой мы понимаем
        // что react готов к работе и можено вызывать другие методы
        fun checkReadyAndConnect() {
            tonWebView.evaluateJavascript("window.ready === true", ValueCallback { isReady ->
                if (isReady == "true") {
                    tonWebView.evaluateJavascript("window.connectWalletManually()", null)
                } else {
                    handler.postDelayed({ checkReadyAndConnect() }, 200)
                }
            })

        }

        checkReadyAndConnect()
    }


}
