package app.example.android.my_google_news.ui;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.widget.RemoteViews;

import app.example.android.my_google_news.R;
import app.example.android.my_google_news.sync.WidgetRemoteViewsService;

/**
 * Implementation of App Widget functionality.
 */
public class GoogleNewsAppWidget extends AppWidgetProvider{
    public static final String UPDATE_WIDGET = "app.example.android.my_google_news.UPDATE_WIDGET";

    @Override
    public void onReceive(final Context context, Intent intent) {
        super.onReceive(context, intent);
        final String action = intent.getAction();
        if (action.equals(UPDATE_WIDGET)) {
            Log.e("GoogleNewsAppWidget","UPDATE_WIDGET");
            AppWidgetManager mgr = AppWidgetManager.getInstance(context);
            ComponentName cn = new ComponentName(context, GoogleNewsAppWidget.class);
            //onUpdate(context,mgr,mgr.getAppWidgetIds(cn));
            mgr.notifyAppWidgetViewDataChanged(mgr.getAppWidgetIds(cn),R.id.widgetListView);
        }
    }
    @Override
    public void onUpdate(Context context,AppWidgetManager appWidgetManager,int[] appWidgetIds){
        for(int appWidgetId : appWidgetIds){
            updateAppWidget(context,appWidgetManager,appWidgetId);
        }
    }
    private void updateAppWidget(Context context,AppWidgetManager appWidgetManager,int appWidgetId){
        Intent intent = new Intent(context,WidgetRemoteViewsService.class);
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));

        RemoteViews views = new RemoteViews(context.getPackageName(),R.layout.google_news_widget);
        views.setRemoteAdapter(R.id.widgetListView,intent);

        Intent clickIntentTemplate = new Intent(context,GoogleNewsListActivity.class);
        PendingIntent clickPendingIntentTemplate = TaskStackBuilder.create(context)
            .addNextIntentWithParentStack(clickIntentTemplate)
            .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setPendingIntentTemplate(R.id.widgetListView, clickPendingIntentTemplate);

        views.setTextViewText(R.id.widgetTitleLabel,getTitle(context));

        appWidgetManager.updateAppWidget(appWidgetId,views);
        //appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId,R.id.widgetListView);
    }
    private String getTitle(Context context){
        String str = context.getString(R.string.app_name);
        return str;
    }
}

