package com.pvlmamaev.decentralizedstorageapp

import java.io.File

data class UploadedFile(
    val fileName: String,
    val cid: String,
    val uploadDate: Long = System.currentTimeMillis(),
    val encryptedFile: File,
    var transactionStatus: TransactionStatus = TransactionStatus.PENDING
)

enum class TransactionStatus {
    PENDING, CONFIRMED, FAILED
}