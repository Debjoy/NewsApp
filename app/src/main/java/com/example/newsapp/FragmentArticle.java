package com.example.newsapp;
import androidx.appcompat.app.ActionBar;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class FragmentArticle extends Fragment{
    private News news;
    private Menu myMenu;
    private ActionBar actionBar;
    private WebView webview;
    private ProgressBar spinner;
    private ProgressBar amtProgress;
    private SwipeRefreshLayout mSwipeRefresh;

    String showHideInitialUse="show";
    FragmentArticle(News news, Menu myMenu, ActionBar actionBar, SwipeRefreshLayout mSwipeRefresh){
        this.myMenu=myMenu;
        this.mSwipeRefresh=mSwipeRefresh;
        this.actionBar=actionBar;
        this.news=news;
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.article_view_fragment,container,false);
        spinner=view.findViewById(R.id.loadingPage);
        amtProgress=view.findViewById(R.id.progress_amt);


        MenuItem searchViewItem = myMenu.findItem(R.id.app_bar_search);
        MenuItem urlPass=myMenu.findItem(R.id.urlPass);
        amtProgress.setMax(100);

        urlPass.setTitle(news.getArticle_url());
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchViewItem);
        while(!searchView.isIconified()){searchView.setIconified(true);}

        webview=view.findViewById(R.id.article_new);
        webview.setVisibility(webview.VISIBLE);
        //webview.setForceDarkAllowed(true);
        //--webview.set
        mSwipeRefresh.setEnabled(false);
        actionBar.setTitle("News Article");
        myMenu.findItem(R.id.addButton).setVisible(true);
        myMenu.findItem(R.id.location).setVisible(false);
        //Toast.makeText(getActivity(), "Article", Toast.LENGTH_SHORT).show();

        if(news.isSaved())
            myMenu.findItem(R.id.addButton).setIcon(R.drawable.ic_done_black_24dp);
        else
            myMenu.findItem(R.id.addButton).setIcon(R.drawable.add);
        webview.setWebViewClient(new CustomWebViewClient());
        myMenu.findItem(R.id.app_bar_search).setVisible(false);
        WebSettings webSettings = webview.getSettings();
        webSettings.setJavaScriptEnabled(true);
        // Force links and redirects to open in the WebView instead of in a browser

        webview.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                amtProgress.setProgress(newProgress);
            }
        });
        webview.loadUrl(news.getArticle_url());
        return view;
    }

    private class CustomWebViewClient extends WebViewClient{
        @Override
        public void onPageStarted(WebView webview, String url, Bitmap favicon){
            if(showHideInitialUse.equals("show")){
                webview.setVisibility(webview.VISIBLE);
               spinner.setVisibility(View.GONE);
            }
        }
        @Override
        public void onPageFinished(WebView view, String url){
            showHideInitialUse="hide";
            spinner.setVisibility(View.GONE);
            amtProgress.setVisibility(View.GONE);
            view.setVisibility(webview.VISIBLE);
            super.onPageFinished(view,url);
        }
    }

}