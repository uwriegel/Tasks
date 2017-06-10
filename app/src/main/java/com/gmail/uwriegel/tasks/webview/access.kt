package com.gmail.uwriegel.tasks.webview

import android.net.Uri
import android.util.Base64
import android.webkit.WebView
import com.gmail.uwriegel.tasks.data.Task
import com.google.gson.Gson

/**
 * Created by urieg on 10.06.2017.
 */
fun WebView.setTasks(tasks: List<Task>) {
    val tasksString = Gson().toJson(tasks)
    val b64 = convertToBase64(tasksString)
    this.loadUrl("javascript:setTasks('$b64')")
}

private fun convertToBase64(text: String): String {
    val encoded = Uri.encode(text)
    val data = encoded.toByteArray()
    return Base64.encodeToString(data, Base64.DEFAULT)
}

