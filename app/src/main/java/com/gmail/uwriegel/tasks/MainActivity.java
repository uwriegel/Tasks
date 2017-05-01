package com.gmail.uwriegel.tasks;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gmail.uwriegel.tasks.json.GoogleAccount;
import com.gmail.uwriegel.tasks.json.TaskList;
import com.gmail.uwriegel.tasks.json.TaskLists;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

// TODO: Bei Klick auf Account den ChooseAccount aufrufen
// TODO: Danach die Tasklisten neu bestimmen
// TODO: Letzter NavHeader-Menüeintrag: Aktualisieren, nur dann werden die Tasklisten neu geholt
// TODO: und wenn das Konto gewechselt wird
// TODO: Wenn keine Taskliste vorhanden ist, wird das Konto bestimmt, und anschließend der Nav-Header geöffnet
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
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout)findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                setAccountInNavigationHeader();
                initializeNavigationDrawer();

                final LinearLayout layout = (LinearLayout)findViewById(R.id.id_nav_header);
                layout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final ImageView myImage = (ImageView)findViewById(R.id.googleAccountSpinner);
                        myImage.setImageResource(R.drawable.dropup);
                        //chooseAccount();


//                        accountAccess.forceNewAccount(new AccountAccess.IOnAccountChosen() {
//                            @Override
//                            public void OnAccount(String account, String name) {
//                                if (account != null) {
//                                    TextView googleAccount = (TextView)findViewById(R.id.textViewGoogleAccount);
//                                    googleAccount.setText(account);
//
//                                    TextView googleDisplay = (TextView)findViewById(R.id.textViewGoogleDisplayName);
//                                    googleDisplay.setText(name);
//
//                                    clearNavigationDrawer(null);
//                                }
//
//                                myImage.setImageResource(R.drawable.dropdown);
//                            }
//
//                            @Override
//                            public void OnPhotoUrl() {
//                                setPhotoUrl();
//                            }
//                        });

                    }
                });
            }
        };
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView)findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        if (!initialzeGoogleAccountFromPreferences()) {
            // TODO: nicht bei AccountName == null, seondern, wenn keine Tasklist ausgewählt
            drawer.openDrawer(Gravity.START);
            chooseAccount();
        }

        // TODO: Test
        if (googleAccount != null) {
            final TasksCredential credential = new TasksCredential(MainActivity.this, googleAccount.name);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    GoogleTasks gt = new GoogleTasks(credential);
                    try {
                        Tasklist ts = gt.getTaskLists()[0];
                        String u = ts.getTitle();
                        String id = ts.getID();
                        String nichts = id;
                    } catch (IOException ie) {

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
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
                if (resultCode == RESULT_OK && data != null && data.getExtras() != null) {
                    GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
                    if (result.isSuccess()) {
                        // Signed in successfully, show authenticated UI.
                        GoogleSignInAccount googleSignInAccount = result.getSignInAccount();
                        if (googleSignInAccount != null) {
                            GoogleAccount googleAccount = new GoogleAccount(googleSignInAccount.getAccount().name,
                                    googleSignInAccount.getDisplayName(),
                                    googleSignInAccount.getPhotoUrl().toString());

                            String settings = new Gson().toJson(googleAccount);
                            SharedPreferences sharedPreferences = getPreferences(Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString(PREF_ACCOUNT, settings);
                            editor.putBoolean(PREF_AVATAR_DOWNLOADED, false);
                            editor.apply();

                            initialzeGoogleAccountFromPreferences();
                            setAccountInNavigationHeader();
                            getTaskLists();
                        }
                    }
                }
                AccountChooser.getInstance().onAccountPicked();
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

    private void setAccountInNavigationHeader() {
        if (googleAccount != null) {
            TextView googleAccountView = (TextView)findViewById(R.id.textViewGoogleAccount);
            googleAccountView.setText(googleAccount.name);

            TextView googleDisplay = (TextView)findViewById(R.id.textViewGoogleDisplayName);
            googleDisplay.setText(googleAccount.displayName);

            setPhotoUrl(false);
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

        String settings = getPreferences(Context.MODE_PRIVATE).getString(PREF_TASKLISTS, null);
        if (settings != null) {
            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.create();
            TaskLists tasklists = gson.fromJson(settings, TaskLists.class);

            int id = MENU_TASKLISTS_START_ID;
            for (TaskList tasklist : tasklists.taskLists) {
                MenuItem mi = menu.add(MENU_GROUP_TASKLISTS, id++, 0, tasklist.name);
                mi.setCheckable(true);
                mi.setIcon(R.drawable.ic_list);
//                if (activeTasklist != null && activeTasklist.compareTo(tl.getId()) == 0)
//                {
//                    mi.setChecked(true);
//                    setTitle(mi.getTitle());
//                }
            }
        }

//        if (activeTasklist != null)
//        {
//            Intent intent = new Intent(this, UpdateService.class);
//            intent.putExtra(UpdateService.ACTION, UpdateService.ACTION_TASKLISTS);
//            startService(intent);
//        }
//        else
//        {
//            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
//            drawer.openDrawer(navigationView);
//        }
    }

    private void chooseAccount() {
        AccountChooser.getInstance().initialize(this);
    }

    private void getTaskLists() {
        final TasksCredential credential = new TasksCredential(MainActivity.this, googleAccount.name);
        final Handler handler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                GoogleTasks googleTasks = new GoogleTasks(credential);
                try {
                    Tasklist[] googleTasklists = googleTasks.getTaskLists();
                    ArrayList<TaskList> tasklists = new ArrayList<TaskList>();
                    for (Tasklist googleTasklist : googleTasklists) {
                        TaskList taskList = new TaskList(googleTasklist.getTitle(), googleTasklist.getID());
                        tasklists.add(taskList);
                    }
                    TaskLists taskLists = new TaskLists(tasklists.toArray(new TaskList[0]));

                    String taskListsString = new Gson().toJson(taskLists);
                    SharedPreferences sharedPreferences = getPreferences(Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(PREF_TASKLISTS, taskListsString);
                    editor.apply();

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            initializeNavigationDrawer();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
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

    private void setPhotoUrl(Boolean internal) {
        ImageView myImage = (ImageView)findViewById(R.id.imageView);
        if (defaultPhotoDrawable == null)
            defaultPhotoDrawable = myImage.getDrawable();

        if (getPreferences(Context.MODE_PRIVATE).getBoolean(PREF_AVATAR_DOWNLOADED, false)) {
            File file = new File(getFilesDir(), AvatarDownloader.FILE);
            if (file.exists()) {
                Bitmap myBitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                myImage.getDrawable();
                myImage.setImageBitmap(myBitmap);
            } else
                myImage.setImageDrawable(defaultPhotoDrawable);
        } else if (!internal)
            AvatarDownloader.start(this, googleAccount.photoUrl, new AvatarDownloader.IOnFinished() {
                @Override
                public void onFinished(Boolean success) {
                    SharedPreferences sharedPreferences = getPreferences(Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean(PREF_AVATAR_DOWNLOADED, true);
                    editor.apply();
                    if (success)
                        setPhotoUrl(true);
                }
            });
    }

    private Boolean initialzeGoogleAccountFromPreferences() {
        String settings = getPreferences(Context.MODE_PRIVATE).getString(PREF_ACCOUNT, null);
        if (settings != null) {
            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.create();
            googleAccount = gson.fromJson(settings, GoogleAccount.class);
            return true;
        } else
            return false;
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
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id >= MENU_TASKLISTS_START_ID) {
//                Tasklist[] tasklists = getTasklists();
            int index = id - MENU_TASKLISTS_START_ID;
            //              Tasklist tasklist = tasklists[index];

            JSONObject jsonObject = new JSONObject();
            //            jsonObject.put("name", tasklist.getTitle());
            //          jsonObject.put("id", tasklist.getID());

            SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString(PREF_SELECTED_TASKLIST, jsonObject.toString());
            editor.apply();
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
    private static final String PREF_TASKLISTS = "tasklists";
    private static final String PREF_AVATAR_DOWNLOADED = "avatarDownloaded";
    private static final String PREF_SELECTED_TASKLIST = "selectedTasklist";
    private static final int MENU_GROUP_TASKLISTS = 200;
    private static final int MENU_TASKLISTS_START_ID = 2000;
    private static final String PREF_ACCOUNT = "googleAccount";

    private Drawable defaultPhotoDrawable;
    private GoogleAccount googleAccount;
}
