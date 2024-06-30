package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WebPageCheckerWorker extends Worker {

    private static final String URL = "https://aniwatchtv.to/home";
    private static final String CHANNEL_ID = "web_change_channel";
    private static final String ACTION_UPDATE_EPISODES = "com.example.myapplication.UPDATE_EPISODES";
    private static final String PREFS_NAME = "RelevantAnimes";
    private static final String PREFS_KEY = "AnimeTitles";
    private Set<Episode> currentEpisodes = new HashSet<>();

    public WebPageCheckerWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            Document document = Jsoup.connect(URL).get();
            Element filmListWrap = document.selectFirst("div.film_list-wrap");

            if (filmListWrap != null) {
                Set<Episode> newEpisodes = new HashSet<>();
                Elements episodeElements = filmListWrap.select("div.flw-item");

                for (Element element : episodeElements) {
                    String title = element.select("h3.film-name > a").attr("title");
                    String link = element.select("h3.film-name > a").attr("href");
                    String episodeInfo = element.select("div.fd-item > span").text();

                    Episode episode = new Episode(title, link, episodeInfo);
                    if (isRelevant(episode)) {
                        newEpisodes.add(episode);
                    }
                }

                if (hasNewRelevantEpisodes(newEpisodes)) {
                    currentEpisodes = newEpisodes;
                    sendNotification("New Relevant Episode", "A new episode of a relevant anime is out!");
                    sendEpisodeUpdateBroadcast(newEpisodes);
                }
            }
            return Result.success();
        } catch (IOException e) {
            e.printStackTrace();
            return Result.retry();
        }
    }

    private boolean hasNewRelevantEpisodes(Set<Episode> newEpisodes) {
        for (Episode newEpisode : newEpisodes) {
            if (!currentEpisodes.contains(newEpisode) && isRelevant(newEpisode)) {
                return true;
            }
        }
        return false;
    }
    private boolean isRelevant(Episode episode) {
        SharedPreferences prefs = getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Set<String> relevantTitles = prefs.getStringSet(PREFS_KEY, new HashSet<>());
        String episodeTitle = episode.getTitle().toLowerCase();
        for (String relevantTitle : relevantTitles) {
            if (episodeTitle.contains(relevantTitle.toLowerCase())) {
                return true;
            }
        }
        return false;
    }



    private void sendNotification(String title, String message) {
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        Notification notification = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setSound(soundUri)
                .setAutoCancel(true)
                .build();

        notificationManager.notify(1, notification);
    }

    private void sendEpisodeUpdateBroadcast(Set<Episode> newEpisodes) {
        Intent intent = new Intent(ACTION_UPDATE_EPISODES);
        intent.putExtra("newEpisodes", (Serializable) newEpisodes);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }
}
