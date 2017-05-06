package com.gmail.uwriegel.tasks;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.NavigationView;
import android.view.View;
import android.widget.ImageView;

import com.gmail.uwriegel.tasks.json.GoogleAccount;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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

    public Boolean initialzeGoogleAccountFromPreferences(Context context) {
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
     */
    public void onRequestAccontPicker(Context context, int resultCode, Intent data) {
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
                }
            }
        }
        AccountChooser.getInstance().onAccountPicked();
    }

    private SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences(toString(), Context.MODE_PRIVATE);
    }

    private Settings() {
    }

    private static final Settings ourInstance = new Settings();
    private static final String PREF_ACCOUNT = "googleAccount";
    private static final String PREF_SELECTED_TASKLIST = "selectedTasklist";
    private static final String PREF_AVATAR_DOWNLOADED = "avatarDownloaded";
    private GoogleAccount googleAccount;
    private String selectedTasklist;
}
