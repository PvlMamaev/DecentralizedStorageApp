package com.pvlmamaev.decentralizedstorageapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class ItemAdapter(private val items: List<UploadedFile>) :
    RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {

    class ItemViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        val nameText: TextView = itemView.findViewById(R.id.fileName)
        val cidText: TextView = view.findViewById(R.id.fileCid)
        val statusText: TextView = view.findViewById(R.id.fileStatus)
        val dateText: TextView = view.findViewById(R.id.fileDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = items[position]
        holder.nameText.text = item.fileName
        holder.cidText.text = "CID: ${item.cid}"
        holder.statusText.text = "Статус: ${item.transactionStatus.name}"
        holder.dateText.text = "Дата: ${formatDate(item.uploadDate)}"
    }

    override fun getItemCount(): Int = items.size

    private fun formatDate(timestamp: Long): String {
        val sdf = java.text.SimpleDateFormat("dd.MM.yyyy HH:mm", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(timestamp))
    }

}