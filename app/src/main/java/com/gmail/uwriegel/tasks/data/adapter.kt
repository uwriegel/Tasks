package com.gmail.uwriegel.tasks.data

import android.content.ContentUris
import android.content.Context
import android.provider.CalendarContract
import com.gmail.uwriegel.tasks.Settings
import com.gmail.uwriegel.tasks.db.TasksContentProvider
import com.gmail.uwriegel.tasks.db.TasksTable
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.util.*

fun queryAllTasks(context: Context, onFinished: (tasks: List<Task>, calendarItems: List<CalendarItem>)->Unit) {
    var tasks: List<Task>? = null
    var calendarItems: List<CalendarItem>? = null

    val calendarsList = Settings.instance.getCalendarsList(context)
    if (calendarsList.count() != 0)
        context.doAsync {
            calendarItems = queryCalendarItems(context, calendarsList)
            if (tasks != null)
                uiThread { onFinished(tasks!!, calendarItems!!) }
        }
    else
        calendarItems = ArrayList()

    context.doAsync {
        tasks = queryTasks(context)
        if (calendarItems != null)
            uiThread { onFinished(tasks!!, calendarItems!!) }
    }
}

/**
 * Created by urieg on 08.06.2017.
 */
fun queryTasks(context: Context): List<Task> {
    val cursor = context.contentResolver.query(TasksContentProvider.CONTENT_URI, arrayOf(TasksTable.KEY_ID, TasksTable.KEY_TITLE, TasksTable.KEY_NOTES, TasksTable.KEY_DUE),
            "${TasksTable.KEY_TASK_TABLE_ID} = '${Settings.instance.selectedTasklist}' AND ${TasksTable.KEY_DELETED} = 0",
            null, "${TasksTable.KEY_HAS_DUE} DESC, ${TasksTable.KEY_DUE}")

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

fun queryCalendarItems(context: Context, calendarIds: List<String>): List<CalendarItem> {
    val cal = Calendar.getInstance()
    cal.add(Calendar.DAY_OF_MONTH, -1)
    val yesterday = cal.timeInMillis
    cal.add(Calendar.DAY_OF_MONTH, 15)
    val week = cal.timeInMillis
    val eventsUriBuilder = CalendarContract.Instances.CONTENT_URI.buildUpon()
    ContentUris.appendId(eventsUriBuilder, yesterday)
    ContentUris.appendId(eventsUriBuilder, week)
    val eventUri = eventsUriBuilder.build()

    var calIds = Collections.nCopies(calendarIds.count(), "${CalendarContract.Events.CALENDAR_ID} = ?")
    var selection = calIds.reduce{total, next -> "$total OR $next"}
    val INSTANCE_PROJECTION = arrayOf(CalendarContract.Instances.TITLE, // 0
            CalendarContract.Instances.DESCRIPTION, // 1
            CalendarContract.Instances.BEGIN, // 2
            CalendarContract.Instances.END, // 3
            CalendarContract.Instances.EVENT_ID, // 4
            CalendarContract.Instances._ID)// 5

    val cursor = context.contentResolver.query(eventUri, INSTANCE_PROJECTION, selection, calendarIds.toTypedArray(), null)
    val list = ArrayList<CalendarItem>()
    while (cursor.moveToNext()) {
        var item = CalendarItem(cursor.getString(0),
                cursor.getString(4),
                cursor.getLong(2))
        list.add(item)
    }
    return list.toList()
}


