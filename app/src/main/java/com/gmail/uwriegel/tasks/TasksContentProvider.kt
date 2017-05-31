package com.gmail.uwriegel.tasks

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.UriMatcher
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.database.sqlite.SQLiteQueryBuilder
import android.net.Uri
import android.text.TextUtils
import android.util.Log

/**
 * Created by urieg on 25.04.2017.

 * Accesses SQLite database
 */
class TasksContentProvider : ContentProvider() {

    override fun onCreate(): Boolean {
        dbHelper = TasksSQLiteOpenHelper(context)
        return true
    }

    override fun query(uri: Uri, projection: Array<String>?, aselection: String?,
                       selectionArgs: Array<String>?, sortOrder: String?): Cursor? {
        var selection = aselection
        val database = dbHelper?.writableDatabase
        val qb = SQLiteQueryBuilder()
        qb.tables = TasksSQLiteOpenHelper.DATABASE_TASKS_TABLE
        // If this is a row query, limit the result set to the passed in row.
        when (uriMatcher.match(uri)) {

        }//            case QUAKE_ID:
        //                qb.appendWhere(KEY_ID + "=" + uri.getPathSegments().get(1));
        //                break;
        //            case SEARCH: qb.appendWhere(KEY_SUMMARY + " LIKE \"%" + uri.getPathSegments().get(1) + "%\"");
        //                qb.setProjectionMap(SEARCH_PROJECTION_MAP);
        //                break;
        //            default:
        //                break;
        // If no sort order is specified, sort by date / time
        val orderBy: String?
        if (TextUtils.isEmpty(sortOrder))
            orderBy = KEY_DUE
        else
            orderBy = sortOrder

        val taskList = Settings.instance.selectedTasklist
        val tasklistRestriction = "$KEY_TASK_TABLE_ID = '$taskList'"
        if (selection == null)
            selection = tasklistRestriction
        else
            selection += " AND " + tasklistRestriction

        // Apply the query to the underlying database.
        val c = qb.query(database, projection, selection, selectionArgs, null, null, orderBy)
        // Register the contexts ContentResolver to be notified if
        // the cursor result set changes.
        c.setNotificationUri(context!!.contentResolver, uri)
        return c
    }

    override fun getType(uri: Uri): String? {
        when (uriMatcher.match(uri)) {
            ALLROWS -> return "vnd.android.cursor.dir/vnd.uwriegel.tasks"
            SINGLE_ROW -> return "vnd.android.cursor.item/vnd.uwriegel.tasks"
            else -> throw IllegalArgumentException("Unsupported URI: " + uri)
        }
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        val database = dbHelper?.writableDatabase

        // Insert the new row. The call to database.insert will return the row number if it is successful.
        val rowID = database?.insert(TasksSQLiteOpenHelper.DATABASE_TASKS_TABLE, "task", values) !!
        // Return a URI to the newly inserted row on success.
        if (rowID > 0) {
            val resultUri = ContentUris.withAppendedId(CONTENT_URI, rowID)
            context!!.contentResolver.notifyChange(resultUri, null)
            return resultUri
        }

        throw SQLException("Failed to insert row into " + uri)
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        return 0
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int {
        return 0
    }

    private class TasksSQLiteOpenHelper(context: Context) : SQLiteOpenHelper(context, TasksSQLiteOpenHelper.DATABASE_NAME, null, TasksSQLiteOpenHelper.DATABASE_VERSION) {

        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL(DATABASE_CREATE)
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            Log.w(TAG, "Upgrading from version" +
                    oldVersion + " to " +
                    newVersion + ", which will destroy all old data")
            db.execSQL("DROP TABLE IF IT EXISTS " + DATABASE_TASKS_TABLE)
            onCreate(db)
        }

        companion object {

            private val TAG = "TasksContentProvider"

            private val DATABASE_NAME = "tasks.db"
            val DATABASE_TASKS_TABLE = "Tasks"
            private val DATABASE_VERSION = 1

            private val DATABASE_CREATE = "create table " +
                    DATABASE_TASKS_TABLE + " (" + KEY_ID + " integer primary key autoincrement, " +
                    KEY_TASK_TABLE_ID + " text not null, " +
                    KEY_TITLE + " text not null, " +
                    KEY_Notes + " text, " +
                    KEY_GOOGLE_ID + " text, " +
                    KEY_DUE + " integer, " +
                    KEY_UPDATED + " integer);"
        }
    }

    private var dbHelper: TasksSQLiteOpenHelper? = null

    companion object {

        val CONTENT_URI = Uri.parse("content://com.gmail.uwriegel.tasks/tasks")

        // The index (key) column name for use in where clauses.
        val KEY_ID = "_id"
        val KEY_TASK_TABLE_ID = "tasksId"
        val KEY_GOOGLE_ID = "googleId"
        val KEY_TITLE = "title"
        val KEY_Notes = "notes"
        val KEY_DUE = "due"
        val KEY_UPDATED = "updated"

        private val ALLROWS = 1
        private val SINGLE_ROW = 2
        private val uriMatcher: UriMatcher

        // Populate the UriMatcher object, where a URI ending
        // in ‘elements’ will correspond to a request for all
        // items, and ‘elements/[rowID]’ represents a single row.
        init {
            uriMatcher = UriMatcher(UriMatcher.NO_MATCH)
            uriMatcher.addURI("content://com.gmail.uwriegel.tasks", "tasks", ALLROWS)
            uriMatcher.addURI("content://com.gmail.uwriegel.tasks", "tasks/#", SINGLE_ROW)
        }
    }
}
