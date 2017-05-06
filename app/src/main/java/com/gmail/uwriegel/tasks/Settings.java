package com.gmail.uwriegel.tasks;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;

import com.gmail.uwriegel.tasks.json.GoogleAccount;
import com.gmail.uwriegel.tasks.json.Tasklist;
import com.gmail.uwriegel.tasks.json.Tasklists;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;

import static android.app.Activity.RESULT_OK;

/**
 * Created by urieg on 06.05.2017.
 */

class Settings {
    /**
     * Get singleton instance
     * @return
     */
    static Settings getInstance() {
        return ourInstance;
    }

    interface ICallback {
        void onTasklistsUpdated();
    }

    String getSelectedTasklist() {
        return selectedTasklist;
    }
    void setSelectedTasklist(Context context, String value) {
        selectedTasklist = value;
        SharedPreferences sharedPreferences = getPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PREF_SELECTED_TASKLIST, selectedTasklist);
        editor.apply();
    }

    Boolean getIsAvatarDownloaded(Context context) {
        return getPreferences(context).getBoolean(PREF_AVATAR_DOWNLOADED, false);
    }
    void  setIsAvatarDownloaded(Context context, Boolean value) {
        SharedPreferences sharedPreferences = getPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(PREF_AVATAR_DOWNLOADED, value);
        editor.apply();
    }

    GoogleAccount getGoogleAccount() {
        return googleAccount;
    }

    Tasklists getTasklists(Context context) {
        String settings = getPreferences(context).getString(PREF_TASKLISTS, null);
        if (settings != null) {
            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.create();
            return gson.fromJson(settings, Tasklists.class);
        }
        else
            return null;
    }

    Boolean initialzeGoogleAccountFromPreferences(Context context) {
        selectedTasklist = getPreferences(context).getString(PREF_SELECTED_TASKLIST, null);
        String settings = getPreferences(context).getString(PREF_ACCOUNT, null);
        if (settings != null) {
            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.create();
            googleAccount = gson.fromJson(settings, GoogleAccount.class);
            return true;
        } else
            return false;
    }

    /**
     * Has to be called from Activity in response to RequestAccontPickerActivity
     * @param resultCode result from AccouuntPicker
     * @param data IntentData from AccountPicker
     * @param callback Callback on TaskListUpdate
     */
    void onRequestAccontPicker(Context context, int resultCode, Intent data, ICallback callback) {
        if (resultCode == RESULT_OK && data != null && data.getExtras() != null) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Signed in successfully, show authenticated UI.
                GoogleSignInAccount googleSignInAccount = result.getSignInAccount();
                if (googleSignInAccount != null) {
                    GoogleAccount googleAccount = new GoogleAccount(googleSignInAccount.getAccount().name,
                            googleSignInAccount.getDisplayName(),
                            googleSignInAccount.getPhotoUrl());

                    String settings = new Gson().toJson(googleAccount);
                    SharedPreferences sharedPreferences = getPreferences(context);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    selectedTasklist = null;
                    editor.putString(PREF_SELECTED_TASKLIST, selectedTasklist);
                    editor.putString(PREF_ACCOUNT, settings);
                    editor.putBoolean(PREF_AVATAR_DOWNLOADED, false);
                    editor.apply();

                    initialzeGoogleAccountFromPreferences(context);
                    updateTaskLists(context, callback);
                }
            }
        }
        AccountChooser.getInstance().onAccountPicked();
    }

    private void updateTaskLists(final Context context, final ICallback callback) {
        final TasksCredential credential = new TasksCredential(context, Settings.getInstance().getGoogleAccount().name);
        final Handler handler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                GoogleTasks googleTasks = new GoogleTasks(credential);
                try {
                    Tasklist[] googleTasklists = googleTasks.getTaskLists();
                    ArrayList<Tasklist> tasklists = new ArrayList<Tasklist>();
                    for (Tasklist googleTasklist : googleTasklists) {
                        Tasklist taskList = new Tasklist(googleTasklist.name, googleTasklist.id);
                        tasklists.add(taskList);
                    }
                    Tasklists taskLists = new Tasklists(tasklists.toArray(new Tasklist[0]));

                    String taskListsString = new Gson().toJson(taskLists);
                    SharedPreferences sharedPreferences = getPreferences(context);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(PREF_TASKLISTS, taskListsString);
                    editor.apply();

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (callback != null)
                                callback.onTasklistsUpdated();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences(SETTINGS, Context.MODE_PRIVATE);
    }

    private Settings() {
    }

    private static final Settings ourInstance = new Settings();
    private static final String SETTINGS = "settings";
    private static final String PREF_ACCOUNT = "googleAccount";
    private static final String PREF_SELECTED_TASKLIST = "selectedTasklist";
    private static final String PREF_AVATAR_DOWNLOADED = "avatarDownloaded";
    private static final String PREF_TASKLISTS = "tasklists";
    private GoogleAccount googleAccount;
    private String selectedTasklist;
}
