package com.example.newsapp;

import androidx.appcompat.app.ActionBar;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmResults;

public class HeadlinesViewFragment extends Fragment {
    private Context mContext;
    private RealmResults<News> news;
    private FragmentManager fm;
    private Menu myMenu;
    private View view;
    private ActionBar actionBar;
    private Realm r=Realm.getDefaultInstance();
    public HeadlinesViewFragment(Context context, RealmResults<News> news, FragmentManager fm, Menu myMenu, ActionBar actionBar){
        mContext=context;
        this.news=news;
        this.fm=fm;
        this.actionBar=actionBar;
        this.myMenu=myMenu;
    }
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.headlines_view_fragment,container,false);
        myMenu.findItem(R.id.addButton).setVisible(false);
        RecyclerView news_list=view.findViewById(R.id.main_list_rv);
        final Adapter ad=new Adapter(mContext,news,fm,myMenu,actionBar);
        news_list.setLayoutManager(new LinearLayoutManager(mContext));
        news_list.setAdapter(ad);
        return view;
    }
}
