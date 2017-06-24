package com.gmail.uwriegel.tasks

import android.app.IntentService
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import com.gmail.uwriegel.tasks.activities.MainActivity
import com.gmail.uwriegel.tasks.db.TasksContentProvider
import com.gmail.uwriegel.tasks.db.TasksTable
import com.gmail.uwriegel.tasks.google.createCredential
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.tasks.model.Task
import com.google.api.services.tasks.model.Tasks
import java.util.*

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
            }
        }
    }

    private fun handleActionUpdate(accountName: String, selectedTasklist: String) {

        val dateNow = Date()

        fun broadcast(type: String) {
            val intent = Intent()
            intent.action = MainActivity.BROADCAST_RECEIVER
            intent.putExtra(MainActivity.BROADCAST_TYPE, type)
            sendBroadcast(intent)
        }

        try {
            broadcast(MainActivity.BROADCAST_START_UPDATE)

            val tasksCredential = createCredential(this, accountName)
            val transport = AndroidHttp.newCompatibleTransport()
            val jsonFactory = JacksonFactory.getDefaultInstance()
            val service = com.google.api.services.tasks.Tasks.Builder(transport, jsonFactory, tasksCredential)
                    .setApplicationName("Aufgaben")
                    .build()

            val lastUpdated = Settings.instance.getUpdateTime(applicationContext)

            fun getTasks(pageToken: String?): Tasks {
                var query = service.tasks().list(selectedTasklist)
                        .setFields("items(id,title,notes,due,completed,updated),nextPageToken")
                        //.setFields("items(id,title,notes,due,updated,status)")
                        .setShowCompleted(true)
                        .setUpdatedMin(lastUpdated.getFormattedString())
                if (pageToken != null)
                    query = query.setPageToken(pageToken)
                return query.execute()
            }

            var result = getTasks(null)
            if (result.items == null)
                return;
            val items = result.items
            while (result.nextPageToken != null) {
                result = getTasks(result.nextPageToken)
                items.plusAssign(result.items)
            }

            val tasks = items.filter { it.completed == null }
            val tasksCompleted = items.filter { it.completed != null }

            for (task in tasksCompleted)
                checkRemoveTask(selectedTasklist, task)
            for (task in tasks)
                insertOrUpdateTask(selectedTasklist, task)

            Settings.instance.setUpdateTime(applicationContext, dateNow)

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            broadcast(MainActivity.BROADCAST_UPDATED)
        }
    }

    private fun checkRemoveTask(taskTableId: String, task: Task) {
        val where = "${TasksTable.KEY_GOOGLE_ID} = '${task.id}'"
        // If the earthquake is new, insert it into the provider.
        val query = contentResolver.query(TasksContentProvider.CONTENT_URI, null, where, null, null)
        if (query?.count != 0)
            contentResolver.delete(TasksContentProvider.CONTENT_URI, where, null)
        query.close()
    }

    private fun insertOrUpdateTask(taskTableId: String, task: Task) {
        // Construct a where clause to make sure we don’t already have this
        // earthquake in the provider.
        val where = "${TasksTable.KEY_GOOGLE_ID} = '${task.id}'"
        // If the earthquake is new, insert it into the provider.
        val query = contentResolver.query(TasksContentProvider.CONTENT_URI, arrayOf(TasksTable.KEY_UPDATED), where, null, null)
        if (query?.count == 0) {
            val values = ContentValues()
            values.put(TasksTable.KEY_TASK_TABLE_ID, taskTableId)
            values.put(TasksTable.KEY_GOOGLE_ID, task.id)
            values.put(TasksTable.KEY_TITLE, task.title)
            values.put(TasksTable.KEY_NOTES, task.notes)
            values.put(TasksTable.KEY_DUE, task.due?.value)
            values.put(TasksTable.KEY_HAS_DUE, if (task.due != null) 1 else 0)
            values.put(TasksTable.KEY_UPDATED, task.updated.value)
            contentResolver.insert(TasksContentProvider.CONTENT_URI, values)
        }
        else {
            query.moveToFirst()
            val dbUpdated = query.getLong(0)

            val affe1 = task.updated.value

            if (dbUpdated < task.updated.value) {
                val values = ContentValues()
                values.put(TasksTable.KEY_TASK_TABLE_ID, taskTableId)
                values.put(TasksTable.KEY_GOOGLE_ID, task.id)
                values.put(TasksTable.KEY_TITLE, task.title)
                values.put(TasksTable.KEY_NOTES, task.notes)
                values.put(TasksTable.KEY_DUE, task.due?.value)
                values.put(TasksTable.KEY_HAS_DUE, if (task.due != null) 1 else 0)
                values.put(TasksTable.KEY_UPDATED, task.updated.value)
                contentResolver.update(TasksContentProvider.CONTENT_URI, values, where, null)
            }
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
