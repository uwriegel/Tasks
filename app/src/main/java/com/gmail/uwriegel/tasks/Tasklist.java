package com.gmail.uwriegel.tasks;

/**
 * Created by urieg on 24.04.2017.
 *
 * Represents a Google tasklist
 */
class Tasklist {
    Tasklist(String id, String title) {
        this.id = id;
        this.title = title;
    }

    String getTitle() {
        return title;
    }

    String getID() {
        return id;
    }

    private String title;
    private String id;
}
