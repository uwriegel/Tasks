package com.gmail.uwriegel.tasks.activities

import android.app.Activity
import android.content.*
import android.os.Bundle
import android.provider.CalendarContract
import android.support.v4.view.GravityCompat
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import com.gmail.uwriegel.tasks.calendar.getCalendarsList
import com.gmail.uwriegel.tasks.data.CalendarItem
import com.gmail.uwriegel.tasks.data.Task
import com.gmail.uwriegel.tasks.data.queryAllTasks
import com.gmail.uwriegel.tasks.google.Tasklist
import com.gmail.uwriegel.tasks.google.TasklistsUpdater
import com.gmail.uwriegel.tasks.webview.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import android.net.ConnectivityManager
import com.gmail.uwriegel.tasks.*
import com.gmail.uwriegel.tasks.db.TasksContentProvider
import com.gmail.uwriegel.tasks.db.TasksTable
import java.util.*


// TODO: Bei Gelöschte nach 5s hochmelden
// TODO: in DB gelöscht, die niht in Google gefunden werden, in DB löschen
// TODO: Neuen Task anlegen (Activity)
// TODO: Task ändern mit Activity

class MainActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(toolbar)

        val webSettings = contentView.settings
        webSettings.javaScriptEnabled = true
        webSettings.domStorageEnabled = true
        WebView.setWebContentsDebuggingEnabled(true)
        contentView.setWebChromeClient(WebChromeClient())
        contentView.addJavascriptInterface(JavascriptInterface(this, contentView, object: Callbacks {
            override fun deleteTask(id: String, delete: Boolean) {
                val where = "${TasksTable.KEY_ID} = '${id}'"
                val values = ContentValues()
                values.put(TasksTable.KEY_UPDATED, Date().time)
                values.put(TasksTable.KEY_DELETED, if (delete) 1 else 0)
                contentResolver.update(TasksContentProvider.CONTENT_URI, values, where, null)
            }

            override fun showEvent(eventId: String) {
                val id = java.lang.Long.parseLong(eventId)
                val uri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, id)
                val intent = Intent(Intent.ACTION_VIEW).setData(uri)
                startActivity(intent)
            }
        }), "Native")
        contentView.isHapticFeedbackEnabled = true
        contentView.loadUrl("file:///android_asset/index.html")

        fab.setOnClickListener {
                //                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                //                        .setAction("Action", null).show();
        }
        swipeRefresh.isEnabled = false

        val toggle = object : ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            override fun onDrawerOpened(drawerView: View?) {
                super.onDrawerOpened(drawerView)
            }
        }

        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        Settings.instance.initialzeGoogleAccountFromPreferences(this)
        if (Settings.instance.googleAccount.name == "")
            chooseAccount()

        val navigationHeader = navigationView.getHeaderView(0)

        if (Settings.instance.selectedTasklist != "") {
            var tls = Settings.instance.getTasklists(this)
            val selectedTasklist = tls.first {  it.id == Settings.instance.selectedTasklist }
            title = selectedTasklist.name
        }

        navView = navigationHeader.findViewById(R.id.navView) as WebView
        val navViewSettings = navView.settings
        navViewSettings.javaScriptEnabled = true
        navView.setWebChromeClient(WebChromeClient())
        navView.addJavascriptInterface(NavJavascriptInterface(this, navView, object : NavHeaderCallbacks {
            override fun getCalendarsList() {
                this@MainActivity.getCalendarsList()
            }

            override fun onTasklistSelected(tasklist: Tasklist) {
                Settings.instance.setSelectedTasklist(this@MainActivity, tasklist.id)

                queryAllTasks(this@MainActivity, { tasks, calendarItems ->
                    title = tasklist.name
                    contentView.setTasks(tasks, calendarItems)
                    drawerLayout.closeDrawer(GravityCompat.START)
                })

                UpdateService.startUpdate(this@MainActivity, Settings.instance.googleAccount.name, Settings.instance.selectedTasklist)
            }

            override fun chooseAccount() {
                this@MainActivity.chooseAccount()
            }

            override fun onSetItems(tasks: List<Task>, calendarItems: List<CalendarItem>) {
                contentView.setTasks(tasks, calendarItems)
            }
        }), "Native")
        navView.isHapticFeedbackEnabled = true
        navView.loadUrl("file:///android_asset/navheader.html?name=${Settings.instance.googleAccount.name}&displayName=${Settings.instance.googleAccount.displayName}&photo=${Settings.instance.googleAccount.photoUrl}")

        if (Settings.instance.googleAccount.name != "" && Settings.instance.selectedTasklist != "")
            UpdateService.startUpdate(this, Settings.instance.googleAccount.name, Settings.instance.selectedTasklist)
        else
            drawerLayout.openDrawer(navigationView)

        val broadcastReceiver = object : BroadcastReceiver() {
            /**
             * This method is called when the BroadcastReceiver is receiving an Intent
             * broadcast.  During this time you can use the other methods on
             * BroadcastReceiver to view/modify the current result values.  This method
             * is always called within the main thread of its process, unless you
             * explicitly asked for it to be scheduled on a different thread using
             * [android.content.Context.registerReceiver]. When it runs on the main
             * thread you should
             * never perform long-running operations in it (there is a timeout of
             * 10 seconds that the system allows before considering the receiver to
             * be blocked and a candidate to be killed). You cannot launch a popup dialog
             * in your implementation of onReceive().

             *
             * **If this BroadcastReceiver was launched through a &lt;receiver&gt; tag,
             * then the object is no longer alive after returning from this
             * function.**  This means you should not perform any operations that
             * return a result to you asynchronously -- in particular, for interacting
             * with services, you should use
             * [Context.startService] instead of
             * [Context.bindService].  If you wish
             * to interact with a service that is already running, you can use
             * [.peekService].

             *
             * The Intent filters used in [android.content.Context.registerReceiver]
             * and in application manifests are *not* guaranteed to be exclusive. They
             * are hints to the operating system about how to find suitable recipients. It is
             * possible for senders to force delivery to specific recipients, bypassing filter
             * resolution.  For this reason, [onReceive()][.onReceive]
             * implementations should respond only to known actions, ignoring any unexpected
             * Intents that they may receive.

             * @param context The Context in which the receiver is running.
             * *
             * @param intent The Intent being received.
             */
            override fun onReceive(context: Context?, intent: Intent?) {
                val type = intent?.getStringExtra(BROADCAST_TYPE)
                when (type) {
                    BROADCAST_START_UPDATE -> this@MainActivity.showUpdate(true)
                    BROADCAST_UPDATED -> queryAllTasks(this@MainActivity, { tasks, calendarItems ->
                            contentView.setTasks(tasks, calendarItems)
                            this@MainActivity.showUpdate(false)
                    })
                }
            }
        }

        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        filter.addAction(BROADCAST_RECEIVER)
        this.registerReceiver(broadcastReceiver, filter)
    }

    /**
     * Dispatch onPause() to fragments.
     */
    override fun onPause() {
        super.onPause()
    }

    /**
     * Dispatch onResume() to fragments.  Note that for better inter-operation
     * with older versions of the platform, at the point of this call the
     * fragments attached to the activity are *not* resumed.  This means
     * that in some cases the previous state may still be saved, not allowing
     * fragment transactions that modify the state.  To correctly interact
     * with fragments in their proper state, you should instead override
     * [.onResumeFragments].
     */
    override fun onResume() {
        super.onResume()
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
                Log.w(TAG, getString(R.string.google_play_services_required))
            else
                chooseAccount()
            REQUEST_ACCOUNT_PICKER -> {
                val accountPicked = resultCode == Activity.RESULT_OK && data.extras != null
                if (accountChooser!!.onAccountPicked(this, accountPicked, data))
                    TasklistsUpdater(this).update {
                        Settings.instance.setTasklists(this, it)
                        navView.setTasksList(it, Settings.instance.selectedTasklist)
                    }

                accountChooser = null

                if (resultCode == Activity.RESULT_OK)
                    navView.setAccount(Settings.instance.googleAccount)

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

    private fun showUpdate(show: Boolean) {
        val swipeRefreshListner = SwipeRefreshLayout.OnRefreshListener {
            Log.i(TAG, "onRefresh called from SwipeRefreshLayout")
            // This method performs the actual data-refresh operation.
            // The method calls setRefreshing(false) when it's finished.
        }

        swipeRefresh.post {
            swipeRefresh.isRefreshing = show
            // directly call onRefresh() method
            swipeRefreshListner.onRefresh()
        }
    }
    private fun chooseAccount() { accountChooser = AccountChooser(this) }

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
    private lateinit var navView: WebView

    companion object {

        internal val TAG = "Tasks"

        val REQUEST_ACCOUNT_PICKER = 1000
        val REQUEST_GOOGLE_PLAY_SERVICES = 1001
        val MY_PERMISSIONS_REQUEST_READ_CALENDAR = 55
        const val REQUEST_PERMISSION_GET_ACCOUNTS = 1002
        val BROADCAST_RECEIVER = "com.gmail.uwriegel.tasks.BROADCAST_RECEIVER"
        val BROADCAST_TYPE = "type"
        val BROADCAST_START_UPDATE = "start"
        val BROADCAST_UPDATED = "updated"
    }
}
