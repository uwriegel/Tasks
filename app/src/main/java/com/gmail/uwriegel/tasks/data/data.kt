package com.gmail.uwriegel.tasks.data

/**
 * Created by urieg on 08.06.2017.
 */
data class Task(val title: String, val notes: String, val id: Long, val due: Long)

data class CalendarItem(val title: String, val id: String, val due: Long)