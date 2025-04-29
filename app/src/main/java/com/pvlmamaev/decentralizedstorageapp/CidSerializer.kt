package com.pvlmamaev.decentralizedstorageapp

import android.util.Base64 // <-- Android Base64, не java.util
import org.ton.boc.BagOfCells
import org.ton.cell.*

object CidSerializer {
    fun cidToBase64Boc(cid: String): String {
        val methodId = 38609877

        val cidCell = CellBuilder.createCell {
            storeBytes(cid.toByteArray(Charsets.UTF_8))
        }

        val payloadCell = CellBuilder.createCell {
            storeUInt(methodId.toLong(), 32)
            storeRef(cidCell)
        }

        // Теперь сериализуем через BagOfCells
        val boc = BagOfCells(payloadCell).toByteArray()

        return Base64.encodeToString(boc, Base64.NO_WRAP)
    }
}

