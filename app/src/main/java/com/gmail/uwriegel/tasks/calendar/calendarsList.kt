package com.gmail.uwriegel.tasks.calendar

import android.Manifest
import android.content.pm.PackageManager
import android.provider.CalendarContract
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AlertDialog
import com.gmail.uwriegel.tasks.R
import com.gmail.uwriegel.tasks.activities.MainActivity
import com.gmail.uwriegel.tasks.activities.MainActivity.Companion.MY_PERMISSIONS_REQUEST_READ_CALENDAR
import kotlinx.android.synthetic.main.nav_header_main.*
import org.json.JSONArray
import org.json.JSONObject

/**
 * Created by urieg on 11.06.2017.
 */
fun MainActivity.getCalendarsList() {
    val uri = CalendarContract.Calendars.CONTENT_URI
    val projection = arrayOf(
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.ACCOUNT_NAME,
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
            CalendarContract.Calendars.NAME,
            CalendarContract.Calendars.CALENDAR_COLOR)
    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
        // Should we show an explanation?
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CALENDAR)) {
            this.runOnUiThread(Runnable {
                val builder = AlertDialog.Builder(this)
                builder.setMessage(this.getString(R.string.permission_on_calendars_required))
                builder.setPositiveButton("Ok") { _, _ -> }
                builder.create()
                builder.show()
            })
        } else {
            // No explanation needed, we can request the permission.
            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.READ_CALENDAR),
                    MY_PERMISSIONS_REQUEST_READ_CALENDAR)

            // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
            // app-defined int constant. The callback method gets the
            // result of the request.
        }
        //    ActivityCompat#requestPermissions
        // here to request the missing permissions, and then overriding
        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
        //                                          int[] grantResults)
        // to handle the case where the user grants the permission. See the documentation
        // for ActivityCompat#requestPermissions for more details.
        return
    }
    val calendarCursor = this.contentResolver.query(uri, projection, null, null, null)
    val jsonArray = JSONArray()

    val selectedCalendars = com.gmail.uwriegel.tasks.Settings.instance.getCalendarsList(this)

    while (calendarCursor!!.moveToNext()) {
        val json = JSONObject()
        json.put("name", calendarCursor.getString(2))
        json.put("id", calendarCursor.getString(0))
        json.put("isSelected", selectedCalendars.contains(calendarCursor.getString(0)))
        json.put("account", calendarCursor.getString(1))
        jsonArray.put(json)
    }
    val json = jsonArray.toString()
    this.runOnUiThread(Runnable { this.navView.loadUrl("javascript:setCalendarsList('$json')") })
}
