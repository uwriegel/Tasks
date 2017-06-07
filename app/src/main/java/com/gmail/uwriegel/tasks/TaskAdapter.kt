package com.gmail.uwriegel.tasks

import android.content.ContentUris
import android.content.Context
import android.database.ContentObserver
import android.database.Cursor
import android.net.Uri
import android.opengl.Visibility
import android.os.Handler
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
import kotlinx.android.synthetic.main.content_main.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by urieg on 17.05.2017.
 */

class TaskAdapter(val context: Context) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    init {
        query({notifyDataSetChanged()})
    }

    fun clear() {
        cursor = DefaultCursor()
        notifyDataSetChanged()
    }

    fun onResume() {
        TasksContentProvider.instance.registerOnDelete{ onDelete(it)}
    }

    fun onPause() {
        TasksContentProvider.instance.unregisterOnDelete()
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
        holder.id = cursor.getInt(0)

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
            cursor = context.contentResolver.query(TasksContentProvider.CONTENT_URI, projection, "${TasksTable.KEY_TASK_TABLE_ID} = '$taskList'", null, null)
            uiThread { onFinished() }
        }
    }

    private fun onDelete(id: Long) {
        cursor.moveToFirst()
        while (true) {
            if (cursor.getLong(0) == id)
                break;
            if (!cursor.moveToNext())
                return
        }
        val index = cursor.position
        query({
            notifyItemRemoved(index)
        })

    }

    class TaskViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        var viewTitle: TextView = v.findViewById(R.id.viewTitle) as TextView
        var viewNotes: TextView = v.findViewById(R.id.viewNotes) as TextView
        var viewDue: TextView = v.findViewById(R.id.viewDue) as TextView
        var id: Int = 0
    }

    internal var cursor: Cursor = DefaultCursor()
    private val projection = arrayOf(TasksTable.KEY_ID, TasksTable.KEY_TITLE, TasksTable.KEY_NOTES, TasksTable.KEY_DUE)
}
