package com.gmail.uwriegel.tasks

import android.database.AbstractCursor

/**
 * Created by urieg on 01.06.2017.
 */
class DefaultCursor : AbstractCursor() {
    override fun getCount(): Int = 0
    override fun getColumnNames() = arrayOf<String>()
    override fun getShort(column: Int): Short = 0
    override fun getFloat(column: Int) = 0f
    override fun getDouble(column: Int): Double = 0.0
    override fun isNull(column: Int) = true
    override fun getInt(column: Int) = 0
    override fun getString(column: Int) = ""
    override fun getLong(column: Int) = 0L
}