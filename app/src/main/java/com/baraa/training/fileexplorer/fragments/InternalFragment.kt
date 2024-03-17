package com.baraa.training.fileexplorer.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.format.Formatter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.baraa.training.fileexplorer.R
import com.baraa.training.fileexplorer.adapters.FileAdapter
import com.baraa.training.fileexplorer.databinding.FragmentInternalBinding
import com.baraa.training.fileexplorer.databinding.OptionDialogBinding
import com.baraa.training.fileexplorer.databinding.OptionLayoutBinding
import com.baraa.training.fileexplorer.others.FileOpener
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date

@Suppress("NAME_SHADOWING")
class InternalFragment : Fragment(), FileAdapter.OnFileSelectedListener{

    private lateinit var binding: FragmentInternalBinding
    private lateinit var fileAdapter: FileAdapter
    private lateinit var fileList: ArrayList<File>
    private lateinit var imageBack: ImageView
    private lateinit var tvPathHolder: TextView
    private lateinit var storage: File

    private lateinit var data : String

    private lateinit var optionDialog: Dialog

    @SuppressLint("SdCardPath")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentInternalBinding.inflate(inflater)

        tvPathHolder = binding.tvPathHolder
        imageBack = binding.imgBack

        val internalStorage = System.getenv("EXTERNAL_STORAGE")
        storage = File(internalStorage!!)

        try {
            data = requireArguments().getString("path", "/sdcard")
            val file = File(data)
            storage = file
        }catch (e: Exception){
            e.printStackTrace()
        }

        tvPathHolder.text = storage.absolutePath

        runTimePermission()

        return binding.root
    }

    private fun runTimePermission() {
        Dexter.withContext(context)
            .withPermissions(android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .withListener(object  : MultiplePermissionsListener {
                override fun onPermissionsChecked(multiplePermissionsReport: MultiplePermissionsReport?) {
                    displayFiles()
                }
                override fun onPermissionRationaleShouldBeShown(
                    list: MutableList<PermissionRequest>?,
                    permissionToken: PermissionToken?
                ) {
                    permissionToken?.continuePermissionRequest()
                }
            }).check()
    }

    private fun findFiles(file: File) : ArrayList<File> {
        val arrayList : ArrayList<File> = ArrayList()
        val files = file.listFiles()

        files?.forEach { singleFile ->
            if (singleFile.isDirectory && !singleFile.isHidden)
                arrayList.add(singleFile)
        }

        files?.forEach { singleFile ->
            if (singleFile.name.lowercase().endsWith(".jpeg") ||
                singleFile.name.lowercase().endsWith(".jpg") ||
                singleFile.name.lowercase().endsWith(".png") ||
                singleFile.name.lowercase().endsWith(".mp3") ||
                singleFile.name.lowercase().endsWith(".wav") ||
                singleFile.name.lowercase().endsWith(".mp4") ||
                singleFile.name.lowercase().endsWith(".pdf") ||
                singleFile.name.lowercase().endsWith(".doc") ||
                singleFile.name.lowercase().endsWith(".apk")) {
                arrayList.add(singleFile)
            }
        }
        return arrayList
    }

    private fun displayFiles() {
        fileList = ArrayList()
        fileList.addAll(findFiles(storage))

        fileAdapter = FileAdapter(requireContext(), fileList, this)
        binding.recyclerInternal.apply {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(context, 2, GridLayoutManager.VERTICAL, false)
            adapter = fileAdapter
        }
    }

    override fun onFileClicked(file: File) {
        if(file.isDirectory) run {
            val bundle = Bundle()
            bundle.putString("path", file.absolutePath)

            val internalFragment = InternalFragment()
            internalFragment.arguments = bundle
            parentFragmentManager.beginTransaction()
                .replace(R.id.container, internalFragment)
                .addToBackStack(null)
                .commit()

        } else run {
            try {
                FileOpener().openFile(context, file)
            }catch (e: IOException){
                e.printStackTrace()
            }
        }
    }

    @SuppressLint("SetTextI18n", "SimpleDateFormat", "NotifyDataSetChanged")
    override fun onFileLongClicked(file: File, position: Int) {

        val bindingOptionDialog = OptionDialogBinding.inflate(layoutInflater)
        optionDialog = Dialog(requireContext())
        optionDialog.run {
            setContentView(bindingOptionDialog.root)
            setTitle("Select Options")
        }

        bindingOptionDialog.listView.adapter = CustomAdapter()
        bindingOptionDialog.listView.setOnItemClickListener { parent, _, position, _ ->
            optionDialog.cancel()

            when (parent.getItemAtPosition(position)){
                "Details" -> run {
                    val detailDialog = AlertDialog.Builder(context)
                    detailDialog.setTitle("Details")
                    val details = TextView(context)
                    detailDialog.setView(details)
                    val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
                    val formattedDate = formatter.format(Date(file.lastModified()))

                    details.text = "File name: ${file.name} \n\n" +
                            "Size: ${Formatter.formatShortFileSize(context, file.length())} \n\n" +
                            "Path: ${file.absolutePath} \n\n" +
                            "Last Modified: $formattedDate"

                    detailDialog.setPositiveButton("Ok") { _, _ -> }

                    detailDialog.create()
                    detailDialog.show()
                }
                "Rename" -> {
                    val renameDialog = AlertDialog.Builder(context)
                    renameDialog.setTitle("Rename File:")
                    val name = EditText(context)
                    renameDialog.setView(name)

                    renameDialog.setPositiveButton("Ok") { _, _ ->
                        val newName = name.text.toString()
//                    val extension = theFile.absolutePath.substring(theFile.absolutePath.lastIndexOf("."))
                        val current = File(file.absolutePath)
                        val destination = File(file.absolutePath.replace(file.name, newName) + file.extension)

                        if(current.renameTo(destination)){
                            fileList[position] = destination
                            fileAdapter.notifyDataSetChanged()
                            Toast.makeText(context, "Renamed!", Toast.LENGTH_LONG).show()
                        }else
                            Toast.makeText(context, "Couldn't rename!", Toast.LENGTH_LONG).show()
                    }

                    renameDialog.setNegativeButton("Cancel") { _, _ ->
                        optionDialog.cancel()
                    }
                    renameDialog.create()
                    renameDialog.show()
                }
                "Delete" -> {
                    val deleteDialog = AlertDialog.Builder(context)
                    deleteDialog.setTitle("Delete ${file.name}?")
                    deleteDialog.setPositiveButton("Yes") { _, _ ->
                        file.delete()
                        fileList.remove(file)
                        fileAdapter.notifyDataSetChanged()
                        Toast.makeText(context, "Deleted!", Toast.LENGTH_LONG).show()
                    }
                    deleteDialog.setNegativeButton("No") { _, _ ->
                        optionDialog.cancel()
                    }
                    deleteDialog.create()
                    deleteDialog.show()
                }
                "Share" -> {
                    val share = Intent().apply {
                        action = Intent.ACTION_SEND
                        type = "image/jpeg"
                        putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file))
                    }
                    startActivity(Intent.createChooser(share, "Share ${file.name}"))
                }
            }
        }
        optionDialog.show()
    }

    inner class CustomAdapter : BaseAdapter(){

        private var items: java.util.ArrayList<String> = java.util.ArrayList()
        init {
            items.addAll(arrayOf("Details", "Rename", "Delete", "Share"))
        }

        override fun getCount(): Int {
            return items.size
        }

        override fun getItem(position: Int): Any {
            return items[position]
        }

        override fun getItemId(position: Int): Long {
            return 0
        }

        @SuppressLint("ViewHolder")
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val adapterBinding = OptionLayoutBinding.inflate(layoutInflater)

            val tvOption = adapterBinding.tvOption
            val imgOption = adapterBinding.imgOption
            tvOption.text = items[position]

            if(items[position] == "Details")
                imgOption.setImageResource(R.drawable.ic_details)

            else if(items[position] == "Rename")
                imgOption.setImageResource(R.drawable.ic_rename)

            else if(items[position] == "Delete")
                imgOption.setImageResource(R.drawable.ic_delete)

            else if(items[position] == "Share")
                imgOption.setImageResource(R.drawable.ic_share)

            return adapterBinding.root
        }
    }
}