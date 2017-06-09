package com.gmail.uwriegel.tasks.data

import android.content.Context
import com.gmail.uwriegel.tasks.Settings
import com.gmail.uwriegel.tasks.db.TasksContentProvider
import com.gmail.uwriegel.tasks.db.TasksTable

/**
 * Created by urieg on 08.06.2017.
 */
fun query(context: Context): List<Task> {
    val cursor = context.contentResolver.query(TasksContentProvider.CONTENT_URI, arrayOf(TasksTable.KEY_ID, TasksTable.KEY_TITLE, TasksTable.KEY_NOTES, TasksTable.KEY_DUE),
            "${TasksTable.KEY_TASK_TABLE_ID} = '${Settings.instance.selectedTasklist}'", null, null)

    fun toTaskList(): List<Task> {
        val list = ArrayList<Task>()
        if (cursor.moveToFirst())
            while (true) {
                val task = Task(cursor.getString(1),
                        cursor.getString(2) ?: "",
                        cursor.getLong(0),
                        cursor.getLong(3))
                list.add(task)
                if (!cursor.moveToNext())
                    break
            }
        return list.toList()
    }

    return toTaskList()
}

