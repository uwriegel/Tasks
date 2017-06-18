package com.gmail.uwriegel.tasks.webview

import android.net.Uri
import android.util.Base64
import android.webkit.WebView
import com.gmail.uwriegel.tasks.data.CalendarItem
import com.gmail.uwriegel.tasks.data.Items
import com.gmail.uwriegel.tasks.data.Task
import com.gmail.uwriegel.tasks.google.Tasklist
import com.gmail.uwriegel.tasks.json.GoogleAccount
import com.google.gson.Gson

/**
 * Created by urieg on 10.06.2017.
 */
fun WebView.setTasks(tasks: List<Task>, calendarItems: List<CalendarItem>) {
    val items = Items(tasks, calendarItems)
    val itemsString = Gson().toJson(items)
    val b64 = convertToBase64(itemsString)
    this.loadUrl("javascript:setTasks('$b64')")
}

fun WebView.setTasksList(tasksList: Iterable<Tasklist>, selectedTasklist: String) {
    val tasksListString = Gson().toJson(tasksList)
    val b64 = convertToBase64(tasksListString)
    this.loadUrl("javascript:setTasksList('$b64', '$selectedTasklist')")
}

fun WebView.setAccount(googleAccount: GoogleAccount) {
    val googleAccountString = Gson().toJson(googleAccount)
    val b64 = convertToBase64(googleAccountString)
    this.loadUrl("javascript:initializeAccount('$b64')")
}

private fun convertToBase64(text: String): String {
    val encoded = Uri.encode(text)
    val data = encoded.toByteArray()
    return Base64.encodeToString(data, Base64.DEFAULT)
}

