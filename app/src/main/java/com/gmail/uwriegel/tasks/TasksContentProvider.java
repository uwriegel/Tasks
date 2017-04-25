package com.gmail.uwriegel.tasks;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by urieg on 25.04.2017.
 *
 * Accesses SQLite database
 */
class TasksContentProvider extends ContentProvider {

    public static final Uri CONTENT_URI = Uri.parse("content://com.gmail.uwriegel.tasks/tasklists");

    // The index (key) column name for use in where clauses.
    public static final String KEY_ID = "_id";
    public static final String KEY_NAME = "name";
    public static final String KEY_GOOGLE_ID = "googleId";

    @Override
    public boolean onCreate() {
        tasksSQLiteOpenHelper = new TasksSQLiteOpenHelper(getContext(),
                TasksSQLiteOpenHelper.DATABASE_NAME, null,
                TasksSQLiteOpenHelper.DATABASE_VERSION);
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        return null;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        switch (uriMatcher.match(uri)) {
            case ALLROWS:
                return "vnd.android.cursor.dir/vnd.paad.todos";
            case SINGLE_ROW:
                return "vnd.android.cursor.item/vnd.paad.todos";
            default: throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        return null;
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

        public TasksSQLiteOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory cursorFactory, int version) {
            super(context,name,cursorFactory,version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
           db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w("TaskDBAdapter", "Upgrading from version" +
                    oldVersion + " to " +
                    newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF IT EXISTS " + DATABASE_TASKLISTS_TABLE);
            onCreate(db);
        }

        private static final String DATABASE_NAME = "tasks.db";
        private static final String DATABASE_TASKLISTS_TABLE = "Tasklists";
        private static final int DATABASE_VERSION = 1;

        private static final String DATABASE_CREATE = "create table " +
                DATABASE_TASKLISTS_TABLE + " (" + KEY_ID +
                "integer primary key autoincrement, " +
                KEY_NAME + " text not null, " +
                KEY_GOOGLE_ID + " text not null);";
    }

    private static final int ALLROWS = 1;
    private static final int SINGLE_ROW = 2;
    private static final UriMatcher uriMatcher;

    // Populate the UriMatcher object, where a URI ending
    // in ‘elements’ will correspond to a request for all
    // items, and ‘elements/[rowID]’ represents a single row.
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI("content://com.gmail.uwriegel.tasks", "tasklists", ALLROWS);
        uriMatcher.addURI("content://com.gmail.uwriegel.tasks", "tasklists/#", SINGLE_ROW);
    }

    private TasksSQLiteOpenHelper tasksSQLiteOpenHelper;
}
