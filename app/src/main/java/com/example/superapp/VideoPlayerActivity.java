package com.example.superapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;
import android.util.Log;
import android.widget.Button;

public class VideoPlayerActivity extends AppCompatActivity {

    public static final String EXTRA_VIDEO_PATH = "extra_video_path";
    private VideoView videoView;
    private Uri videoUri;
    private boolean saveFile = false;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        videoView = findViewById(R.id.videoViewPlayer);

        Button buttonSaveFile = findViewById(R.id.buttonDownload);
        Button buttonExit = findViewById(R.id.buttonExit);

        // 1. Получаем путь к видео из Intent
        String videoPath = getIntent().getStringExtra(EXTRA_VIDEO_PATH);

        if (videoPath != null && !videoPath.isEmpty()) {
            videoUri = Uri.parse(videoPath);

            Log.e("VideoPlayerActivity", "Попытка воспроизведения URI: " + videoUri);
            videoView.setVideoURI(videoUri);

            // 2. Добавляем контроллеры (play/pause/progress bar)
            MediaController mediaController = new MediaController(this);
            mediaController.setAnchorView(videoView);
            videoView.setMediaController(mediaController);

            // 3. Запускаем воспроизведение
            videoView.start();
        } else {
            // Если путь не был передан, можно закрыть активность или показать ошибку
            finish();
        }

        // Опционально: Закрыть активность после завершения видео
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                // finish();
            }
        });

        context = this;

        buttonSaveFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveFile = true;
                Toast.makeText(context, "Файл сохранен", Toast.LENGTH_SHORT).show();
            }
        });

        buttonExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Останавливаем воспроизведение при сворачивании приложения
        if (videoView.isPlaying()) {
            videoView.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Освобождаем ресурсы VideoView
        videoView.stopPlayback();
        if (!saveFile)
        {
            getContentResolver().delete(videoUri, null, null);
            Toast.makeText(context, "Файл не сохранен", Toast.LENGTH_SHORT).show();
        }
    }
}