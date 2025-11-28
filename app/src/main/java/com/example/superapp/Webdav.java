package com.example.superapp;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;
import com.thegrizzlylabs.sardineandroid.DavResource;
import com.thegrizzlylabs.sardineandroid.Sardine;
import com.thegrizzlylabs.sardineandroid.impl.OkHttpSardine;
import java.io.IOException;
import java.net.DatagramSocket;
import java.util.List;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.concurrent.Executors;

public class Webdav extends Thread  {
    private static final String TAG = "UdpCommunication";
    private final Handler uiHandler;
    private final RecyclerView recyclerView;
    private MyAdapter adapter;
    public Context context; // Сохраняем ссылку на контекст
    List<DavResource> resources;
    private final String URL_WEBDAV;
    private final String username_WEBDAV;
    private final String password_WEBDAV;
    private RecyclerViewFragment recyclerViewFragment;

    public Webdav(Handler handler, RecyclerView recyclerView, RecyclerViewFragment recyclerViewFragment, Context context) {
        this.uiHandler = handler;
        this.recyclerView = recyclerView;
        this.context = context;
        this.recyclerViewFragment = recyclerViewFragment;

        SharedPreferences prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
        URL_WEBDAV = prefs.getString("url_WEBDAV","");
        username_WEBDAV = prefs.getString("username_WEBDAV","");
        password_WEBDAV = prefs.getString("password_WEBDAV","");
    }

    @Override
    public void run() {

            Sardine sardine = new OkHttpSardine();
            sardine.setCredentials(username_WEBDAV, password_WEBDAV);

            try {
                resources = sardine.list(URL_WEBDAV);

                uiHandler.post(() ->
                {
                    adapter = new MyAdapter(getSortedByCreationDate(resources), this);
                    recyclerView.setAdapter(adapter);
                    recyclerViewFragment.onLoad();
                });

            }
            catch (IOException e)
            {
                /*
                e.printStackTrace();
                throw new RuntimeException(e);
                */

                uiHandler.post(() ->
                {
                    Toast.makeText(context, "Ошибка подключения к WEBDAV", Toast.LENGTH_SHORT).show();
                });

                recyclerViewFragment.onLoad();
            }

    }

    public List<DavResource> getSortedByCreationDate(List<DavResource> resources) {
        return resources.stream()
                .sorted(Comparator.comparing(DavResource::getCreation).reversed())
                .filter(resource -> !resource.isDirectory())
                .collect(Collectors.toList());
    }

    public void onClick(String filename)
    {
        // Создание и запуск скачивания файла
        WebDavDownloader downloader = new WebDavDownloader();

        Executors.newSingleThreadExecutor().execute(() ->
        {
            downloader.downloadFile(resources, URL_WEBDAV, username_WEBDAV, password_WEBDAV, filename, context, uiHandler);
        });
    }


}
