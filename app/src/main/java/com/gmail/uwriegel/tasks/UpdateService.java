package com.gmail.uwriegel.tasks;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.tasks.model.Task;
import com.google.api.services.tasks.model.Tasks;

import java.io.IOException;
import java.util.List;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * helper methods.
 */
public class UpdateService extends IntentService {
    public UpdateService() {
        super("UpdateService");
    }

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startUpdate(Context context, String accountName, String selectedTasklist) {
        Intent intent = new Intent(context, UpdateService.class);
        intent.setAction(ACTION_UPDATE);
        intent.putExtra(EXTRA_ACCOUNT_NAME, accountName);
        intent.putExtra(EXTRA_SELECTED_TASKLIST, selectedTasklist);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            switch (action)
            {
                case ACTION_UPDATE:
                    String accountName = intent.getStringExtra(EXTRA_ACCOUNT_NAME);
                    String selectedTasklist = intent.getStringExtra(EXTRA_SELECTED_TASKLIST);
                    handleActionUpdate(accountName, selectedTasklist);
                    break;
                default:
                    break;
            }
        }
    }

    private void handleActionUpdate(String accountName, String selectedTasklist) {
        try {
            TasksCredential tasksCredential = new TasksCredential(this, accountName);
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            com.google.api.services.tasks.Tasks service = new com.google.api.services.tasks.Tasks.Builder(transport, jsonFactory, tasksCredential.getCredential())
                    .setApplicationName("Aufgaben")
                    .build();
            Tasks result = service.tasks().list(selectedTasklist)
                    .setFields("items(id,title,notes,due,updated),nextPageToken")
                    //.setFields("items(id,title,notes,due,updated,status)")
//                    .setShowCompleted(true)
                    //.setPageToken()

                    .setShowCompleted(false)
                    //.setUpdatedMin("2017-05-01T00:00:00.000Z")
                    .execute();
                    //.setShowCompleted(false).setUpdatedMin("2017-02-01T00:00:00.000Z").execute();
            List<Task> tasks = result.getItems();
            for (Task task : tasks)
                insertTask(selectedTasklist, task);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void insertTask(String taskTableId, Task task) {
        ContentResolver cr = getContentResolver();

        // Construct a where clause to make sure we don’t already have this
        // earthquake in the provider.
        String where = TasksContentProvider.KEY_GOOGLE_ID + " = '" + task.getId() + "'";
        // If the earthquake is new, insert it into the provider.
        Cursor query = cr.query(TasksContentProvider.CONTENT_URI, null, where, null, null);
        if (query.getCount() == 0) {
            ContentValues values = new ContentValues();
            values.put(TasksContentProvider.KEY_TASK_TABLE_ID, taskTableId);
            values.put(TasksContentProvider.KEY_GOOGLE_ID, task.getId());
            values.put(TasksContentProvider.KEY_TITLE, task.getTitle());
            values.put(TasksContentProvider.KEY_Notes, task.getNotes());
            values.put(TasksContentProvider.KEY_DUE, task.getDue().getValue());
            values.put(TasksContentProvider.KEY_UPDATED, task.getUpdated().getValue());
            cr.insert(TasksContentProvider.CONTENT_URI, values);
        }
        query.close();
    }

    private static final String ACTION_UPDATE = "com.gmail.uwriegel.tasks.action.update";
    private static final String EXTRA_ACCOUNT_NAME = "com.gmail.uwriegel.tasks.extra.ACCOUNT_NAME";
    private static final String EXTRA_SELECTED_TASKLIST = "com.gmail.uwriegel.tasks.extra.SELECTED_TASKLIST";
}
