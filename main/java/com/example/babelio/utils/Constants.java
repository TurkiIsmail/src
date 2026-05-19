package com.example.babelio.utils;

/**
 * Constants for the application
 */
public class Constants {
    // Firebase Collections
    public static final String USERS_COLLECTION = "users";
    public static final String BOOKS_COLLECTION = "books";
    public static final String COMMENTS_COLLECTION = "comments";
    public static final String FAVORITES_COLLECTION = "favorites";

    // Preferences
    public static final String PREF_NAME = "ShelfyPrefs";
    public static final String PREF_USER_ID = "user_id";
    public static final String PREF_USER_NAME = "user_name";
    public static final String PREF_USER_EMAIL = "user_email";

    // Request Codes
    public static final int RC_PICK_IMAGE = 100;
    public static final int RC_PERMISSION_READ_STORAGE = 101;
    public static final int RC_PERMISSION_WRITE_STORAGE = 102;

    // Colors
    public static final String COLOR_PRIMARY = "#2E7D32";
    public static final String COLOR_SECONDARY = "#A5D6A7";
    public static final String COLOR_ACCENT = "#FFC107";

    // Animations
    public static final long ANIMATION_DURATION = 300;

    // Other
    public static final String EMPTY_STRING = "";
    public static final int DEFAULT_TIMEOUT = 10000;
}
