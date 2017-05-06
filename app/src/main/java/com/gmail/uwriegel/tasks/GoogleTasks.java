package com.gmail.uwriegel.tasks;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.tasks.model.Task;
import com.google.api.services.tasks.model.TaskList;
import com.google.api.services.tasks.model.TaskLists;
import com.google.api.services.tasks.model.Tasks;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by urieg on 21.04.2017.
 * <p>
 * Verbunden mit dem Google API-Projekt "Aufgaben"
 */

class GoogleTasks {
    GoogleTasks(TasksCredential credential) {
        this.credential = credential;
    }

    Tasklist[] getTaskLists() throws IOException {
        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        com.google.api.services.tasks.Tasks service = new com.google.api.services.tasks.Tasks.Builder(transport, jsonFactory, credential.getCredential())
                .setApplicationName("Aufgaben")
                .build();

        TaskLists result = service.tasklists().list()
                .setMaxResults(10L)
                .execute();
        List<TaskList> tasklists = result.getItems();

        List<Tasklist> resultList = new ArrayList<>();
        for (TaskList tasklist : tasklists) {
            resultList.add(new Tasklist(tasklist.getId(), tasklist.getTitle()));
        }
        return resultList.toArray(new Tasklist[0]);
    }

    void getTest(String tasklist) throws IOException {
        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        com.google.api.services.tasks.Tasks service = new com.google.api.services.tasks.Tasks.Builder(transport, jsonFactory, credential.getCredential())
                .setApplicationName("Aufgaben")
                .build();
        Tasks result2 = service.tasks().list(tasklist).setShowCompleted(false).setUpdatedMin("2017-05-01T00:00:00.000Z").execute();
        List<Task> tasks = result2.getItems();
        for (Task task : tasks) {
            String watt = task.getTitle();
            String wott = task.getId();


        }


          Task task = new Task();
//        task.setTitle("New Task");
//        task.setNotes("Please complete me");
//        task.setHidden()
//        task.setDue(new DateTime(System.currentTimeMillis() + 3600000), 0);
//
//        Task result = service.tasks().insert("@default", task).execute();
    }

    private final TasksCredential credential;
}
