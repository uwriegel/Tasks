package com.gmail.uwriegel.tasks.utils

import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

/**
 * Created by urieg on 04.06.2017.
 */
fun downloadFile(urlString: String, outputStream: OutputStream) {
    val url = URL(urlString)
    val httpConnection = url.openConnection() as HttpURLConnection
    val responseCode = httpConnection.responseCode
    if (responseCode == HttpURLConnection.HTTP_OK) {
        val stream = httpConnection.inputStream
        val bytes = ByteArray(20000)
        while (true) {
            val read = stream.read(bytes)
            if (read == -1)
                break
            outputStream.write(bytes, 0, read)
        }
    }
}
