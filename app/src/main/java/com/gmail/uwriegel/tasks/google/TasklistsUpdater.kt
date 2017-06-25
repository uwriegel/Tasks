package com.gmail.uwriegel.tasks.google

import android.app.Activity
import android.content.Context
import com.gmail.uwriegel.tasks.Settings
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.tasks.Tasks
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

/**
 * Created by urieg on 04.06.2017.
 */
class TasklistsUpdater(val activity: Activity) {

    fun update(onSuccessCallback: (Iterable<Tasklist>)->Unit) {
        val credential = createCredential(activity, Settings.instance.googleAccount.name)
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
                    val capture = activity
                    onSuccessCallback(taskLists)
                }
            } catch (userRecoverableException: UserRecoverableAuthIOException) {
                activity.startActivityForResult(userRecoverableException.intent, 333);
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

