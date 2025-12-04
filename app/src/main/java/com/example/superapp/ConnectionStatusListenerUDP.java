package com.example.superapp;
import android.graphics.Bitmap;

public interface ConnectionStatusListenerUDP {
    void onImageReceivedUDP(Bitmap bitmap);
    void onProgressUpdateUDP(int progress, String statusText);
    void onStatusUpdateUDP(String statusText);
    void onErrorUDP(String errorMessage);
}
