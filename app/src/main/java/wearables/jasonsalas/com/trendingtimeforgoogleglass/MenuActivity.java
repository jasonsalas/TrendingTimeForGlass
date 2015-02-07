package wearables.jasonsalas.com.trendingtimeforgoogleglass;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.glass.widget.CardBuilder;
import com.google.android.glass.widget.CardScrollView;

public class MenuActivity extends Activity {

    protected TrendingTopicsScrollerAdapter adapter;
    protected CardScrollView scrollView;

    private static final String DATA_KEY = "freshTrendingTopics";

    SharedPreferences sharedPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // create an adapter to hold cards
        adapter = new TrendingTopicsScrollerAdapter(this);

        adapter
                .add(new CardBuilder(this, CardBuilder.Layout.TEXT).setText("Send to Phone").getView())
                .add(new CardBuilder(this, CardBuilder.Layout.TEXT).setText("Send to Chrome").getView())
                .add(new CardBuilder(this, CardBuilder.Layout.TEXT).setText("View in Browser").getView());

        // create the view and set its cards
        scrollView = new CardScrollView(this);
        scrollView.setAdapter(adapter);

        // show this view
//        setContentView(scrollView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        scrollView.activate();
    }

    @Override
    protected void onPause() {
        scrollView.deactivate();
        super.onPause();
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        openOptionsMenu();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.trendingtopics, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        sharedPrefs = getSharedPreferences("TrendingTimeTopics", Context.MODE_PRIVATE);
        String[] topics = sharedPrefs.getString(DATA_KEY, "No topics yet").split(";");

        switch(item.getItemId()) {
            case R.id.sendToPhone:
                Toast.makeText(this, "Coming soon...", Toast.LENGTH_LONG).show();
                return true;
            case R.id.sendToChrome:
                Toast.makeText(this, "Coming soon...",Toast.LENGTH_LONG).show();
                return true;
            case R.id.viewInBrowser:
                // launch the Glass browser with a URL format Twitter expects (no hashtags)
                String twitterURL = "https://twitter.com";
                if(topics[0].contains("#")) {
                    twitterURL += "/hashtag/" + topics[0].replace("#", "");
                } else {
                    twitterURL += "/search?q=" + topics[0];
                }

                twitterURL += "?src=tren";

                Intent topicForBrowserIntent = new Intent(Intent.ACTION_VIEW);
                topicForBrowserIntent.setData(Uri.parse(twitterURL));
                startActivity(topicForBrowserIntent);
                return true;
            case R.id.stop:
                // stop the running live card service
                stopService(new Intent(MenuActivity.this, TrendingTopicsLiveCardService.class));

                // remove the scheduled recurring updates
                cancelScheduledUpdate();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onOptionsMenuClosed(Menu menu) {
        finish();
    }

    // TODO: confirm that this actually kills the pending intent created in a separate context as per:
    // http://stackoverflow.com/questions/10055462/android-alarmmanager-cancel-from-another-activity
    // http://stackoverflow.com/questions/4556670/how-to-check-if-alarmmamager-already-has-an-alarm-set
    /*
    * cancel the recurring update set when the live card was created.
    * this code block does this even though the pending intent was created in the context
    * of another component (TrendingTopicsLiveCardService.java)
    * */
    private void cancelScheduledUpdate() {
        Intent alarmIntent = new Intent(MenuActivity.this, TrendingTopicsUpdaterReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(MenuActivity.this, 0, alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
        pendingIntent.cancel();
    }

    private void showTrendingMenuItems() {
        // TODO: finish implementation
    }
}
