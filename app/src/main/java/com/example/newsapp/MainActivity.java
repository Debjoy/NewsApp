package com.example.newsapp;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.GravityCompat;
import androidx.core.view.MenuItemCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;


import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Objects;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import io.realm.Sort;
import io.realm.exceptions.RealmMigrationNeededException;

import static android.os.Build.VERSION_CODES.M;

public class MainActivity extends AppCompatActivity{
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;
    private Context mContext;
    private Menu myMenu;
    private int exitFlag=1;
    private int headFlag=1;
    private int selectedPosition=0;
    private ProgressBar loadingFirst;
    private LinearLayout no_article;
    RealmResults<News> list;
    RealmResults<News> saved_list;
    RealmResults<News> current_list;
    RealmResults<News> searched_list;
    FragmentManager fm = getSupportFragmentManager();
    Realm r;
    ActionBar actionBar;
    MenuItem headlinesDrawer;
    MenuItem savedDrawer;
    private SearchView searchView;
    private SharedPreferences mSharedPrefernces;
    private FrameLayout headlineFrame;
    private SwipeRefreshLayout mSwipeRefresh;
    private String location="in";
    private NavigationView navigationView;
    //String url="https://newsapi.org/v2/top-headlines?sources=google-news&apiKey=bdf9851146d24ea497cf4397288f4cde";
    String url="https://newsapi.org/v2/top-headlines?apiKey=681bce98d4104756b73da99d430f07d0&pageSize=10&country=";
    @RequiresApi(api = M)
    @SuppressLint({"ResourceAsColor", "ResourceType"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try{
            r = Realm.getDefaultInstance();

        }catch (Exception e){
            //Get a Realm instance for this thread
            RealmConfiguration config = new RealmConfiguration.Builder()
                    .deleteRealmIfMigrationNeeded()
                    .build();
            r = Realm.getInstance(config);}
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        actionBar=getSupportActionBar();
        actionBar.setTitle("Headlines");
        headlineFrame=findViewById(R.id.headlines_list);
        headlineFrame.setBackgroundColor(Color.WHITE);
        loadingFirst=findViewById(R.id.loadingFirst);
        mContext=this;
        mDrawerLayout= findViewById(R.id.drawer);
        mToggle= new ActionBarDrawerToggle(this,mDrawerLayout,R.string.open,R.string.close);
        mDrawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();
        no_article=findViewById(R.id.no_articles);
        mSwipeRefresh=findViewById(R.id.swipeRefresh);
        mSharedPrefernces = getApplicationContext().getSharedPreferences("Location", 0);
        location=mSharedPrefernces.getString("country","in");
        //Toast.makeText(mContext, "Headlines", Toast.LENGTH_SHORT).show();
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        navigationView=findViewById(R.id.nav_view);
        Menu menu1=navigationView.getMenu();
        headlinesDrawer=menu1.findItem(R.id.nav_headline);
        savedDrawer=menu1.findItem(R.id.nav_saved_list);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                Log.i("Mainak", String.valueOf(menuItem.getItemId()));
                switch (menuItem.getItemId()){
                    case R.id.nav_headline:
                        FragmentTransaction ft = fm.beginTransaction();
                        /*if(current_list==searched_list)
                        ft.replace(R.id.headlines_list,new HeadlinesViewFragment(mContext,list,fm,myMenu,actionBar,mSwipeRefresh),"HEADLINES");
                        else*/
                        ft.replace(R.id.headlines_list,new HeadlinesViewFragment(mContext,list,fm,myMenu,actionBar,mSwipeRefresh),"HEADLINES");//.addToBackStack("HEADLINES");
                        actionBar.setTitle("Headlines");
                        myMenu.findItem(R.id.app_bar_search).setVisible(true);
                        myMenu.findItem(R.id.location).setVisible(true);
                        headlineFrame.setBackgroundColor(Color.WHITE);
                        ft.commit();
                        while(!searchView.isIconified()){
                            searchView.setIconified(true);
                        }

                        mSwipeRefresh.setEnabled(true);
                        //Toast.makeText(mContext, "Headlines", Toast.LENGTH_SHORT).show();
                        current_list=list;
                        mDrawerLayout.closeDrawer(GravityCompat.START);
                        no_article.setVisibility(View.INVISIBLE);
                        exitFlag=1;
                        break;
                    case R.id.nav_saved_list:
                        saved_list=r.where(News.class).equalTo("saved",true).findAll().sort("timestamp",Sort.DESCENDING);
                        ft=fm.beginTransaction();
                        myMenu.findItem(R.id.location).setVisible(false);
                        myMenu.findItem(R.id.app_bar_search).setVisible(false);
                        if(current_list==searched_list){
                            Log.i("DEBJOY","current_list==searched_list");
                            ft.replace(R.id.headlines_list,new HeadlinesViewFragment(mContext,list,fm,myMenu,actionBar,mSwipeRefresh),"HEADLINES");
                            ft.commit();
                            current_list=list;
                        }
                        ft=fm.beginTransaction();
                        ft.replace(R.id.headlines_list,new HeadlinesViewFragment(mContext,saved_list,fm,myMenu,actionBar,mSwipeRefresh),"SAVED").addToBackStack("SAVED");
                        ft.commit();
                        mSwipeRefresh.setEnabled(false);
                       // Toast.makeText(mContext, "Saved Articles", Toast.LENGTH_SHORT).show();
                        headlineFrame.setBackgroundColor(Color.TRANSPARENT);
                        actionBar.setTitle("Saved Articles");

                        while(!searchView.isIconified()){
                            searchView.setIconified(true);
                        }

                        current_list=saved_list;
                        if(current_list.size()==0)
                            no_article.setVisibility(View.VISIBLE);
                        else
                            no_article.setVisibility(View.INVISIBLE);
                        mDrawerLayout.closeDrawer(GravityCompat.START);
                        exitFlag=1;
                        break;
                    case R.id.nav_clear_cache:
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle("Are you sure?")
                                .setMessage("This will erase all data")
                                .setPositiveButton("Ok",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                mDrawerLayout.closeDrawer(GravityCompat.START);
                                                FragmentTransaction ft = fm.beginTransaction();
                                                ft.replace(R.id.headlines_list,new HeadlinesViewFragment(mContext,list,fm,myMenu,actionBar,mSwipeRefresh),"HEADLINES");//.addToBackStack("HEADLINES");
                                                actionBar.setTitle("Headlines");
                                                myMenu.findItem(R.id.location).setVisible(true);
                                                headlineFrame.setBackgroundColor(Color.WHITE);
                                                ft.commit();
                                                mSwipeRefresh.setEnabled(true);
                                                //Toast.makeText(mContext, "Headlines", Toast.LENGTH_SHORT).show();
                                                current_list=list;
                                                no_article.setVisibility(View.INVISIBLE);
                                                restartApp();
                                                navigationView.setCheckedItem(R.id.nav_headline);
                                                actionBar.setTitle("Headlines");
                                                no_article.setVisibility(View.INVISIBLE);
                                            }
                                        })
                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                }).show();
                        exitFlag=1;
                        break;
                }
                return true;
            }
        });
        navigationView.setCheckedItem(R.id.nav_headline);
        loadData(1);
        mSwipeRefresh.setEnabled(true);
        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadData(0);
            }
        });

    }

    public void loadData(final int addReplace){

        navigationView.getMenu().findItem(R.id.nav_headline).setEnabled(false);
        navigationView.getMenu().findItem(R.id.nav_saved_list).setEnabled(false);
        navigationView.getMenu().findItem(R.id.nav_clear_cache).setEnabled(false);
        if(!haveNetworkConnection()){
            exitDialog(addReplace);
            return;
        }
        Toast.makeText(mContext, "Fetching", Toast.LENGTH_SHORT).show();
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        String extraUrl="&rand="+(int)(Math.random()*10000);
        JsonObjectRequest request=new JsonObjectRequest(Request.Method.GET, url+location+extraUrl,null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        JSONArray a = null;

                        Log.i("Mainak", "onResponse: "+response.toString());
                        try {
                            a=response.getJSONArray("articles");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Log.i("Mainak",a.toString());
                        try {
                            Log.i("Mainak",a.getJSONObject(0).getString("title"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Realm r=null;
                        try{
                            r = Realm.getDefaultInstance();
                        }catch (Exception e){
                            //Get a Realm instance for this thread
                            RealmConfiguration config = new RealmConfiguration.Builder()
                                    .deleteRealmIfMigrationNeeded()
                                    .build();
                            r = Realm.getInstance(config);}
                        for(int i=a.length()-1;i>=0;i--){
                            try {
                                JSONObject temp = a.getJSONObject(i);
                                r.beginTransaction();
                                r.copyToRealm(new News(temp.getString("title"),temp.getString("urlToImage"),temp.getString("url"),temp.getString("publishedAt"),location,temp.getJSONObject("source").getString("name"),temp.getString("author"),temp.getString("content")));
                                r.commitTransaction();
                            }
                            catch (Exception e) {
                                r.cancelTransaction();
                                e.printStackTrace();
                                //Toast.makeText(mContext, "error occured", Toast.LENGTH_SHORT).show();
                            }
                        }
                        list=r.where(News.class).equalTo("location",location).findAll().sort("timestamp", Sort.DESCENDING);
                        FragmentTransaction ft = fm.beginTransaction();
                        Log.i("mainak",String.valueOf(list.size()));
                        if(addReplace==1)
                        ft.add(R.id.headlines_list,new HeadlinesViewFragment(mContext,list,fm,myMenu,actionBar,mSwipeRefresh));
                        else{
                            ft.replace(R.id.headlines_list,new HeadlinesViewFragment(mContext,list,fm,myMenu,actionBar,mSwipeRefresh),"HEADLINES");//.addToBackStack("HEADLINES");
                            Toast.makeText(mContext, "News updated", Toast.LENGTH_SHORT).show();
                            mSwipeRefresh.setRefreshing(false);
                        }
                        ft.commit();
                        loadingFirst.setVisibility(View.INVISIBLE);
                        navigationView.getMenu().findItem(R.id.nav_headline).setEnabled(true);
                        navigationView.getMenu().findItem(R.id.nav_saved_list).setEnabled(true);
                        navigationView.getMenu().findItem(R.id.nav_clear_cache).setEnabled(true);

                            current_list=list;

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Mainak", "onErrorResponse: "+error.getMessage());
            }
        });
        requestQueue.add(request);
        mDrawerLayout.closeDrawer(GravityCompat.START);
        exitFlag=1;
    }

    public void loadData(final String toSearch){

        Toast.makeText(mContext, "Fetching", Toast.LENGTH_SHORT).show();
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        String URL="https://newsapi.org/v2/everything?apiKey=681bce98d4104756b73da99d430f07d0&pageSize=10&q="+toSearch;
        JsonObjectRequest request=new JsonObjectRequest(Request.Method.GET, URL,null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        JSONArray a = null;
                        try {
                            a=response.getJSONArray("articles");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        try {
                            Log.i("Mainak",a.getJSONObject(0).getString("title"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Realm r=null;
                        try{
                            r = Realm.getDefaultInstance();
                        }catch (Exception e){
                            RealmConfiguration config = new RealmConfiguration.Builder()
                                    .deleteRealmIfMigrationNeeded()
                                    .build();
                            r = Realm.getInstance(config);}

                        try{
                            r.beginTransaction();
                            r.where(News.class).equalTo("location","SER").notEqualTo("saved",true).findAll().deleteAllFromRealm();
                            r.commitTransaction();
                        }catch (Exception e){
                            e.printStackTrace();
                            r.cancelTransaction();
                        }
                        Toast.makeText(mContext, a.length()+" results", Toast.LENGTH_SHORT).show();

                        for(int i=a.length()-1;i>=0;i--){
                            try {
                                JSONObject temp = a.getJSONObject(i);
                                r.beginTransaction();
                                r.copyToRealm(new News(temp.getString("title"),temp.getString("urlToImage"),temp.getString("url"),temp.getString("publishedAt"),"SER",temp.getJSONObject("source").getString("name"),temp.getString("author"),temp.getString("content")));
                                r.commitTransaction();
                            }
                            catch (Exception e) {
                                r.cancelTransaction();
                                e.printStackTrace();
                                //Toast.makeText(mContext, "error occured", Toast.LENGTH_SHORT).show();
                            }
                        }
                        searched_list=r.where(News.class).equalTo("location","SER").findAll().sort("timestamp", Sort.DESCENDING);
                        FragmentTransaction ft = fm.beginTransaction();
                        Log.i("mainak",String.valueOf(list.size()));

                            ft.replace(R.id.headlines_list,new HeadlinesViewFragment(mContext,searched_list,fm,myMenu,actionBar,mSwipeRefresh),"SEARCH");

                        ft.commit();
                        loadingFirst.setVisibility(View.INVISIBLE);
                        current_list=searched_list;

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Mainak", "onErrorResponse: "+error.getMessage());
            }
        });
        requestQueue.add(request);
        mDrawerLayout.closeDrawer(GravityCompat.START);
    }

    private boolean haveNetworkConnection() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);

        boolean isConnected = false;
        if (connectivityManager != null) {
            NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
            isConnected = (activeNetwork != null) && (activeNetwork.isConnectedOrConnecting());
        }

        return isConnected;
    }

    public void exitDialog(final int a){
            if(haveNetworkConnection()){
                loadData(a);
                return;
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Network Error!");
            builder.setMessage("Please connect to the internet!")
                    .setCancelable(false);
            builder.setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }});
            builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    exitDialog(a);
                }});

                    AlertDialog alert = builder.create();
            alert.show();
    }


    public void restartApp(){
        FragmentManager fm = getSupportFragmentManager();
        int count = fm.getBackStackEntryCount();
        for(int i = 0; i < count; ++i) {
            fm.popBackStackImmediate();
        }
        Realm r=null;
        try{
            r = Realm.getDefaultInstance();
        }catch (Exception e){
            //Get a Realm instance for this thread
            RealmConfiguration config = new RealmConfiguration.Builder()
                    .deleteRealmIfMigrationNeeded()
                    .build();
            r = Realm.getInstance(config);}
        try{
            r.beginTransaction();
            r.delete(News.class);
            r.commitTransaction();
        }catch (Exception e){
            r.cancelTransaction();
            e.printStackTrace();
        }
        loadingFirst.setVisibility(View.VISIBLE);
        clearApplicationData();//Clears all application data
        SharedPreferences.Editor edit=mSharedPrefernces.edit();
        edit.putString("country",location);
        edit.commit();
        loadData(0);
        exitFlag=1;
    }

    public void showLocationChooser(){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        int pos=0;
        switch(location){
            case "au":pos=0; break;
            case "de":pos=1;break;
            case "fr":pos=2; break;
            case "gb":pos=3;break;
            case "in": pos=4;break;
            case "jp":pos=5;break;
            case "ru":pos=6;break;
            case "us":pos=7;break;
            case "za": pos=8; break;}
        final int finalPos = pos;
        builder.setTitle("Choose One").setSingleChoiceItems(R.array.choices, pos, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                    }

         }).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                        selectedPosition = ((AlertDialog)dialog).getListView().getCheckedItemPosition();
                        if(selectedPosition!= finalPos){
                            new AlertDialog.Builder(MainActivity.this)
                                    .setTitle("Are you sure you want to change your location?")
                                    .setPositiveButton("Yes",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    switch(selectedPosition){
                                                        case 0:location="au";myMenu.findItem(R.id.location).setTitle("au");break;
                                                        case 1:location="de";myMenu.findItem(R.id.location).setTitle("de");break;
                                                        case 2:location="fr";myMenu.findItem(R.id.location).setTitle("fr");break;
                                                        case 3:location="gb";myMenu.findItem(R.id.location).setTitle("gb");break;
                                                        case 4:location="in";myMenu.findItem(R.id.location).setTitle("in");break;
                                                        case 5:location="jp";myMenu.findItem(R.id.location).setTitle("jp");break;
                                                        case 6:location="ru";myMenu.findItem(R.id.location).setTitle("ru");break;
                                                        case 7:location="us";myMenu.findItem(R.id.location).setTitle("us");break;
                                                        case 8:location="za";myMenu.findItem(R.id.location).setTitle("za");break;}
                                                    loadingFirst.setVisibility(View.VISIBLE);
                                                    SharedPreferences.Editor edit=mSharedPrefernces.edit();
                                                    edit.putString("country",location);
                                                    edit.commit();
                                                    loadData(0);
                                                }
                                            })
                                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    }).show();
                        }
                    }
         }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                    }
          }).show();
        exitFlag=1;
    }

    //DELETE CACHE
    public void clearApplicationData() {
        File cache = getCacheDir();
        File appDir = new File(cache.getParent());
        if(appDir.exists()){
            String[] children = appDir.list();
            for(String s : children){
                if(!s.equals("lib")){
                    deleteDir(new File(appDir, s));
                    Toast.makeText(mContext, "Deleted", Toast.LENGTH_SHORT).show();
                    Log.i("TAG", "File /data/data/APP_PACKAGE/" + s +" DELETED");
                }
            }
        }
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }

        return dir.delete();
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item){


        exitFlag=1;
        if(item.getItemId()==R.id.addButton){
            //optionSelect();
            WebView w=findViewById(R.id.article_new);
            String urlfetched="";
             MenuItem urlPass=myMenu.findItem(R.id.urlPass);
             urlfetched=String.valueOf(urlPass);

            News temp=r.where(News.class).equalTo("article_url",urlfetched).findFirst();
            //Log.i("Mainak",w.getUrl());Log.i("Mainak",temp.toString());
            if(temp==null){
                Toast.makeText(this,"Cannot Save :/ ",Toast.LENGTH_SHORT).show();

                return true;
            }
            if(temp.isSaved()) {
                item.setIcon(R.drawable.add);
                r.beginTransaction();
                r.where(News.class).equalTo("article_url", urlfetched).findFirst().setSaved(false);
                r.commitTransaction();
                /*
                FragmentTransaction ft=fm.beginTransaction();
                getSupportFragmentManager().popBackStack();
                ft.replace(R.id.headlines_list,new HeadlinesViewFragment(mContext,current_list,fm,myMenu));
                ft.commit();*/


                if(current_list==list)
                    ((NavigationView)findViewById(R.id.nav_view)).setCheckedItem(R.id.nav_headline);
                else
                    ((NavigationView)findViewById(R.id.nav_view)).setCheckedItem(R.id.nav_saved_list);
                if(current_list.size()==0)
                    findViewById(R.id.no_articles).setVisibility(View.VISIBLE);
                else
                    findViewById(R.id.no_articles).setVisibility(View.INVISIBLE);
                Toast.makeText(this,"Removed from saved articles",Toast.LENGTH_SHORT).show();
            }
            else{
                item.setIcon(R.drawable.ic_done_black_24dp);
                r.beginTransaction();
                r.where(News.class).equalTo("article_url", urlfetched).findFirst().setSaved(true);
                r.commitTransaction();
                no_article.setVisibility(View.INVISIBLE);
                mSharedPrefernces=getApplicationContext().getSharedPreferences("Location",0);
                Toast.makeText(this,"Added to saved articles",Toast.LENGTH_SHORT).show();
            }
        }


        if(item.getItemId()==R.id.location){
            showLocationChooser();
        }
        if(mToggle.onOptionsItemSelected(item))
            return true;
        return super.onOptionsItemSelected(item);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_item1,menu);

        myMenu=menu;
        myMenu.findItem(R.id.addButton).setVisible(false);
        myMenu.findItem(R.id.location).setTitle(location);

        MenuItem searchViewItem = menu.findItem(R.id.app_bar_search);
        searchView = (SearchView) MenuItemCompat.getActionView(searchViewItem);


        searchView.setQueryHint("Search Topic");
        searchView.setOnClickListener(new SearchView.OnClickListener(){

            @Override
            public void onClick(View view) {
                exitFlag=1;
                if(mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                    mDrawerLayout.closeDrawer(GravityCompat.START);
                }
            }
        });
        int searchPlateId = searchView.getContext().getResources().getIdentifier("android:id/search_plate", null, null);
        View searchPlate = searchView.findViewById(searchPlateId);
        if (searchPlate!=null) {
            searchPlate.setBackgroundColor(Color.parseColor("#263238"));

            int searchTextId = searchPlate.getContext().getResources().getIdentifier("android:id/search_src_text", null, null);
            TextView searchText = (TextView) searchPlate.findViewById(searchTextId);
            if (searchText!=null) {
                searchText.setTextColor(Color.WHITE);
                searchText.setHintTextColor(Color.parseColor("#838f96"));
            }
        }
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Toast.makeText(mContext, query, Toast.LENGTH_SHORT).show();
                loadData(query);
                searchView.clearFocus();
             /*   if(list.contains(query)){
                    adapter.getFilter().filter(query);
                }else{
                    Toast.makeText(MainActivity.this, "No Match found",Toast.LENGTH_LONG).show();
                }*/
                return false;

            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }

        });
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
//                FragmentTransaction ft = fm.beginTransaction();
//                ft.replace(R.id.headlines_list,new HeadlinesViewFragment(mContext,list,fm,myMenu,actionBar,mSwipeRefresh),"HEADLINES");
//                ft.commit();
                return false;
            }
        });
        return true;
    }

    @Override
    public void onBackPressed(){
        if(mDrawerLayout.isDrawerOpen(GravityCompat.START)){
            exitFlag=1;
            mDrawerLayout.closeDrawer(GravityCompat.START);
            return;
        }
        if(actionBar.getTitle().equals("Saved Articles")){
            //Toast.makeText(mContext, "this is test", Toast.LENGTH_SHORT).show();
            actionBar.setTitle("Headlines");
            myMenu.findItem(R.id.app_bar_search).setVisible(true);
            myMenu.findItem(R.id.location).setVisible(true);
            mSwipeRefresh.setEnabled(true);
            searchView.setIconified(true);
            current_list=list;
            no_article.setVisibility(View.INVISIBLE);
            exitFlag=1;
            if(getSupportFragmentManager().getBackStackEntryCount()==0){
                FragmentTransaction ft = fm.beginTransaction();
                ft.replace(R.id.headlines_list, new HeadlinesViewFragment(mContext, list, fm, myMenu, actionBar, mSwipeRefresh), "HEADLINES");
                ft.commit();
                return;
            }
                super.onBackPressed();
            return;
        }
        if(!searchView.isIconified() && current_list!=list) {
            exitFlag=1;
            searchView.setIconified(true);
            actionBar.setTitle("Headlines");
            myMenu.findItem(R.id.app_bar_search).setVisible(true);
            myMenu.findItem(R.id.location).setVisible(true);
            headlineFrame.setBackgroundColor(Color.WHITE);
            mSwipeRefresh.setEnabled(true);
            mDrawerLayout.closeDrawer(GravityCompat.START);
            no_article.setVisibility(View.INVISIBLE);
            return;
        }else if(!searchView.isIconified()){
            exitFlag=1;
            searchView.setIconified(true);
            myMenu.findItem(R.id.app_bar_search).setVisible(true);
            return;
        }
        if(current_list==searched_list){
            exitFlag=1;
            if(getSupportFragmentManager().getBackStackEntryCount()==0) {
                current_list = list;
                FragmentTransaction ft = fm.beginTransaction();
                ft.replace(R.id.headlines_list, new HeadlinesViewFragment(mContext, current_list, fm, myMenu, actionBar, mSwipeRefresh), "HEADLINES");
                //fm.popBackStackImmediate();
                FragmentManager f1 = getSupportFragmentManager();
                for(int i = 0; i < f1.getBackStackEntryCount(); ++i) {
                    f1.popBackStack();
                }
                actionBar.setTitle("Headlines");
                myMenu.findItem(R.id.location).setVisible(true);
                headlinesDrawer.setChecked(true);
                mSwipeRefresh.setEnabled(true);
                myMenu.findItem(R.id.app_bar_search).setVisible(true);
                headlineFrame.setBackgroundColor(Color.WHITE);
                ft.commit();
                return;
            }
            FragmentTransaction ft = fm.beginTransaction();
            FragmentManager f1 = getSupportFragmentManager();
            for(int i = 0; i < f1.getBackStackEntryCount(); ++i) {
                f1.popBackStack();
            }
            ft.replace(R.id.headlines_list, new HeadlinesViewFragment(mContext, current_list, fm, myMenu, actionBar, mSwipeRefresh), "HEADLINES");
            //fm.popBackStackImmediate();
            actionBar.setTitle("Headlines");
            myMenu.findItem(R.id.location).setVisible(true);
            headlinesDrawer.setChecked(true);
            mSwipeRefresh.setEnabled(true);
            myMenu.findItem(R.id.app_bar_search).setVisible(true);
            headlineFrame.setBackgroundColor(Color.WHITE);
            ft.commit();
            return;
        }
        if(getSupportFragmentManager().getBackStackEntryCount()==0){
            if(exitFlag==1){
                Toast.makeText(mContext, "Press back again to exit", Toast.LENGTH_SHORT).show();
                exitFlag=0;
                return;
            }
            finish();
            return;
        }
        if(getSupportFragmentManager().getBackStackEntryAt(getSupportFragmentManager().getBackStackEntryCount()-1).getName().equals("ARTICLE")){
            exitFlag=1;
            if (current_list==searched_list) {
                FragmentTransaction ft = fm.beginTransaction();
                ft.replace(R.id.headlines_list, new HeadlinesViewFragment(mContext, current_list, fm, myMenu, actionBar, mSwipeRefresh), "HEADLINES");
                //fm.popBackStackImmediate();
                FragmentManager f1 = getSupportFragmentManager();
                for(int i = 0; i < f1.getBackStackEntryCount(); ++i) {
                    f1.popBackStack();
                }
                actionBar.setTitle("Headlines");
                myMenu.findItem(R.id.location).setVisible(true);
                headlinesDrawer.setChecked(true);
                mSwipeRefresh.setEnabled(true);
                myMenu.findItem(R.id.app_bar_search).setVisible(true);
                headlineFrame.setBackgroundColor(Color.WHITE);
                ft.commit();
                return;
            }
            if(current_list==list) {
                FragmentTransaction ft = fm.beginTransaction();
                ft.replace(R.id.headlines_list, new HeadlinesViewFragment(mContext, current_list, fm, myMenu, actionBar, mSwipeRefresh), "HEADLINES");
                //fm.popBackStackImmediate();
                FragmentManager f1 = getSupportFragmentManager();
                for(int i = 0; i < f1.getBackStackEntryCount(); ++i) {
                    f1.popBackStack();
                }
                ft.commit();
                actionBar.setTitle("Headlines");
                myMenu.findItem(R.id.location).setVisible(true);
                headlinesDrawer.setChecked(true);
                mSwipeRefresh.setEnabled(true);
                myMenu.findItem(R.id.app_bar_search).setVisible(true);
                headlineFrame.setBackgroundColor(Color.WHITE);
                navigationView.getMenu().findItem(R.id.nav_headline).setChecked(true);
                return;
            }
            else{
                FragmentTransaction ft = fm.beginTransaction();
                ft.replace(R.id.headlines_list, new HeadlinesViewFragment(mContext, current_list, fm, myMenu, actionBar, mSwipeRefresh), "SAVED");
                //fm.popBackStackImmediate();
                FragmentManager f1 = getSupportFragmentManager();
                for(int i = 0; i < f1.getBackStackEntryCount(); ++i) {
                    f1.popBackStack();
                }
                ft.commit();
                actionBar.setTitle("Saved Articles");
                headlinesDrawer.setChecked(true);
                //current_list=saved_list;
                if(saved_list.size()==0)
                    no_article.setVisibility(View.VISIBLE);
                else
                    no_article.setVisibility(View.INVISIBLE);
                //headlineFrame.setBackgroundColor(Color.WHITE);
                navigationView.getMenu().findItem(R.id.nav_saved_list).setChecked(true);
                return;
            }
        }
        else{
            if(exitFlag==1){
                Toast.makeText(mContext, "Press back again to exit", Toast.LENGTH_SHORT).show();
                exitFlag=0;
                return;
            }
            finish();
        }
    }


    @Override
    protected void onDestroy() {
        r.close();
        super.onDestroy();
    }

    public void openNewsApi(View view){
        String url = "https://newsapi.org";
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }
}
