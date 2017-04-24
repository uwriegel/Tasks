package com.gmail.uwriegel.tasks;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.tasks.model.TaskList;
import com.google.api.services.tasks.model.TaskLists;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by urieg on 21.04.2017.
 *
 * Verbunden mit dem Google API-Projekt "Aufgaben"
 */

class GoogleTasks {
    GoogleTasks(GoogleAccountCredential credential) {
        this.credential = credential;
    }

    List<String> getTaskLists() throws IOException {
        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        com.google.api.services.tasks.Tasks service = new com.google.api.services.tasks.Tasks.Builder(transport, jsonFactory, credential)
            .setApplicationName("Aufgaben")
            .build();

        List<String> taskListInfo = new ArrayList<String>();
        TaskLists result = service.tasklists().list()
                .setMaxResults(10L)
                .execute();
        List<TaskList> tasklists = result.getItems();
        return taskListInfo;
    }

    private GoogleAccountCredential credential;
}
