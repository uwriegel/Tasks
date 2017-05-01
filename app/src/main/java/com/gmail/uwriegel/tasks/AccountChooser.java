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
import java.net.URL;
import java.util.Arrays;

import pub.devrel.easypermissions.EasyPermissions;

import static com.gmail.uwriegel.tasks.MainActivity.TAG;

/**
 * Created by urieg on 21.04.2017.
 */
class AccountChooser {

    private AccountChooser() {
    }

    public static AccountChooser getInstance() {
        return instance;
    }

    /**
     * Check that Google Play services APK is installed and up to date.
     *
     * @return true if Google Play Services is available and up to
     * date on this device; false otherwise.
     */
    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        final int connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(context);
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }

    /**
     * Attempt to resolve a missing, out-of-date, invalid or disabled Google
     * Play Services installation via a user dialog, if possible.
     */
    private void acquireGooglePlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        final int connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(context);
        if (apiAvailability.isUserResolvableError(connectionStatusCode))
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
    }

    void initialize(Context context) {
        this.context = context;
        if (!isGooglePlayServicesAvailable())
            acquireGooglePlayServices();
        else if (EasyPermissions.hasPermissions(context, Manifest.permission.GET_ACCOUNTS)) {
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .build();
            // Build a GoogleApiClient with access to the Google Sign-In API and the options specified by gso.
            googleApiClient = new GoogleApiClient.Builder(context)
                    .enableAutoManage((FragmentActivity)context, new GoogleApiClient.OnConnectionFailedListener() {
                        @Override
                        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                            Log.w(TAG, "Could not choose account: connection failed");
                        }
                    })
                    .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                    .build();

            Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
            ((Activity)context).startActivityForResult(signInIntent, MainActivity.REQUEST_ACCOUNT_PICKER);
        } else
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(context, context.getString(R.string.google_account_access_needed),
                    MainActivity.REQUEST_PERMISSION_GET_ACCOUNTS, Manifest.permission.GET_ACCOUNTS);
    }

    void onAccountPicked() {
        Auth.GoogleSignInApi.signOut(googleApiClient);
        googleApiClient.stopAutoManage((FragmentActivity)context);
        googleApiClient = null;
        context = null;
    }

    /**
     * Display an error dialog showing that Google Play Services is missing
     * or out of date.
     *
     * @param connectionStatusCode code describing the presence (or lack of)
     *                             Google Play Services on this device.
     */
    void showGooglePlayServicesAvailabilityErrorDialog(final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                (Activity)context,
                connectionStatusCode,
                MainActivity.REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }
    private static final AccountChooser instance = new AccountChooser();
    private Context context;
    private GoogleApiClient googleApiClient;
}
