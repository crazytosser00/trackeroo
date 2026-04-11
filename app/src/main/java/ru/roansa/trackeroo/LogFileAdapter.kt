package ru.roansa.trackeroo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

/**
 * Адаптер для отображения списка лог-файлов
 * @param onFileClick callback при клике на файл
 */
class LogFileAdapter(
    private val onFileClick: (LogFileItem) -> Unit
) : RecyclerView.Adapter<LogFileAdapter.LogFileViewHolder>() {

    private val files = mutableListOf<LogFileItem>()
    private val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())

    fun updateFiles(newFiles: List<LogFileItem>) {
        files.clear()
        files.addAll(newFiles)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogFileViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_log_file, parent, false)
        return LogFileViewHolder(view)
    }

    override fun onBindViewHolder(holder: LogFileViewHolder, position: Int) {
        holder.bind(files[position])
    }

    override fun getItemCount(): Int = files.size

    inner class LogFileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvFileName: TextView = itemView.findViewById(R.id.tvFileName)
        private val tvFileSize: TextView = itemView.findViewById(R.id.tvFileSize)
        private val tvFileDate: TextView = itemView.findViewById(R.id.tvFileDate)

        fun bind(file: LogFileItem) {
            tvFileName.text = file.name
            tvFileSize.text = formatFileSize(file.size)
            tvFileDate.text = dateFormat.format(Date(file.lastModified))

            itemView.setOnClickListener {
                onFileClick(file)
            }
        }

        private fun formatFileSize(bytes: Long): String {
            return when {
                bytes < 1024 -> "$bytes bytes"
                bytes < 1024 * 1024 -> "${bytes / 1024} KB"
                else -> "${bytes / (1024 * 1024)} MB"
            }
        }
    }
}
