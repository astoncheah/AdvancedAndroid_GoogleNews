package app.example.android.my_google_news.sync;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import app.example.android.my_google_news.R;
import app.example.android.my_google_news.data.Contract;

/**
 * Created by cheah on 1/6/17.
 */
public class WidgetRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory{
    private Context mContext;
    private Cursor mCursor;

    public WidgetRemoteViewsFactory(Context context, Intent intent) {
        mContext = context;
    }
    @Override
    public void onCreate(){}
    @Override
    public void onDataSetChanged(){
        if (mCursor != null) {
            mCursor.close();
        }
        try{
            final long identityToken = Binder.clearCallingIdentity();
            mCursor = getCursor();
            Log.e("WidgetRemoteViewsFact","getCount(): "+getCount());
            Binder.restoreCallingIdentity(identityToken);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    private Cursor getCursor(){
        Uri uri = Contract.GoogleNews.URI_GOOGLE_NEWS;
        Cursor c = mContext.getContentResolver().query(
            uri,
            Contract.Query.PROJECTION_GOOGLE_NEWS,
            null,
            null,
            null);
        return c;
    }
    @Override
    public void onDestroy(){
        if (mCursor != null) {
            mCursor.close();
        }
    }
    @Override
    public int getCount(){
        return mCursor == null ? 0 : mCursor.getCount();
    }
    @Override
    public RemoteViews getViewAt(int position){
        if (position == AdapterView.INVALID_POSITION ||
            mCursor == null || !mCursor.moveToPosition(position)) {
            return null;
        }

        mCursor.moveToPosition(position);
        String desc =
            mCursor.getString(mCursor.getColumnIndex(Contract.GoogleNews.COLUMN_TITLE));

        RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.collection_widget_list_item);
        rv.setTextViewText(R.id.widgetTextItemList,desc);

        Intent i = new Intent();
        rv.setOnClickFillInIntent(R.id.widgetItemContainer, i);
        return rv;
    }
    @Override
    public RemoteViews getLoadingView(){
        return null;
    }
    @Override
    public int getViewTypeCount(){
        return 1;
    }
    @Override
    public long getItemId(int position){
        return mCursor.moveToPosition(position) ? mCursor.getLong(0) : position;
    }
    @Override
    public boolean hasStableIds(){
        return false;
    }
}
