package app.example.android.my_google_news.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

class GoogleNewsDbHelper extends SQLiteOpenHelper {
    private static final String NAME = "GoogleNews.db";
    private static final int VERSION = 3;


    GoogleNewsDbHelper(Context context) {
        super(context, NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String builder = "CREATE TABLE " + Contract.GoogleNews.TABLE_NAME + " ("
                + Contract.GoogleNews._ID                       + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + Contract.GoogleNews.COLUMN_AUTHOR             + " TEXT NOT NULL, "
                + Contract.GoogleNews.COLUMN_TITLE              + " TEXT, "
                + Contract.GoogleNews.COLUMN_DESC               + " TEXT, "
                + Contract.GoogleNews.COLUMN_URL                + " TEXT, "
                + Contract.GoogleNews.COLUMN_URL_IMAGE          + " TEXT, "
                + Contract.GoogleNews.COLUMN_PUBLISHED_DATE     + " TEXT, "
                + Contract.GoogleNews.COLUMN_BOOKMARKS          + " TEXT"
                + ")";
        db.execSQL(builder);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(" DROP TABLE IF EXISTS " + Contract.GoogleNews.TABLE_NAME);
        onCreate(db);
    }
}
