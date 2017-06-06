package com.gmail.uwriegel.tasks

import android.content.Context
import android.database.Cursor
import android.opengl.Visibility
import android.support.v7.widget.RecyclerView
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.TextView
import com.gmail.uwriegel.tasks.db.TasksContentProvider
import com.gmail.uwriegel.tasks.db.TasksTable
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

/**
 * Created by urieg on 17.05.2017.
 */

class TaskAdapter(context: Context) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    init {
        val projection = arrayOf(TasksTable.KEY_ID, TasksTable.KEY_TITLE, TasksTable.KEY_NOTES, TasksTable.KEY_DUE)

        doAsync {
            val taskList = Settings.instance.selectedTasklist
            cursor = context.contentResolver.query(TasksContentProvider.CONTENT_URI, projection, "${TasksTable.KEY_TASK_TABLE_ID} = '$taskList'", null, null)
            uiThread { notifyDataSetChanged() }
        }
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
        holder.viewNotes.visibility = if (holder.viewNotes.text != "") VISIBLE else GONE
        //noinspection deprecation
        holder.viewDue.text = Html.fromHtml("12.04<br>2017")
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
