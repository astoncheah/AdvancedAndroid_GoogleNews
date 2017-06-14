package app.example.android.my_google_news.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import app.example.android.my_google_news.R;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * An activity representing a single RecipeStep detail screen. This
 * activity is only used narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link GoogleNewsListActivity}.
 */
public class GoogleNewsDetailActivity extends AppCompatActivity{
    private String urlLink;

    @BindView(R.id.toolbar_progress_bar)
    ProgressBar progressBar;

    private BroadcastReceiver receiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context,Intent intent){
            String action = intent.getAction();
            if(action.equals(GoogleNewsDetailFragment.SHOW_PROGRESS)){
                progressBar.setVisibility(View.VISIBLE);
            }else if(action.equals(GoogleNewsDetailFragment.HIDE_PROGRESS)){
                progressBar.setVisibility(View.GONE);
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_news_detail);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar)findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if(actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        if(savedInstanceState==null){
            urlLink = getIntent().getStringExtra(GoogleNewsDetailFragment.URL_LINK);
        }else{
            urlLink = savedInstanceState.getString(GoogleNewsDetailFragment.URL_LINK);
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(GoogleNewsDetailFragment.SHOW_PROGRESS);
        filter.addAction(GoogleNewsDetailFragment.HIDE_PROGRESS);
        this.registerReceiver(receiver,filter);

        setStepsPage(urlLink);
    }
    @Override
    public void onResume(){
        super.onResume();
    }
    @Override
    public void onPause(){
        super.onPause();
    }
    @Override
    protected void onDestroy(){
        this.unregisterReceiver(receiver);
        super.onDestroy();
    }
    private void setStepsPage(String urlLink){
        this.urlLink = urlLink;
        Bundle arguments = new Bundle();
        arguments.putString(GoogleNewsDetailFragment.URL_LINK,urlLink);
        GoogleNewsDetailFragment fragment = new GoogleNewsDetailFragment();
        fragment.setArguments(arguments);

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        //fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        fragmentTransaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        fragmentTransaction.replace(R.id.recipestep_detail_container,fragment);
        fragmentTransaction.commit();
    }
    @Override
    protected void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);

        outState.putString(GoogleNewsDetailFragment.URL_LINK,urlLink);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();
        if(id==android.R.id.home){
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. Use NavUtils to allow users
            // to navigate up one level in the application structure. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            //NavUtils.navigateUpTo(this,new Intent(this,GoogleNewsListActivity.class));
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
