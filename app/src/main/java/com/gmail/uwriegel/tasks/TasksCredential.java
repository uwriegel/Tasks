package com.gmail.uwriegel.tasks;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.tasks.TasksScopes;

import java.util.Arrays;

/**
 * Created by urieg on 01.05.2017.
 */
class TasksCredential {
    /**
     * @param context context
     * @param accountName
     */
    TasksCredential(Context context, String accountName) {
        // Initialize credentials and service object.
        credential = GoogleAccountCredential.usingOAuth2(context, Arrays.asList(SCOPES)).setBackOff(new ExponentialBackOff());
        credential.setSelectedAccountName(accountName);
    }

    GoogleAccountCredential getCredential() {
        return credential;
    }

    private final GoogleAccountCredential credential;
    private static final String[] SCOPES = {TasksScopes.TASKS_READONLY};
}
