package com.pepsdev.timedlamp;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.net.Uri;
import android.util.Log;

public class TimedLampWidgetProvider extends AppWidgetProvider {

    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {
        final int N = appWidgetIds.length;

        // Perform this loop procedure for each App Widget that belongs to this provider
        for (int i=0; i<N; i++) {
            int appWidgetId = appWidgetIds[i];

            // Create an Intent to launch ExampleActivity
            Intent intent_30 = new Intent(context, TimedLamp.class);
            intent_30.setAction("timestamp" + System.currentTimeMillis());

            intent_30.putExtra(TimedLamp.EXTRA_TIMEOUT, 30 * 1000);
            PendingIntent pendingIntent_30 =
                         PendingIntent.getActivity(context, 0, intent_30,
                                         PendingIntent.FLAG_CANCEL_CURRENT);

            // Get the layout for the App Widget and attach an on-click listener to the button
            RemoteViews views = new RemoteViews(context.getPackageName(),
                                             R.layout.timedlamp_widget_layout);
            views.setOnClickPendingIntent(R.id.button_30, pendingIntent_30);


            Intent intent_60 = new Intent(context, TimedLamp.class);
            intent_60.setAction("timestamp" + System.currentTimeMillis());
            intent_60.putExtra(TimedLamp.EXTRA_TIMEOUT, 60 * 1000);
            PendingIntent pendingIntent_60 = PendingIntent.getActivity(
                    context, 0, intent_60, PendingIntent.FLAG_CANCEL_CURRENT);
            views.setOnClickPendingIntent(R.id.button_60, pendingIntent_60);


            Intent intent_120 = new Intent(context, TimedLamp.class);
            intent_120.setAction("timestamp" + System.currentTimeMillis());
            intent_120.putExtra(TimedLamp.EXTRA_TIMEOUT, 120000);

            PendingIntent pendingIntent_120 = PendingIntent.getActivity(
                    context, 0, intent_120, PendingIntent.FLAG_CANCEL_CURRENT);
            views.setOnClickPendingIntent(R.id.button_120, pendingIntent_120);

            // Tell the AppWidgetManager to perform an update on the current App Widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }
}
