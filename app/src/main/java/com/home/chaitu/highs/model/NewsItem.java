package com.home.chaitu.highs.model;

import java.net.URL;

/**
 * Created by chaitu on 14-08-2016.
 */

public class NewsItem {
    private String title, description, date, imageUrl;
    private URL url;

    public NewsItem() {
    }


    public NewsItem(String title, String description, String date, URL url, String imageUrl) {
        this.title = title;
        this.description = description;
        this.date = date;
        this.url = url;
        this.imageUrl = imageUrl;
    }

    public NewsItem(String title, String description, String imageUrl) {
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public URL getUrl() {
        return url;
    }

    public void setURL(URL url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public NewsItem copy() {
        NewsItem copy = new NewsItem();
        copy.title = title;
        copy.description = description;
        copy.date = date;
        copy.imageUrl = imageUrl;
        copy.url = url;
        return copy;
    }
}