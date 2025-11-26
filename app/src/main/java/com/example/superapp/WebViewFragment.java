package com.example.superapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.HttpAuthHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;

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
        View view = inflater.inflate(R.layout.fragment_webview, container, false);

        WebView webView = view.findViewById(R.id.my_webview);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setDatabaseEnabled(true);


        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl(URL); // Загрузите нужный URL

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


        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm) {
                // Check if you have stored credentials for this host/realm

                //  Log.d(TAG, "your_server_host "+ host );
                //  Log.d(TAG, "your_server_realm "+ realm );

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
        });

        return view;
    }



}