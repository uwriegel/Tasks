package com.gmail.uwriegel.tasks.activities

import android.content.ContentValues
import android.opengl.Visibility
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.View
import android.widget.CalendarView
import android.widget.EditText
import com.gmail.uwriegel.tasks.R
import com.gmail.uwriegel.tasks.db.TasksContentProvider
import com.gmail.uwriegel.tasks.db.TasksTable
import kotlinx.android.synthetic.main.activity_task.*

class TaskActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task)

        editTitle.addTextChangedListener(textWatcher)
        editNotes.addTextChangedListener(textWatcher)
        taskDate.setOnDateChangeListener(object: CalendarView.OnDateChangeListener{
            /**
             * Called upon change of the selected day.
             *
             * @param view The view associated with this listener.
             * @param year The year that was set.
             * @param month The month that was set [0-11].
             * @param dayOfMonth The day of the month that was set.
             */
            override fun onSelectedDayChange(view: CalendarView?, year: Int, month: Int, dayOfMonth: Int) {
                changed = true
                invalidateOptionsMenu()
            }
        })

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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.task, menu)
        menu.getItem(0).isVisible = changed
        return true
    }

    val textWatcher = object: TextWatcher {
        override fun afterTextChanged(p0: Editable?) {
            changed = true
            invalidateOptionsMenu()
        }

        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }
    }

    var changed = false

    companion object {
        internal val ID = "ID"
    }
}
