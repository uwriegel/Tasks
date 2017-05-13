package com.gmail.uwriegel.tasks;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.gmail.uwriegel.tasks.json.Tasklist;
import com.gmail.uwriegel.tasks.json.Tasklists;

import java.io.File;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

// TODO: Wenn kein due, dann Jahr 3000 verwenden
// TODO: Letzter NavHeader-Menüeintrag: Aktualisieren, nur dann werden die Tasklisten neu geholt
// TODO: In die Nav-Liste Kalender übernehmen

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        EasyPermissions.PermissionCallbacks {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton)findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final SwipeRefreshLayout.OnRefreshListener swipeRefreshListner = new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        Log.i(TAG, "onRefresh called from SwipeRefreshLayout");
                        // This method performs the actual data-refresh operation.
                        // The method calls setRefreshing(false) when it's finished.
                    }
                };

                final SwipeRefreshLayout swipeLayout = (SwipeRefreshLayout)findViewById(R.id.swipe_container);
                swipeLayout.post(new Runnable() {
                    @Override public void run() {
                        swipeLayout.setRefreshing(true);
                        // directly call onRefresh() method
                        swipeRefreshListner.onRefresh();
                    }
                });
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout)findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        Boolean accountSelected = Settings.getInstance().initialzeGoogleAccountFromPreferences(this);
        if (!accountSelected)
            chooseAccount();

        NavigationView navigationView = (NavigationView)findViewById(R.id.nav_view);
        View header = navigationView.getHeaderView(0);
        navigationView.setNavigationItemSelectedListener(this);
        setAccountInNavigationHeader(header);
        initializeNavigationDrawer();
        header.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ImageView myImage = (ImageView)findViewById(R.id.googleAccountSpinner);
                myImage.setImageResource(R.drawable.dropup);
                chooseAccount();
            }
        });

        if (accountSelected && Settings.getInstance().getSelectedTasklist() != null)
            UpdateService.startUpdate(this, Settings.getInstance().getGoogleAccount().name, Settings.getInstance().getSelectedTasklist());
        else
            drawer.openDrawer(navigationView);
    }

    /**
     * Called when an activity launched here (specifically, AccountPicker
     * and authorization) exits, giving you the requestCode you started it with,
     * the resultCode it returned, and any additional data from it.
     *
     * @param requestCode code indicating which activity result is incoming.
     * @param resultCode  code indicating the result of the incoming
     *                    activity result.
     * @param data        Intent (containing result data) returned by incoming
     *                    activity result.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK)
                    Log.w(TAG, "This app requires Google Play Services. Please install Google Play Services on your device and relaunch this app.");
                else
                    chooseAccount();
                break;
            case REQUEST_ACCOUNT_PICKER:
                Settings.getInstance().onRequestAccontPicker(this, resultCode, data, new Settings.ICallback() {
                    @Override
                    public void onTasklistsUpdated() {
                        initializeNavigationDrawer();
                    }
                });
                if (resultCode == RESULT_OK) {
                    NavigationView navigationView = (NavigationView)findViewById(R.id.nav_view);
                    View header =  navigationView.getHeaderView(0);
                    setAccountInNavigationHeader(header);
                }
                final ImageView image = (ImageView)findViewById(R.id.googleAccountSpinner);
                if (image != null)
                    image.setImageResource(R.drawable.dropdown);
                setTitle(getString(R.string.app_name));
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK)
                    ; //initializeGoogleAccount();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout)findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    /**
     * Respond to requests for permissions at runtime for API 23 and above.
     *
     * @param requestCode  The request code passed in
     *                     requestPermissions(android.app.Activity, String, int, String[])
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either PERMISSION_GRANTED or PERMISSION_DENIED. Never null.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    private void setAccountInNavigationHeader(View navigationHeader) {
        if (Settings.getInstance().getGoogleAccount() != null) {
            TextView googleAccountView = (TextView)navigationHeader.findViewById(R.id.textViewGoogleAccount);
            googleAccountView.setText(Settings.getInstance().getGoogleAccount().name);

            TextView googleDisplay = (TextView)navigationHeader.findViewById(R.id.textViewGoogleDisplayName);
            googleDisplay.setText(Settings.getInstance().getGoogleAccount().displayName);

            setPhotoUrl(navigationHeader, false);
        }
    }

    private void clearNavigationDrawer(Menu menu) {
        if (menu == null) {
            NavigationView navigationView = (NavigationView)findViewById(R.id.nav_view);
            menu = navigationView.getMenu();
        }
        menu.clear();
    }

    private void initializeNavigationDrawer() {
        NavigationView navigationView = (NavigationView)findViewById(R.id.nav_view);
        Menu menu = navigationView.getMenu();
        clearNavigationDrawer(menu);

        Tasklists tasklists = Settings.getInstance().getTasklists(this);
        if (tasklists != null) {
            int id = MENU_TASKLISTS_START_ID;
            for (Tasklist tasklist : tasklists.taskLists) {
                MenuItem mi = menu.add(MENU_GROUP_TASKLISTS, id++, 0, tasklist.name);
                mi.setCheckable(true);
                mi.setIcon(R.drawable.ic_list);
                String selectedTasklist = Settings.getInstance().getSelectedTasklist();
                if (selectedTasklist != null && selectedTasklist.compareTo(tasklist.id) == 0) {
                    mi.setChecked(true);
                    setTitle(mi.getTitle());
                } else
                    mi.setChecked(false);
            }
        }
    }

    private void chooseAccount() {
        AccountChooser.getInstance().initialize(this);
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
        chooseAccount();
    }

    private void setPhotoUrl(final View navigationHeader, Boolean internal) {
        final ImageView myImage = (ImageView)navigationHeader.findViewById(R.id.imageView);
        if (defaultPhotoDrawable == null)
            defaultPhotoDrawable = myImage.getDrawable();

        if (Settings.getInstance().getIsAvatarDownloaded(this)) {
            File file = new File(getFilesDir(), AvatarDownloader.FILE);
            if (file.exists()) {
                Bitmap myBitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                myImage.getDrawable();
                myImage.setImageBitmap(myBitmap);
            } else
                myImage.setImageDrawable(defaultPhotoDrawable);
        } else if (!internal)
            AvatarDownloader.start(this, Settings.getInstance().getGoogleAccount().photoUrl, new AvatarDownloader.IOnFinished() {
                @Override
                public void onFinished(Boolean success) {
                    Settings.getInstance().setIsAvatarDownloaded(MainActivity.this, true);
                    if (success)
                        setPhotoUrl(navigationHeader, true);
                    else
                        myImage.setImageDrawable(defaultPhotoDrawable);
                }
            });
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

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id >= MENU_TASKLISTS_START_ID) {
            Tasklists tasklists = Settings.getInstance().getTasklists(this);
            if (tasklists != null) {
                int index = id - MENU_TASKLISTS_START_ID;
                Tasklist taskList = tasklists.taskLists[index];
                String selectedTasklist = taskList.id;
                Settings.getInstance().setSelectedTasklist(this, selectedTasklist);
                setTitle(taskList.name);
            }
        }

        DrawerLayout drawer = (DrawerLayout)findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Callback for when a permission is granted using the EasyPermissions
     * library.
     *
     * @param requestCode The request code associated with the requested
     *                    permission
     * @param list        The requested permission list. Never null.
     */
    @Override
    public void onPermissionsGranted(int requestCode, List<String> list) {
        // Do nothing.
    }

    /**
     * Callback for when a permission is denied using the EasyPermissions
     * library.
     *
     * @param requestCode The request code associated with the requested
     *                    permission
     * @param list        The requested permission list. Never null.
     */
    @Override
    public void onPermissionsDenied(int requestCode, List<String> list) {
        // Do nothing.
    }
    static final String TAG = "Tasks";

    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;
    private static final int REQUEST_AUTHORIZATION = 1001;
    private static final int MENU_GROUP_TASKLISTS = 200;
    private static final int MENU_TASKLISTS_START_ID = 2000;

    private Drawable defaultPhotoDrawable;
}
