package com.example.superapp;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.thegrizzlylabs.sardineandroid.DavResource;

import java.util.List;
import android.widget.AdapterView.OnItemClickListener;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

    private OnItemClickListener listener; // Объявляем слушатель
    public static List<DavResource> itemList;

    private static Webdav webdav;
    public MyAdapter(List<DavResource> itemList, Webdav webdav/*, OnItemClickListener listener*/) {
        this.itemList = itemList;
        this.listener = listener;
        this.webdav = webdav;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        DavResource item = itemList.get(position);
        holder.textViewItem.setText(item.getName());
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView textViewItem;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewItem = itemView.findViewById(R.id.textViewItem);

            // Устанавливаем слушатель нажатий на элемент
            itemView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    int position = getAdapterPosition();
                    String clickedItem = itemList.get(position).getName();
                    webdav.onClick(clickedItem);

                }
            });
        }
    }
}