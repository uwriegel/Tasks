package com.gmail.uwriegel.tasks.google

/**
 * Created by urieg on 02.06.2017.
 */
data class Tasklists(val taskLists: Array<Tasklist>)

data class Tasklist(val name: String, val id: String)

