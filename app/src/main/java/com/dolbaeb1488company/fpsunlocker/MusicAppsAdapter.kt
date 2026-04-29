package com.dolbaeb1488company.fpsunlocker

import android.content.pm.PackageManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MusicAppsAdapter(
    private val packageManager: PackageManager,
    private val onRemoveClick: (Int) -> Unit,
    private val onAddClick: () -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val items = mutableListOf<String>()
    private val TYPE_ITEM = 0
    private val TYPE_ADD = 1

    class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val appIcon: ImageView = view.findViewById(R.id.app_icon)
        val appName: TextView = view.findViewById(R.id.app_name)
        val packageName: TextView = view.findViewById(R.id.package_name)
        val removeButton: ImageButton = view.findViewById(R.id.remove_button)
    }

    class AddViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val appName: TextView = view.findViewById(R.id.app_name)
        val appIcon: ImageView = view.findViewById(R.id.app_icon)
        init {
            appName.text = "Add Apps"
            appName.gravity = android.view.Gravity.CENTER
            appIcon.setImageResource(android.R.drawable.ic_input_add)
            appIcon.setColorFilter(android.graphics.Color.parseColor("#34D399"))
            view.findViewById<View>(R.id.remove_button).visibility = View.GONE
            view.findViewById<View>(R.id.package_name).visibility = View.GONE
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position < items.size) TYPE_ITEM else TYPE_ADD
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_music_app, parent, false)
        return if (viewType == TYPE_ITEM) ItemViewHolder(view) else AddViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == TYPE_ITEM) {
            val itemHolder = holder as ItemViewHolder
            val pkg = items[position]
            itemHolder.packageName.text = pkg
            
            try {
                val appInfo = packageManager.getApplicationInfo(pkg, 0)
                itemHolder.appName.text = packageManager.getApplicationLabel(appInfo)
                itemHolder.appIcon.setImageDrawable(packageManager.getApplicationIcon(appInfo))
            } catch (e: Exception) {
                itemHolder.appName.text = "Unknown App"
                itemHolder.appIcon.setImageResource(android.R.drawable.sym_def_app_icon)
            }

            itemHolder.removeButton.setOnClickListener { onRemoveClick(position) }
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