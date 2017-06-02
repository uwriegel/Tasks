package com.gmail.uwriegel.tasks

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Handler
import com.gmail.uwriegel.tasks.google.Tasklist
import com.gmail.uwriegel.tasks.google.Tasklists
import com.gmail.uwriegel.tasks.json.GoogleAccount
import com.google.android.gms.auth.api.Auth
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.tasks.Tasks
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.util.*

/**
 * Created by urieg on 06.05.2017.
 */

class Settings private constructor() {

    internal interface ICallback {
        fun onTasklistsUpdated()
    }

    fun setSelectedTasklist(context: Context, value: String) {
        selectedTasklist = value
        val sharedPreferences = getPreferences(context)
        val editor = sharedPreferences.edit()
        editor.putString(PREF_SELECTED_TASKLIST, selectedTasklist)
        editor.apply()
    }

    fun getIsAvatarDownloaded(context: Context): Boolean {
        return getPreferences(context).getBoolean(PREF_AVATAR_DOWNLOADED, false)
    }

    fun setIsAvatarDownloaded(context: Context, value: Boolean) {
        val sharedPreferences = getPreferences(context)
        val editor = sharedPreferences.edit()
        editor.putBoolean(PREF_AVATAR_DOWNLOADED, value)
        editor.apply()
    }

    fun getTasklists(context: Context): Tasklists {
        val settings = getPreferences(context).getString(PREF_TASKLISTS, null)
        if (settings != null) {
            val builder = GsonBuilder()
            val gson = builder.create()
            return gson.fromJson<Tasklists>(settings, Tasklists::class.java)
        } else
            return Tasklists(taskLists = arrayOf())
    }

    fun initialzeGoogleAccountFromPreferences(context: Context): Boolean {
        selectedTasklist = getPreferences(context).getString(PREF_SELECTED_TASKLIST, null) ?: ""
        val settings = getPreferences(context).getString(PREF_ACCOUNT, null)
        if (settings != null) {
            val builder = GsonBuilder()
            val gson = builder.create()
            googleAccount = gson.fromJson<GoogleAccount>(settings, GoogleAccount::class.java)
            return true
        } else
            return false
    }

    /**
     * Has to be called from Activity in response to RequestAccontPickerActivity
     * @param resultCode result from AccouuntPicker
     * *
     * @param data IntentData from AccountPicker
     * *
     * @param callback Callback on TaskListUpdate
     */
    internal fun onRequestAccontPicker(context: Context, resultCode: Int, data: Intent?, callback: ICallback) {
        if (resultCode == RESULT_OK && data != null && data.extras != null) {
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            if (result.isSuccess) {
                // Signed in successfully, show authenticated UI.
                val googleSignInAccount = result.signInAccount
                if (googleSignInAccount != null) {
                    val googleAccount = GoogleAccount(googleSignInAccount.account!!.name, googleSignInAccount.displayName!!,
                            googleSignInAccount.photoUrl?.toString() ?: "")

                    val settings = Gson().toJson(googleAccount)
                    val sharedPreferences = getPreferences(context)
                    val editor = sharedPreferences.edit()
                    selectedTasklist = ""
                    editor.putString(PREF_SELECTED_TASKLIST, selectedTasklist)
                    editor.putString(PREF_ACCOUNT, settings)
                    editor.putBoolean(PREF_AVATAR_DOWNLOADED, false)
                    editor.apply()

                    initialzeGoogleAccountFromPreferences(context)
                    updateTaskLists(context, callback)
                }
            }
        }
    }

    private fun updateTaskLists(context: Context, callback: ICallback) {
        val credential = TasksCredential(context, Settings.instance.googleAccount?.name)
        val handler = Handler()
        Thread(Runnable {
            try {
                val transport = AndroidHttp.newCompatibleTransport()
                val jsonFactory = JacksonFactory.getDefaultInstance()
                val service = com.google.api.services.tasks.Tasks.Builder(transport, jsonFactory, credential.credential)
                        .setApplicationName("Aufgaben")
                        .build()

                val result = service.tasklists().list()
                        .setMaxResults(10L)
                        .execute()
                val googleTasklists = result.items
                val tasklists = ArrayList<Tasklist>()
                for (googleTasklist in googleTasklists) {
                    val taskList = Tasklist(googleTasklist.title, googleTasklist.id)
                    tasklists.add(taskList)
                }
                val taskLists = Tasklists(tasklists.toTypedArray())
                val taskListsString = Gson().toJson(taskLists)
                val sharedPreferences = getPreferences(context)
                val editor = sharedPreferences.edit()
                editor.putString(PREF_TASKLISTS, taskListsString)
                editor.apply()

                handler.post {
                    callback.onTasklistsUpdated()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }).start()
    }

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(SETTINGS, Context.MODE_PRIVATE)
    }

    var googleAccount: GoogleAccount? = null
        private set
    var selectedTasklist: String = ""
        private set

    companion object {

        /**
         * Get singleton instance
         * @return
         */
        val instance = Settings()
        private val SETTINGS = "settings"
        private val PREF_ACCOUNT = "googleAccount"
        private val PREF_SELECTED_TASKLIST = "selectedTasklist"
        private val PREF_AVATAR_DOWNLOADED = "avatarDownloaded"
        private val PREF_TASKLISTS = "tasklists"
    }
}
