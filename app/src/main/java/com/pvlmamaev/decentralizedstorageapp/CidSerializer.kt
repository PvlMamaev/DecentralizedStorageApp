package com.pvlmamaev.decentralizedstorageapp

import android.util.Base64 // <-- Android Base64, не java.util
import org.ton.boc.BagOfCells
import org.ton.cell.CellBuilder

object CidSerializer {
    fun cidToBase64Boc(cid: String): String {
        val bytes = cid.toByteArray(Charsets.UTF_8)
        val builder = CellBuilder()
        builder.storeBytes(bytes)
        val cell = builder.endCell()
        val boc = BagOfCells(cell).toByteArray()
        return Base64.encodeToString(boc, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
    }
}

