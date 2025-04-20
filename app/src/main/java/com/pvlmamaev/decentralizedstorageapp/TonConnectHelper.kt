package com.pvlmamaev.decentralizedstorageapp

import android.util.Base64

object TonConnectHelper {
    fun buildTransactionLink(contractAddress: String): String {
        val json = """
        {
          "id": 1,
          "jsonrpc": "2.0",
          "method": "ton_sendTransaction",
          "params": [
            {
              "valid_until": ${System.currentTimeMillis() / 1000 + 600},
              "messages": [
                {
                  "address": "$contractAddress",
                  "amount": "50000000"
                }
              ]
            }
          ]
        }
        """.trimIndent()

        val encoded = Base64.encodeToString(
            json.toByteArray(),
            Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP
        )

        return "tonkeeper://ton-connect?request=$encoded"
    }
}


