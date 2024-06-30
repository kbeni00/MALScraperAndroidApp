package com.example.myapplication;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SettingsActivity extends AppCompatActivity implements AnimeAdapter.OnItemLongClickListener {

    private EditText editText;
    private Button button;
    private RecyclerView recyclerView;
    private AnimeAdapter animeAdapter;
    private List<String> animeList;
    private static final String PREFS_NAME = "RelevantAnimes";
    private static final String PREFS_KEY = "AnimeTitles";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        editText = findViewById(R.id.editTextAnimeTitle);
        button = findViewById(R.id.buttonSave);
        recyclerView = findViewById(R.id.recyclerViewAnimes);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        animeList = new ArrayList<>();
        animeAdapter = new AnimeAdapter(animeList, this);
        recyclerView.setAdapter(animeAdapter);

        loadSavedAnimes();

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveAnimeTitle(editText.getText().toString());
            }
        });
    }

    private void loadSavedAnimes() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        Set<String> titles = prefs.getStringSet(PREFS_KEY, new HashSet<>());
        animeList.clear();
        animeList.addAll(titles);
        animeAdapter.notifyDataSetChanged();
    }

    private void saveAnimeTitle(String title) {
        if (!title.isEmpty()) {
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            Set<String> titles = prefs.getStringSet(PREFS_KEY, new HashSet<>());
            titles.add(title);
            prefs.edit().putStringSet(PREFS_KEY, titles).apply();
            editText.setText("");
            loadSavedAnimes();
            Toast.makeText(this, "Anime title saved", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Anime title cannot be empty", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onItemLongClick(String animeTitle) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Anime")
                .setMessage("Are you sure you want to delete " + animeTitle + "?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    deleteAnimeTitle(animeTitle);
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void deleteAnimeTitle(String title) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        Set<String> titles = prefs.getStringSet(PREFS_KEY, new HashSet<>());
        if (titles.remove(title)) {
            prefs.edit().putStringSet(PREFS_KEY, titles).apply();
            loadSavedAnimes();
            Toast.makeText(this, "Anime title deleted", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Anime title not found", Toast.LENGTH_SHORT).show();
        }
    }
}
