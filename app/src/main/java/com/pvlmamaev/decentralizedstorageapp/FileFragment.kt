package com.pvlmamaev.decentralizedstorageapp

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.launch
import java.io.File
import javax.crypto.SecretKey

class FileFragment : Fragment(R.layout.fragment_file) {

    private val vm: MainViewModel by activityViewModels()

    /* =====  Блоки полей из MainActivity  ===== */
    private var encryptionKey: SecretKey? = null
    private var encryptedFile: File? = null
    private var selectedFileUri: Uri? = null

    // TextView-для-лога можно оставить здесь
    private lateinit var selectedFileText: TextView

    // Регистрация колбэка на результат выбора файла
    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // Получение URI выбранного файла
            result.data?.data?.let { uri ->
                selectedFileUri = uri
                vm.selectedFileUri.value = uri          // + ViewModel
                // Отображение имени выбранного файла
                selectedFileText.text = "Выбран файл: ${uri.lastPathSegment}"

                // Генерируем ключ и шифруем файл
                encryptionKey = FileEncryptor.generateKey()
                encryptedFile = FileEncryptor.encryptFile(requireContext(), uri, encryptionKey!!)
                vm.encryptedFile.value = encryptedFile  // + ViewModel

                // Показываем путь к зашифрованному файлу (для отладки)
                selectedFileText.append("\nФайл зашифрован: ${encryptedFile?.absolutePath}")

                viewLifecycleOwner.lifecycleScope.launch {
                    try {
                        val cid = PinataUploader.uploadFile(encryptedFile!!)
                        selectedFileText.append("\nCID: $cid")
                        val boc = CidSerializer.cidToBase64Boc(cid)
                        vm.base64Payload.postValue(boc)   // главное!
                    } catch (e: Exception) {
                        selectedFileText.append("\nОшибка: ${e.message}")
                    }
                }
            }
        }
    }

    override fun onViewCreated(v: View, savedInstanceState: Bundle?) {
        selectedFileText = v.findViewById(R.id.selectedFileText)

        v.findViewById<View>(R.id.selectFileButton).setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "*/*"
            }
            filePickerLauncher.launch(Intent.createChooser(intent, "Выберите файл"))
        }

        v.findViewById<Button>(R.id.nextButton).setOnClickListener {
            findNavController().navigate(R.id.action_fileFragment_to_walletFragment)
        }
    }
}

