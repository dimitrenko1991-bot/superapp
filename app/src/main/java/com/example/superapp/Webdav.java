package com.example.superapp;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
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
    public Webdav(Handler handler, List<DavResource> itemList,  RecyclerView recyclerView, Context context) {
        this.uiHandler = handler;
      //  this.itemList = itemList;
        this.recyclerView = recyclerView;
        this.context = context;

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

                });

            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }

    }

    public List<DavResource> getSortedByCreationDate(List<DavResource> resources) {
        return resources.stream()
                .sorted(Comparator.comparing(DavResource::getCreation).reversed())
                .collect(Collectors.toList());
    }

    public void onClick(String filename)
    {
        // Разрешение уже предоставлено, можно начинать скачивание
        startDownload(resources, filename);
    }


    private void startDownload(List<DavResource> resources, String filename) {
        // Создание и запуск скачивания файла
        WebDavDownloader downloader = new WebDavDownloader();

        Executors.newSingleThreadExecutor().execute(() ->
       {
            downloader.downloadFile(resources, URL_WEBDAV, username_WEBDAV, password_WEBDAV, filename, context, uiHandler);
       });
    }

}
