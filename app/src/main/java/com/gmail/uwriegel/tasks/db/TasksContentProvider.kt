package com.gmail.uwriegel.tasks.db

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.database.sqlite.SQLiteQueryBuilder
import android.net.Uri
import android.os.Handler
import android.text.TextUtils

/**
 * Created by urieg on 05.06.2017.
 */
class TasksContentProvider: ContentProvider() {

    init {
        instance = this
    }

    override fun onCreate(): Boolean {
        db = TasksSQLiteOpenHelper(context)
        return true
    }

    override fun query(uri: Uri?, projection: Array<out String>?, selection: String?, selectionArgs: Array<out String>?, sortOrder: String?): Cursor {
        val qb = SQLiteQueryBuilder()
        qb.tables = TasksTable.NAME

        val orderBy = if (TextUtils.isEmpty(sortOrder)) TasksTable.KEY_DUE else sortOrder
        val cursor = qb.query(db.readableDatabase, projection, selection, selectionArgs, null, null, orderBy)
        cursor.setNotificationUri(context.contentResolver, uri)
        return cursor
    }

    override fun insert(uri: Uri?, values: ContentValues?): Uri {
        // Insert the new row. The call to database.insert will return the row number if it is successful.
        val rowID = db.writableDatabase.insert(TasksTable.NAME, "task", values)
        // Return a URI to the newly inserted row on success.
        if (rowID > 0) {
            val resultUri = ContentUris.withAppendedId(CONTENT_URI, rowID)
            handler.post({ onInsert?.invoke(rowID) })
            return resultUri
        }

        throw SQLException("Failed to insert row into $uri")
    }

    override fun update(uri: Uri?, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int {
        return 0
    }

    override fun delete(uri: Uri?, selection: String?, selectionArgs: Array<out String>?): Int {
        val result = db.writableDatabase.delete(TasksTable.NAME, selection, selectionArgs)
        if (selectionArgs != null)
            handler.post({ onDelete?.invoke(selectionArgs[0].toLong()) })
        return result
    }

    override fun getType(uri: Uri?): String {
        return ""
    }

    fun registerOnInsert(onInsert: (Long)->Unit) {
        this.onInsert = onInsert
    }

    fun registerOnDelete(onDelete: (Long)->Unit) {
        this.onDelete = onDelete
    }

    fun unregisterOnInsert() {
        this.onInsert = null
    }

    fun unregisterOnDelete() {
        this.onDelete = null
    }

    private class TasksSQLiteOpenHelper(context: Context)
        : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL("CREATE TABLE ${TasksTable.NAME} (${TasksTable.KEY_ID} INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "${TasksTable.KEY_TASK_TABLE_ID} TEXT NOT NULL, " +
                    "${TasksTable.KEY_TITLE} TEXT NOT NULL, " +
                    "${TasksTable.KEY_NOTES} TEXT, " +
                    "${TasksTable.KEY_GOOGLE_ID} TEXT UNIQUE, " +
                    "${TasksTable.KEY_DUE} INTEGER, " +
                    "${TasksTable.KEY_UPDATED} INTEGER);")
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            db.execSQL("DROP TABLE IF IT EXISTS ${TasksTable.NAME}")
            onCreate(db)
        }

        companion object {
            val DB_NAME = "tasks.db"
            val DB_VERSION = 1
        }
    }

    companion object {
        val CONTENT_URI: Uri = Uri.parse("content://com.gmail.uwriegel.tasks/tasks")
        lateinit var instance: TasksContentProvider
    }

    private var onDelete: ((Long)->Unit)? = null
    private var onInsert: ((Long)->Unit)? = null
    private val handler: Handler = Handler()
    private lateinit var db: TasksSQLiteOpenHelper
}