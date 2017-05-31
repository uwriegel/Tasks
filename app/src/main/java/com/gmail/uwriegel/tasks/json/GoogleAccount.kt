package com.gmail.uwriegel.tasks.json

import android.net.Uri

/**
 * Created by urieg on 01.05.2017.
 */

class GoogleAccount(var name: String, var displayName: String, photoUrl: Uri?) {
    var photoUrl: String = ""

    init {
        if (photoUrl != null)
            this.photoUrl = photoUrl.toString()
    }
}
