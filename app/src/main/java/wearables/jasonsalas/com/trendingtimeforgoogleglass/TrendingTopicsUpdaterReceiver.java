package wearables.jasonsalas.com.trendingtimeforgoogleglass;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class TrendingTopicsUpdaterReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent trendingTopicsIntent = new Intent(context, TrendingTopicsUpdaterService.class);
        context.startService(trendingTopicsIntent);
    }
}