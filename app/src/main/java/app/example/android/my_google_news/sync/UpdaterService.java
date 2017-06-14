package app.example.android.my_google_news.sync;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;

import app.example.android.my_google_news.R;
import app.example.android.my_google_news.data.Contract;
import app.example.android.my_google_news.ui.GoogleNewsAppWidget;
import app.example.android.my_google_news.ui.GoogleNewsListActivity;

public class UpdaterService extends IntentService{
    private static final String TAG = "UpdaterService";

    public static final String BROADCAST_ACTION_STATE_CHANGE = "app.example.android.my_google_news.intent.action.STATE_CHANGE";
    public static final String EXTRA_REFRESHING = "app.example.android.my_google_news.intent.extra.REFRESHING";
    //// TODO: 6/6/17 need to remove before submit
    public static final String API_KEY = "ADD_YOUR_OWN_KEY";
    public static final String NEWS_SOURCE1 = "google-news";
    public static final String NEWS_SOURCE2 = "the-next-web";
    public static final String NEWS_URL_TOP     = "https://newsapi.org/v1/articles?source="+NEWS_SOURCE1+"&sortBy=top&apiKey="+API_KEY;
    public static final String NEWS_URL_LATEST  = "https://newsapi.org/v1/articles?source="+NEWS_SOURCE2+"&sortBy=latest&apiKey="+API_KEY;
    public static final String NEWS_BOOKMARKS = "NEWS_BOOKMARKS";
    public static final String PREF_SORT = "PREF_SORT";

    public UpdaterService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (!isInternetConnected(this)) {
            return;
        }
        sendBroadcast(
            new Intent(BROADCAST_ACTION_STATE_CHANGE).putExtra(EXTRA_REFRESHING, true));
        downloadData(getUrl(this));
        sendBroadcast(
            new Intent(BROADCAST_ACTION_STATE_CHANGE).putExtra(EXTRA_REFRESHING, false));
        sendBroadcast(
            new Intent(GoogleNewsAppWidget.UPDATE_WIDGET));
        //new LoadURL().execute(TEST_URL);
    }
    public static boolean isInternetConnected(Context context){
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni == null || !ni.isConnected()) {
            Toast.makeText(context,R.string.no_internet,Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
    public static String getUrl(Context context){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(PREF_SORT,NEWS_URL_TOP);
    }
    public static void savePref(Context context, String Url){
        SharedPreferences.Editor edit = PreferenceManager.getDefaultSharedPreferences(context).edit();
        edit.putString(PREF_SORT,Url);
        edit.commit();
    }
    public void downloadData(String url) {
        Cursor cursor = null;
        try {
            Uri uri = Contract.GoogleNews.URI_GOOGLE_NEWS;
            JSONObject json = readJsonFromUrl(url);
            JSONArray jsonArray = json.optJSONArray("articles");
            //JSONArray cast = jsonObj.getJSONArray("abridged_cast");
            //JSONArray jsonArray = readJsonFromUrl(url);
            if (jsonArray == null) {
                throw new JSONException("Invalid parsed item array" );
            }

            // Delete all items
            ContentResolver contentResolver = getContentResolver();
            contentResolver.delete(uri,Contract.GoogleNews.COLUMN_BOOKMARKS+"=? ",new String[]{""});
            cursor = contentResolver.query(uri,Contract.Query.PROJECTION_GOOGLE_NEWS,null,null,null);

            int count = jsonArray.length();
            for (int i = 0; i < count; i++) {
                ContentValues values = new ContentValues();
                JSONObject object = jsonArray.getJSONObject(i);

                String newUrl = object.getString(Contract.GoogleNews.COLUMN_URL);
                if(!hasBookmarks(cursor,newUrl)){
                    values.put(Contract.GoogleNews.COLUMN_AUTHOR, object.getString(Contract.GoogleNews.COLUMN_AUTHOR));
                    values.put(Contract.GoogleNews.COLUMN_TITLE, object.getString(Contract.GoogleNews.COLUMN_TITLE));
                    values.put(Contract.GoogleNews.COLUMN_DESC, object.getString(Contract.GoogleNews.COLUMN_DESC));
                    values.put(Contract.GoogleNews.COLUMN_URL, newUrl);
                    values.put(Contract.GoogleNews.COLUMN_URL_IMAGE, object.getString(Contract.GoogleNews.COLUMN_URL_IMAGE));
                    values.put(Contract.GoogleNews.COLUMN_PUBLISHED_DATE, object.getString(Contract.GoogleNews.COLUMN_PUBLISHED_DATE));
                    values.put(Contract.GoogleNews.COLUMN_BOOKMARKS,"");
                    contentResolver.insert(uri,values);
                }
            }

            getContentResolver().notifyChange(uri, null);
            showNotification(count);
        } catch (Exception e) {
            e.printStackTrace();
        } finally{
            if(cursor!=null){
                cursor.close();
            }
        }
    }
    private boolean hasBookmarks(Cursor cursor,String url){
        if(cursor.getCount()!=0){
            cursor.moveToFirst();
            do{
                String newUrl = cursor.getString(cursor.getColumnIndex(Contract.GoogleNews.COLUMN_URL));
                if(newUrl.equals(url)){
                    return true;
                }
            }while(cursor.moveToNext());
        }
        return false;
    }
    private void showNotification(int count){
        // Prepare intent which is triggered if the
        // notification is selected
        Intent intent = new Intent(this, GoogleNewsListActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent, 0);

        // Build notification
        // Actions are just fake
        Notification noti = new NotificationCompat.Builder(this)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.sync_updated)+": "+count)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pIntent)
            .setAutoCancel(true)
            .build();

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(0, noti);
    }
    private JSONObject readJsonFromUrl(String url) throws IOException, JSONException{
        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;

        try {
            urlConnection = (HttpURLConnection) new URL(url).openConnection();
            urlConnection.setReadTimeout(5000 /* milliseconds */);
            urlConnection.setConnectTimeout(10000 /* milliseconds */);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                BufferedReader rd = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8")));
                String jsonText = readAll(rd);

                return new JSONObject(jsonText);
                //return new JSONArray(jsonText);
            } else {
                Log.e("readJsonFromUrl", "Error response code: " + urlConnection.getResponseCode());
            }
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return null;
    }
    private String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

}
