package com.gmail.uwriegel.tasks.json;

/**
 * Created by urieg on 01.05.2017.
 */

public class GoogleAccount {
    public GoogleAccount(String name, String displayName, String photoUrl) {
        this.name = name;
        this.displayName = displayName;
        this.photoUrl = photoUrl;
    }

    public String name;
    public String displayName;
    public String photoUrl;
}
