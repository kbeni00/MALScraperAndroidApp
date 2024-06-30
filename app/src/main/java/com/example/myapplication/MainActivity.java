package com.example.myapplication;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private static final String CHANNEL_ID = "web_change_channel";
    private static final String ACTION_UPDATE_EPISODES = "com.example.myapplication.UPDATE_EPISODES";
    private static final int SETTINGS_REQUEST_CODE = 1;
    private RecyclerView recyclerView;
    private EpisodeAdapter episodeAdapter;
    private List<String> animeList;
    private static final String PREFS_NAME = "RelevantAnimes";
    private static final String PREFS_KEY = "AnimeTitles";
    private final BroadcastReceiver episodeUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && ACTION_UPDATE_EPISODES.equals(intent.getAction())) {
                Set<Episode> newEpisodes = (Set<Episode>) intent.getSerializableExtra("newEpisodes");
                if (newEpisodes != null) {
                    animeList.clear();
                    for (Episode episode : newEpisodes) {
                        animeList.add(episode.getTitle());
                    }
                    episodeAdapter.notifyDataSetChanged();
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        animeList = new ArrayList<>();
        episodeAdapter = new EpisodeAdapter(animeList);
        recyclerView.setAdapter(episodeAdapter);

        findViewById(R.id.buttonSettings).setOnClickListener(v -> {
            startActivityForResult(new Intent(MainActivity.this, SettingsActivity.class), SETTINGS_REQUEST_CODE);
        });

        findViewById(R.id.buttonFetch).setOnClickListener(v -> {
            // Trigger a one-time worker to fetch episodes immediately
            OneTimeWorkRequest oneTimeWorkRequest = new OneTimeWorkRequest.Builder(WebPageCheckerWorker.class).build();
            WorkManager.getInstance(MainActivity.this).enqueue(oneTimeWorkRequest);
        });

        createNotificationChannel();

        PeriodicWorkRequest periodicWorkRequest = new PeriodicWorkRequest.Builder(WebPageCheckerWorker.class, 300, TimeUnit.SECONDS)
                .build();
        WorkManager.getInstance(this).enqueue(periodicWorkRequest);

        LocalBroadcastManager.getInstance(this).registerReceiver(episodeUpdateReceiver, new IntentFilter(ACTION_UPDATE_EPISODES));

        // Fetch the predefined anime list
        new FetchAnimeListTask().execute();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(episodeUpdateReceiver);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SETTINGS_REQUEST_CODE && resultCode == RESULT_OK) {
            // Trigger a one-time worker to check for episodes immediately
            OneTimeWorkRequest oneTimeWorkRequest = new OneTimeWorkRequest.Builder(WebPageCheckerWorker.class).build();
            WorkManager.getInstance(this).enqueue(oneTimeWorkRequest);
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Web Change Channel";
            String description = "Channel for web change notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private class FetchAnimeListTask extends AsyncTask<Void, Void, List<String>> {
        @Override
        protected List<String> doInBackground(Void... voids) {
            try {
                return MyAnimeListScraper.getAnimeList();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<String> result) {
            if (result != null) {
                animeList.clear();
                animeList.addAll(result);
                episodeAdapter.notifyDataSetChanged();
                saveToSharedPreferences(result);
            }
        }

        private void saveToSharedPreferences(List<String> animeTitles) {
            SharedPreferences prefs = getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            Set<String> relevantTitles = new HashSet<>(animeTitles);
            editor.putStringSet(PREFS_KEY, relevantTitles);
            editor.apply();
        }
    }
}
