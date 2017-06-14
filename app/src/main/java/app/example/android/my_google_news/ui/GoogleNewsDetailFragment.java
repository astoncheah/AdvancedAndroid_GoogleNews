package app.example.android.my_google_news.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import app.example.android.my_google_news.R;

/**
 * A fragment representing a single RecipeStep detail screen.
 * This fragment is either contained in a {@link GoogleNewsListActivity}
 * in two-pane mode (on tablets) or a {@link GoogleNewsDetailActivity}
 * on handsets.
 */
public class GoogleNewsDetailFragment extends Fragment{
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String URL_LINK = "URL_LINK";
    public static final String SHOW_PROGRESS = "SHOW_PROGRESS";
    public static final String HIDE_PROGRESS = "HIDE_PROGRESS";

    private Activity context;
    private String link;

    private WebView webView;
    public GoogleNewsDetailFragment(){
    }
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        context = this.getActivity();

        if(getArguments().containsKey(URL_LINK)){
            link = getArguments().getString(URL_LINK);

            Toolbar appBarLayout = (Toolbar)context.findViewById(R.id.detail_toolbar);
            if(appBarLayout!=null){
                appBarLayout.setTitle(R.string.app_name);
            }//*/
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState){
        View rootView = inflater.inflate(R.layout.google_news_detail,container,false);
        FloatingActionButton fab = (FloatingActionButton)rootView.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_SUBJECT,getString(R.string.app_name));
                i.putExtra(Intent.EXTRA_TEXT, link);
                startActivity(Intent.createChooser(i,getString(R.string.action_share)));
                //Snackbar.make(view,"Replace with your own action",Snackbar.LENGTH_LONG).setAction("Action",null).show();
            }
        });

        webView = (WebView)rootView.findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);

        webView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                //Log.e("setWebChromeClient", "cursor.progress(): " + progress);
                if(progress==100){
                    setShowProgress(false);
                }
            }
        });
        webView.setWebViewClient(new WebViewClient() {
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                setShowProgress(false);
                Toast.makeText(context, getString(R.string.oh_no) +" "+ description, Toast.LENGTH_SHORT).show();
            }
        });

        setShowProgress(true);
        webView.loadUrl(link);
        return rootView;
    }
    @Override
    public void onViewCreated(View view,@Nullable Bundle savedInstanceState){
        super.onViewCreated(view,savedInstanceState);
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
    public void onDestroy(){
        super.onDestroy();
    }
    private void setShowProgress(boolean val){
        if(val){
            context.sendBroadcast(new Intent(SHOW_PROGRESS));
        }else{
            context.sendBroadcast(new Intent(HIDE_PROGRESS));
        }
    }
}
