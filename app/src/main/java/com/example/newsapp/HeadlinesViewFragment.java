package com.example.newsapp;

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

public class HeadlinesViewFragment extends Fragment {
    private Context mContext;
    private ArrayList<News> news;
    private FragmentManager fm;
    private Menu myMenu;
    public HeadlinesViewFragment(Context context, ArrayList<News> news, FragmentManager fm, Menu myMenu){
        mContext=context;
        this.news=news;
        this.fm=fm;
        this.myMenu=myMenu;
    }
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.headlines_view_fragment,container,false);
        myMenu.findItem(R.id.addButton).setVisible(false);
        RecyclerView news_list=view.findViewById(R.id.main_list_rv);
        final Adapter ad=new Adapter(mContext,news,fm,myMenu);
        news_list.setLayoutManager(new LinearLayoutManager(mContext));
        news_list.setAdapter(ad);
        return view;
    }
}
