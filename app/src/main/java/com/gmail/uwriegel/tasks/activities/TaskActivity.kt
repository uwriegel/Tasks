package com.gmail.uwriegel.tasks.activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.gmail.uwriegel.tasks.R

class TaskActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task)

        val id = intent.getStringExtra(ID)
        if (id == null)
            title = "Neue Aufgabe"
    }

    companion object {
        internal val ID = "ID"
    }
}
