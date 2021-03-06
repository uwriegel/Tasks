package com.gmail.uwriegel.tasks

import android.content.Context
import android.content.SharedPreferences
import com.gmail.uwriegel.tasks.google.Tasklist
import com.gmail.uwriegel.tasks.json.GoogleAccount
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.util.*

/**
 * Created by urieg on 06.05.2017.
 */

class Settings private constructor() {

    var googleAccount: GoogleAccount = GoogleAccount()
        private set
    var selectedTasklist: String = ""
        private set

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

    fun setCalendarsList(context: Context, calendarsList: List<String>) {
        val sharedPreferences = getPreferences(context)
        val editor = sharedPreferences.edit()
        val calendarsListString = Gson().toJson(calendarsList)
        editor.putString(PREF_CALENDARS_LIST, calendarsListString)
        editor.apply()
    }

    fun getCalendarsList(context: Context) : List<String> {
        val settings = getPreferences(context).getString(PREF_CALENDARS_LIST, null)
        if (settings != null) {
            val builder = GsonBuilder()
            val gson = builder.create()
            val idType = object : TypeToken<List<String>>() {}.type
            return gson.fromJson<List<String>>(settings, idType)
        } else
            return emptyList()
    }

    fun setLocalUpdateTime(context: Context, updateTime: Date) {
        val sharedPreferences = getPreferences(context)
        val editor = sharedPreferences.edit()
        editor.putLong(PREF_UPDATE_TIME_LOCAL, updateTime.time)
        editor.apply()
    }

    fun getLocalUpdateTime(context: Context) : Date {
        val date = getPreferences(context).getLong(PREF_UPDATE_TIME_LOCAL, 0)
        return Date(date)
    }

    fun setRemoteUpdateTime(context: Context, updateTime: Date) {
        val sharedPreferences = getPreferences(context)
        val editor = sharedPreferences.edit()
        editor.putLong(PREF_UPDATE_TIME_REMOTE, updateTime.time)
        editor.apply()
    }

    fun getRemoteUpdateTime(context: Context) : Date {
        val date = getPreferences(context).getLong(PREF_UPDATE_TIME_REMOTE, 0)
        return Date(date)
    }

    fun getTasklists(context: Context): Iterable<Tasklist> {
        val settings = getPreferences(context).getString(PREF_TASKLISTS, null)
        if (settings != null) {
            val builder = GsonBuilder()
            val gson = builder.create()
            val turnsType = object : TypeToken<List<Tasklist>>() {}.type
            return gson.fromJson<List<Tasklist>>(settings, turnsType)
        } else
            return emptyList()
    }

    fun setTasklists(context: Context, tasklists: Iterable<Tasklist>) {
        val taskListsString = Gson().toJson(tasklists)
        val sharedPreferences = getPreferences(context)
        val editor = sharedPreferences.edit()
        editor.putString(Settings.PREF_TASKLISTS, taskListsString)
        editor.apply()
    }

    fun initialzeGoogleAccountFromPreferences(context: Context) {
        selectedTasklist = getPreferences(context).getString(PREF_SELECTED_TASKLIST, null) ?: ""
        val settings = getPreferences(context).getString(PREF_ACCOUNT, null)
        if (settings != null) {
            val builder = GsonBuilder()
            val gson = builder.create()
            googleAccount = gson.fromJson<GoogleAccount>(settings, GoogleAccount::class.java)
        }
    }

    fun setPickedAccount(context: Context, googleAccount: GoogleAccount) {
        val settings = Gson().toJson(googleAccount)
        val sharedPreferences = getPreferences(context)
        val editor = sharedPreferences.edit()
        selectedTasklist = ""
        editor.putString(PREF_SELECTED_TASKLIST, selectedTasklist)
        editor.putString(PREF_ACCOUNT, settings)
        editor.putBoolean(PREF_AVATAR_DOWNLOADED, false)
        editor.apply()
        initialzeGoogleAccountFromPreferences(context)
    }

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(SETTINGS, Context.MODE_PRIVATE)
    }

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
        private val PREF_CALENDARS_LIST = "calendarsList"
        private val PREF_TASKLISTS = "tasklists"
        private val PREF_UPDATE_TIME_LOCAL = "localUpdateTime"
        private val PREF_UPDATE_TIME_REMOTE = "remoteUpdateTime"
    }
}
