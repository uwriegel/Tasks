package com.gmail.uwriegel.tasks

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.database.SQLException
import android.os.Handler
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

/**
 * Created by urieg on 17.05.2017.
 */

class TaskAdapter(context: Context) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    init {
        val projection = arrayOf(TasksContentProvider.KEY_ID, TasksContentProvider.KEY_TITLE, TasksContentProvider.KEY_Notes, TasksContentProvider.KEY_DUE)

        val handler = Handler()
        Thread(Runnable {
            val cr = context.contentResolver
            val taskList = Settings.instance.selectedTasklist
            cursor = cr.query(TasksContentProvider.CONTENT_URI, projection, "${TasksContentProvider.KEY_TASK_TABLE_ID} = '$taskList'", null, null)
            handler.post({
                notifyDataSetChanged()
            })
        }).start()
    }

    fun clear() {
        cursor = DefaultCursor()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.taskview, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        cursor.moveToPosition(position)
        holder.viewTitle.text = cursor.getString(1)
        holder.viewNotes.text = cursor.getString(2)
    }

    override fun getItemCount(): Int {
        return cursor.count
    }

    class TaskViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        var viewTitle: TextView = v.findViewById(R.id.viewTitle) as TextView
        var viewNotes: TextView = v.findViewById(R.id.viewNotes) as TextView
        var viewDue: TextView = v.findViewById(R.id.viewDue) as TextView

    }

    internal var cursor: Cursor = DefaultCursor()
}
