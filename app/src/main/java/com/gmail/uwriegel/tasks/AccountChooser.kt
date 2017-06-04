package com.gmail.uwriegel.tasks

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.support.v4.app.FragmentActivity
import android.util.Log
import com.gmail.uwriegel.tasks.MainActivity.Companion.TAG
import com.gmail.uwriegel.tasks.json.GoogleAccount
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.GoogleApiClient
import com.google.gson.Gson
import pub.devrel.easypermissions.EasyPermissions

/**
 * Created by urieg on 21.04.2017.
 */
class AccountChooser {

    constructor(mainActivity: FragmentActivity) {
        this.mainActivity = mainActivity
        if (!isGooglePlayServicesAvailable)
            acquireGooglePlayServices()
        else if (EasyPermissions.hasPermissions(mainActivity, Manifest.permission.GET_ACCOUNTS)) {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .build()
            // Build a GoogleApiClient with access to the Google Sign-In API and the options specified by gso.
            googleApiClient = GoogleApiClient.Builder(mainActivity)
                    .enableAutoManage(mainActivity) { Log.w(TAG, "Could not choose account: connection failed") }
                    .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                    .build()

            val signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient)
            mainActivity.startActivityForResult(signInIntent, MainActivity.REQUEST_ACCOUNT_PICKER)
        } else
        // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(mainActivity, mainActivity.getString(R.string.google_account_access_needed),
                    MainActivity.REQUEST_PERMISSION_GET_ACCOUNTS, Manifest.permission.GET_ACCOUNTS)
    }

    fun onAccountPicked(context: Context, accountPicked: Boolean, data: Intent): Boolean {
        var isSuccess = false
        if (accountPicked) {
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            if (result.isSuccess) {
                // Signed in successfully, show authenticated UI.
                val googleSignInAccount = result.signInAccount
                if (googleSignInAccount != null) {
                    val googleAccount = GoogleAccount(googleSignInAccount.account!!.name,
                            googleSignInAccount.displayName!!, googleSignInAccount.photoUrl?.toString() ?: "")

                    Settings.instance.setPickedAccount(context, googleAccount)
                    isSuccess = true
                }
            }
        }

        Auth.GoogleSignInApi.signOut(googleApiClient)
        googleApiClient?.stopAutoManage(mainActivity)
        googleApiClient = null

        return isSuccess
    }

    /**
     * Display an error dialog showing that Google Play Services is missing
     * or out of date.

     * @param connectionStatusCode code describing the presence (or lack of)
     * *                             Google Play Services on this device.
     */
    fun showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode: Int) {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val dialog = apiAvailability.getErrorDialog(mainActivity, connectionStatusCode, MainActivity.REQUEST_GOOGLE_PLAY_SERVICES)
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
            val connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(mainActivity)
            return connectionStatusCode == ConnectionResult.SUCCESS
        }

    /**
     * Attempt to resolve a missing, out-of-date, invalid or disabled Google
     * Play Services installation via a user dialog, if possible.
     */
    private fun acquireGooglePlayServices() {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(mainActivity)
        if (apiAvailability.isUserResolvableError(connectionStatusCode))
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode)
    }

    private val mainActivity: FragmentActivity
    private var googleApiClient: GoogleApiClient? = null
}


