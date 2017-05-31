package com.gmail.uwriegel.tasks

import android.app.IntentService
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.Cursor

import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.http.HttpRequest
import com.google.api.client.http.HttpRequestInitializer
import com.google.api.client.http.HttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.tasks.model.Task
import com.google.api.services.tasks.model.Tasks

import java.io.IOException

/**
 * An [IntentService] subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 *
 *
 * helper methods.
 */
class UpdateService : IntentService("UpdateService") {

    override fun onHandleIntent(intent: Intent?) {
        if (intent != null) {
            val action = intent.action
            when (action) {
                ACTION_UPDATE -> {
                    val accountName = intent.getStringExtra(EXTRA_ACCOUNT_NAME)
                    val selectedTasklist = intent.getStringExtra(EXTRA_SELECTED_TASKLIST)
                    handleActionUpdate(accountName, selectedTasklist)
                }
                else -> {
                }
            }
        }
    }

    private fun handleActionUpdate(accountName: String, selectedTasklist: String) {
        try {
            val tasksCredential = TasksCredential(this, accountName)
            val transport = AndroidHttp.newCompatibleTransport()
            val jsonFactory = JacksonFactory.getDefaultInstance()
            val service = com.google.api.services.tasks.Tasks.Builder(transport, jsonFactory, tasksCredential.credential)
                    .setApplicationName("Aufgaben")
                    .build()
            val result = service.tasks().list(selectedTasklist)
                    .setFields("items(id,title,notes,due,updated),nextPageToken")
                    //.setFields("items(id,title,notes,due,updated,status)")
                    //                    .setShowCompleted(true)
                    //.setPageToken()

                    .setShowCompleted(false)
                    //.setUpdatedMin("2017-05-01T00:00:00.000Z")
                    .execute()
            //.setShowCompleted(false).setUpdatedMin("2017-02-01T00:00:00.000Z").execute();
            val tasks = result.items
            for (task in tasks)
                insertTask(selectedTasklist, task)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun insertTask(taskTableId: String, task: Task) {
        val cr = contentResolver

        // Construct a where clause to make sure we don’t already have this
        // earthquake in the provider.
        val where = TasksContentProvider.KEY_GOOGLE_ID + " = '" + task.id + "'"
        // If the earthquake is new, insert it into the provider.
        val query = cr.query(TasksContentProvider.CONTENT_URI, null, where, null, null)
        if (query!!.count == 0) {
            val values = ContentValues()
            values.put(TasksContentProvider.KEY_TASK_TABLE_ID, taskTableId)
            values.put(TasksContentProvider.KEY_GOOGLE_ID, task.id)
            values.put(TasksContentProvider.KEY_TITLE, task.title)
            values.put(TasksContentProvider.KEY_Notes, task.notes)
            values.put(TasksContentProvider.KEY_DUE, task.due.value)
            values.put(TasksContentProvider.KEY_UPDATED, task.updated.value)
            cr.insert(TasksContentProvider.CONTENT_URI, values)
        }
        query.close()
    }

    companion object {

        /**
         * Starts this service to perform action Baz with the given parameters. If
         * the service is already performing a task this action will be queued.

         * @see IntentService
         */
        fun startUpdate(context: Context, accountName: String, selectedTasklist: String) {
            val intent = Intent(context, UpdateService::class.java)
            intent.action = ACTION_UPDATE
            intent.putExtra(EXTRA_ACCOUNT_NAME, accountName)
            intent.putExtra(EXTRA_SELECTED_TASKLIST, selectedTasklist)
            context.startService(intent)
        }

        private val ACTION_UPDATE = "com.gmail.uwriegel.tasks.action.update"
        private val EXTRA_ACCOUNT_NAME = "com.gmail.uwriegel.tasks.extra.ACCOUNT_NAME"
        private val EXTRA_SELECTED_TASKLIST = "com.gmail.uwriegel.tasks.extra.SELECTED_TASKLIST"
    }
}
