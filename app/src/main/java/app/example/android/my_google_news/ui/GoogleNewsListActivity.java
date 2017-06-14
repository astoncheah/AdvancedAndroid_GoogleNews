package app.example.android.my_google_news.ui;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.test.espresso.IdlingResource;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.transition.Slide;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.squareup.picasso.Picasso;

import app.example.android.my_google_news.R;
import app.example.android.my_google_news.data.Contract;
import app.example.android.my_google_news.sync.MyJobService;
import app.example.android.my_google_news.sync.TestIdlingResource;
import app.example.android.my_google_news.sync.UpdaterService;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * An activity representing a list of RecipeSteps. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link GoogleNewsDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class GoogleNewsListActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{
    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    @BindView(R.id.recipestep_list)
    RecyclerView mRecyclerView;
    @BindView(R.id.toolbar_progress_bar)
    ProgressBar progressBar;
    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.textError)
    TextView textError;

    private SimpleItemRecyclerViewAdapter adapter;
    private View PressedView;
    private boolean needRefreshViews = true;
    private boolean needReload = false;
    private InterstitialAd mInterstitialAd;
    private TestIdlingResource mIdlingResource;
    private String sortData = UpdaterService.NEWS_URL_TOP;
    private FirebaseAnalytics mFirebaseAnalytics;

    public IdlingResource getIdlingResource() {
        if (mIdlingResource == null) {
            mIdlingResource = new TestIdlingResource();
        }
        return mIdlingResource;
    }
    private BroadcastReceiver receiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context,Intent intent){
            String action = intent.getAction();
            switch(action){
                case GoogleNewsDetailFragment.SHOW_PROGRESS:
                    progressBar.setVisibility(View.VISIBLE);
                    break;
                case GoogleNewsDetailFragment.HIDE_PROGRESS:
                    progressBar.setVisibility(View.GONE);
                    break;
                case UpdaterService.BROADCAST_ACTION_STATE_CHANGE:
                    boolean refresh = intent.getBooleanExtra(UpdaterService.EXTRA_REFRESHING,false);
                    if(refresh){
                        swipeRefreshLayout.setRefreshing(true);
                    }else{
                        swipeRefreshLayout.setRefreshing(false);
                        if(needReload){
                            needReload = false;
                            getSupportLoaderManager().restartLoader(0,null,GoogleNewsListActivity.this);
                        }
                    }
                    break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        if (mIdlingResource != null) {
            mIdlingResource.setIdleState(false);
        }
        setContentView(R.layout.activity_google_news_list);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if(actionBar!=null){
            //actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.app_name);
        }

        if(findViewById(R.id.recipestep_detail_container)!=null){
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }
        adapter = new SimpleItemRecyclerViewAdapter();
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener(){
            @Override
            public void onRefresh(){
                Log.e("GoogleNewsListActivity", "onStartJob");
                refresh();
            }
        });
        prepareAds();
        getSupportLoaderManager().initLoader(0, null, this);
    }
    @Override
    public void onResume(){
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(GoogleNewsDetailFragment.SHOW_PROGRESS);
        filter.addAction(GoogleNewsDetailFragment.HIDE_PROGRESS);
        filter.addAction(UpdaterService.BROADCAST_ACTION_STATE_CHANGE);
        this.registerReceiver(receiver,filter);
    }
    @Override
    public void onPause(){
        super.onPause();
        this.unregisterReceiver(receiver);
    }
    @Override
    protected void onDestroy(){
        MyJobService.initialize(GoogleNewsListActivity.this);
        super.onDestroy();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.sort_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.home:
                finish();
                return true;
            case R.id.top_rated:
                needReload = true;
                sortData = UpdaterService.NEWS_URL_TOP;
                UpdaterService.savePref(this,sortData);
                refresh();
                break;
            case R.id.latest:
                needReload = true;
                sortData = UpdaterService.NEWS_URL_LATEST;
                UpdaterService.savePref(this,sortData);
                refresh();
                break;
            case R.id.bookmarks:
                sortData = UpdaterService.NEWS_BOOKMARKS;
                getSupportLoaderManager().restartLoader(0, null, this);
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public Loader<Cursor> onCreateLoader(int id,Bundle args){
        if(sortData.equals(UpdaterService.NEWS_BOOKMARKS)){
            return new CursorLoader(this,
                Contract.GoogleNews.URI_GOOGLE_NEWS,
                Contract.Query.PROJECTION_GOOGLE_NEWS,
                Contract.GoogleNews.COLUMN_BOOKMARKS+"=?",
                new String[]{Contract.GoogleNews.COLUMN_BOOKMARKS},
                null);
        }else{
            return new CursorLoader(this,
                Contract.GoogleNews.URI_GOOGLE_NEWS,
                Contract.Query.PROJECTION_GOOGLE_NEWS,
                null,
                null,
                null);
        }
    }
    @Override
    public void onLoadFinished(Loader<Cursor> loader,Cursor cursor){
        if(cursor.getCount()==0){
            if(UpdaterService.isInternetConnected(this)){
                refresh();
            }else{
                textError.setVisibility(View.VISIBLE);
            }
        }else{
            adapter.setCursor(cursor);
            if(needRefreshViews){
                textError.setVisibility(View.GONE);
                needRefreshViews = false;

                mRecyclerView.setAdapter(adapter);
                mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

                if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP){
                    Slide slide = new Slide();
                    slide.setSlideEdge(Gravity.BOTTOM);

                    TransitionManager.beginDelayedTransition(mRecyclerView, slide);
                }
            }
        }
        if (mIdlingResource != null) {
            mIdlingResource.setIdleState(true);
        }
    }
    @Override
    public void onLoaderReset(Loader<Cursor> loader){
        mRecyclerView.setAdapter(null);
    }
    private void refresh() {
        needRefreshViews = true;
        MyJobService.downloadData(this);
    }
    private void prepareAds(){
        AdRequest adRequest = new AdRequest.Builder()
            .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
            .build();

        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173733");
        mInterstitialAd.setAdListener(new AdListener(){
            @Override
            public void onAdClosed(){
                mInterstitialAd = null;
            }
            @Override
            public void onAdLoaded(){

            }
        });
        mInterstitialAd.loadAd(adRequest);
    }
    private void logAnalyticsEvent(String id,String link){
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, id);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, link);
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "link");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }
    private void showAds() {
        if(mInterstitialAd!=null && mInterstitialAd.isLoaded()){
            mInterstitialAd.show();
        }
    }
    private void launchStepDetailFragment(String urlLink,final View v){
        if(mTwoPane){
            if(PressedView!=null){
                PressedView.setPressed(false);
            }
            v.postDelayed(new Runnable(){
                @Override
                public void run(){
                    PressedView = v;
                    PressedView.setPressed(true);
                }
            },300);
        }

        Bundle arguments = new Bundle();
        arguments.putString(GoogleNewsDetailFragment.URL_LINK,urlLink);
        GoogleNewsDetailFragment fragment = new GoogleNewsDetailFragment();
        fragment.setArguments(arguments);
        getSupportFragmentManager()
            .beginTransaction()
            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
            .replace(R.id.recipestep_detail_container,fragment)
            .commit();
    }
    private void launchStepDetailActivity(String urlLink){
        Intent intent = new Intent(this,GoogleNewsDetailActivity.class);
        intent.putExtra(GoogleNewsDetailFragment.URL_LINK,urlLink);
        startActivity(intent);
    }
    public class SimpleItemRecyclerViewAdapter extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder>{
        private Cursor cursor;

        void setCursor(Cursor cursor) {
            this.cursor = cursor;
            notifyDataSetChanged();
        }
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent,int viewType){
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.google_news_card,parent,false);
            return new ViewHolder(view);
        }
        @Override
        public void onBindViewHolder(final ViewHolder holder,int position){
            cursor.moveToPosition(position);

            final String id = cursor.getString(cursor.getColumnIndex(Contract.GoogleNews._ID));
            final String urlLink = cursor.getString(cursor.getColumnIndex(Contract.GoogleNews.COLUMN_URL));
            final String imageLink = cursor.getString(cursor.getColumnIndex(Contract.GoogleNews.COLUMN_URL_IMAGE));
            final String imageBookmarks = cursor.getString(cursor.getColumnIndex(Contract.GoogleNews.COLUMN_BOOKMARKS));

            Picasso.with(GoogleNewsListActivity.this).load(imageLink).into(holder.imageUrl);
            holder.txtTitle.setText(cursor.getString(cursor.getColumnIndex(Contract.GoogleNews.COLUMN_TITLE)));
            holder.txtDate.setText(cursor.getString(cursor.getColumnIndex(Contract.GoogleNews.COLUMN_PUBLISHED_DATE)));

            holder.mainView.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    logAnalyticsEvent(id,urlLink);
                    showAds();
                    if(mTwoPane){
                        launchStepDetailFragment(urlLink,v);
                    }else{
                        launchStepDetailActivity(urlLink);
                    }
                }
            });
            if(position==0 && mTwoPane){
                launchStepDetailFragment(urlLink,holder.mainView);
            }
            if(imageBookmarks.equals(Contract.GoogleNews.COLUMN_BOOKMARKS)){
                holder.imageBookmarks.setImageResource(android.R.drawable.btn_star_big_on);
                holder.imageBookmarks.setTag(android.R.drawable.btn_star_big_on);
            }else{
                holder.imageBookmarks.setImageResource(android.R.drawable.btn_star_big_off);
                holder.imageBookmarks.setTag(android.R.drawable.btn_star_big_off);
            }
            holder.imageBookmarks.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    if((int)v.getTag()==android.R.drawable.btn_star_big_on){
                        holder.imageBookmarks.setImageResource(android.R.drawable.btn_star_big_off);
                        v.setTag(android.R.drawable.btn_star_big_off);
                        updateBookmarks(id,"");
                    }else{
                        holder.imageBookmarks.setImageResource(android.R.drawable.btn_star_big_on);
                        v.setTag(android.R.drawable.btn_star_big_on);
                        updateBookmarks(id,Contract.GoogleNews.COLUMN_BOOKMARKS);
                    }
                }
            });
        }
        private void updateBookmarks(String id,String val){
            ContentValues values = new ContentValues();
            values.put(Contract.GoogleNews.COLUMN_BOOKMARKS,val);
            int number = getContentResolver().update(
                Contract.GoogleNews.URI_GOOGLE_NEWS,
                values,
                Contract.GoogleNews._ID+"=? ",new String[]{id});
            Log.e("updateBookmarks","number: "+number);
        }
        @Override
        public int getItemCount(){
            int count = 0;
            if (cursor != null) {
                count = cursor.getCount();
            }
            return count;
        }
        public class ViewHolder extends RecyclerView.ViewHolder{
            @BindView(R.id.mainView)
            LinearLayout mainView;
            @BindView(R.id.imageUrl)
            ImageView imageUrl;
            @BindView(R.id.txtTitle)
            TextView txtTitle;
            @BindView(R.id.txtDate)
            TextView txtDate;
            @BindView(R.id.imageBookmarks)
            ImageView imageBookmarks;
            public ViewHolder(View view){
                super(view);
                ButterKnife.bind(this, view);
            }
        }
    }
}
