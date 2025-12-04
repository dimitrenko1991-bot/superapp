package com.example.superapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.widget.Button;
import android.view.View;
import android.util.Log;

import android.widget.TextView;
import android.os.Handler;
import android.os.Looper;

import android.widget.ProgressBar;

import android.widget.ImageView;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.Context;

import android.content.Intent;
import android.net.Uri;

import android.widget.RadioButton;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.SharedPreferences;

import android.os.Build;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.content.pm.ActivityInfo;

import android.graphics.Color;


public class MainActivity extends AppCompatActivity implements ConnectionStatusListenerTCP, ConnectionStatusListenerUDP {

    private static final String TAG = "MainActivity";
    private static String SERVER_IP;
    private static int SERVER_PORT_UDP;
    private static int SERVER_PORT_TCP;
    private static String URL_WEBDAV;
    private String URL_RED;
    private static String URL_GREEN;
    private final Handler uiHandler = new Handler(Looper.getMainLooper());
    public TextView textViewSpaceRed;
    private ImageView imageView;
    public ProgressBar progressBarFreeSpace;
    public ProgressBar myProgressBarTCP;
    private UdpCommunicationThread udpThread;
    private TcpClient tcpClient;
    private ExecutorService executorService;
    private TextView textViewLastUpdate;

    public Context context;
    private static final int PERMISSION_REQUEST_CODE = 101;

    RadioButton radioWEBDAV;
    RadioButton radioCAM1;
    RadioButton radioCAM2;

    Button buttonRefreshSpace;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Устанавливаем ориентацию только в портретную
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // 1. Get the SharedPreferences instance
        SharedPreferences prefs = getSharedPreferences("settings", Context.MODE_PRIVATE);
/*
        // 2. Create an editor to write data
        SharedPreferences.Editor editor = prefs.edit();

        // 3. Add data with a key and value
        editor.putString("url_WEBDAV", "URL");
        editor.putString("username_WEBDAV", "USERNAME");
        editor.putString("password_WEBDAV", "PASSWORD");

        editor.putString("url_RED", "URL");
        editor.putString("username_RED", "USERNAME");
        editor.putString("password_RED", "PASSWORD");

        editor.putString("url_GREEN", "URL");
        editor.putString("username_GREEN", "USERNAME");
        editor.putString("password_GREEN", "PASSWORD");

        editor.putInt("port_udp_target", 1);
        editor.putInt("port_udp_source", 1);

        editor.putInt("port_tcp_server", 1);

        editor.putString("dest_ip", "0.0.0.0");

        // 4. Commit the changes to save the data
        editor.apply(); // or editor.commit();
*/

        SERVER_IP = prefs.getString("dest_ip", "");
        SERVER_PORT_UDP = prefs.getInt("port_udp_target", 1);
        int SOURCE_PORT_UDP = prefs.getInt("port_udp_source", 1);
        URL_WEBDAV = prefs.getString("url_WEBDAV", "");
        URL_RED = prefs.getString("url_RED", "");
        URL_GREEN = prefs.getString("url_GREEN", "");
        SERVER_PORT_TCP = prefs.getInt("port_tcp_server", 1);

        textViewSpaceRed = findViewById(R.id.textViewSpaceRed);
        Button buttonClearSpace = findViewById(R.id.buttonClear);
        buttonRefreshSpace = findViewById(R.id.buttonRefresh);

        radioWEBDAV = findViewById(R.id.radioButtonWEBDAV);
        radioCAM1 = findViewById(R.id.radioButtonCAM1);
        radioCAM2 = findViewById(R.id.radioButtonCAM2);

        Button buttonWEBBROWSER = findViewById(R.id.buttonWEB);

        imageView = findViewById(R.id.imageView);

        progressBarFreeSpace = findViewById(R.id.progressBarFreeSpace);

        myProgressBarTCP = findViewById(R.id.progressBarTCP);

        textViewLastUpdate = findViewById(R.id.TimeUpdate);

        context = this;

        executorService = Executors.newSingleThreadExecutor();

        udpThread = new UdpCommunicationThread(SOURCE_PORT_UDP, this);

        udpThread.start();

        checkAndRequestPermissions();

        radioWEBDAV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                radioCAM1.setChecked(false);
                radioCAM2.setChecked(false);

                replaceFragment(new RecyclerViewFragment());
            }
        });

        radioCAM1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                radioWEBDAV.setChecked(false);
                radioCAM2.setChecked(false);

                WebViewFragment webViewFragment = WebViewFragment.newInstance(URL_RED);
                replaceFragment(webViewFragment);
            }
        });

        radioCAM2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                radioWEBDAV.setChecked(false);
                radioCAM1.setChecked(false);

                WebViewFragment webViewFragment = WebViewFragment.newInstance(URL_GREEN);
                replaceFragment(webViewFragment);
            }
        });

        buttonWEBBROWSER.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Uri address;
                if (radioWEBDAV.isChecked())
                    address = Uri.parse(URL_WEBDAV);
                else if (radioCAM1.isChecked())
                    address = Uri.parse(URL_RED);
                else if (radioCAM2.isChecked())
                    address = Uri.parse(URL_GREEN);
                else
                    return;
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, address);
                startActivity(browserIntent);
            }
        });

        buttonRefreshSpace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //String message = "Remote"; //0x10
                byte[] bufferPACK = {0x10};
                udpThread.sendMessage(bufferPACK, SERVER_IP, SERVER_PORT_UDP);

            }
        });

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                byte[] bufferPACK = {0x11}; //request img on UDP
                udpThread.sendMessage(bufferPACK, SERVER_IP, SERVER_PORT_UDP);
            }
        });

        myProgressBarTCP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myProgressBarTCP.setEnabled(false);

                myProgressBarTCP.setAlpha(0.05f);

                Drawable background = myProgressBarTCP.getBackground();
                if (background instanceof ColorDrawable && ((ColorDrawable) background).getColor() == 0x3000FF00)
                {
                    stopTcpClient();
                }
                else
                {
                    executorService = Executors.newSingleThreadExecutor();
                    startTcpClient();
                }


            }
        });

        buttonClearSpace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //      String message = "CL"; //0x02
                byte[] bufferPACK = {0x02};
                udpThread.sendMessage(bufferPACK, SERVER_IP, SERVER_PORT_UDP);
            }
        });

       // buttonRefreshSpace.performClick();
        radioWEBDAV.performClick();

    }

    @SuppressLint("SetTextI18n")
    private void startTcpClient() {

        executorService.execute(() ->
        {
            try {
                tcpClient = new TcpClient(SERVER_IP, SERVER_PORT_TCP, this);
                tcpClient.run();
            } catch (Exception e) {
                Log.e(TAG, "Ошибка в фоновом потоке при startTcpClient: " + e.getMessage());
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopTcpClient();
    }

    protected void stopTcpClient()
    {
        if (tcpClient != null) {
            tcpClient.stopClient();
        }
        if (executorService != null) {
            executorService.shutdown();
        }
    }


    private void checkAndRequestPermissions() {
        // Определяем, какое разрешение запрашивать
        String permissionToRequest;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU /* Android 13 */) {
            permissionToRequest = Manifest.permission.READ_MEDIA_VIDEO;
        } else {
            permissionToRequest = Manifest.permission.READ_EXTERNAL_STORAGE;
        }

        if (ContextCompat.checkSelfPermission(this, permissionToRequest) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{permissionToRequest},
                    PERMISSION_REQUEST_CODE);
            Log.e(TAG, "Разрешение на запись в хранилище не разрешено. Запрос");
        } else {
            //playVideoFromDownloadFolder();
            Log.e(TAG, "Разрешение на запись в хранилище предоставлено");
        }
    }

    // Обработка результата запроса разрешений
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Разрешение предоставлено
                //startDownload(getDavResources()); // Вызов скачивания
                Log.e(TAG, "Разрешение на запись в хранилище разрешено.");
            } else {
                // Разрешение отклонено
                Log.e(TAG, "Разрешение на запись в хранилище отклонено." + ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE));
            }
        }
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setReorderingAllowed(true);

        // Применение пользовательских анимаций для входа, выхода и возврата
        fragmentTransaction.setCustomAnimations(
                //    R.anim.slide_in_right, // enter
                //    R.anim.slide_out_left,  // exit
                R.anim.slide_in,  // popEnter
                R.anim.slide_out // popExit
        );

        // Заменяем текущий фрагмент в контейнере R.id.fragment_placeholder
        // на новый экземпляр fragment
        fragmentTransaction.replace(R.id.fragmentContainerView, fragment);

        // Добавляем транзакцию в стек возврата, чтобы можно было нажать "Назад"
        fragmentTransaction.addToBackStack(null);

        // Применяем изменения
        fragmentTransaction.commit();
    }


    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: Activity становится видимым");
        stopTcpClient();
        executorService = Executors.newSingleThreadExecutor();
        buttonRefreshSpace.performClick();
        startTcpClient();

        if (radioWEBDAV.isChecked())
        {}
           // radioWEBDAV.performClick();
        else if (radioCAM1.isChecked())
            radioCAM1.performClick();
        else if (radioCAM2.isChecked())
            radioCAM2.performClick();
    }

    @Override
    public void onConnectionSuccessTCP() {
        // Здесь мы используем НАСТОЯЩИЙ Context Activity
        // для обновления UI безопасно
        // myProgressBarTCP.setBackgroundColor(0x6000FF00);
        // myProgressBarTCP.setForeground(ContextCompat.getDrawable(this, R.drawable.ic_launcher_foreground));
        uiHandler.post(() ->
        {
            myProgressBarTCP.setBackgroundColor(0x3000FF00);
            Drawable emptyDrawable = new ColorDrawable(Color.TRANSPARENT);
            myProgressBarTCP.setForeground(emptyDrawable);
            myProgressBarTCP.setEnabled(true);
            myProgressBarTCP.setAlpha(1.0f);
        });
    }

    @Override
    public void onConnectionFailureTCP() {
        // Здесь мы используем НАСТОЯЩИЙ Context Activity
        // для обновления UI безопасно
        //myProgressBarTCP.setBackgroundColor(0x6000FF00);
        //myProgressBarTCP.setForeground(ContextCompat.getDrawable(this, R.drawable.ic_launcher_foreground));

        uiHandler.post(() ->
        {
            myProgressBarTCP.setBackgroundColor(0x30FF0000);
            Drawable emptyDrawable = new ColorDrawable(Color.TRANSPARENT);
            myProgressBarTCP.setForeground(emptyDrawable);
            myProgressBarTCP.setEnabled(true);
            myProgressBarTCP.setAlpha(1.0f);
        });
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onImageReceivedTCP(Bitmap bitmap) {
        uiHandler.post(() ->
        {
            //responseTextView.append("Server: " + message. + "\n");
            //Log.d(TAG, "Message received: " + message.getByteCount());
            imageView.setImageBitmap(bitmap);

            LocalTime currentTime = LocalTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

            textViewLastUpdate.setText(currentTime.format(formatter) + " (TCP)");
        });
    }


    @Override
    public void onProgressUpdateUDP(int progress, String statusText) {
        uiHandler.post(() -> {
            progressBarFreeSpace.setProgress(progress, true);
            textViewSpaceRed.setText(statusText);
        });
    }

    @Override
    public void onImageReceivedUDP(Bitmap bitmap) {
        uiHandler.post(() -> {
            imageView.setImageBitmap(bitmap);
            LocalTime currentTime = LocalTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
            textViewLastUpdate.setText(currentTime.format(formatter) + " (UDP)");
        });

    }

    @Override
    public void onStatusUpdateUDP(String statusText) {
        // Обработка общего статуса, если нужно
    }

    @Override
    public void onErrorUDP(String errorMessage) {
        uiHandler.post(() -> {
            Log.e("MainActivity", "UDP Error: " + errorMessage);
            // Показать Toast или AlertDialog
        });
    }

}