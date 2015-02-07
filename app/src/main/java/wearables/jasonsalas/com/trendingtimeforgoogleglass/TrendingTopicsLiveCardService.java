package wearables.jasonsalas.com.trendingtimeforgoogleglass;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

import com.google.android.glass.timeline.LiveCard;


public class TrendingTopicsLiveCardService extends Service {

    private static final String LIVE_CARD_ID = "TrendingTopicsLiveCard";
    private static final String TAG = "TrendingTimeForGlass";
    private static final String DATA_KEY = "freshTrendingTopics";
    private static final String TIMESTAMP_KEY = "updateTimestamp";

    private LiveCard liveCard;
    private RemoteViews remoteViews;
    private PendingIntent pendingIntent;
    SharedPreferences sharedPrefs;

    private Handler handler = new Handler();
    private final UpdateLiveCardRunnable updateLiveCardRunnable = new UpdateLiveCardRunnable();

    private static final long DELAY_IN_MILLISECONDS = 1000 * 60 * 5; // 5 minutes


    @Override
    public void onCreate() {
        super.onCreate();

        Intent alarmIntent = new Intent(TrendingTopicsLiveCardService.this, TrendingTopicsUpdaterReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(TrendingTopicsLiveCardService.this, 0, alarmIntent, 0);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), AlarmManager.INTERVAL_HOUR, pendingIntent);
        Log.i(TAG, "created recurring updates");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "starting LiveCard service...");

        if(liveCard == null) {
            // get an instance of a LiveCard
            liveCard = new LiveCard(this, LIVE_CARD_ID);

            // inflate a layout into the RemoteViews
            remoteViews = new RemoteViews(getPackageName(), R.layout.trending_topics_live_card);

            // setup the initial default value for the trending topics
            remoteViews.setTextViewText(R.id.trendingTopics, "New topics soon!");
            remoteViews.setTextViewText(R.id.footer, getString(R.string.footer_trending));

            // create a pending intent to load the menu when the live card is tapped
            Intent menuIntent = new Intent(this, MenuActivity.class);
            menuIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            liveCard.setAction(PendingIntent.getActivity(this, 0, menuIntent, 0));

            // publish the live card
            liveCard.publish(LiveCard.PublishMode.REVEAL);

            // queue the update runnable
            handler.post(updateLiveCardRunnable);
        } else {
            // send the user directly to the existing live card if the Glassware invoked if while it's already running
            liveCard.navigate();
        }

        // the service doesn't need to be persisted if Glass reboots or the system shuts the service down to conserve resources
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        if(liveCard != null && liveCard.isPublished()) {
            // stop the handler from queuing any additional runnable jobs
            updateLiveCardRunnable.setStop(true);

            liveCard.unpublish();
            liveCard = null;
        }

        super.onDestroy();
    }

    private class UpdateLiveCardRunnable implements Runnable {

        private boolean mIsStopped = false;

        public boolean isStopped() {
            return mIsStopped;
        }

        public void setStop(boolean isStopped) {
            this.mIsStopped = isStopped;
        }

        @Override
        public void run() {
            if(!isStopped()) {
                // update the LiveCard's data
                sharedPrefs = getSharedPreferences("TrendingTimeTopics", Context.MODE_PRIVATE);
                String freshTopics = sharedPrefs.getString(DATA_KEY, "New trending topics soon!");
                String updateTimestamp = sharedPrefs.getString(TIMESTAMP_KEY, "1 hour ago");

                Log.i(TAG, String.format("stored trending topics: %s", freshTopics));
                remoteViews.setTextViewText(R.id.trendingTopics, freshTopics.replace(";", "\r\n"));
                remoteViews.setTextViewText(R.id.timestamp, getString(R.string.footer_timestamp) + updateTimestamp);

                // lock-in the updates
                liveCard.setViews(remoteViews);

                // queue another update according to the interval
                handler.postDelayed(updateLiveCardRunnable, DELAY_IN_MILLISECONDS);
            }
        }
    }
}