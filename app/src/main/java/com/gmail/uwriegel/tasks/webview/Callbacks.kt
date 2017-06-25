package com.gmail.uwriegel.tasks.webview

/**
 * Created by urieg on 18.06.2017.
 */
interface Callbacks {
    abstract fun deleteTask(id: String, delete: Boolean): Unit
    abstract fun updateDeletedTask(id: String): Unit
    abstract fun showEvent(eventId: String): Unit
}
