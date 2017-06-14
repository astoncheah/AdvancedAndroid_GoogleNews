package app.example.android.my_google_news.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class Provider extends ContentProvider {

    private static final int ITEM                   = 100;
    private static final int ITEM_FOR_ID            = 101;

    private static final UriMatcher uriMatcher = buildUriMatcher();

    private GoogleNewsDbHelper recipeDbHelper;

    private static UriMatcher buildUriMatcher() {
        UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        matcher.addURI(Contract.AUTHORITY, Contract.PATH_ITEM, ITEM);
        matcher.addURI(Contract.AUTHORITY, Contract.PATH_ITEM_ID, ITEM_FOR_ID);
        return matcher;
    }

    @Override
    public boolean onCreate() {
        recipeDbHelper = new GoogleNewsDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor returnCursor;
        switch (uriMatcher.match(uri)) {
            case ITEM:
                returnCursor = recipeDbHelper.getReadableDatabase().query(
                    Contract.GoogleNews.TABLE_NAME,
                    projection,
                    selection,
                    selectionArgs,
                    sortOrder,
                    null,
                    sortOrder
                );
                break;
            case ITEM_FOR_ID:
                returnCursor = recipeDbHelper.getReadableDatabase().query(
                    Contract.GoogleNews.TABLE_NAME,
                    projection,
                    Contract.GoogleNews._ID + " = ?",
                    new String[]{Contract.GoogleNews.getGoogleNewsFromUri(uri)},
                    sortOrder,
                    null,
                    sortOrder
                );
                break;
            default:
                throw new UnsupportedOperationException("Unknown URI:" + uri);
        }

        Context context = getContext();
        if (context != null){
            returnCursor.setNotificationUri(context.getContentResolver(), uri);
        }

        return returnCursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        Uri returnUri;
        switch (uriMatcher.match(uri)) {
            case ITEM:
                recipeDbHelper.getWritableDatabase().insert(
                        Contract.GoogleNews.TABLE_NAME,
                        null,
                        values
                );
                returnUri = Contract.GoogleNews.URI_GOOGLE_NEWS;
                break;
            default:
                throw new UnsupportedOperationException("Unknown URI:" + uri);
        }

        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        int rowsDeleted;

        if (null == selection) {
            selection = "1";
        }
        switch (uriMatcher.match(uri)) {
            case ITEM:
                rowsDeleted = recipeDbHelper.getWritableDatabase().delete(
                        Contract.GoogleNews.TABLE_NAME,
                        selection,
                        selectionArgs
                );

                break;
            case ITEM_FOR_ID:
                String idRecipe = Contract.GoogleNews.getGoogleNewsFromUri(uri);
                rowsDeleted = recipeDbHelper.getWritableDatabase().delete(
                        Contract.GoogleNews.TABLE_NAME,
                        '"' + idRecipe + '"' + " =" + Contract.GoogleNews._ID,
                        selectionArgs
                );
                break;
            default:
                throw new UnsupportedOperationException("Unknown URI:" + uri);
        }

        if (rowsDeleted != 0) {
            Context context = getContext();
            if (context != null){
                context.getContentResolver().notifyChange(uri, null);
            }
        }

        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int rowsUpdated = 0;
        switch (uriMatcher.match(uri)) {
            case ITEM:
                rowsUpdated = recipeDbHelper.getWritableDatabase().update(
                    Contract.GoogleNews.TABLE_NAME,
                    values,
                    selection,
                    selectionArgs
                );

                break;
            case ITEM_FOR_ID:
                String idRecipe = Contract.GoogleNews.getGoogleNewsFromUri(uri);
                rowsUpdated = recipeDbHelper.getWritableDatabase().update(
                    Contract.GoogleNews.TABLE_NAME,
                    values,
                    '"' + idRecipe + '"' + " =" + Contract.GoogleNews._ID,
                    selectionArgs
                );
                break;
            default:
                throw new UnsupportedOperationException("Unknown URI:" + uri);
        }
        return rowsUpdated;
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        switch (uriMatcher.match(uri)) {
            case ITEM:
                final SQLiteDatabase db = recipeDbHelper.getWritableDatabase();
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        db.insert(
                                Contract.GoogleNews.TABLE_NAME,
                                null,
                                value
                        );
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                Context context = getContext();
                if (context != null) {
                    context.getContentResolver().notifyChange(uri, null);
                }

                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }
}
