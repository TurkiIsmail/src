package com.example.babelio.models;

import java.io.Serializable;

/**
 * Comment model class for storing book reviews/comments
 */
public class Comment implements Serializable {
    private String commentId;
    private String bookId;
    private String userId;
    private String userName;
    private String userProfileImage;
    private String text;
    private double rating;
    private long timestamp;

    public Comment() {
    }

    public Comment(String bookId, String userId, String userName, String text, double rating) {
        this.bookId = bookId;
        this.userId = userId;
        this.userName = userName;
        this.text = text;
        this.rating = rating;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserProfileImage() {
        return userProfileImage;
    }

    public void setUserProfileImage(String userProfileImage) {
        this.userProfileImage = userProfileImage;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
