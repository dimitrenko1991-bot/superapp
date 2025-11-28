package com.example.superapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.HttpAuthHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.android.material.transition.Hold;
import android.os.Build;

import java.util.Timer;
import java.util.TimerTask;

public class WebViewFragment extends Fragment {

    private static final String TAG = "WebViewFragment";

    private static String URL;
    private static String URL_WEBDAV;
    private String username_WEBDAV;
    private String password_WEBDAV;
    private static String URL_RED;
    private String username_RED;
    private String password_RED;
    private String URL_GREEN;
    private String username_GREEN;
    private String password_GREEN;

    public Context context; // Сохраняем ссылку на контекст

    public WebViewFragment(String URL, Context context)
    {
        WebViewFragment.URL = URL;
        this.context = context;
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Устанавливаем переход Hold, чтобы текущий фрагмент оставался
        // видимым и не анимировался до готовности целевого фрагмента.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      //     setExitTransition(new Hold());
        }

        // 1. Отложить переход сразу после создания представления
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
       //     postponeEnterTransition();
        }

     //    startPostponedEnterTransition();

        View view = inflater.inflate(R.layout.fragment_webview, container, false);

        WebView webView = view.findViewById(R.id.my_webview);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setDatabaseEnabled(true);

        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(false);
        webView.getSettings().setUseWideViewPort(true);

        webView.getSettings().setLoadWithOverviewMode(true); // Загружает страницу полностью, используя широкий вьюпорт
        webView.getSettings().setSupportZoom(true);



        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl(URL); // Загрузите нужный URL
        //webView.loadUrl("https:/www.mail.ru");

        SharedPreferences prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
        URL_WEBDAV = prefs.getString("url_WEBDAV","");
        username_WEBDAV = prefs.getString("username_WEBDAV","");
        password_WEBDAV = prefs.getString("password_WEBDAV","");

        URL_RED = prefs.getString("url_RED","");
        username_RED = prefs.getString("username_RED","");
        password_RED = prefs.getString("password_RED","");

        URL_GREEN = prefs.getString("url_GREEN","");
        username_GREEN = prefs.getString("username_GREEN","");
        password_GREEN = prefs.getString("password_GREEN","");
/*
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            public void run() {

                Log.d(TAG, "TIMER ");
                startPostponedEnterTransition();
                setExitTransition(new Hold());
                postponeEnterTransition();
                timer.cancel();
            };
        };
        timer.schedule(task, 3000);
  */


        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm) {
                // Check if you have stored credentials for this host/realm

                //  Log.d(TAG, "your_server_host "+ host );
                //  Log.d(TAG, "your_server_realm "+ realm );

                //  Log.d(TAG, "HttpAuthRequest_____________________");

                if (URL_GREEN.contains(host) && realm.equals("Motion"))
                {
                    handler.proceed(username_GREEN, password_GREEN);
                }
                else if (URL_RED.contains(host) && realm.equals("Motion"))
                {
                    handler.proceed(username_RED, password_RED);
                }
                else if (URL_WEBDAV.contains(host))
                {
                    handler.proceed(username_WEBDAV, password_WEBDAV);
                }
                else
                {
                    // If no credentials are available, cancel the request or show a dialog
                    handler.cancel();
                }
            }

            @Override
            public void onPageFinished(WebView view, String url)
            {
                startPostponedEnterTransition();
                Log.d(TAG, "onPageFinished_____________________");
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon)
            {
                //Log.d(TAG, "onPageStarted_____________________"+url+" vs "+URL_GREEN);

                if (url.contains(URL_GREEN))
                {
                    startPostponedEnterTransition();
                }
                else if (url.contains(URL_RED))
                {
                    startPostponedEnterTransition();
                }
            }

            @Override
            public void onLoadResource(WebView view, String url)
            {
                //Log.d(TAG, "onLoadResource____________________"+url);
            }

        });

        return view;
    }



}