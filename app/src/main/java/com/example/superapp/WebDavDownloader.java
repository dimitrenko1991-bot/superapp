package com.example.superapp;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import com.thegrizzlylabs.sardineandroid.DavResource;
import com.thegrizzlylabs.sardineandroid.Sardine;
import com.thegrizzlylabs.sardineandroid.impl.OkHttpSardine;
//import com.thegrizzlylabs.sardineandroid.SardineFactory;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import android.provider.MediaStore;
import android.os.Build;
import android.widget.Toast;

import android.content.Intent;
import java.io.FileNotFoundException;

import android.content.ContentResolver;
import android.database.Cursor;

public class WebDavDownloader {

    private static final String TAG = "WebDavDownloader";

    private Handler uiHandler;

    Context context;

    /**
     * Скачивает указанный файл с WebDAV-сервера.
     *
     * @param resources      Список ресурсов, полученный с сервера.
     * @param username       Имя пользователя для WebDAV.
     * @param password       Пароль для WebDAV.
     * @param targetFileName Имя файла, который нужно скачать.
     * @param context        Контекст приложения для доступа к файловой системе.
     */
    public void downloadFile(List<DavResource> resources, String url, String username, String password, String targetFileName, Context context, Handler uiHandler) {
        // Создание экземпляра Sardine должно выполняться в фоновом потоке.

        this.uiHandler = uiHandler;
        this.context = context;

        new Thread(() -> {
            try {

                ContentResolver resolver = context.getContentResolver();

                ContentValues contentValues = new ContentValues();

                contentValues.put(MediaStore.Downloads.DISPLAY_NAME, targetFileName);
               // contentValues.put(MediaStore.Downloads.MIME_TYPE, mimeType);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
                }

                Uri uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues);

                // 1. Создание экземпляра Sardine с учетными данными

                Sardine sardine = new OkHttpSardine();
                sardine.setCredentials(username, password);


                // 2. Поиск нужного файла в списке ресурсов
                DavResource fileToDownload = null;
                for (DavResource resource : resources) {
                    if (resource.getName().equalsIgnoreCase(targetFileName) && !resource.isDirectory()) {
                        fileToDownload = resource;
                        break;
                    }
                }

                if (fileToDownload != null)
                {

                    // 3. Получение InputStream для скачиваемого файла
                    InputStream inputStream = sardine.get(url+"/"+fileToDownload.getName());

                    // 4. Запись файла в локальное хранилище
                   // File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

                    if (uri != null) {
                        try (OutputStream outputStream = resolver.openOutputStream(uri))
                        {
                            if (outputStream != null)
                            {

                                uiHandler.post(() ->
                                {
                                    Toast.makeText(context, "Начало скачивания файла " + targetFileName, Toast.LENGTH_SHORT).show();
                                });

                                byte[] buffer = new byte[4096];
                                int bytesRead;
                                while ((bytesRead = inputStream.read(buffer)) != -1) {
                                    outputStream.write(buffer, 0, bytesRead);
                                }

                                uiHandler.post(() ->
                                {
                                    //Toast.makeText(context, "Файл успешно сохранен в папку Загрузки", Toast.LENGTH_SHORT).show();

                                 //   Log.e("WebDavDownloader videoUrl3 ", getVideoUriFromDownloadFolder(targetFileName));

                                    // Если видео в ресурсах raw (R.raw.my_video):
                                    // String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.my_video;

                                    // Запускаем новую активность и передаем путь

                                    try {
                                        // Попытка открыть InputStream как способ проверки существования данных
                                        InputStream inputStream1 = context.getContentResolver().openInputStream(uri);
                                        if (inputStream1 != null) {
                                            inputStream1.close();
                                            // Данные существуют, можно воспроизводить
                                            //Log.e(TAG, "URI подтвержден, воспроизведение начато: " + uri);
                                            Intent intent = new Intent(context, VideoPlayerActivity.class);
                                            intent.putExtra(VideoPlayerActivity.EXTRA_VIDEO_PATH, uri.toString());
                                            context.startActivity(intent);
                                        }
                                        else
                                        {
                                            Log.e(TAG, "Данные по URI не найдены: " + uri);
                                            Toast.makeText(context, "Данные по URI не найдены", Toast.LENGTH_SHORT).show();
                                        }
                                    } catch (FileNotFoundException e)
                                    {
                                        Log.e(TAG, "Файл не найден по URI: " + uri, e);
                                        Toast.makeText(context, "Данные по URI не найдены", Toast.LENGTH_SHORT).show();
                                    } catch (IOException e)
                                    {
                                        Log.e(TAG, "Ошибка ввода-вывода при проверке URI: " + uri, e);
                                        Toast.makeText(context, "Данные по URI не найдены", Toast.LENGTH_SHORT).show();
                                    }


                                });

                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            uiHandler.post(() ->
                            {
                                Toast.makeText(context, "Ошибка при сохранении файла: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            });

                        }
                    } else
                    {
                        uiHandler.post(() ->
                        {
                            Toast.makeText(context, "Не удалось получить Uri для сохранения файла", Toast.LENGTH_LONG).show();
                        });
                    }


                    /*

                    Log.e("WebDavDownloader ",".....5 "+downloadsDir.toString());

                    if (!downloadsDir.exists()) {
                        downloadsDir.mkdirs();
                    }
                    File localFile = new File(downloadsDir, targetFileName);

                    try (OutputStream outputStream = new FileOutputStream(localFile)) {
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                        }
                        Log.d(TAG, "Файл " + targetFileName + " успешно скачан в " + localFile.getAbsolutePath());
                    } catch (IOException e) {
                        Log.e(TAG, "Ошибка при записи файла: " + e.getMessage());
                    } finally {
                        try {
                            inputStream.close();
                        } catch (IOException e) {
                            Log.e(TAG, "Ошибка при закрытии потока: " + e.getMessage());
                        }
                    }

                    */

                } else {
                    Log.d(TAG, "Файл " + targetFileName + " не найден в списке.");
                }

            } catch (IOException e) {
                Log.e(TAG, "Ошибка при скачивании файла: " + e.getMessage());
            }
        }).start();
    }

}