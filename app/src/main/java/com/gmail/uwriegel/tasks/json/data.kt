package com.gmail.uwriegel.tasks.json

/**
 * Created by urieg on 02.06.2017.
 */
data class GoogleAccount(val name: String, val displayName: String, val photoUrl: String) {
    constructor() :this("", "", "")
}