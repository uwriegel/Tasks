package com.gmail.uwriegel.tasks;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;

import java.io.File;
import java.io.IOException;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        EasyPermissions.PermissionCallbacks {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        {
            @Override
            public void onDrawerOpened(View drawerView)
            {
                super.onDrawerOpened(drawerView);

                String name = accountAccess.getCredential().getSelectedAccountName();
                if (name != null) {
                    TextView googleAccount = (TextView)findViewById(R.id.textViewGoogleAccount);
                    googleAccount.setText(name);

                    TextView googleDisplay = (TextView)findViewById(R.id.textViewGoogleDisplayName);
                    googleDisplay.setText(accountAccess.getDisplayName());

                    setPhotoUrl();
                }

                final LinearLayout layout = (LinearLayout)findViewById(R.id.id_nav_header);
                layout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final ImageView myImage = (ImageView)findViewById(R.id.googleAccountSpinner);
                        myImage.setImageResource(R.drawable.dropup);
                        accountAccess.forceNewAccount(new AccountAccess.IOnAccountChosen() {
                            @Override
                            public void OnAccount(String account, String name) {
                                if (account != null) {
                                    TextView googleAccount = (TextView) findViewById(R.id.textViewGoogleAccount);
                                    googleAccount.setText(account);

                                    TextView googleDisplay = (TextView) findViewById(R.id.textViewGoogleDisplayName);
                                    googleDisplay.setText(name);
                                }

                                myImage.setImageResource(R.drawable.dropdown);
                            }

                            @Override
                            public void OnPhotoUrl() {
                                setPhotoUrl();
                            }
                        });
                        InitializeGoogle();
                    }
                });
            }
        };
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        accountAccess = new AccountAccess(this);
        InitializeGoogle();
    }

    /**
     * Respond to requests for permissions at runtime for API 23 and above.
     * @param requestCode The request code passed in
     *     requestPermissions(android.app.Activity, String, int, String[])
     * @param permissions The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *     which is either PERMISSION_GRANTED or PERMISSION_DENIED. Never null.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Called when an activity launched here (specifically, AccountPicker
     * and authorization) exits, giving you the requestCode you started it with,
     * the resultCode it returned, and any additional data from it.
     * @param requestCode code indicating which activity result is incoming.
     * @param resultCode code indicating the result of the incoming
     *     activity result.
     * @param data Intent (containing result data) returned by incoming
     *     activity result.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                    // mOutputText.setText(
                    //                            "This app requires Google Play Services. Please install " +
                    //                "Google Play Services on your device and relaunch this app.");
                } else
                    initializeGoogleAccount();
                break;
            case REQUEST_ACCOUNT_PICKER:
                String accountName = null;
                String accountDisplayName = null;
                Uri photoUrl = null;
                if (resultCode == RESULT_OK && data != null && data.getExtras() != null) {
                    GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
                    if (result.isSuccess()) {
                        // Signed in successfully, show authenticated UI.
                        GoogleSignInAccount acct = result.getSignInAccount();
                        photoUrl = acct.getPhotoUrl();
                        accountName = acct.getAccount().name;
                        accountDisplayName = acct.getDisplayName();
                    }
                }
                accountAccess.onAccountPicked(accountName, accountDisplayName, photoUrl);
                if (accountName != null)
                    initializeGoogleAccount();
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK)
                    initializeGoogleAccount();
                break;
        }
    }

    void initializeGoogleAccount() {
        try {
            final TaskListsTask taskListsTask = new TaskListsTask();
            accountAccess.initialize(new AccountAccess.IOnReady() {
                @Override
                public void OnReady() {
                    googleTasks = new GoogleTasks(accountAccess.getCredential());
                    taskListsTask.execute(0);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Attempts to set the account used with the API credentials. If an account
     * name was previously saved it will use that one; otherwise an account
     * picker dialog will be shown to the user. Note that the setting the
     * account to use with the credentials object requires the app to have the
     * GET_ACCOUNTS permission, which is requested here if it is not already
     * present. The AfterPermissionGranted annotation indicates that this
     * function will be rerun automatically whenever the GET_ACCOUNTS permission
     * is granted.
     */
    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private void AfterPermissionGranted() {
        initializeGoogleAccount();
    }

    /**
     * Callback for when a permission is granted using the EasyPermissions
     * library.
     * @param requestCode The request code associated with the requested
     *         permission
     * @param list The requested permission list. Never null.
     */
    @Override
    public void onPermissionsGranted(int requestCode, List<String> list) {
        // Do nothing.
    }

    /**
     * Callback for when a permission is denied using the EasyPermissions
     * library.
     * @param requestCode The request code associated with the requested
     *         permission
     * @param list The requested permission list. Never null.
     */
    @Override
    public void onPermissionsDenied(int requestCode, List<String> list) {
        // Do nothing.
    }

    private void setPhotoUrl() {
        ImageView myImage = (ImageView)findViewById(R.id.imageView);
        if (defaultPhotoDrawable == null)
            defaultPhotoDrawable = myImage.getDrawable();

        File file = new File(getFilesDir(), "account.jpg");
        if (file.exists()) {
            Bitmap myBitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
            myImage.getDrawable();
            myImage.setImageBitmap(myBitmap);
        }
        else
            myImage.setImageDrawable(defaultPhotoDrawable);
    }

    private void InitializeGoogle() {
        new AccountTask().execute();
    }

    private class AccountTask extends AsyncTask<Integer, Integer, Integer> {
        @Override
        protected Integer doInBackground(Integer... params) {
            initializeGoogleAccount();
            return 0;
        }
    }

    private class TaskListsTask extends AsyncTask<Integer, Integer, List<String>> {
        @Override
        protected List<String> doInBackground(Integer... params) {
            try {
                List<String> result = googleTasks.getTaskLists();
                return result;
            } catch (IOException e) {
                error = e;
                cancel(true);
                return null;
            }
        }

        @Override
        protected void onCancelled() {
            if (error != null) {
                if (error instanceof GooglePlayServicesAvailabilityIOException)
                    accountAccess.showGooglePlayServicesAvailabilityErrorDialog(((GooglePlayServicesAvailabilityIOException)error)
                                    .getConnectionStatusCode());
                else if (error instanceof UserRecoverableAuthIOException)
                    startActivityForResult(((UserRecoverableAuthIOException)error).getIntent(), REQUEST_AUTHORIZATION);
                else
                    error.printStackTrace();
            }
        }

        private IOException error;
    }

    static final String TAG = "Tasks";

    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;

    private AccountAccess accountAccess;
    private GoogleTasks googleTasks;
    private Drawable defaultPhotoDrawable;
}
