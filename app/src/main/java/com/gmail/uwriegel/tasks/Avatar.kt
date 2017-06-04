package com.gmail.uwriegel.tasks

import android.content.Context
import android.graphics.BitmapFactory
import android.widget.ImageView
import com.gmail.uwriegel.tasks.utils.downloadFile
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.io.File
import java.io.IOException

/**
 * Created by urieg on 04.06.2017.
 */
class Avatar(val context: Context) {

    fun set(imageView: ImageView) {
        fun delete(file: File) {
            if (file.exists())
                try {
                    file.delete()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
        }

        val file = File(context.filesDir, ACCOUNT_IMAGE_FILE)
        if (Settings.instance.getIsAvatarDownloaded(context)) {
            if (file.exists()) {
                val myBitmap = BitmapFactory.decodeFile(file.absolutePath)
                imageView.setImageBitmap(myBitmap)
            } else
                imageView.setImageDrawable(context.getDrawable(R.drawable.avatar))
        }
        else {
            delete(file)
            doAsync {
                if (Settings.instance.googleAccount.photoUrl != "") {
                    val outputStream = context.openFileOutput(ACCOUNT_IMAGE_FILE, Context.MODE_PRIVATE)
                    try {
                        downloadFile(Settings.instance.googleAccount.photoUrl, outputStream)
                        outputStream?.close()
                        uiThread {
                            val myBitmap = BitmapFactory.decodeFile(file.absolutePath)
                            imageView.setImageBitmap(myBitmap)
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                        uiThread { imageView.setImageDrawable(context.getDrawable(R.drawable.avatar)) }
                        outputStream?.close()
                        delete(file)
                    } finally {
                        Settings.instance.setIsAvatarDownloaded(context, true)
                    }
                }
                else
                    uiThread { imageView.setImageDrawable(context.getDrawable(R.drawable.avatar)) }
            }
        }
    }

    private val ACCOUNT_IMAGE_FILE = "account.jpg"
}