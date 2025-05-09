package com.pvlmamaev.decentralizedstorageapp

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import java.io.File
import javax.crypto.SecretKey

class FileFragment : Fragment(R.layout.fragment_file) {
    private val tonConnectBottomSheet = TonConnectBottomSheet()
    private val vm: MainViewModel by activityViewModels()

    private var encryptionKey: SecretKey? = null
    private var encryptedFile: File? = null
    private var selectedFileUri: Uri? = null

    // Для сортировки карточек
    private var sortDescending = true

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
                // Отображение имени выбранного файла
                selectedFileText.text = "Выбран файл: ${uri.lastPathSegment}"

                // Генерируем ключ и шифруем файл
                encryptionKey = FileEncryptor.generateKey()
                encryptedFile = FileEncryptor.encryptFile(requireContext(), uri, encryptionKey!!)

                // Показываем путь к зашифрованному файлу (для отладки)
                selectedFileText.append("\nФайл зашифрован: ${encryptedFile?.absolutePath}")

                viewLifecycleOwner.lifecycleScope.launch {
                    try {
                        val cid = PinataUploader.uploadFile(encryptedFile!!)
                        val fileName = selectedFileUri?.lastPathSegment ?: "noname"
                        val file = UploadedFile(fileName, cid, encryptedFile = encryptedFile!!)

                        // Добавляем в список загруженных файлов
                        val updatedList = vm.uploadedFiles.value ?: mutableListOf()
                        updatedList.add(file)
                        vm.uploadedFiles.postValue(updatedList)
                        // В TextView показываем полученный CID
                        selectedFileText.append("\nCID: $cid")

                        // Запускаем транзакцию
                        val boc = CidSerializer.cidToBase64Boc(cid)
                        tonConnectBottomSheet.show(parentFragmentManager, "TonConnectBottomSheet")
                        // Ждём, пока отрисуется WebView (или добавь флаг isLoaded)
                        Handler(Looper.getMainLooper()).postDelayed({
                            tonConnectBottomSheet.sendTransaction(boc)
                        }, 500)
                    } catch (e: Exception) {
                        selectedFileText.append("\nОшибка: ${e.message}")
                    }
                }
            }
        }
    }

    override fun onViewCreated(v: View, savedInstanceState: Bundle?) {

        // RecyclerView
        val recycler = v.findViewById<RecyclerView>(R.id.filesRecyclerView)
        recycler.layoutManager = LinearLayoutManager(requireContext())

        selectedFileText = v.findViewById(R.id.selectedFileText)

        // FAB или кнопка загрузки
        v.findViewById<View>(R.id.selectFileButton).setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "*/*"
            }
            filePickerLauncher.launch(Intent.createChooser(intent, "Выберите файл"))
        }

        // Наблюдаем за списком, но применяем текущую сортировку
        vm.uploadedFiles.observe(viewLifecycleOwner) { fileList ->
            val sortedList = if (sortDescending) {
                fileList.sortedByDescending { it.uploadDate }
            } else {
                fileList.sortedBy { it.uploadDate }
            }
            recycler.adapter = ItemAdapter(sortedList)
        }

        // Сортировка списка
        val sortButton = v.findViewById<MaterialButton>(R.id.sortButton)
        sortButton.setOnClickListener {
            sortDescending = !sortDescending

            // Повторно применим сортировку к текущему списку
            vm.uploadedFiles.value?.let { currentList ->
                val sortedList = if (sortDescending) {
                    currentList.sortedByDescending { it.uploadDate }
                } else {
                    currentList.sortedBy { it.uploadDate }
                }
                recycler.adapter = ItemAdapter(sortedList)
            }

            // Меняем иконку
            val iconRes = if (sortDescending) R.drawable.ic_arrow_down else R.drawable.ic_arrow_up
            val text = if (sortDescending) "Недавно добавленные" else "Старые в начале"
            sortButton.text = text
            sortButton.setIconResource(iconRes)
        }

    }
}

