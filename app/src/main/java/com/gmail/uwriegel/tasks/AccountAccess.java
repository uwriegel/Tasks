package com.gmail.uwriegel.tasks;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.tasks.TasksScopes;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

import pub.devrel.easypermissions.EasyPermissions;

import static android.support.v7.widget.AppCompatDrawableManager.get;

/**
 * Created by urieg on 21.04.2017.
 */

public class AccountAccess {

    public interface IOnReady {
        void OnReady();
    }

    public GoogleAccountCredential getCredential() {
        return credential;
    }

    public String getDisplayName() { return mainActivity.getPreferences(Context.MODE_PRIVATE).getString(PREF_ACCOUNT_DISPLAYNAME, null); }


    public AccountAccess(Activity mainActivity) {
        this.mainActivity = mainActivity;

        // Initialize credentials and service object.
        credential = GoogleAccountCredential.usingOAuth2(mainActivity.getApplicationContext(), Arrays.asList(SCOPES)).setBackOff(new ExponentialBackOff());
    }

    public void initialize(IOnReady onReady) {
        if (!isGooglePlayServicesAvailable())
            acquireGooglePlayServices();
        else if (credential.getSelectedAccountName() == null)
            chooseAccount(onReady);
        else
            onReady.OnReady();
    }

    public void setAccountName(String accountName, String displayName) {
        Auth.GoogleSignInApi.signOut(googleApiClient);
        googleApiClient.stopAutoManage((FragmentActivity)mainActivity);
        SharedPreferences settings = mainActivity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(PREF_ACCOUNT_NAME, accountName);
        editor.putString(PREF_ACCOUNT_DISPLAYNAME, displayName);
        editor.apply();
        credential.setSelectedAccountName(accountName);
    }

    /**
     * Display an error dialog showing that Google Play Services is missing
     * or out of date.
     * @param connectionStatusCode code describing the presence (or lack of)
     *     Google Play Services on this device.
     */
    public void showGooglePlayServicesAvailabilityErrorDialog(final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                mainActivity,
                connectionStatusCode,
                MainActivity.REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    public void downloadAvatar(final IOnReady onReady) {
        String account = mainActivity.getPreferences(Context.MODE_PRIVATE).getString(PREF_ACCOUNT_NAME, null);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .setAccountName(account)
                .build();

        final GoogleApiClient client = new GoogleApiClient.Builder(mainActivity)
            .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
            .build();

        final OptionalPendingResult<GoogleSignInResult> pendingResult = Auth.GoogleSignInApi.silentSignIn(client);

        class DownloadTask extends AsyncTask<String, Integer, Integer> {
            @Override
            protected Integer doInBackground(String... params) {
                try {
                    URL url = new URL(params[0]);
                    HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
                    int responseCode = httpConnection.getResponseCode();

                    FileOutputStream outputStream = mainActivity.openFileOutput("account.jpg", Context.MODE_PRIVATE);
                    InputStream stream = httpConnection.getInputStream();
                    byte[] bytes = new byte[20000];
                    while (true) {
                        int read = stream.read(bytes);
                        if (read == -1)
                            break;
                        outputStream.write(bytes, 0, read);
                    }
                    outputStream.close();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return 0;
            }

            protected void onPostExecute(Integer result) {
                onReady.OnReady();
            }
        }

        final DownloadTask downloadTask = new DownloadTask();
        pendingResult.setResultCallback(new ResultCallback<GoogleSignInResult>() {
            @Override
            public void onResult(@NonNull GoogleSignInResult googleSignInResult) {
                GoogleSignInAccount acct = googleSignInResult.getSignInAccount();
                final Uri uri = acct.getPhotoUrl();
                Auth.GoogleSignInApi.signOut(client);
                if (uri != null)
                    downloadTask.execute(uri.toString());
            }
        });
        client.connect();
    }

    /**
     * Check that Google Play services APK is installed and up to date.
     * @return true if Google Play Services is available and up to
     *     date on this device; false otherwise.
     */
    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        final int connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(mainActivity);
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }

    /**
     * Attempt to resolve a missing, out-of-date, invalid or disabled Google
     * Play Services installation via a user dialog, if possible.
     */
    private void acquireGooglePlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        final int connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(mainActivity);
        if (apiAvailability.isUserResolvableError(connectionStatusCode))
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
    }

    private void chooseAccount(IOnReady onReady) {
        if (EasyPermissions.hasPermissions(mainActivity, Manifest.permission.GET_ACCOUNTS)) {
            String accountName = mainActivity.getPreferences(Context.MODE_PRIVATE).getString(PREF_ACCOUNT_NAME, null);
            if (accountName != null) {
                credential.setSelectedAccountName(accountName);
                initialize(onReady);
            }
            else
                startAccountChooser();
        } else
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(mainActivity, mainActivity.getString(R.string.google_account_access_needed),
                    MainActivity.REQUEST_PERMISSION_GET_ACCOUNTS, Manifest.permission.GET_ACCOUNTS);
    }

    private void startAccountChooser() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        // Build a GoogleApiClient with access to the Google Sign-In API and the options specified by gso.
        googleApiClient = new GoogleApiClient.Builder(mainActivity)
                .enableAutoManage((FragmentActivity)mainActivity, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        int u = 0;
                        int uu = u;
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        mainActivity.startActivityForResult(signInIntent, MainActivity.REQUEST_ACCOUNT_PICKER);
    }

    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String PREF_ACCOUNT_DISPLAYNAME = "accountDisplayName";
    private static final String[] SCOPES = { TasksScopes.TASKS_READONLY };

    private Activity mainActivity;
    private GoogleAccountCredential credential;
    private GoogleApiClient googleApiClient;
}
