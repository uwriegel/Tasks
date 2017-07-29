package com.gmail.uwriegel.tasks.activities

import android.content.ContentValues
import android.content.DialogInterface
import android.opengl.Visibility
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.CalendarView
import android.widget.EditText
import com.gmail.uwriegel.tasks.R
import com.gmail.uwriegel.tasks.db.TasksContentProvider
import com.gmail.uwriegel.tasks.db.TasksTable
import kotlinx.android.synthetic.main.activity_task.*
import java.util.*
import android.app.Activity
import android.content.Intent
import com.gmail.uwriegel.tasks.Settings


class TaskActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task)

        setResult(Activity.RESULT_CANCELED, Intent())

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
        if (id == null) {
            title = getString(R.string.new_task)
            isNew = true
        }
        else {
            isNew = false
            val where = "${TasksTable.KEY_ID} = '${id}'"
            val query = contentResolver.query(TasksContentProvider.CONTENT_URI, arrayOf(
                    TasksTable.KEY_TITLE,
                    TasksTable.KEY_NOTES,
                    TasksTable.KEY_DUE,
                    TasksTable.KEY_HAS_DUE,
                    TasksTable.KEY_UPDATED,
                    TasksTable.KEY_DELETED,
                    TasksTable.KEY_ID), where, null, null)
            if (query?.count == 1) {
                query.moveToFirst()
                editTitle.setText(query.getString(0))
                editNotes.setText(query.getString(1))
                val date = query.getLong(2)
                if (date != 0L)
                    taskDate.date = date
                else
                    taskDate.visibility = View.GONE
                this.id = query.getInt(6)
            }
            query.close()
        }

        changed = false
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.task, menu)
        menu.getItem(0).isVisible = changed
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == R.id.save) {
            saveChanged()
            finish()
            return true;
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * Take care of popping the fragment back stack or finishing the activity
     * as appropriate.
     */
    override fun onBackPressed() {
        if (!changed)
            super.onBackPressed()
        else {
            val builder = AlertDialog.Builder(this)
            builder.setMessage("Änderungen speichern?")
            builder.setPositiveButton("Ok") { _, _ ->
                run {
                    saveChanged()
                    super.onBackPressed()
                }
            }
            builder.setNegativeButton("Abbrechen") { _, _ -> super.onBackPressed() }
            builder.create()
            builder.show()
        }
    }

    private fun saveChanged() {
        var now = Date()
        val values = ContentValues()
        values.put(TasksTable.KEY_TITLE, editTitle.text.toString())
        values.put(TasksTable.KEY_NOTES, editNotes.text.toString())
        values.put(TasksTable.KEY_UPDATED, now.time)
        if (taskDate.visibility == View.VISIBLE)
            // TODO: Unter S3 wird Datum nicht upgedated!!
            values.put(TasksTable.KEY_DUE, taskDate.date)

        if (isNew) {
            var tls = Settings.instance.getTasklists(this)
            val selectedTasklist = tls.first {  it.id == Settings.instance.selectedTasklist }
            values.put(TasksTable.KEY_TASK_TABLE_ID, selectedTasklist.id)
            values.put(TasksTable.KEY_HAS_DUE, if (taskDate.visibility == View.VISIBLE) 1 else 0)
            contentResolver.insert(TasksContentProvider.CONTENT_URI, values)
        }
        else
            contentResolver.update(TasksContentProvider.CONTENT_URI, values, "${TasksTable.KEY_ID} = '${id}'", null)

        val intent = Intent()

        intent.putExtra(MainActivity.TASK_UPDATED_ID, id)
        setResult(Activity.RESULT_OK, intent)
    }

    private val textWatcher = object: TextWatcher {
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
    var id: Int = 0
    var isNew = false

    companion object {
        internal val ID = "ID"
    }
}
