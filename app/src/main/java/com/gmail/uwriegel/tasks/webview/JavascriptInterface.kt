package com.gmail.uwriegel.tasks.webview

import android.content.Context
import android.webkit.JavascriptInterface
import android.webkit.WebView
import com.gmail.uwriegel.tasks.data.queryAllTasks

/**
 * Created by urieg on 09.06.2017.
 */
class JavascriptInterface(val context: Context, val contentView: WebView) {
    @JavascriptInterface
    fun initialize() {
        queryAllTasks(context, {contentView.setTasks(it) })
    }

    @JavascriptInterface
    fun affe(schrott: String) {
        val aff = schrott
        val a = aff
    }
}

