package com.example.superapp;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import androidx.fragment.app.Fragment;

import com.thegrizzlylabs.sardineandroid.DavResource;

import java.io.IOException;
import java.util.List;

import com.google.android.material.transition.Hold;
import android.os.Build;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.Timer;
import java.util.TimerTask;

public class RecyclerViewFragment extends Fragment {

    public Context context; // Сохраняем ссылку на контекст
    private final Handler uiHandler = new Handler(Looper.getMainLooper());

    public RecyclerViewFragment(Context context) {
        this.context = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        /*
        // Устанавливаем переход Hold, чтобы текущий фрагмент оставался
        // видимым и не анимировался до готовности целевого фрагмента.

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setExitTransition(new Hold());
        }

        // 1. Отложить переход сразу после создания представления
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            postponeEnterTransition();
        }
       */


        View view = inflater.inflate(R.layout.fragment_recyclerview, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        Webdav webdav = new Webdav(uiHandler, recyclerView, this,  context);

        webdav.start();

/*
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            public void run() {
                startPostponedEnterTransition();
                setExitTransition(new Hold());
                postponeEnterTransition();
                timer.cancel();
            };
        };
        timer.schedule(task, 3000);
*/
        //      setExitTransition(new Hold());
      //  postponeEnterTransition();
        return view;
    }

    public void onLoad()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            startPostponedEnterTransition();
        }
    }

}
