package com.gmail.uwriegel.tasks.webview

import android.content.Context
import android.view.SoundEffectConstants
import android.webkit.JavascriptInterface
import android.webkit.WebView
import com.gmail.uwriegel.tasks.Settings
import com.gmail.uwriegel.tasks.data.queryAllTasks
import com.gmail.uwriegel.tasks.google.Tasklist
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

/**
 * Created by urieg on 11.06.2017.
 */
class NavJavascriptInterface(val context: Context, val navView: WebView, val callback: NavHeaderCallbacks) {
    @JavascriptInterface
    fun initialize() {
        context.doAsync {
            callback.getCalendarsList()
            val tasklists = Settings.instance.getTasklists(context)
            uiThread { navView.setTasksList(tasklists, Settings.instance.selectedTasklist) }
        }
    }

    @JavascriptInterface
    fun doHapticFeedback() {
        context.doAsync {
            uiThread { navView.playSoundEffect(SoundEffectConstants.CLICK) }
        }
    }

    @JavascriptInterface
    fun chooseAccount() {
        callback.chooseAccount()
    }

    @JavascriptInterface
    fun selectTasklist(tasklistString: String) {
        val builder = GsonBuilder()
        val gson = builder.create()
        val tasklist = gson.fromJson<Tasklist>(tasklistString, Tasklist::class.java)
        callback.onTasklistSelected(tasklist)
    }

    @JavascriptInterface
    fun selectCalendarsList(CalendarsListString: String) {
        val builder = GsonBuilder()
        val gson = builder.create()
        val listsType = object : TypeToken<List<String>>() {}.type
        val calendarsList = gson.fromJson<List<String>>(CalendarsListString, listsType)
        Settings.instance.setCalendarsList(context, calendarsList)
        queryAllTasks(context, { t, c -> callback.onSetItems(t, c)})
    }
}

