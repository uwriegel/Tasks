package com.gmail.uwriegel.tasks;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by urieg on 17.05.2017.
 */

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {
    @Override
    public TaskViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.taskview, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TaskViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        protected TextView viewTitle;
        protected TextView viewNotes;
        protected TextView viewDue;

        public TaskViewHolder(View v) {
            super(v);
            viewTitle = (TextView)v.findViewById(R.id.viewTitle);
            viewNotes = (TextView)v.findViewById(R.id.viewNotes);
            viewDue = (TextView)v.findViewById(R.id.viewDue);
        }
    }
}
