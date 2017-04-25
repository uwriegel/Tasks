package com.gmail.uwriegel.tasks;

/**
 * Created by urieg on 24.04.2017.
 *
 * Represents a Google tasklist
 */
class Tasklist {
    String getTitle() {
        return title;
    }
    private String title;

    String getID() {
        return id;
    }
    private String id;

    Tasklist(String id, String title) {
        this.id = id;
        this.title = title;
    }
}
