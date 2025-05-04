package com.pvlmamaev.decentralizedstorageapp

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController

private val tonConnectBottomSheet = TonConnectBottomSheet()

class WalletFragment : Fragment(R.layout.fragment_wallet) {

    // Здесь мы что-то делаем с объектом состояния
    private val vm: MainViewModel by activityViewModels()

    // Эта функция вызывается при создании экрана
    override fun onViewCreated(v: View, savedInstanceState: Bundle?) {

        // Обработчик нажатия на кнопку подключения кошелька
        v.findViewById<Button>(R.id.connectToWallet).setOnClickListener {
            // Открываем BottomSheet
            tonConnectBottomSheet.show(parentFragmentManager, "TonConnectBottomSheet")
            // Ждём, пока отрисуется WebView
            Handler(Looper.getMainLooper()).postDelayed({
                tonConnectBottomSheet.connectWallet()
            }, 500)
        }
    }


}
