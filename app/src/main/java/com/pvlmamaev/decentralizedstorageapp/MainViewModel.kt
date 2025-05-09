package com.pvlmamaev.decentralizedstorageapp

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.io.File

class MainViewModel : ViewModel() {

    val uploadedFiles = MutableLiveData<MutableList<UploadedFile>>(mutableListOf())

    /** true, когда tonConnectUI уже привязал кошелёк */
    val walletConnected = MutableLiveData<Boolean>(false)
}
