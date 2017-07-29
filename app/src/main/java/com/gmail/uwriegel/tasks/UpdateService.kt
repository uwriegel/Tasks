package com.gmail.uwriegel.tasks

import android.app.IntentService
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.Cursor
import com.gmail.uwriegel.tasks.activities.MainActivity
import com.gmail.uwriegel.tasks.db.TasksContentProvider
import com.gmail.uwriegel.tasks.db.TasksTable
import com.gmail.uwriegel.tasks.google.createCredential
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.DateTime
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
                ACTION_UPDATE_LOCAL -> {
                    val accountName = intent.getStringExtra(EXTRA_ACCOUNT_NAME)
                    val selectedTasklist = intent.getStringExtra(EXTRA_SELECTED_TASKLIST)
                    handleActionUpdateLocal(accountName, selectedTasklist)
                }
                ACTION_UPDATE_REMOTE -> {
                    val accountName = intent.getStringExtra(EXTRA_ACCOUNT_NAME)
                    val selectedTasklist = intent.getStringExtra(EXTRA_SELECTED_TASKLIST)
                    handleActionUpdateRemote(accountName, selectedTasklist)
                }
            }
        }
    }

    private fun handleActionUpdateLocal(accountName: String, selectedTasklist: String) {

        val dateNow = Date()

        fun broadcast(type: String) {
            val intent = Intent()
            intent.action = MainActivity.BROADCAST_RECEIVER
            intent.putExtra(MainActivity.BROADCAST_TYPE, type)
            sendBroadcast(intent)
        }

        try {
            broadcast(MainActivity.BROADCAST_START_UPDATE)

            val service = getService(accountName)

            val lastUpdated = Settings.instance.getLocalUpdateTime(applicationContext)

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
            if (result.items != null) {

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
                    insertOrUpdateTask(service, selectedTasklist, selectedTasklist, task)
            }

            Settings.instance.setLocalUpdateTime(applicationContext, dateNow)

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            broadcast(MainActivity.BROADCAST_UPDATED)
        }
    }

    private fun handleActionUpdateRemote(accountName: String, selectedTasklist: String) {
        fun updateTask(queryUpdated: Cursor) {

            val service = getService(accountName)

            val googleId = queryUpdated.getString(0)
            val dbUpdated = queryUpdated.getLong(5)

            var task: Task? = null
            if (googleId != null)
                task = service.tasks().get(selectedTasklist, googleId)
                        //.setFields("items(id,title,notes,due,completed,updated)")
                        .execute()
            if (googleId != null && task != null) {
                if (dbUpdated > task.updated.value) {
                    if (queryUpdated.getLong(6).compareTo(1) == 0) {
                        task.updated = DateTime(dbUpdated)
                        task.completed = DateTime(Date(), TimeZone.getDefault())
                        task.status = "completed"
                        service.tasks().update(selectedTasklist, googleId, task).execute()
                        contentResolver.delete(TasksContentProvider.CONTENT_URI, "${TasksTable.KEY_GOOGLE_ID} = '${googleId}'", null)
                    } else {
                        // Update
                        task.updated = DateTime(dbUpdated)
                        task.title = queryUpdated.getString(1)
                        task.notes =  queryUpdated.getString(2)
                        if (queryUpdated.getLong(4) == 1L)
                            task.due =  DateTime(Date(queryUpdated.getLong(3)), TimeZone.getDefault())
                        service.tasks().update(selectedTasklist, googleId, task).execute()
                    }
                }
            }
            else {
                if (queryUpdated.getLong(6).compareTo(1) == 0)
                    contentResolver.delete(TasksContentProvider.CONTENT_URI, "${TasksTable.KEY_GOOGLE_ID} = '${googleId}'", null)
                else {
                    // Neue einfügen
                    task = Task()
                    task.updated = DateTime(dbUpdated)
                    task.title = queryUpdated.getString(1)
                    task.notes =  queryUpdated.getString(2)
                    if (queryUpdated.getLong(4) == 1L)
                        task.due =  DateTime(Date(queryUpdated.getLong(3)), TimeZone.getDefault())
                    val taskNew = service.tasks().insert(selectedTasklist, task).execute()

                    val values = ContentValues()
                    val id = queryUpdated.getLong(7)
                    values.put(TasksTable.KEY_GOOGLE_ID, taskNew.id)
                    val where = "${TasksTable.KEY_ID} = ${id}"
                    contentResolver.update(TasksContentProvider.CONTENT_URI, values, where, null)
                }
            }
        }

        val dateNow = Date()

        val lastUpdated = Settings.instance.getRemoteUpdateTime(applicationContext)

        val where = "${TasksTable.KEY_UPDATED} > ${lastUpdated.time}"
        val queryUpdated = contentResolver.query(TasksContentProvider.CONTENT_URI, arrayOf(
                TasksTable.KEY_GOOGLE_ID,
                TasksTable.KEY_TITLE,
                TasksTable.KEY_NOTES,
                TasksTable.KEY_DUE,
                TasksTable.KEY_HAS_DUE,
                TasksTable.KEY_UPDATED,
                TasksTable.KEY_DELETED,
                TasksTable.KEY_ID), where, null, null)

        if (queryUpdated.count > 0) {
            queryUpdated.moveToFirst()
            while (true) {
                updateTask(queryUpdated)
                if (!queryUpdated.moveToNext())
                    break
            }
        }

        Settings.instance.setRemoteUpdateTime(applicationContext, dateNow)
    }

    private fun getService(accountName: String): com.google.api.services.tasks.Tasks {
        val tasksCredential = createCredential(this, accountName)
        val transport = AndroidHttp.newCompatibleTransport()
        val jsonFactory = JacksonFactory.getDefaultInstance()
        return com.google.api.services.tasks.Tasks.Builder(transport, jsonFactory, tasksCredential)
                .setApplicationName("Aufgaben")
                .build()
    }

    private fun checkRemoveTask(taskTableId: String, task: Task) {
        val where = "${TasksTable.KEY_GOOGLE_ID} = '${task.id}'"
        // If the earthquake is new, insert it into the provider.
        val query = contentResolver.query(TasksContentProvider.CONTENT_URI, null, where, null, null)
        if (query?.count != 0)
            contentResolver.delete(TasksContentProvider.CONTENT_URI, where, null)
        query.close()
    }

    private fun insertOrUpdateTask(service: com.google.api.services.tasks.Tasks, selectedTasklist: String, taskTableId: String, task: Task) {
        // Construct a where clause to make sure we don’t already have this
        // earthquake in the provider.
        val where = "${TasksTable.KEY_GOOGLE_ID} = '${task.id}'"
        // If the earthquake is new, insert it into the provider.
        val query = contentResolver.query(TasksContentProvider.CONTENT_URI, arrayOf(
            TasksTable.KEY_TITLE,
            TasksTable.KEY_NOTES,
            TasksTable.KEY_DUE,
            TasksTable.KEY_HAS_DUE,
            TasksTable.KEY_UPDATED,
            TasksTable.KEY_DELETED), where, null, null)
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
            val dbUpdated = query.getLong(4)
            if (dbUpdated < task.updated.value) {
                val values = ContentValues()
                values.put(TasksTable.KEY_TASK_TABLE_ID, taskTableId)
                values.put(TasksTable.KEY_GOOGLE_ID, task.id)
                values.put(TasksTable.KEY_TITLE, task.title)
                values.put(TasksTable.KEY_NOTES, task.notes)
                values.put(TasksTable.KEY_DUE, task.due?.value)
                values.put(TasksTable.KEY_HAS_DUE, if (task.due != null) 1 else 0)
                values.put(TasksTable.KEY_UPDATED, task.updated.value)
                values.put(TasksTable.KEY_DELETED, 0)
                contentResolver.update(TasksContentProvider.CONTENT_URI, values, where, null)
            }
        }
        query.close()
    }

    companion object {

        /**
         * Starts this service to perform Update from Google to local Database with the given parameters. If
         * the service is already performing a task this action will be queued.

         * @see IntentService
         */
        fun startUpdateLocal(context: Context, accountName: String, selectedTasklist: String) {
            val intent = Intent(context, UpdateService::class.java)
            intent.action = ACTION_UPDATE_LOCAL
            intent.putExtra(EXTRA_ACCOUNT_NAME, accountName)
            intent.putExtra(EXTRA_SELECTED_TASKLIST, selectedTasklist)
            context.startService(intent)
        }

        /**
         * Starts this service to perform Update from local Database to Google with the given parameters. If
         * the service is already performing a task this action will be queued.

         * @see IntentService
         */
        fun startUpdateRemote(context: Context, accountName: String, selectedTasklist: String) {
            val intent = Intent(context, UpdateService::class.java)
            intent.action = ACTION_UPDATE_REMOTE
            intent.putExtra(EXTRA_ACCOUNT_NAME, accountName)
            intent.putExtra(EXTRA_SELECTED_TASKLIST, selectedTasklist)
            context.startService(intent)
        }

        private val ACTION_UPDATE_LOCAL = "com.gmail.uwriegel.tasks.action.update.local"
        private val ACTION_UPDATE_REMOTE = "com.gmail.uwriegel.tasks.action.update.remote"
        private val EXTRA_ACCOUNT_NAME = "com.gmail.uwriegel.tasks.extra.ACCOUNT_NAME"
        private val EXTRA_SELECTED_TASKLIST = "com.gmail.uwriegel.tasks.extra.SELECTED_TASKLIST"
    }
}
