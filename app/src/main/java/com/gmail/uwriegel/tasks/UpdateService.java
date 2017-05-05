package com.gmail.uwriegel.tasks;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class UpdateService extends IntentService {
    public UpdateService() {
        super("UpdateService");
    }

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startUpdate(Context context, String param1, String param2) {
        Intent intent = new Intent(context, UpdateService.class);
        intent.setAction(ACTION_UPDATE);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            switch (action)
            {
                case ACTION_UPDATE:
                    String param1 = intent.getStringExtra(EXTRA_PARAM1);
                    String param2 = intent.getStringExtra(EXTRA_PARAM2);
                    handleActionUpdate(param1, param2);
                    break;
                default:
                    break;
            }
//            if (ACTION_FOO.equals(action)) {
//                handleActionFoo(param1, param2);
        }
    }

    private void handleActionUpdate(String param1, String param2) {
        String affe = param1;
        String naff = param2;

        for (int i = 0; i < 200; i++) {
            try {
                URL url = new URL("http://riegel.selfhost.eu/reitbeteiligung/" + ((Integer)i).toString());
                HttpURLConnection httpConnection = (HttpURLConnection)url.openConnection();
                int responseCode = httpConnection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpConnection.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        sb.append(line);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                Thread.sleep(3000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static final String ACTION_UPDATE = "com.gmail.uwriegel.tasks.action.update";
    // TODO: Rename parameters
    private static final String EXTRA_PARAM1 = "com.gmail.uwriegel.tasks.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "com.gmail.uwriegel.tasks.extra.PARAM2";
}
