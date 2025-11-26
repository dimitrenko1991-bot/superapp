package com.example.superapp;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import androidx.fragment.app.Fragment;

import com.thegrizzlylabs.sardineandroid.DavResource;

import java.util.List;

import androidx.recyclerview.widget.LinearLayoutManager;

public class RecyclerViewFragment extends Fragment {

    public Context context; // Сохраняем ссылку на контекст
    private List<DavResource> itemList;
    private final Handler uiHandler = new Handler(Looper.getMainLooper());

    public RecyclerViewFragment(Context context) {
        this.context = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recyclerview, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        Webdav webdav = new Webdav(uiHandler, itemList, recyclerView, context);
        webdav.start();

        return view;
    }

}
