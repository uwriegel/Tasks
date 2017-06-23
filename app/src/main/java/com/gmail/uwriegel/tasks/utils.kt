package com.gmail.uwriegel.tasks

import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by urieg on 23.06.2017.
 */
fun Date.getFormattedString() : String {
    val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
    sdf.timeZone = TimeZone.getTimeZone("UTC")
    return sdf.format(this) + ".000Z"
}