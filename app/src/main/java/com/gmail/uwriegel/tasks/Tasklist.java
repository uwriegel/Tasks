package com.gmail.uwriegel.tasks;

/**
 * Created by urieg on 24.04.2017.
 */
class Tasklist {
    String getTitle() {
        return title;
    }
    private String title;

    String getID() {
        return id;
    }
    String id;

    Tasklist(String id, String title) {
        this.id = id;
        this.title = title;
    }
}
