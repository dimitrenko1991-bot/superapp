package com.example.superapp;

import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import java.io.DataInputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.io.InputStream;

public class TcpClient {

    private static final String TAG = "TcpClient";
    private final String serverIp;
    private final int serverPort;
    private final ConnectionStatusListenerTCP statusListener;
    private boolean isRunning = false;
    private PrintWriter out;
    private Thread senderThread; // Ссылка на новый поток отправки
    // Используем потокобезопасную очередь для сообщений на отправку
    private final BlockingQueue<String> messageQueue = new LinkedBlockingQueue<>();

    public TcpClient(String serverIp, int serverPort, ConnectionStatusListenerTCP statusListener)
    {
        this.serverIp = serverIp;
        this.serverPort = serverPort;
        this.statusListener = statusListener;
    }
    public void sendMessage(final String message) {
        Log.d(TAG, "Queueing message for send: " + message);
        // offer() добавляет элемент, poll() извлекает
        messageQueue.offer(message);
    }
    public void stopClient() {
        isRunning = false;
        // Прерываем поток отправки, если он существует
        if (senderThread != null) {
            senderThread.interrupt();
        }
        // Очередь будет очищена или обработана при завершении работы сокета
    }
    public void run() {
        isRunning = true;
        Socket socket = null;
        DataInputStream dis = null;

        try {
            InetAddress serverAddr = InetAddress.getByName(serverIp);
            Log.d(TAG, "Connecting to server...");
            socket = new Socket(serverAddr, serverPort);
            statusListener.onConnectionSuccessTCP();

            // Инициализируем потоки ввода/вывода ОДИН раз
            out = new PrintWriter(socket.getOutputStream(), true);
            InputStream inputStream = socket.getInputStream();
            dis = new DataInputStream(inputStream);

            Log.d(TAG, "Connection established. Starting sender thread and listening for messages...");

            // --- ЗАПУСК НОВОГО ПОТОКА ДЛЯ ОТПРАВКИ ---
            senderThread = new Thread(new SenderTask());
            senderThread.start();
            // ----------------------------------------

            // Основной цикл теперь занимается ТОЛЬКО ЧТЕНИЕМ
            while (isRunning && !socket.isClosed()) {

                // Чтение данных (блокирующий вызов)
                int imageSize = dis.readInt();

                if (imageSize > 0) {
                    byte[] imageBytes = new byte[imageSize];
                    dis.readFully(imageBytes);

                    final Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageSize);

                    if (bitmap != null) {
                        statusListener.onImageReceivedTCP(bitmap);
                    } else {
                        Log.e(TAG, "Failed to decode bitmap from received data.");
                    }
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "Error in socket communication or connection: ", e);
            if (isRunning) {
                statusListener.onConnectionFailureTCP();
            }
        } finally {
            // Убеждаемся, что поток отправки тоже останавливается при закрытии сокета
            if (senderThread != null) {
                senderThread.interrupt();
            }
            // Закрытие ресурсов
            closeResources(socket);
            Log.d(TAG, "Socket closed. Client stopped.");
            isRunning = false;
            statusListener.onConnectionFailureTCP();
        }
    }

    // Вспомогательный метод для закрытия ресурсов
    private void closeResources(Socket socket) {
        if (out != null) {
            out.flush();
            out.close();
            out = null;
        }
        if (socket != null && !socket.isClosed()) {
            try {
                socket.close();
            } catch (Exception e) {
                Log.e(TAG, "Error closing socket: ", e);
            }
        }
    }

    /**
     * Внутренний класс (или Runnable) для выполнения задачи отправки сообщений.
     */
    private class SenderTask implements Runnable {
        @Override
        public void run() {
            try {
                while (isRunning && !Thread.currentThread().isInterrupted()) {
                    // take() блокирует поток до тех пор, пока в очереди не появится сообщение
                    String message = messageQueue.take();

                    if (out != null && !out.checkError()) {
                        out.println(message);
                        out.flush();
                        Log.d(TAG, "Sent message: " + message);
                    }
                }
            } catch (InterruptedException e) {
                // Поток был прерван (например, через stopClient())
                Log.d(TAG, "Sender thread interrupted.");
                Thread.currentThread().interrupt(); // Восстанавливаем флаг прерывания
            } catch (Exception e) {
                Log.e(TAG, "Error in sender thread: ", e);
            }
        }
    }

}