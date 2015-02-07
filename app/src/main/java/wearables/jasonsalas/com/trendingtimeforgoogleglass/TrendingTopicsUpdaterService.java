package wearables.jasonsalas.com.trendingtimeforgoogleglass;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;

public class TrendingTopicsUpdaterService extends Service {

    private static final String URL = "http://divine-display-828.appspot.com";  // custom proxy handler hosted on Google App Engine (cache expires every 57 minutes)
    private static final String TAG = "TrendingTimeForGlass";
    private static final String DATA_KEY = "freshTrendingTopics";
    private static final String TIMESTAMP_KEY = "updateTimestamp";

    HttpClient client;
    HttpGet request;
    HttpResponse response;
    String responseBody;
    SharedPreferences sharedPrefs;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.i(TAG, "updating trending topics");
                    client = new DefaultHttpClient();
                    request = new HttpGet(URL);
                    response = client.execute(request);
                    responseBody = EntityUtils.toString(response.getEntity());

                    // save the fresh trending topics data to SharedPreferences
                    String updateTimestamp = DateFormat.getTimeInstance().format(new Date());

                    sharedPrefs = getSharedPreferences("TrendingTimeTopics", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPrefs.edit();
                    editor.remove(DATA_KEY);
                    editor.commit();
                    editor.putString(TIMESTAMP_KEY, updateTimestamp);
                    editor.putString(DATA_KEY, responseBody);
                    editor.commit();
                } catch (ClientProtocolException ex) {
                    Log.i(TAG, ex.getMessage());
                    ex.printStackTrace();
                } catch (IOException ex) {
                    Log.i(TAG, ex.getMessage());
                    ex.printStackTrace();
                }
            }
        }).start();

        // no need to keep the live card running if Glass reboots
        return Service.START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}