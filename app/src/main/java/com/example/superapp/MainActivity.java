package com.example.superapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.SharedPreferences;

import android.os.Build;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;


public class MainActivity extends AppCompatActivity  {

    private static final String TAG = "MainActivity";
    private static String SERVER_IP;
    private static int SERVER_PORT_UDP;
    private static int SERVER_PORT_TCP;
    private static String URL_WEBDAV;
    private static String URL_RED;
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

        SERVER_IP = prefs.getString("dest_ip","");
        SERVER_PORT_UDP = prefs.getInt("port_udp_target",1);
        int SOURCE_PORT_UDP = prefs.getInt("port_udp_source", 1);
        URL_WEBDAV = prefs.getString("url_WEBDAV","");
        URL_RED = prefs.getString("url_RED","");
        URL_GREEN = prefs.getString("url_GREEN","");
        SERVER_PORT_TCP = prefs.getInt("port_tcp_server",1);

        textViewSpaceRed = findViewById(R.id.textViewSpaceRed);
        Button sendButton = findViewById(R.id.buttonRefresh);
        Button sendButtonClear= findViewById(R.id.buttonClear);

        RadioButton radioWEBDAV = findViewById(R.id.radioButtonWEBDAV);
        RadioButton radioCAM1 = findViewById(R.id.radioButtonCAM1);
        RadioButton radioCAM2 = findViewById(R.id.radioButtonCAM2);

        Button buttonWEBBROWSER = findViewById(R.id.buttonWEB);

        imageView = findViewById(R.id.imageView);

        progressBarFreeSpace = findViewById(R.id.progressBarFreeSpace);

        myProgressBarTCP =  findViewById(R.id.progressBarTCP);

        textViewLastUpdate = findViewById(R.id.TimeUpdate);

        context = this;

        executorService = Executors.newSingleThreadExecutor();

        // Создаём поток для UDP-коммуникации, передавая ему Handler для взаимодействия с UI
        udpThread = new UdpCommunicationThread(SOURCE_PORT_UDP, uiHandler, textViewSpaceRed, textViewLastUpdate, progressBarFreeSpace, imageView);
        udpThread.start();

        startTcpClient();

        checkAndRequestPermissions();

        radioWEBDAV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                radioCAM1.setChecked(false);
                radioCAM2.setChecked(false);

                replaceFragment(new RecyclerViewFragment(context));
            }
        });

        radioCAM1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                radioWEBDAV.setChecked(false);
                radioCAM2.setChecked(false);

                replaceFragment(new WebViewFragment(URL_RED, context));
            }
        });

        radioCAM2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                radioWEBDAV.setChecked(false);
                radioCAM1.setChecked(false);

                replaceFragment(new WebViewFragment(URL_GREEN, context));
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

        sendButton.setOnClickListener(new View.OnClickListener() {
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

                Drawable background = myProgressBarTCP.getBackground();

                if (background instanceof ColorDrawable && ((ColorDrawable) background).getColor() == 0x6000FF00) {
                    onStop();
                }
                else
                {
                    executorService = Executors.newSingleThreadExecutor();
                    startTcpClient();
                }
            }
        });

        sendButtonClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
          //      String message = "CL"; //0x02
                byte[] bufferPACK = {0x02};
                udpThread.sendMessage(bufferPACK, SERVER_IP, SERVER_PORT_UDP);
            }
        });

        sendButton.performClick();
        radioWEBDAV.performClick();

    }

    @SuppressLint("SetTextI18n")
    private void startTcpClient()
    {

        executorService.execute(() ->
        {
            try {
                tcpClient = new TcpClient(SERVER_IP, SERVER_PORT_TCP, uiHandler, myProgressBarTCP, context, message ->
                {
                    // Post UI updates to the main thread
                    uiHandler.post(() ->
                    {
                        //responseTextView.append("Server: " + message. + "\n");
                        //Log.d(TAG, "Message received: " + message.getByteCount());
                        imageView.setImageBitmap(message);

                        LocalTime currentTime = LocalTime.now();
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

                        textViewLastUpdate.setText(currentTime.format(formatter)+" (TCP)");
                    });
                });
                tcpClient.run();
            } catch (Exception e) {
                Log.e(TAG, "Ошибка в фоновом потоке при startTcpClient: " + e.getMessage());
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
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

        if (ContextCompat.checkSelfPermission(this, permissionToRequest) != PackageManager.PERMISSION_GRANTED)
        {
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
                Log.e(TAG, "Разрешение на запись в хранилище отклонено."+ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE));
            }
        }
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setReorderingAllowed(true);

        // Применение пользовательских анимаций для входа, выхода и возврата
        fragmentTransaction.setCustomAnimations(
                R.anim.slide_in_right, // enter
                R.anim.slide_out_left,  // exit
                R.anim.slide_in_left,  // popEnter
                R.anim.slide_out_right // popExit
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
        //Log.d(TAG, "onStart: Activity становится видимым");
        onStop();
        executorService = Executors.newSingleThreadExecutor();
        startTcpClient();
    }



}