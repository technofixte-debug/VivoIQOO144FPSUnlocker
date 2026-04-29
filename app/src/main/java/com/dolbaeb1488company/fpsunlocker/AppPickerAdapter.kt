package com.dolbaeb1488company.fpsunlocker

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class AppItem(
    val info: ApplicationInfo,
    val name: String,
    var isSelected: Boolean = false
)

class AppPickerAdapter(
    private val packageManager: PackageManager,
    private val apps: List<AppItem>
) : RecyclerView.Adapter<AppPickerAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.picker_app_icon)
        val name: TextView = view.findViewById(R.id.picker_app_name)
        val pkg: TextView = view.findViewById(R.id.picker_package_name)
        val checkbox: CheckBox = view.findViewById(R.id.picker_checkbox)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_app_picker, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = apps[position]
        holder.name.text = item.name
        holder.pkg.text = item.info.packageName
        holder.icon.setImageDrawable(item.info.loadIcon(packageManager))
        holder.checkbox.isChecked = item.isSelected

        holder.itemView.setOnClickListener {
            item.isSelected = !item.isSelected
            holder.checkbox.isChecked = item.isSelected
        }
    }

    override fun getItemCount() = apps.size
    
    fun getSelectedPackages(): List<String> {
        return apps.filter { it.isSelected }.map { it.info.packageName }
    }
}