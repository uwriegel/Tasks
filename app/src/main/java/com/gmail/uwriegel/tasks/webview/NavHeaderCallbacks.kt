package com.gmail.uwriegel.tasks.webview

import com.gmail.uwriegel.tasks.google.Tasklist

/**
 * Created by urieg on 11.06.2017.
 */
interface NavHeaderCallbacks {
    abstract fun onTasklistSelected(tasklist: Tasklist): Unit
}