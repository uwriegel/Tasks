package com.gmail.uwriegel.tasks.google

import android.content.Context
import android.util.Log
import com.gmail.uwriegel.tasks.MainActivity
import com.gmail.uwriegel.tasks.Settings
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.tasks.Tasks
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

/**
 * Created by urieg on 04.06.2017.
 */
class TasklistsUpdater(val context: Context) {

    fun update(onSuccessCallback: (Iterable<Tasklist>)->Unit) {
        val credential = createCredential(context, Settings.instance.googleAccount.name)
        doAsync {
            try {
                val transport = AndroidHttp.newCompatibleTransport()
                val jsonFactory = JacksonFactory.getDefaultInstance()
                val service = Tasks.Builder(transport, jsonFactory, credential)
                        .setApplicationName("Aufgaben")
                        .build()

                val result = service.tasklists().list()
                        .setMaxResults(10L)
                        .execute()

                val taskLists = result.items.map { Tasklist(it.title, it.id) }
                uiThread {
                    val capture = context
                    onSuccessCallback(taskLists)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

