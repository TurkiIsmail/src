package com.example.babelio.models;

import java.io.Serializable;

/**
 * Book model class for storing book data
 */
public class Book implements Serializable {
    private String id;
    private String title;
    private String author;
    private String description;
    private String imageUrl;
    private double rating;
    private int reviewCount;
    private String genre;
    private long timestamp;

    public Book() {
    }

    public Book(String id, String title, String author, String description, String imageUrl) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.description = description;
        this.imageUrl = imageUrl;
        this.rating = 0.0;
        this.reviewCount = 0;
        this.genre = "Fiction";
        this.timestamp = System.currentTimeMillis();
    }

    public Book(String id, String title, String author, String description, String imageUrl, double rating, int reviewCount) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.description = description;
        this.imageUrl = imageUrl;
        this.rating = rating;
        this.reviewCount = reviewCount;
        this.genre = "Fiction";
        this.timestamp = System.currentTimeMillis();
    }

    public Book(String id, String title, String author, String genre, String description, String imageUrl) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.genre = genre != null ? genre : "Fiction";
        this.description = description;
        this.imageUrl = imageUrl;
        this.rating = 0.0;
        this.reviewCount = 0;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public int getReviewCount() {
        return reviewCount;
    }

    public void setReviewCount(int reviewCount) {
        this.reviewCount = reviewCount;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
