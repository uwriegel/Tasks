package com.gmail.uwriegel.tasks.webview

import android.content.Context
import android.net.Uri
import android.util.Base64
import android.webkit.JavascriptInterface
import android.webkit.WebView
import com.gmail.uwriegel.tasks.data.query
import com.google.gson.Gson
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

/**
 * Created by urieg on 09.06.2017.
 */
class JavascriptInterface(val context: Context, val contentView: WebView) {
    @JavascriptInterface
    fun initialize() {
        context.doAsync {
            val tasks = query(context)
            uiThread { contentView.setTasks(tasks)}
        }
    }

    @JavascriptInterface
    fun affe(schrott: String) {
        val aff = schrott
        val a = aff
    }
}

