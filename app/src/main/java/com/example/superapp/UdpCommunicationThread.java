package com.example.superapp;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import java.util.Arrays;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ProgressBar;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.thegrizzlylabs.sardineandroid.DavResource;
import com.thegrizzlylabs.sardineandroid.Sardine;
import com.thegrizzlylabs.sardineandroid.impl.OkHttpSardine;

import java.io.ByteArrayInputStream;

import java.lang.reflect.Field;

import java.util.Base64;
import java.util.List;


public class UdpCommunicationThread extends Thread {
    private static final String TAG = "UdpCommunication";
    private final Handler uiHandler;
    private final int port;
    private DatagramSocket socket;
    private volatile boolean running = true;
    public TextView textViewSpaceRed;
    public TextView textViewLastUpdate;
    public ProgressBar myProgressBar;
    private final ImageView imageView;

    public UdpCommunicationThread(int port, Handler handler, TextView textViewSpaceRed, TextView textViewLastUpdate, ProgressBar myProgressBar, ImageView imageView) {
        this.port = port;
        this.uiHandler = handler;
        this.textViewSpaceRed = textViewSpaceRed;
        this.textViewLastUpdate = textViewLastUpdate;
        this.myProgressBar = myProgressBar;
        this.imageView = imageView;
    }

    @Override
    public void run() {
        try {

            socket = new DatagramSocket(port);

           // byte[] buffer = new byte[65507];
            byte[] bufferSHARE = new byte[1000000];
            byte[] bufferPACK = new byte[1472];
            int pos = 0;
            int totalLength = 0;
            int headerSize=1;

            while (running) {
                // Приём пакета
                DatagramPacket packet = new DatagramPacket(bufferPACK, bufferPACK.length);
                socket.receive(packet); // Блокирующая операция

                byte[] packetData = Arrays.copyOf(packet.getData(), packet.getLength());

                // Считываем заголовок
              //  long messageId = getLong(packetData, 0);
              //  int totalChunks = getInt(packetData, 8);
              //  int chunkIndex = getInt(packetData, 12);


                if ((packetData[0]==0x51) || (packetData[0]==0x52))
                {

                    System.arraycopy(packetData, headerSize, bufferSHARE, pos, packetData.length - headerSize);

                    pos += packetData.length - headerSize;
                    totalLength += packetData.length - headerSize;

                    Log.d(TAG, "Received size " + packet.getLength() + "have " + totalLength);

                    if (packetData.length != 1472)
                    {
                        final Bitmap bitmap = BitmapFactory.decodeByteArray(bufferSHARE, 0, totalLength);

                        if (bitmap != null) {

                            Log.d(TAG, "Received Bitmap: true " + totalLength);

                            uiHandler.post(() ->
                            {
                                imageView.setImageBitmap(bitmap);

                                LocalTime currentTime = LocalTime.now();
                                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

                                textViewLastUpdate.setText(currentTime.format(formatter)+" (UDP)");

                            });

                        } else {
                            Log.d(TAG, "Received Bitmap: false " + totalLength);
                            //  String arrayString = Arrays.toString(bufferSHARE);
                            //  Log.d("TAG", "Byte array: " + arrayString);

                        /*
                        StringBuilder hexStringBuilder1 = new StringBuilder();
                        for (byte b : bufferSHARE) {
                            hexStringBuilder1.append(String.format("%02X", b));
                        }

                        Log.d("MyByteHex", hexStringBuilder1.toString());
                         */
                        }

                        pos = 0;
                        totalLength = 0;

                    }
                }
                else if (packetData[0]==0x53)
                {
                    // Отправка сообщения в UI-поток
                    uiHandler.post(() ->
                    {
                        // Обновление UI-элементов, например, TextView
                        // TextView.append("Получено: " + receivedMessage + "\n");
                        // receivedMessagesTextView.setText(receivedMessage);

                        ByteBuffer byteBuffer = ByteBuffer.wrap(bufferPACK);
                        int receivedNumber = byteBuffer.getInt(1);

                        myProgressBar.setProgress(receivedNumber, true);

                        LocalTime currentTime = LocalTime.now();
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

                        textViewSpaceRed.setText(currentTime.format(formatter)+": "+Integer.toString(receivedNumber)+ "%");

                        //     Button sendButton = findViewById(R.id.send_button); // Убедитесь, что у вас есть кнопка с этим ID в разметке
                    });
                }

                //        String receivedMessage = new String(packet.getData(), 0, packet.getLength());
                //      Log.d(TAG, "Received: " + receivedMessage);

            }
        } catch (SocketException e) {
            if (running) {
                Log.e(TAG, "Socket error", e);
            }
        } catch (IOException e) {
            Log.e(TAG, "IO error", e);
        } finally {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        }
    }

    public void sendMessage(byte[] messageBytes, String targetIp, int targetPort) {
        new Thread(() -> {
            try {
                if (socket != null && !socket.isClosed()) {
                    InetAddress address = InetAddress.getByName(targetIp);
                    DatagramPacket packet = new DatagramPacket(messageBytes, messageBytes.length, address, targetPort);
                    socket.send(packet);
             //       Log.d(TAG, "Sent: " + message);
                }
            } catch (UnknownHostException e) {
                Log.e(TAG, "Unknown host", e);
            } catch (IOException e) {
                Log.e(TAG, "Send failed", e);
            }
        }).start();
    }

    public void stopCommunication() {
        running = false;
        if (socket != null) {
            socket.close(); // Закрытие сокета разблокирует receive()
        }
    }
}
