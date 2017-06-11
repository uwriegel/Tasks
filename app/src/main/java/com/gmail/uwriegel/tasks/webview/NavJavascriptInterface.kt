package com.gmail.uwriegel.tasks.webview

import android.content.Context
import android.net.Uri
import android.util.Base64
import android.view.SoundEffectConstants
import android.webkit.JavascriptInterface
import android.webkit.WebView
import com.gmail.uwriegel.tasks.Settings
import com.gmail.uwriegel.tasks.data.query
import com.gmail.uwriegel.tasks.google.Tasklist
import com.gmail.uwriegel.tasks.json.GoogleAccount
import com.google.gson.GsonBuilder
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.json.JSONObject

/**
 * Created by urieg on 11.06.2017.
 */
class NavJavascriptInterface(val context: Context, val navView: WebView) {
    @JavascriptInterface
    fun initialize() {
        context.doAsync {
            val tasks = query(context)
            val tasklists = Settings.instance.getTasklists(context)
            uiThread { navView.setTasksList(tasklists) }
        }
    }

    @JavascriptInterface
    fun doHapticFeedback() {
        context.doAsync {
            uiThread { navView.playSoundEffect(SoundEffectConstants.CLICK) }
        }
    }

    @JavascriptInterface
    fun selectTasklist(tasklistString: String) {
        val builder = GsonBuilder()
        val gson = builder.create()
        val tasklist = gson.fromJson<Tasklist>(tasklistString, Tasklist::class.java)
        context.doAsync {
            uiThread { navView.playSoundEffect(SoundEffectConstants.CLICK) }
        }
    }
}

