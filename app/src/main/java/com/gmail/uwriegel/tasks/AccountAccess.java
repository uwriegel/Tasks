package com.gmail.uwriegel.tasks;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.tasks.TasksScopes;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

import pub.devrel.easypermissions.EasyPermissions;

import static com.gmail.uwriegel.tasks.MainActivity.TAG;

/**
 * Created by urieg on 21.04.2017.
 */

public class AccountAccess {

    public interface IOnReady {
        void OnReady();
    }

    public interface IOnAccountChosen {
        void OnAccount(String account, String name);
        void OnPhotoUrl();
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
        else if (forceNewAccount || credential.getSelectedAccountName() == null)
            chooseAccount(onReady);
        else
            onReady.OnReady();
    }

    public void forceNewAccount(IOnAccountChosen onAccountChosen) {
        forceNewAccount = true;
        this.onAccountChosen = onAccountChosen;
    }

    public void onAccountPicked(String accountName, String displayName, Uri photoUrl) {
        Auth.GoogleSignInApi.signOut(googleApiClient);
        googleApiClient.stopAutoManage((FragmentActivity)mainActivity);
        forceNewAccount = false;
        if (accountName != null) {
            SharedPreferences settings = mainActivity.getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString(PREF_ACCOUNT_NAME, accountName);
            editor.putString(PREF_ACCOUNT_DISPLAYNAME, displayName);
            editor.apply();
            if (onAccountChosen != null)
                onAccountChosen.OnAccount(accountName, displayName);
            credential.setSelectedAccountName(accountName);
            downloadAvatar(photoUrl);
        }
        else {
            if (onAccountChosen != null)
                onAccountChosen.OnAccount(null, null);
            onAccountChosen = null;
        }
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

    private void downloadAvatar(Uri photoUri) {
        File file = new File(mainActivity.getFilesDir(), "account.jpg");
        if (file.exists())
            file.delete();

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
                if (onAccountChosen != null)
                    onAccountChosen.OnPhotoUrl();
                onAccountChosen = null;
            }
        }

        if (photoUri != null) {
            final DownloadTask downloadTask = new DownloadTask();
            downloadTask.execute(photoUri.toString());
        }
        else {
            if (onAccountChosen != null)
                onAccountChosen.OnPhotoUrl();
            onAccountChosen = null;
        }
    }

    private void startChoosingAccount() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        // Build a GoogleApiClient with access to the Google Sign-In API and the options specified by gso.
        googleApiClient = new GoogleApiClient.Builder(mainActivity)
                .enableAutoManage((FragmentActivity)mainActivity, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Log.w(TAG, "Could not choose account: connection failed");
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        mainActivity.startActivityForResult(signInIntent, MainActivity.REQUEST_ACCOUNT_PICKER);
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
            if (!forceNewAccount && accountName != null) {
                credential.setSelectedAccountName(accountName);
                initialize(onReady);
            }
            else
                startChoosingAccount();
        } else
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(mainActivity, mainActivity.getString(R.string.google_account_access_needed),
                    MainActivity.REQUEST_PERMISSION_GET_ACCOUNTS, Manifest.permission.GET_ACCOUNTS);
    }

    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String PREF_ACCOUNT_DISPLAYNAME = "accountDisplayName";
    private static final String[] SCOPES = { TasksScopes.TASKS_READONLY };

    private Activity mainActivity;
    private GoogleAccountCredential credential;

    private GoogleApiClient googleApiClient;
    private boolean forceNewAccount;
    private IOnAccountChosen onAccountChosen;
}
