package com.gmail.uwriegel.tasks;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

/**
 * Created by urieg on 25.04.2017.
 *
 * Accesses SQLite database
 */
public class TasksContentProvider extends ContentProvider {

    public static final Uri CONTENT_URI = Uri.parse("content://com.gmail.uwriegel.tasks/tasks");

    // The index (key) column name for use in where clauses.
    public static final String KEY_ID = "_id";
    public static final String KEY_TASK_TABLE_ID = "tasksId";
    public static final String KEY_GOOGLE_ID = "googleId";
    public static final String KEY_TITLE = "title";
    public static final String KEY_Notes = "notes";
    public static final String KEY_DUE = "due";
    public static final String KEY_UPDATED = "updated";

    @Override
    public boolean onCreate() {
        dbHelper = new TasksSQLiteOpenHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(TasksSQLiteOpenHelper.DATABASE_TASKS_TABLE);
        // If this is a row query, limit the result set to the passed in row.
        switch (uriMatcher.match(uri)) {
//            case QUAKE_ID:
//                qb.appendWhere(KEY_ID + "=" + uri.getPathSegments().get(1));
//                break;
//            case SEARCH: qb.appendWhere(KEY_SUMMARY + " LIKE \"%" + uri.getPathSegments().get(1) + "%\"");
//                qb.setProjectionMap(SEARCH_PROJECTION_MAP);
//                break;
//            default:
//                break;
        }
        // If no sort order is specified, sort by date / time
        String orderBy;
        if (TextUtils.isEmpty(sortOrder))
            orderBy = KEY_DUE;
        else
            orderBy = sortOrder;

        String taskList = Settings.getInstance().getSelectedTasklist();
        String tasklistRestriction = KEY_TASK_TABLE_ID + " = '" + taskList + "'";
        if (selection == null)
            selection = tasklistRestriction;
        else
            selection += " AND " + tasklistRestriction;

        // Apply the query to the underlying database.
        Cursor c = qb.query(database, projection, selection, selectionArgs, null, null, orderBy);
        // Register the contexts ContentResolver to be notified if
        // the cursor result set changes.
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        switch (uriMatcher.match(uri)) {
            case ALLROWS:
                return "vnd.android.cursor.dir/vnd.uwriegel.tasks";
            case SINGLE_ROW:
                return "vnd.android.cursor.item/vnd.uwriegel.tasks";
            default: throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();

        // Insert the new row. The call to database.insert will return the row number if it is successful.
        long rowID = database.insert(TasksSQLiteOpenHelper.DATABASE_TASKS_TABLE, "task", values);
        // Return a URI to the newly inserted row on success.
        if (rowID > 0) {
            Uri resultUri = ContentUris.withAppendedId(CONTENT_URI, rowID);
            getContext().getContentResolver().notifyChange(resultUri, null);
            return resultUri;
        }

        throw new SQLException("Failed to insert row into " + uri);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    private static class TasksSQLiteOpenHelper extends SQLiteOpenHelper {

        public TasksSQLiteOpenHelper(Context context) {
            super(context,TasksSQLiteOpenHelper.DATABASE_NAME, null, TasksSQLiteOpenHelper.DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
           db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading from version" +
                    oldVersion + " to " +
                    newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF IT EXISTS " + DATABASE_TASKS_TABLE);
            onCreate(db);
        }

        private static final String TAG = "TasksContentProvider";

        private static final String DATABASE_NAME = "tasks.db";
        private static final String DATABASE_TASKS_TABLE = "Tasks";
        private static final int DATABASE_VERSION = 1;

        private static final String DATABASE_CREATE = "create table " +
                DATABASE_TASKS_TABLE + " (" + KEY_ID + " integer primary key autoincrement, " +
                KEY_TASK_TABLE_ID + " text not null, " +
                KEY_TITLE + " text not null, " +
                KEY_Notes + " text, " +
                KEY_GOOGLE_ID + " text, " +
                KEY_DUE + " integer, " +
                KEY_UPDATED + " integer);";
    }

    private static final int ALLROWS = 1;
    private static final int SINGLE_ROW = 2;
    private static final UriMatcher uriMatcher;

    // Populate the UriMatcher object, where a URI ending
    // in ‘elements’ will correspond to a request for all
    // items, and ‘elements/[rowID]’ represents a single row.
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI("content://com.gmail.uwriegel.tasks", "tasks", ALLROWS);
        uriMatcher.addURI("content://com.gmail.uwriegel.tasks", "tasks/#", SINGLE_ROW);
    }

    TasksSQLiteOpenHelper dbHelper;
}
