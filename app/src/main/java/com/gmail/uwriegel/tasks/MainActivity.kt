package com.gmail.uwriegel.tasks

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import java.io.File

// TODO: Nach UpdateService Einträge anzeigen
// TODO: Wenn kein due, dann Jahr 3000 verwenden
// TODO: Letzter NavHeader-Menüeintrag: Aktualisieren, nur dann werden die Tasklisten neu geholt
// TODO: In die Nav-Liste Kalender übernehmen

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, EasyPermissions.PermissionCallbacks {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(toolbar)

        fab.setOnClickListener {
            val swipeRefreshListner = SwipeRefreshLayout.OnRefreshListener {
                Log.i(TAG, "onRefresh called from SwipeRefreshLayout")
                // This method performs the actual data-refresh operation.
                // The method calls setRefreshing(false) when it's finished.
            }

            swipeLayout.post {
                swipeLayout.isRefreshing = true
                // directly call onRefresh() method
                swipeRefreshListner.onRefresh()
            }
            //                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
            //                        .setAction("Action", null).show();
        }

        val toggle = object : ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            override fun onDrawerOpened(drawerView: View?) {
                super.onDrawerOpened(drawerView)
            }
        }
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        val accountSelected = Settings.instance.initialzeGoogleAccountFromPreferences(this)
        if (!accountSelected)
            chooseAccount()

        val navigationHeader = navigationView.getHeaderView(0)
        navigationView.setNavigationItemSelectedListener(this)
        setAccountInNavigationHeader(navigationHeader)
        initializeNavigationDrawer()
        navigationHeader.setOnClickListener {
            val googleAccountSpinner = navigationHeader.findViewById(R.id.googleAccountSpinner) as ImageView
            googleAccountSpinner.setImageResource(R.drawable.dropup)
            chooseAccount()
        }

        if (accountSelected && Settings.instance.selectedTasklist != "")
            UpdateService.startUpdate(this, Settings.instance.googleAccount?.name!!,
                    Settings.instance.selectedTasklist, UpdateSuccessReceiver(this, Handler()))
        else
            drawerLayout.openDrawer(navigationView)

        recyclerView.setHasFixedSize(true)
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        recyclerView.layoutManager = linearLayoutManager

        //val projection = arrayOf(TasksContentProvider.KEY_ID, TasksContentProvider.KEY_TITLE, TasksContentProvider.KEY_Notes, TasksContentProvider.KEY_DUE)
        recyclerView.adapter = TaskAdapter(this)
    }

    /**
     * Called when an activity launched here (specifically, AccountPicker
     * and authorization) exits, giving you the requestCode you started it with,
     * the resultCode it returned, and any additional data from it.

     * @param requestCode code indicating which activity result is incoming.
     * *
     * @param resultCode  code indicating the result of the incoming
     * *                    activity result.
     * *
     * @param data        Intent (containing result data) returned by incoming
     * *                    activity result.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_GOOGLE_PLAY_SERVICES -> if (resultCode != Activity.RESULT_OK)
                Log.w(TAG, "This app requires Google Play Services. Please install Google Play Services on your device and relaunch this app.")
            else
                chooseAccount()
            REQUEST_ACCOUNT_PICKER -> {
                Settings.instance.onRequestAccontPicker(this, resultCode, data, object : Settings.ICallback {
                    override fun onTasklistsUpdated() {
                        initializeNavigationDrawer()
                    }
                })
                accountChooser?.onAccountPicked()
                accountChooser = null

                val navigationHeader = navigationView.getHeaderView(0)
                if (resultCode == Activity.RESULT_OK)
                    setAccountInNavigationHeader(navigationHeader)
                val googleAccountSpinner = navigationHeader.findViewById(R.id.googleAccountSpinner) as ImageView
                googleAccountSpinner.setImageResource(R.drawable.dropdown)
                title = getString(R.string.app_name)
            }
        // REQUEST_AUTHORIZATION ->
        //if (resultCode == Activity.RESULT_OK)
        //initializeGoogleAccount();
        }
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START))
            drawerLayout.closeDrawer(GravityCompat.START)
        else
            super.onBackPressed()
    }

    /**
     * Respond to requests for permissions at runtime for API 23 and above.

     * @param requestCode  The request code passed in
     * *                     requestPermissions(android.app.Activity, String, int, String[])
     * *
     * @param permissions  The requested permissions. Never null.
     * *
     * @param grantResults The grant results for the corresponding permissions
     * *                     which is either PERMISSION_GRANTED or PERMISSION_DENIED. Never null.
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    fun notifyDataSetChanged() = recyclerView.adapter.notifyDataSetChanged()

    private fun setAccountInNavigationHeader(navigationHeader: View) {
        if (Settings.instance.googleAccount != null) {
            val textViewGoogleAccount = navigationHeader.findViewById(R.id.textViewGoogleAccount) as TextView
            textViewGoogleAccount.text = Settings.instance.googleAccount?.name
            val textViewGoogleDisplayName = navigationHeader.findViewById(R.id.textViewGoogleDisplayName) as TextView
            textViewGoogleDisplayName.text = Settings.instance.googleAccount?.displayName
            setPhotoUrl(navigationHeader, false)
        }
    }

    private fun clearNavigationDrawer(menu: Menu?) {
        var menuToClear = menu
        if (menuToClear == null)
            menuToClear = navigationView.menu
        menuToClear?.clear()
    }

    private fun initializeNavigationDrawer() {
        val menu = navigationView.menu
        clearNavigationDrawer(menu)

        val tasklists = Settings.instance.getTasklists(this)
        if (tasklists.taskLists.size > 0) {
            var menuId = MENU_TASKLISTS_START_ID
            tasklists.taskLists.forEach { (name, id) ->
                val mi = menu.add(MENU_GROUP_TASKLISTS, menuId++, 0, name)
                mi.isCheckable = true
                mi.setIcon(R.drawable.ic_list)
                val selectedTasklist = Settings.instance.selectedTasklist
                if (selectedTasklist != "" && selectedTasklist.compareTo(id) == 0) {
                    mi.isChecked = true
                    title = mi.title
                } else
                    mi.isChecked = false
            }
        }
    }

    private fun chooseAccount() { accountChooser = AccountChooser(this)}

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
    private fun AfterPermissionGranted() = chooseAccount()

    private fun setPhotoUrl(navigationHeader: View, internal: Boolean) {
        val noAccount = defaultPhotoDrawable // lazy!!
        val imageView = navigationHeader.findViewById(R.id.imageView) as ImageView
        if (Settings.instance.getIsAvatarDownloaded(this)) {
            val file = File(filesDir, ACCOUNT_IMAGE_FILE)
            if (file.exists()) {
                val myBitmap = BitmapFactory.decodeFile(file.absolutePath)
                imageView.drawable
                imageView.setImageBitmap(myBitmap)
            } else
                imageView.setImageDrawable(noAccount)
        } else if (!internal)
            AvatarDownloader.start(this, Settings.instance.googleAccount!!.photoUrl, object : AvatarDownloader.IOnFinished {
                override fun onFinished(success: Boolean) {
                    Settings.instance.setIsAvatarDownloaded(this@MainActivity, true)
                    if (success)
                        setPhotoUrl(navigationHeader, true)
                    else
                        imageView.setImageDrawable(noAccount)
                }
            })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        if (item.itemId == R.id.action_settings)
            return true

        return super.onOptionsItemSelected(item)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        if (item.itemId >= MENU_TASKLISTS_START_ID) {
            val tasklists = Settings.instance.getTasklists(this)
            if (tasklists.taskLists.size >  0) {
                val index = item.itemId - MENU_TASKLISTS_START_ID
                val taskList = tasklists.taskLists[index]
                val selectedTasklist = taskList.id
                Settings.instance.setSelectedTasklist(this, selectedTasklist)
                title = taskList.name
                UpdateService.startUpdate(this, Settings.instance.googleAccount!!.name,
                        Settings.instance.selectedTasklist, UpdateSuccessReceiver(this, Handler()))
                recyclerView.adapter = TaskAdapter(this)
            }
        }

        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    /**
     * Callback for when a permission is granted using the EasyPermissions
     * library.

     * @param requestCode The request code associated with the requested
     * *                    permission
     * *
     * @param list        The requested permission list. Never null.
     */
    override fun onPermissionsGranted(requestCode: Int, list: List<String>) {
        // Do nothing.
    }

    /**
     * Callback for when a permission is denied using the EasyPermissions
     * library.

     * @param requestCode The request code associated with the requested
     * *                    permission
     * *
     * @param list        The requested permission list. Never null.
     */
    override fun onPermissionsDenied(requestCode: Int, list: List<String>) {
        // Do nothing.
    }

    private var accountChooser: AccountChooser? = null
    private val defaultPhotoDrawable: Drawable by lazy {
        val navigationHeader = navigationView.getHeaderView(0)
        (navigationHeader.findViewById(R.id.imageView) as ImageView).drawable
    }

    companion object {

        internal val TAG = "Tasks"

        internal val REQUEST_ACCOUNT_PICKER = 1000
        internal val REQUEST_GOOGLE_PLAY_SERVICES = 1002
        internal const val REQUEST_PERMISSION_GET_ACCOUNTS = 1003
        //private val REQUEST_AUTHORIZATION = 1001
        private val MENU_GROUP_TASKLISTS = 200
        private val MENU_TASKLISTS_START_ID = 2000
        private val ACCOUNT_IMAGE_FILE = "account.jpg"
    }
}
