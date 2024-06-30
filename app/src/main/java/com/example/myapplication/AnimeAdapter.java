package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AnimeAdapter extends RecyclerView.Adapter<AnimeAdapter.AnimeViewHolder> {

    private List<String> animeList;
    private OnItemLongClickListener longClickListener;

    public AnimeAdapter(List<String> animeList, OnItemLongClickListener longClickListener) {
        this.animeList = animeList;
        this.longClickListener = longClickListener;
    }

    @NonNull
    @Override
    public AnimeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.anime_item, parent, false);
        return new AnimeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AnimeViewHolder holder, int position) {
        String animeTitle = animeList.get(position);
        holder.title.setText(animeTitle);
        holder.itemView.setOnLongClickListener(v -> {
            longClickListener.onItemLongClick(animeTitle);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return animeList.size();
    }

    public static class AnimeViewHolder extends RecyclerView.ViewHolder {
        TextView title;

        public AnimeViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tv_anime_title);
        }
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(String animeTitle);
 
    }
}
