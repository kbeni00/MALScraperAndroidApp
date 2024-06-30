package com.example.myapplication;

import java.io.Serializable;

public class Episode implements Serializable {
    private String title;
    private String score;
    private String progress;

    public Episode(String title, String score, String progress) {
        this.title = title;
        this.score = score;
        this.progress = progress;
    }

    public String getTitle() {
        return title;
    }

    public String getEpisodeInfo() {
        return score;
    }

    public String getLink() {
        return progress;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Episode episode = (Episode) o;

        if (!title.equals(episode.title)) return false;
        if (!score.equals(episode.score)) return false;
        return progress.equals(episode.progress);
    }

    @Override
    public int hashCode() {
        int result = title.hashCode();
        result = 31 * result + score.hashCode();
        result = 31 * result + progress.hashCode();
        return result;
    }
}
