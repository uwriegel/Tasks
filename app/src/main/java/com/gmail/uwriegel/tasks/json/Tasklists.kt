package com.gmail.uwriegel.tasks.json

/**
 * Created by urieg on 01.05.2017.
 */

class Tasklists {
    constructor()
    constructor(taskLists: Array<Tasklist>) {
        this.taskLists = taskLists
    }

    var taskLists: Array<Tasklist> = arrayOf()
}
