package com.pvlmamaev.decentralizedstorageapp

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController

private val tonConnectBottomSheet = TonConnectBottomSheet()

class WalletFragment : Fragment(R.layout.fragment_wallet) {

    // Здесь мы что-то делаем с объектом состояния
    private val vm: MainViewModel by activityViewModels()

    // Объявляем кнопку отправки транзакции
    private lateinit var sendButton: Button

    // Эта функция вызывается при создании экрана
    override fun onViewCreated(v: View, savedInstanceState: Bundle?) {
        // Находим представление для кнопки отправки транзакции
        sendButton = v.findViewById(R.id.sendTransaction)

        v.findViewById<Button>(R.id.buttonNextFragment).setOnClickListener {
            findNavController().navigate(R.id.action_walletFragment_to_fileFragment)
        }

        // Обработчик нажатия на кнопку подключения кошелька
        v.findViewById<Button>(R.id.connectToWallet).setOnClickListener {
            // Открываем BottomSheet
            tonConnectBottomSheet.show(parentFragmentManager, "TonConnectBottomSheet")
            // Ждём, пока отрисуется WebView
            Handler(Looper.getMainLooper()).postDelayed({
                tonConnectBottomSheet.connectWallet()
            }, 500)
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

            // Показываем BottomSheet с WebView
            tonConnectBottomSheet.show(parentFragmentManager, "TonConnectBottomSheet")

            // Ждём, пока отрисуется WebView (или добавь флаг isLoaded)
            Handler(Looper.getMainLooper()).postDelayed({
                tonConnectBottomSheet.sendTransaction(boc)
            }, 500)
        }

        // хз че тут происходит
        vm.base64Payload.observe(viewLifecycleOwner) { payload ->
            sendButton.isEnabled = !payload.isNullOrEmpty()
        }
    }


}
