package app.example.android.my_google_news.data;


import android.net.Uri;
import android.provider.BaseColumns;

public final class Contract{

    public static final String AUTHORITY = "app.example.android.my_google_news";
    public static final String PATH_ITEM = "item";
    public static final String PATH_ITEM_ID = "item/*";
    private static final Uri BASE_URI = Uri.parse("content://" + AUTHORITY);

    @SuppressWarnings("unused")
    public static final class GoogleNews implements BaseColumns {
        public static final String ALL = "GET_ALL";
        public static final Uri URI_GOOGLE_NEWS = BASE_URI.buildUpon().appendPath(PATH_ITEM).build();

        public static final String TABLE_NAME = "google_news";

        public static final String COLUMN_AUTHOR = "author";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_DESC = "description";
        public static final String COLUMN_URL = "url";
        public static final String COLUMN_URL_IMAGE = "urlToImage";
        public static final String COLUMN_PUBLISHED_DATE = "publishedAt";
        public static final String COLUMN_BOOKMARKS = "bookmarks";

        /*
        "author": "Charlie Savage",
        "title": "Intelligence Contractor Is Charged in First Leak Case Under Trump",
        "description": "Reality Leigh Winner, 25, has been charged under the Espionage Act for sending a report about Russia’s interference in the 2016 election to the news media.",
        "url": "https://www.nytimes.com/2017/06/05/us/politics/reality-winner-contractor-leaking-russia-nsa.html",
        "urlToImage": "https://static01.nyt.com/images/2017/06/06/us/06dc-intel/06dc-intel-facebookJumbo.jpg",
        "publishedAt": "2017-06-06T01:14:53Z"
        */

        /*
        sort by:
        top	= Requests a list of the source's headlines sorted in the order they appear on its homepage.
        latest = Requests a list of the source's headlines sorted in chronological order, newest first.
        popular	= Requests a list of the source's current most popular or currently trending headlines.
        */
        public static Uri makeUriForGoogleNews(String id) {
            return URI_GOOGLE_NEWS.buildUpon().appendPath(id).build();
        }

        public static String getGoogleNewsFromUri(Uri queryUri) {
            return queryUri.getLastPathSegment();
        }
    }
    public interface Query {
        String[] PROJECTION_GOOGLE_NEWS = {
            GoogleNews._ID,
            GoogleNews.COLUMN_AUTHOR,
            GoogleNews.COLUMN_TITLE,
            GoogleNews.COLUMN_DESC,
            GoogleNews.COLUMN_URL,
            GoogleNews.COLUMN_URL_IMAGE,
            GoogleNews.COLUMN_PUBLISHED_DATE,
            GoogleNews.COLUMN_BOOKMARKS
        };
    }
}
