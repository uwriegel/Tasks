package com.gmail.uwriegel.tasks.activities

import android.content.ContentValues
import android.opengl.Visibility
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.gmail.uwriegel.tasks.R
import com.gmail.uwriegel.tasks.db.TasksContentProvider
import com.gmail.uwriegel.tasks.db.TasksTable
import kotlinx.android.synthetic.main.activity_task.*

class TaskActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task)

        val id = intent.getStringExtra(ID)
        if (id == null)
            title = getString(R.string.new_task)
        else {
            val where = "${TasksTable.KEY_ID} = '${id}'"
            val query = contentResolver.query(TasksContentProvider.CONTENT_URI, arrayOf(
                    TasksTable.KEY_TITLE,
                    TasksTable.KEY_NOTES,
                    TasksTable.KEY_DUE,
                    TasksTable.KEY_HAS_DUE,
                    TasksTable.KEY_UPDATED,
                    TasksTable.KEY_DELETED), where, null, null)
            if (query?.count == 1) {
                query.moveToFirst()
                editTitle.setText(query.getString(0))
                editNotes.setText(query.getString(1))
                val date = query.getLong(2)
                if (date != 0L)
                    taskDate.date = date
                else
                    taskDate.visibility = View.GONE
            }
            query.close()

        }
    }

    companion object {
        internal val ID = "ID"
    }
}
