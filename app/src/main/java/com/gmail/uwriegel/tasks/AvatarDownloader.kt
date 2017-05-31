package com.gmail.uwriegel.tasks

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.text.BoringLayout

import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.logging.LogRecord

/**
 * Created by urieg on 01.05.2017.
 */
internal object AvatarDownloader {
    val FILE = "account.jpg"

    fun start(mainActivity: Activity, urlString: String, onFinished: IOnFinished) {
        val file = File(mainActivity.filesDir, FILE)
        if (file.exists())
            try {
                file.delete()
            } catch (e: Exception) {
                e.printStackTrace()
            }

        val handler = Handler()
        Thread(Runnable {
            var success: Boolean = false
            try {
                val url = URL(urlString)
                val httpConnection = url.openConnection() as HttpURLConnection
                val responseCode = httpConnection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val outputStream = mainActivity.openFileOutput(FILE, Context.MODE_PRIVATE)
                    val stream = httpConnection.inputStream
                    val bytes = ByteArray(20000)
                    while (true) {
                        val read = stream.read(bytes)
                        if (read == -1)
                            break
                        outputStream.write(bytes, 0, read)
                    }
                    outputStream.close()
                    success = true
                }
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                val result = success
                handler.post { onFinished.onFinished(result) }
            }
        }).start()
    }

    internal interface IOnFinished {
        fun onFinished(success: Boolean)
    }
}
