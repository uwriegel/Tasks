package com.gmail.uwriegel.tasks;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by urieg on 17.05.2017.
 */

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    public TaskAdapter(final Context context) {
        final String[] projection = new String[]{
                TasksContentProvider.KEY_ID,
                TasksContentProvider.KEY_TITLE,
                TasksContentProvider.KEY_Notes,
                TasksContentProvider.KEY_DUE,
        };

        final Handler handler = new Handler();
        new Thread(new Runnable() {

            @Override
            public void run() {
                ContentResolver cr = context.getContentResolver();
                cursor = cr.query(TasksContentProvider.CONTENT_URI, projection, null, null, null);
                try {
                    TaskAdapter.this.notifyDataSetChanged();
                } catch (IllegalStateException ise) {

                }
            }
        }).start();
    }

    @Override
    public TaskViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.taskview, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TaskViewHolder holder, int position) {
        cursor.moveToPosition(position);
        holder.viewTitle.setText(cursor.getString(1));
        holder.viewNotes.setText(cursor.getString(2));
    }

    @Override
    public int getItemCount() {

        if (cursor == null)
            return 0;
        else
            return cursor.getCount();
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

    Cursor cursor;
}
