package com.gmail.uwriegel.tasks

import android.content.Context
import android.net.Uri
import android.util.Base64
import android.webkit.JavascriptInterface
import android.webkit.WebView
import com.gmail.uwriegel.tasks.data.query
import com.google.gson.Gson
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.net.URLEncoder

/**
 * Created by urieg on 09.06.2017.
 */
class JavascriptInterface(val context: Context, val contentView: WebView) {
    @JavascriptInterface
    fun initialize() {
        context.doAsync {
            val tasks = query(context)
            val tasksString = Gson().toJson(tasks)
            val b64 = convertToBase64(tasksString)
            uiThread { contentView.loadUrl("javascript:setTasks('$b64')")}
        }
    }

    @JavascriptInterface
    fun affe(schrott: String) {
        val aff = schrott
        val a = aff
    }
}

private fun convertToBase64(text: String): String {
    val encoded = Uri.encode(text)
    val data = encoded.toByteArray()
    return Base64.encodeToString(data, Base64.DEFAULT)
}