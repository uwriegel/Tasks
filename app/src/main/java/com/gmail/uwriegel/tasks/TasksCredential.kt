package com.gmail.uwriegel.tasks

import android.content.Context
import android.content.SharedPreferences

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.tasks.TasksScopes

import java.util.Arrays

/**
 * Created by urieg on 01.05.2017.
 */
internal class TasksCredential
/**
 * @param context context
 * *
 * @param accountName
 */
(context: Context, accountName: String?) {
    val credential: GoogleAccountCredential

    init {
        // Initialize credentials and service object.
        credential = GoogleAccountCredential.usingOAuth2(context, Arrays.asList(*SCOPES)).setBackOff(ExponentialBackOff())
        credential.selectedAccountName = accountName
    }

    companion object {
        private val SCOPES = arrayOf(TasksScopes.TASKS_READONLY)
    }
}
