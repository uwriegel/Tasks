package com.gmail.uwriegel.tasks.webview

import android.content.Context
import android.view.SoundEffectConstants
import android.webkit.JavascriptInterface
import android.webkit.WebView
import com.gmail.uwriegel.tasks.data.queryAllTasks
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

/**
 * Created by urieg on 09.06.2017.
 */
class JavascriptInterface(val context: Context, val contentView: WebView, val callbacks: Callbacks) {
    @JavascriptInterface
    fun initialize() = queryAllTasks(context, {tasks, calendarItems -> contentView.setTasks(tasks, calendarItems) })
    @JavascriptInterface
    fun doHapticFeedback() = context.doAsync { uiThread { contentView.playSoundEffect(SoundEffectConstants.CLICK) } }
    @JavascriptInterface
    fun deleteTask(id: String, delete: Boolean) = callbacks.deleteTask(id, delete)
    @JavascriptInterface
    fun showEvent(id: String) = context.doAsync { uiThread { callbacks.showEvent(id) } }
}

