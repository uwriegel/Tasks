package com.gmail.uwriegel.tasks;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.text.BoringLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.LogRecord;

/**
 * Created by urieg on 01.05.2017.
 */
class AvatarDownloader {
    public final static String FILE = "account.jpg";

    public static void start(final Activity mainActivity, final String urlString, final IOnFinished onFinished) {
        File file = new File(mainActivity.getFilesDir(), FILE);
        if (file.exists()) {
            try {
                file.delete();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        final Handler handler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                Boolean success = false;
                try {
                    URL url = new URL(urlString);
                    HttpURLConnection httpConnection = (HttpURLConnection)url.openConnection();
                    int responseCode = httpConnection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        FileOutputStream outputStream = mainActivity.openFileOutput(FILE, Context.MODE_PRIVATE);
                        InputStream stream = httpConnection.getInputStream();
                        byte[] bytes = new byte[20000];
                        while (true) {
                            int read = stream.read(bytes);
                            if (read == -1)
                                break;
                            outputStream.write(bytes, 0, read);
                        }
                        outputStream.close();
                        success = true;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    final boolean result = success;
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            onFinished.onFinished(result);
                        }
                    });
                }
            }
        }).start();
    }

    interface IOnFinished {
        void onFinished(Boolean success);
    }
}
