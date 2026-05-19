package com.example.babelio.firebase;

import android.util.Log;

import androidx.annotation.NonNull;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.example.babelio.models.Book;
import com.example.babelio.models.Comment;
import com.example.babelio.models.User;
import com.example.babelio.utils.Constants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Firebase Helper class for handling Firebase operations
 * Includes Auth, Firestore, and Storage
 */
public class FirebaseHelper {
    private static final String TAG = "FirebaseHelper";

    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;
    private FirebaseStorage mStorage;

    // Callback interfaces
    public interface AuthCallback {
        void onSuccess();
        void onFailure(String error);
    }

    public interface UserCallback {
        void onSuccess(User user);
        void onFailure(String error);
    }

    public interface BooksCallback {
        void onSuccess(List<Book> books);
        void onFailure(String error);
    }

    public interface CommentsCallback {
        void onSuccess(List<Comment> comments);
        void onFailure(String error);
    }

    public interface SingleBookCallback {
        void onSuccess(Book book);
        void onFailure(String error);
    }

    public FirebaseHelper() {
        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();
        mStorage = FirebaseStorage.getInstance();
    }

    // ============ AUTHENTICATION ============

    /**
     * Register a new user
     */
    public void registerUser(String email, String password, String name, AuthCallback callback) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            User user = new User(firebaseUser.getUid(), name, email);
                            saveUserToFirestore(user, new AuthCallback() {
                                @Override
                                public void onSuccess() {
                                    Log.d(TAG, "User registered successfully");
                                    callback.onSuccess();
                                }

                                @Override
                                public void onFailure(String error) {
                                    callback.onFailure(error);
                                }
                            });
                        }
                    } else {
                        String errorMsg = task.getException() != null ? task.getException().getMessage() : "Registration failed";
                        Log.e(TAG, "Registration failed: " + errorMsg);
                        callback.onFailure(errorMsg);
                    }
                });
    }

    /**
     * Login user
     */
    public void loginUser(String email, String password, AuthCallback callback) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "User logged in successfully");
                        callback.onSuccess();
                    } else {
                        String errorMsg = task.getException() != null ? task.getException().getMessage() : "Login failed";
                        Log.e(TAG, "Login failed: " + errorMsg);
                        callback.onFailure(errorMsg);
                    }
                });
    }

    /**
     * Logout user
     */
    public void logoutUser() {
        mAuth.signOut();
        Log.d(TAG, "User logged out");
    }

    /**
     * Get current user
     */
    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }

    /**
     * Check if user is logged in
     */
    public boolean isUserLoggedIn() {
        return mAuth.getCurrentUser() != null;
    }

    // ============ USER OPERATIONS ============

    /**
     * Save user to Firestore
     */
    private void saveUserToFirestore(User user, AuthCallback callback) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("uid", user.getUid());
        userData.put("name", user.getName());
        userData.put("email", user.getEmail());
        userData.put("profileImageUrl", user.getProfileImageUrl());
        userData.put("bio", user.getBio());
        userData.put("favoritesCount", user.getFavoritesCount());
        userData.put("reviewsCount", user.getReviewsCount());
        userData.put("role", user.getRole());
        userData.put("createdAt", user.getCreatedAt());

        mFirestore.collection(Constants.USERS_COLLECTION)
                .document(user.getUid())
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User saved to Firestore");
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error saving user: " + e.getMessage());
                    callback.onFailure(e.getMessage());
                });
    }

    /**
     * Get user from Firestore
     */
    public void getUserFromFirestore(String userId, UserCallback callback) {
        mFirestore.collection(Constants.USERS_COLLECTION)
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        callback.onSuccess(user);
                    } else {
                        callback.onFailure("User not found");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting user: " + e.getMessage());
                    callback.onFailure(e.getMessage());
                });
    }

    /**
     * Update user profile
     */
    public void updateUserProfile(String userId, Map<String, Object> updates, AuthCallback callback) {
        mFirestore.collection(Constants.USERS_COLLECTION)
                .document(userId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User profile updated");
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating user: " + e.getMessage());
                    callback.onFailure(e.getMessage());
                });
    }

    public void updateBook(String bookId, Map<String, Object> updates, AuthCallback callback) {
        if (bookId == null || bookId.isEmpty()) {
            callback.onFailure("Missing book id");
            return;
        }

        mFirestore.collection(Constants.BOOKS_COLLECTION)
                .document(bookId)
                .update(updates)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void deleteBook(String bookId, AuthCallback callback) {
        if (bookId == null || bookId.isEmpty()) {
            callback.onFailure("Missing book id");
            return;
        }

        mFirestore.collection(Constants.BOOKS_COLLECTION)
                .document(bookId)
                .delete()
                .addOnSuccessListener(aVoid -> deleteCommentsForBook(bookId, callback))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    private void deleteCommentsForBook(String bookId, AuthCallback callback) {
        mFirestore.collection(Constants.COMMENTS_COLLECTION)
                .whereEqualTo("bookId", bookId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        deleteFavoritesForBook(bookId, callback);
                        return;
                    }

                    com.google.firebase.firestore.WriteBatch batch = mFirestore.batch();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        batch.delete(doc.getReference());
                    }

                    batch.commit()
                            .addOnSuccessListener(aVoid -> deleteFavoritesForBook(bookId, callback))
                            .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    private void deleteFavoritesForBook(String bookId, AuthCallback callback) {
        mFirestore.collection(Constants.FAVORITES_COLLECTION)
                .whereEqualTo("bookId", bookId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        callback.onSuccess();
                        return;
                    }

                    com.google.firebase.firestore.WriteBatch batch = mFirestore.batch();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        batch.delete(doc.getReference());
                    }

                    batch.commit()
                            .addOnSuccessListener(aVoid -> callback.onSuccess())
                            .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    // ============ BOOK OPERATIONS ============

    /**
     * Add a book to Firestore
     */
    public void addBook(Book book, AuthCallback callback) {
        String bookId = book.getId() != null ? book.getId() : UUID.randomUUID().toString();
        book.setId(bookId);

        Map<String, Object> bookData = new HashMap<>();
        bookData.put("id", book.getId());
        bookData.put("title", book.getTitle());
        bookData.put("author", book.getAuthor());
        bookData.put("description", book.getDescription());
        bookData.put("imageUrl", book.getImageUrl());
        bookData.put("rating", book.getRating());
        bookData.put("reviewCount", book.getReviewCount());
        bookData.put("genre", book.getGenre());
        bookData.put("timestamp", book.getTimestamp());

        // Add search keywords for easier searching
        List<String> keywords = new ArrayList<>();
        if (book.getTitle() != null) {
            String[] titleWords = book.getTitle().toLowerCase().split("\\s+");
            for (String word : titleWords) {
                keywords.add(word);
            }
        }
        if (book.getAuthor() != null) {
            String[] authorWords = book.getAuthor().toLowerCase().split("\\s+");
            for (String word : authorWords) {
                keywords.add(word);
            }
        }
        bookData.put("searchKeywords", keywords);

        mFirestore.collection(Constants.BOOKS_COLLECTION)
                .document(book.getId())
                .set(bookData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Book added successfully");
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error adding book: " + e.getMessage());
                    callback.onFailure(e.getMessage());
                });
    }

    /**
     * Get all books from Firestore
     */
    public void getAllBooks(BooksCallback callback) {
        mFirestore.collection(Constants.BOOKS_COLLECTION)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Book> books = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        Book book = doc.toObject(Book.class);
                        if (book != null) {
                            books.add(book);
                        }
                    }
                    callback.onSuccess(books);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting books: " + e.getMessage());
                    callback.onFailure(e.getMessage());
                });
    }

    /**
     * Get a single book by ID
     */
    public void getBookById(String bookId, SingleBookCallback callback) {
        mFirestore.collection(Constants.BOOKS_COLLECTION)
                .document(bookId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Book book = documentSnapshot.toObject(Book.class);
                        callback.onSuccess(book);
                    } else {
                        callback.onFailure("Book not found");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting book: " + e.getMessage());
                    callback.onFailure(e.getMessage());
                });
    }

    /**
     * Mass migrate books from local SQLite to Firestore
     */
    public void massMigrateBooks(List<Book> books, AuthCallback callback) {
        if (books == null || books.isEmpty()) {
            callback.onSuccess();
            return;
        }

        com.google.firebase.firestore.WriteBatch batch = mFirestore.batch();
        for (Book book : books) {
            String bookId = book.getId() != null ? book.getId() : UUID.randomUUID().toString();
            book.setId(bookId);

            Map<String, Object> bookData = new HashMap<>();
            bookData.put("id", book.getId());
            bookData.put("title", book.getTitle());
            bookData.put("author", book.getAuthor());
            bookData.put("description", book.getDescription());
            bookData.put("imageUrl", book.getImageUrl());
            bookData.put("rating", book.getRating());
            bookData.put("reviewCount", book.getReviewCount());
            bookData.put("genre", book.getGenre());
            bookData.put("timestamp", book.getTimestamp());

            List<String> keywords = new ArrayList<>();
            if (book.getTitle() != null) {
                String[] titleWords = book.getTitle().toLowerCase().split("\\s+");
                for (String word : titleWords) {
                    if (!keywords.contains(word)) keywords.add(word);
                }
            }
            if (book.getAuthor() != null) {
                String[] authorWords = book.getAuthor().toLowerCase().split("\\s+");
                for (String word : authorWords) {
                    if (!keywords.contains(word)) keywords.add(word);
                }
            }
            bookData.put("searchKeywords", keywords);

            batch.set(mFirestore.collection(Constants.BOOKS_COLLECTION).document(book.getId()), bookData);
        }

        batch.commit()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Books migrated successfully");
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error migrating books: " + e.getMessage());
                    callback.onFailure(e.getMessage());
                });
    }

    /**
     * Search books by title or author
     */
    public void searchBooks(String query, BooksCallback callback) {
        mFirestore.collection(Constants.BOOKS_COLLECTION)
                .whereArrayContains("searchKeywords", query.toLowerCase())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Book> books = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        Book book = doc.toObject(Book.class);
                        if (book != null) {
                            books.add(book);
                        }
                    }
                    callback.onSuccess(books);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error searching books: " + e.getMessage());
                    callback.onFailure(e.getMessage());
                });
    }

    // ============ COMMENTS OPERATIONS ============

    /**
     * Add a comment to a book
     */
    public void addComment(Comment comment, AuthCallback callback) {
        String commentId = UUID.randomUUID().toString();
        comment.setCommentId(commentId);

        Map<String, Object> commentData = new HashMap<>();
        commentData.put("commentId", comment.getCommentId());
        commentData.put("bookId", comment.getBookId());
        commentData.put("userId", comment.getUserId());
        commentData.put("userName", comment.getUserName());
        commentData.put("userProfileImage", comment.getUserProfileImage());
        commentData.put("text", comment.getText());
        commentData.put("rating", comment.getRating());
        commentData.put("timestamp", comment.getTimestamp());

        mFirestore.collection(Constants.COMMENTS_COLLECTION)
                .document(comment.getCommentId())
                .set(commentData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Comment added successfully");
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error adding comment: " + e.getMessage());
                    callback.onFailure(e.getMessage());
                });
    }

    /**
     * Get all comments for a book
     */
    public void getCommentsForBook(String bookId, CommentsCallback callback) {
        mFirestore.collection(Constants.COMMENTS_COLLECTION)
                .whereEqualTo("bookId", bookId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Comment> comments = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        Comment comment = doc.toObject(Comment.class);
                        if (comment != null) {
                            comments.add(comment);
                        }
                    }
                    callback.onSuccess(comments);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting comments: " + e.getMessage());
                    callback.onFailure(e.getMessage());
                });
    }

    public void updateBookRatingFromComments(String bookId, AuthCallback callback) {
        if (bookId == null || bookId.isEmpty()) {
            callback.onFailure("Missing book id");
            return;
        }

        mFirestore.collection(Constants.COMMENTS_COLLECTION)
                .whereEqualTo("bookId", bookId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    double totalRating = 0.0;
                    int count = 0;

                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        Comment comment = doc.toObject(Comment.class);
                        if (comment != null) {
                            totalRating += comment.getRating();
                            count += 1;
                        }
                    }

                    double average = count == 0 ? 0.0 : totalRating / count;
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("rating", average);
                    updates.put("reviewCount", count);

                    mFirestore.collection(Constants.BOOKS_COLLECTION)
                            .document(bookId)
                            .update(updates)
                            .addOnSuccessListener(aVoid -> callback.onSuccess())
                            .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    // ============ FAVORITES OPERATIONS ============

    /**
     * Add a book to user's favorites
     */
    public void addToFavorites(String bookId, AuthCallback callback) {
        String uid = getCurrentUser() != null ? getCurrentUser().getUid() : null;
        if (uid == null) {
            callback.onFailure("User not logged in");
            return;
        }

        Map<String, Object> favoriteData = new HashMap<>();
        favoriteData.put("userId", uid);
        favoriteData.put("bookId", bookId);
        favoriteData.put("timestamp", System.currentTimeMillis());

        mFirestore.collection(Constants.FAVORITES_COLLECTION)
                .document(uid + "_" + bookId)
                .set(favoriteData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Added to favorites");
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error adding to favorites: " + e.getMessage());
                    callback.onFailure(e.getMessage());
                });
    }

    /**
     * Remove a book from user's favorites
     */
    public void removeFromFavorites(String bookId, AuthCallback callback) {
        String uid = getCurrentUser() != null ? getCurrentUser().getUid() : null;
        if (uid == null) {
            callback.onFailure("User not logged in");
            return;
        }

        mFirestore.collection(Constants.FAVORITES_COLLECTION)
                .document(uid + "_" + bookId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Removed from favorites");
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error removing from favorites: " + e.getMessage());
                    callback.onFailure(e.getMessage());
                });
    }

    /**
     * Check if a book is in user's favorites
     */
    public void isFavorite(String bookId, AuthCallback callback) {
        String uid = getCurrentUser() != null ? getCurrentUser().getUid() : null;
        if (uid == null) {
            callback.onFailure("User not logged in");
            return;
        }

        mFirestore.collection(Constants.FAVORITES_COLLECTION)
                .document(uid + "_" + bookId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        callback.onSuccess();
                    } else {
                        callback.onFailure("Not in favorites");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error checking favorite: " + e.getMessage());
                    callback.onFailure(e.getMessage());
                });
    }

    /**
     * Get all favorite books for the current user
     */
    public void getFavoriteBooks(BooksCallback callback) {
        String uid = getCurrentUser() != null ? getCurrentUser().getUid() : null;
        if (uid == null) {
            callback.onFailure("User not logged in");
            return;
        }

        mFirestore.collection(Constants.FAVORITES_COLLECTION)
                .whereEqualTo("userId", uid)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> bookIds = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        bookIds.add(doc.getString("bookId"));
                    }

                    if (bookIds.isEmpty()) {
                        callback.onSuccess(new ArrayList<>());
                        return;
                    }

                    // Fetch actual book details for these IDs
                    mFirestore.collection(Constants.BOOKS_COLLECTION)
                            .whereIn("id", bookIds)
                            .get()
                            .addOnSuccessListener(bookSnapshots -> {
                                List<Book> books = new ArrayList<>();
                                for (DocumentSnapshot doc : bookSnapshots.getDocuments()) {
                                    Book book = doc.toObject(Book.class);
                                    if (book != null) {
                                        books.add(book);
                                    }
                                }
                                callback.onSuccess(books);
                            })
                            .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    // ============ STORAGE OPERATIONS ============

    /**
     * Upload book cover to Cloudinary
     */
    public void uploadBookCover(android.net.Uri fileUri, String bookId, AuthCallback callback) {
        Log.d(TAG, "Attempting to upload cover to Cloudinary for book: " + bookId);

        String publicId = "books/" + bookId + "_" + UUID.randomUUID();

        MediaManager.get().upload(fileUri)
                .unsigned("ml_default")
            .option("public_id", publicId)
                .option("resource_type", "auto")
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {
                        Log.d(TAG, "Cloudinary upload started: " + requestId);
                    }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {
                    }

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        String imageUrl = (String) resultData.get("secure_url");
                        Log.d(TAG, "Cloudinary upload success: " + imageUrl);

                        // Update book with image URL in Firestore
                        Map<String, Object> updates = new HashMap<>();
                        updates.put("imageUrl", imageUrl);
                        mFirestore.collection(Constants.BOOKS_COLLECTION).document(bookId)
                                .update(updates)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "Firestore book document updated with Cloudinary imageUrl");
                                    callback.onSuccess();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Failed to update Firestore: " + e.getMessage());
                                    callback.onFailure("Cloudinary upload OK but Firestore update failed");
                                });
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        Log.e(TAG, "Cloudinary upload error Code " + error.getCode() + ": " + error.getDescription());
                        callback.onFailure("Error " + error.getCode() + ": " + error.getDescription());
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {
                    }
                }).dispatch();
    }

    /**
     * Get download URL for an image
     */
    public void getImageUrl(String imagePath, AuthCallback callback) {
        StorageReference storageRef = mStorage.getReference(imagePath);
        storageRef.getDownloadUrl()
                .addOnSuccessListener(uri -> {
                    Log.d(TAG, "Got image URL: " + uri.toString());
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting image URL: " + e.getMessage());
                    callback.onFailure(e.getMessage());
                });
    }

    /**
     * Upload profile image to Cloudinary
     */
    public void uploadProfileImage(android.net.Uri fileUri, String userId, AuthCallback callback) {
        String publicId = "profiles/" + userId + "_" + UUID.randomUUID();

        MediaManager.get().upload(fileUri)
                .unsigned("ml_default")
            .option("public_id", publicId)
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {
                    }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {
                    }

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        String imageUrl = (String) resultData.get("secure_url");
                        Map<String, Object> updates = new HashMap<>();
                        updates.put("profileImageUrl", imageUrl);
                        mFirestore.collection(Constants.USERS_COLLECTION).document(userId)
                                .update(updates)
                                .addOnSuccessListener(aVoid -> updateUserCommentsProfileImage(userId, imageUrl, callback))
                                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        callback.onFailure(error.getDescription());
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {
                    }
                }).dispatch();
    }

    private void updateUserCommentsProfileImage(String userId, String imageUrl, AuthCallback callback) {
        mFirestore.collection(Constants.COMMENTS_COLLECTION)
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        callback.onSuccess();
                        return;
                    }

                    com.google.firebase.firestore.WriteBatch batch = mFirestore.batch();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        batch.update(doc.getReference(), "userProfileImage", imageUrl);
                    }

                    batch.commit()
                            .addOnSuccessListener(aVoid -> callback.onSuccess())
                            .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }
}
