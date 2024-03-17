package com.baraa.training.fileexplorer.others

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException
import java.util.Locale

class FileOpener {

    @Throws(IOException::class)
    fun openFile(context: Context?, file: File?) {
        val uri = context?.let { theContext ->
            file?.let { theFile ->
                FileProvider.getUriForFile(
                    theContext, theContext.applicationContext?.packageName + ".provider",
                    theFile
                )
            }
        }
        val intent = Intent(Intent.ACTION_VIEW)

        if(uri.toString().lowercase(Locale.ROOT).contains(".doc"))
            intent.setDataAndType(uri, "application/msword")

        else if (uri.toString().lowercase(Locale.ROOT).contains(".pdf"))
            intent.setDataAndType(uri, "application/pdf")

        else if (uri.toString().lowercase(Locale.ROOT).contains(".mp3")
            || uri.toString().lowercase(Locale.ROOT).contains(".wav"))
            intent.setDataAndType(uri, "audio/x-wav")

        else if (uri.toString().lowercase(Locale.ROOT).contains(".jpeg")
            || uri.toString().lowercase(Locale.ROOT).contains(".jpg")
            || uri.toString().lowercase(Locale.ROOT).contains(".png"))
            intent.setDataAndType(uri, "image/jpeg")

        else if (uri.toString().lowercase(Locale.ROOT).contains(".mp4"))
            intent.setDataAndType(uri, "video/*")

        else
            intent.setDataAndType(uri, "*/*")

        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        context?.startActivity(intent)
    }
}