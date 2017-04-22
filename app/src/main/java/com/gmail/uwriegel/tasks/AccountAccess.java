package com.gmail.uwriegel.tasks;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.tasks.TasksScopes;

import java.util.Arrays;

import pub.devrel.easypermissions.EasyPermissions;

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

    public void setAccountName(String accountName) {
        SharedPreferences settings = mainActivity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(PREF_ACCOUNT_NAME, accountName);
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
    private static final String[] SCOPES = { TasksScopes.TASKS_READONLY };

    private Activity mainActivity;
    private GoogleAccountCredential credential;
    private GoogleApiClient googleApiClient;
}
