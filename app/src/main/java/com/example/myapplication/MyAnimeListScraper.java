package com.example.myapplication;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MyAnimeListScraper {
    private static final String CLIENT_ID = "f6c98d51c291e2ed2b3b2046cd9fe9b7";
    private static final String ANIME_LIST_URL = "https://api.myanimelist.net/v2/users/{user_id}/animelist?status=watching";
    static List<String> getAnimeList() throws IOException {
        List<String> resultList = new ArrayList<>();
        OkHttpClient client = new OkHttpClient();
        String url = ANIME_LIST_URL.replace("{user_id}", "kissbenedek001");

        Request request = new Request.Builder()
                .url(url)
                .header("X-MAL-CLIENT-ID", MyAnimeListScraper.CLIENT_ID)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                System.err.println("Failed to get anime list: " + response);
                return null;
            }
            String responseBody = response.body().string();
            JsonObject jsonObject = JsonParser.parseString(responseBody).getAsJsonObject();
            JsonArray animeList = jsonObject.getAsJsonArray("data");

            for (int i = 0; i < animeList.size(); i++) {
                JsonObject anime = animeList.get(i).getAsJsonObject().getAsJsonObject("node");
                String title = anime.get("title").getAsString();
                resultList.add(title);
            }
        }
        return resultList;
    }
}