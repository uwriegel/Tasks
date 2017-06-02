package com.gmail.uwriegel.tasks

import android.Manifest
import android.app.Activity
import android.content.Context
import android.support.v4.app.FragmentActivity
import android.util.Log
import com.gmail.uwriegel.tasks.MainActivity.Companion.TAG
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.GoogleApiClient
import pub.devrel.easypermissions.EasyPermissions

/**
 * Created by urieg on 21.04.2017.
 */
class AccountChooser private constructor() {

    companion object {
        val instance = AccountChooser()
    }

    fun initialize(context: Context) {
        this.context = context
        if (!isGooglePlayServicesAvailable)
            acquireGooglePlayServices()
        else if (EasyPermissions.hasPermissions(context, Manifest.permission.GET_ACCOUNTS)) {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .build()
            // Build a GoogleApiClient with access to the Google Sign-In API and the options specified by gso.
            googleApiClient = GoogleApiClient.Builder(context)
                    .enableAutoManage(context as FragmentActivity) { Log.w(TAG, "Could not choose account: connection failed") }
                    .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                    .build()

            val signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient)
            (context as Activity).startActivityForResult(signInIntent, MainActivity.REQUEST_ACCOUNT_PICKER)
        } else
        // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(context, context.getString(R.string.google_account_access_needed),
                    MainActivity.REQUEST_PERMISSION_GET_ACCOUNTS, Manifest.permission.GET_ACCOUNTS)
    }

    fun onAccountPicked() {
        Auth.GoogleSignInApi.signOut(googleApiClient)
        googleApiClient?.stopAutoManage((context as FragmentActivity?)!!)
        googleApiClient = null
        context = null
    }

    /**
     * Display an error dialog showing that Google Play Services is missing
     * or out of date.

     * @param connectionStatusCode code describing the presence (or lack of)
     * *                             Google Play Services on this device.
     */
    fun showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode: Int) {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val dialog = apiAvailability.getErrorDialog(context as Activity?, connectionStatusCode, MainActivity.REQUEST_GOOGLE_PLAY_SERVICES)
        dialog.show()
    }

    /**
     * Check that Google Play services APK is installed and up to date.

     * @return true if Google Play Services is available and up to
     * * date on this device; false otherwise.
     */
    private val isGooglePlayServicesAvailable: Boolean
        get() {
            val apiAvailability = GoogleApiAvailability.getInstance()
            val connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(context!!)
            return connectionStatusCode == ConnectionResult.SUCCESS
        }

    /**
     * Attempt to resolve a missing, out-of-date, invalid or disabled Google
     * Play Services installation via a user dialog, if possible.
     */
    private fun acquireGooglePlayServices() {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(context!!)
        if (apiAvailability.isUserResolvableError(connectionStatusCode))
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode)
    }

    private var context: Context? = null
    private var googleApiClient: GoogleApiClient? = null
}
