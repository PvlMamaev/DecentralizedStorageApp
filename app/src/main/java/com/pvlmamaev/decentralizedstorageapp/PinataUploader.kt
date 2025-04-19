package com.pvlmamaev.decentralizedstorageapp

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

object PinataUploader {

    private const val PINATA_API = "https://api.pinata.cloud/pinning/pinFileToIPFS"
    private const val JWT = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySW5mb3JtYXRpb24iOnsiaWQiOiI1NjIyMGZiYS1jMjI5LTQwZWEtODk1ZC04OTJhMTU1OTI0NGUiLCJlbWFpbCI6Im1wYXNodGV0cEB5YW5kZXgucnUiLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwicGluX3BvbGljeSI6eyJyZWdpb25zIjpbeyJkZXNpcmVkUmVwbGljYXRpb25Db3VudCI6MSwiaWQiOiJGUkExIn0seyJkZXNpcmVkUmVwbGljYXRpb25Db3VudCI6MSwiaWQiOiJOWUMxIn1dLCJ2ZXJzaW9uIjoxfSwibWZhX2VuYWJsZWQiOmZhbHNlLCJzdGF0dXMiOiJBQ1RJVkUifSwiYXV0aGVudGljYXRpb25UeXBlIjoic2NvcGVkS2V5Iiwic2NvcGVkS2V5S2V5IjoiNjBhZWI3ZjU5ZjA1ZDM1MmQzNjAiLCJzY29wZWRLZXlTZWNyZXQiOiJiNDM0ZDAzNjJmMzcyM2U0MzcxNmQ5YjhkN2EyNThmZWJlZTNiMDlmYmIwMmE4NzgzZGY4YWI4YzA2MjFkY2I1IiwiZXhwIjoxNzc2MjY2NTA3fQ.AEYUZmK37gaSM3bXXYfzRRbfcfJ1ROG1mNoOg2i4ktc"

    suspend fun uploadFile(file: File): String = withContext(Dispatchers.IO) {
        val client = OkHttpClient()

        val body = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart(
                "file",
                file.name,
                file.asRequestBody("application/octet-stream".toMediaType())
            )
            .build()

        val request = Request.Builder()
            .url(PINATA_API)
            .addHeader("Authorization", JWT)
            .post(body)
            .build()

        val response = client.newCall(request).execute()

        if (!response.isSuccessful) {
            throw RuntimeException("Ошибка загрузки в Pinata: ${response.code} - ${response.message}")
        }

        val responseBody = response.body?.string()
            ?: throw RuntimeException("Пустой ответ от Pinata")

        // Ищем CID в JSON-ответе (простой способ)
        val cidRegex = """"IpfsHash"\s*:\s*"([^"]+)"""".toRegex()
        val match = cidRegex.find(responseBody)
        return@withContext match?.groupValues?.get(1)
            ?: throw RuntimeException("CID не найден в ответе: $responseBody")
    }
}
