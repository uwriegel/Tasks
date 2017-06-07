package com.gmail.uwriegel.tasks

import android.content.Context
import android.database.Cursor
import android.os.Handler
import android.support.v7.widget.RecyclerView
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.TextView
import com.gmail.uwriegel.tasks.db.TasksContentProvider
import com.gmail.uwriegel.tasks.db.TasksTable
import com.gmail.uwriegel.tasks.db.getPosition
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by urieg on 17.05.2017.
 */

class TaskAdapter(val context: Context) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    init {
        refresh()
    }

    fun refresh() {
        query { notifyDataSetChanged() }
    }

    fun onResume() {
        TasksContentProvider.instance.registerOnInsert{ onInsert(it)}
        TasksContentProvider.instance.registerOnDelete{ onDelete(it)}
    }

    fun onPause() {
        TasksContentProvider.instance.unregisterOnInsert()
        TasksContentProvider.instance.unregisterOnDelete()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.taskview, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        try {
            cursor.moveToPosition(position)
            holder.viewTitle.text = cursor.getString(1)
            holder.viewNotes.text = cursor.getString(2)
            holder.viewNotes.visibility = if (holder.viewNotes.text != "") VISIBLE else GONE
            holder.id = cursor.getInt(0)
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        val date = Date(cursor.getLong(3))
        //noinspection deprecation
        val simpleDateFormat = SimpleDateFormat("dd.MM.")
        val simpleDateFormat2 = SimpleDateFormat("EE")
        holder.viewDue.text = Html.fromHtml("${simpleDateFormat2.format(date)}<br>${simpleDateFormat.format(date)}")
    }

    override fun getItemCount(): Int {
        return cursor.count
    }

    private fun query(onFinished: ()->Unit) {
        doAsync {
            val taskList = Settings.instance.selectedTasklist
            val newCursor = context.contentResolver.query(TasksContentProvider.CONTENT_URI,
                    arrayOf(TasksTable.KEY_ID, TasksTable.KEY_TITLE, TasksTable.KEY_NOTES, TasksTable.KEY_DUE),
                    "${TasksTable.KEY_TASK_TABLE_ID} = '$taskList'", null, null)
            uiThread {
                cursor.close()
                cursor = newCursor
                onFinished()
            }
        }
    }

    private fun onDelete(id: Long) {
        val index = cursor.getPosition(id)
        query({
            if (index != -1)
                notifyItemRemoved(index)
        })
    }

    private fun onInsert(id: Long) {

        val preIndex = cursor.getPosition(id)
        query({
            if (preIndex == -1) {
                val index = cursor.getPosition(id)
                notifyItemInserted(index)
            }
        })
    }

    class TaskViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        var viewTitle: TextView = v.findViewById(R.id.viewTitle) as TextView
        var viewNotes: TextView = v.findViewById(R.id.viewNotes) as TextView
        var viewDue: TextView = v.findViewById(R.id.viewDue) as TextView
        var id: Int = 0
    }

    internal var cursor: Cursor = DefaultCursor()
}
