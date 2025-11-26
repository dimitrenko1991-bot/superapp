package com.example.superapp;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.util.Log;
import android.widget.ProgressBar;
import java.io.DataInputStream;
import java.io.InputStream;
import android.graphics.drawable.Drawable;
import androidx.core.content.ContextCompat;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.Color;

public class TcpClient {

    private static final String TAG = "TcpClient";
    private final String serverIp;
    private final int serverPort;
    private OnMessageReceived messageListener = null;
    private boolean isRunning = false;
    private boolean needWrite= false;
    private PrintWriter out;
    private String msg;
    public ProgressBar myProgressBar3;
    private final Handler uiHandler;

    public Context context; // Сохраняем ссылку на контекст

    /**
     * Constructor for the TcpClient.
     * @param serverIp The IP address of the server.
     * @param serverPort The port number of the server.
     * @param listener The callback for received messages.
     */
    public TcpClient(String serverIp, int serverPort, Handler ui,  ProgressBar pb, Context context, OnMessageReceived listener) {
        this.serverIp = serverIp;
        this.serverPort = serverPort;
        this.messageListener = listener;
        this.myProgressBar3 = pb;
        this.uiHandler = ui;
        this.context = context;
    }

    /**
     * Sends a message to the server.
     * @param message The message to send.
     */

    public void sendMessage1(final String message) {

        msg = message;
        needWrite = true;

    }

    public void sendMessage(final String message) {
        Log.d(TAG, "sendMessage_ : " + message);
        if (out != null && !out.checkError()) {
            out.println(message);
            out.flush();
            Log.d(TAG, "write : " + message);
        }
    }

    /*
     * Stops the client connection.
     */

    public void stopClient() {
        isRunning = false;
        if (out != null) {
            out.flush();
            out.close();
        }
        out = null;
        BufferedReader in = null;
    }

    /**
     * Runs the client in a background thread.
     */
    public void run() {
        isRunning = true;

        try {
            // Retrieve the server's IP address
            InetAddress serverAddr = InetAddress.getByName(serverIp);
            Log.d(TAG, "Connecting to server...");

            // Create a new socket connection
            Socket socket = new Socket(serverAddr, serverPort);

            uiHandler.post(() ->
            {
                myProgressBar3.setBackgroundColor(0x6000FF00);
                myProgressBar3.setForeground(ContextCompat.getDrawable(context, R.drawable.ic_launcher_foreground));
            });

            try {
                // Set up the output stream for writing messages to the server
                out = new PrintWriter(socket.getOutputStream(), true);

                InputStream inputStream = socket.getInputStream();

                Log.d(TAG, "Connection established. Listening for messages...");

                // Loop while the connection is active
                while (isRunning) {

                    if (needWrite)
                    {
                        sendMessage(msg);
                        needWrite = false;
                    }

                    DataInputStream dis = new DataInputStream(inputStream);

                    // Сначала читаем размер изображения
                    int imageSize = dis.readInt();

                    byte[] imageBytes = new byte[imageSize];

                    // Читаем байты изображения
                    int bytesRead = 0;
                    while (bytesRead < imageSize)
                    {
                        bytesRead += dis.read(imageBytes, bytesRead, imageSize - bytesRead);
                    }

                    final Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageSize);

                    if (bitmap != null)
                    {
                        messageListener.messageReceived(bitmap);
                    }

                }
            } catch (Exception e) {
                Log.e(TAG, "Error in socket communication: " + e.getMessage());
                uiHandler.post(() ->
                {
                    myProgressBar3.setBackgroundColor(0x60FF0000);
                    Drawable emptyDrawable = new ColorDrawable(Color.TRANSPARENT);
                    myProgressBar3.setForeground(emptyDrawable);
                });
            } finally {
                socket.close();
                Log.d(TAG, "Socket closed.");
                uiHandler.post(() ->
                {
                    myProgressBar3.setBackgroundColor(0x60FF0000);
                    Drawable emptyDrawable = new ColorDrawable(Color.TRANSPARENT);
                    myProgressBar3.setForeground(emptyDrawable);
                });
            }
        } catch (Exception e) {
            Log.e(TAG, "Error connecting to server: " + e.getMessage());
            uiHandler.post(() ->
            {
                myProgressBar3.setBackgroundColor(0x60FF0000);
                Drawable emptyDrawable = new ColorDrawable(Color.TRANSPARENT);
                myProgressBar3.setForeground(emptyDrawable);
            });
        }
    }

    public interface OnMessageReceived {
        void messageReceived(Bitmap message);
    }
}