package com.gmail.uwriegel.tasks.json;

import android.net.Uri;

/**
 * Created by urieg on 01.05.2017.
 */

public class GoogleAccount {
    public GoogleAccount(String name, String displayName, Uri photoUrl) {
        this.name = name;
        this.displayName = displayName;
        if (photoUrl != null)
            this.photoUrl = photoUrl.toString();
    }

    public String name;
    public String displayName;
    public String photoUrl;
}
