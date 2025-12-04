package com.example.superapp;
import android.graphics.Bitmap;


public interface ConnectionStatusListenerTCP {
    void onConnectionSuccessTCP();
    void onConnectionFailureTCP();
    void onImageReceivedTCP(Bitmap bitmap);
}