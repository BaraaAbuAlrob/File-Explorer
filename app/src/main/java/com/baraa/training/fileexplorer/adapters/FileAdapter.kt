package com.baraa.training.fileexplorer.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.baraa.training.fileexplorer.R
import com.baraa.training.fileexplorer.databinding.FileContainerBinding
import java.io.File

class FileAdapter(private var context: Context, private var files: List<File>, private var listener: OnFileSelectedListener) :
    RecyclerView.Adapter<FileAdapter.FileViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        return FileViewHolder(FileContainerBinding
                .inflate(LayoutInflater
                .from(parent.context)))
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        holder.binding.tvFileName.text = files[position].name
        holder.binding.tvFileName.isSelected = true

        var items = 0
        if (files[position].isDirectory) {
            val filesInDirectory = files[position].listFiles()
            filesInDirectory?.forEach { singleFileInDirectory ->
                if (!singleFileInDirectory.isHidden) {
                    items++
                }
            }
            holder.binding.tvFileSize.text = "$items Files"
        } else {
            holder.binding.tvFileSize.text =
                android.text.format.Formatter.formatShortFileSize(context, files[position].length())
        }

        if (files[position].name.lowercase().endsWith(".jpeg")) {
            holder.binding.imgFileType.setImageResource(R.drawable.ic_image)

        } else if (files[position].name.lowercase().endsWith(".jpg")) {
            holder.binding.imgFileType.setImageResource(R.drawable.ic_image)

        } else if (files[position].name.lowercase().endsWith(".png")) {
            holder.binding.imgFileType.setImageResource(R.drawable.ic_image)

        } else if (files[position].name.lowercase().endsWith(".pdf")) {
            holder.binding.imgFileType.setImageResource(R.drawable.ic_pdf)

        } else if (files[position].name.lowercase().endsWith(".doc")) {
            holder.binding.imgFileType.setImageResource(R.drawable.ic_docs)

        } else if (files[position].name.lowercase().endsWith(".mp3")) {
            holder.binding.imgFileType.setImageResource(R.drawable.ic_music)

        } else if (files[position].name.lowercase().endsWith(".wav")) {
            holder.binding.imgFileType.setImageResource(R.drawable.ic_music)

        } else if (files[position].name.lowercase().endsWith(".mp4")) {
            holder.binding.imgFileType.setImageResource(R.drawable.ic_play)

        } else if (files[position].name.lowercase().endsWith(".apk")) {
            holder.binding.imgFileType.setImageResource(R.drawable.ic_android)

        } else {
            holder.binding.imgFileType.setImageResource(R.drawable.ic_folder)
        }

        holder.binding.fileContainer.setOnClickListener {
            listener.onFileClicked(files[position])
        }

        holder.binding.fileContainer.setOnLongClickListener {
            listener.onFileLongClicked(files[position], position)
            true
        }
    }

    override fun getItemCount(): Int {
        return files.size
    }

    interface OnFileSelectedListener{
        fun onFileClicked(file: File)
        fun onFileLongClicked(file: File, position: Int)
    }

    inner class FileViewHolder(val binding: FileContainerBinding) : RecyclerView.ViewHolder(binding.root)
}