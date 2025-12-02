package com.example.superapp;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.recyclerview.widget.RecyclerView;

import androidx.fragment.app.Fragment;

import androidx.recyclerview.widget.LinearLayoutManager;

public class RecyclerViewFragment extends Fragment {

    private final Handler uiHandler = new Handler(Looper.getMainLooper());

    public RecyclerViewFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {

        View view = inflater.inflate(R.layout.fragment_recyclerview, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        Webdav webdav = new Webdav(uiHandler, recyclerView, this,  requireContext());

        webdav.start();

        setupOnBackPressedCallback();

        return view;
    }

    public void onLoad()
    {

    }

    // Вынесем логику обработки жеста "назад" в отдельный метод
    private void setupOnBackPressedCallback() {
        // Создаем callback, который перехватывает нажатие/жест назад
        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled */) {
            @Override
            public void handleOnBackPressed() {
                // Здесь мы блокируем стандартное поведение "назад".
                // Вы можете добавить сюда любую свою логику.

                // Например, показать сообщение пользователю:
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Действие 'Назад' заблокировано в этом режиме.", Toast.LENGTH_SHORT).show();
                }

                // Если WebView может вернуться на предыдущую страницу внутри себя:
                /*
                if (webView.canGoBack()) {
                    webView.goBack();
                } else {
                    // Если WebView не может вернуться, вы можете вызвать системный "назад"
                    // setEnabled(false); // Сначала отключаем callback
                    // requireActivity().onBackPressed(); // Затем вызываем стандартное действие
                }
                */
            }
        };

        // Регистрируем этот callback в диспетчере нажатий кнопки "назад" вашей активности
        // Привязываем его к жизненному циклу представления фрагмента (getViewLifecycleOwner()),
        // чтобы он автоматически удалялся, когда фрагмент исчезает.
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), callback);
    }

}
