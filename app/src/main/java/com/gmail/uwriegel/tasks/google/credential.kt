package com.gmail.uwriegel.tasks.google

import android.content.Context
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.tasks.TasksScopes
import java.util.*

/**
 * Created by urieg on 04.06.2017.
 */
fun createCredential(context: Context, accountName: String): GoogleAccountCredential {
    val credential = GoogleAccountCredential.usingOAuth2(context, Arrays.asList(*arrayOf(TasksScopes.TASKS))).setBackOff(ExponentialBackOff())
    credential.selectedAccountName = accountName
    return credential
}
