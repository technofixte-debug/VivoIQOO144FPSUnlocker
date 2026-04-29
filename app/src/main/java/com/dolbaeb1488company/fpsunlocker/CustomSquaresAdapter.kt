package com.dolbaeb1488company.fpsunlocker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CustomSquaresAdapter(
    private var items: MutableList<String>,
    private val onItemClick: (Int, String) -> Unit,
    private val onItemLongClick: (Int) -> Unit,
    private val onAddClick: () -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_ITEM = 0
    private val TYPE_ADD = 1

    class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val squareText: TextView = view.findViewById(R.id.square_text)
    }

    class AddViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        init {
            view.findViewById<TextView>(R.id.square_text).text = "+"
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position < items.size) TYPE_ITEM else TYPE_ADD
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layout = R.layout.item_custom_square
        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return if (viewType == TYPE_ITEM) ItemViewHolder(view) else AddViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == TYPE_ITEM) {
            val itemHolder = holder as ItemViewHolder
            val item = items[position]
            itemHolder.squareText.text = item
            itemHolder.itemView.setOnClickListener { onItemClick(position, item) }
            itemHolder.itemView.setOnLongClickListener {
                onItemLongClick(position)
                true
            }
        } else {
            holder.itemView.setOnClickListener { onAddClick() }
        }
    }

    override fun getItemCount() = items.size + 1

    fun updateData(newItems: List<String>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}