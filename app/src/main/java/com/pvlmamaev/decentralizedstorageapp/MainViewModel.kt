package com.pvlmamaev.decentralizedstorageapp

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.io.File

class MainViewModel : ViewModel() {

    val selectedFileUri = MutableLiveData<Uri?>()
    val encryptedFile   = MutableLiveData<File?>()
    val base64Payload   = MutableLiveData<String?>()

    /** true, когда tonConnectUI уже привязал кошелёк */
    val walletConnected = MutableLiveData<Boolean>(false)
}
