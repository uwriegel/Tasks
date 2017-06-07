package com.gmail.uwriegel.tasks.db

import android.database.Cursor

/**
 * Created by urieg on 07.06.2017.
 */
fun Cursor.getPosition(id: Long): Int {
    if (this.count < 1)
        return -1
    this.moveToFirst()
    while (true) {
        if (this.getLong(0) == id)
            break;
        if (!this.moveToNext())
            return -1
    }
    return this.position
}

