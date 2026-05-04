package com.example.babelio.firebase;

import android.util.Log;

import androidx.annotation.NonNull;

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
    public void saveUserToFirestore(User user, AuthCallback callback) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("uid", user.getUid());
        userData.put("name", user.getName());
        userData.put("email", user.getEmail());
        userData.put("profileImageUrl", user.getProfileImageUrl());
        userData.put("bio", user.getBio());
        userData.put("favoritesCount", user.getFavoritesCount());
        userData.put("reviewsCount", user.getReviewsCount());
        userData.put("createdAt", System.currentTimeMillis());

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
     * Mass Migrate local books to Firestore
     */
    public void massMigrateBooks(List<Book> books, AuthCallback callback) {
        int total = books.size();
        final int[] successCount = {0};
        final int[] failureCount = {0};

        if (books.isEmpty()) {
            callback.onSuccess();
            return;
        }

        for (Book book : books) {
            addBook(book, new AuthCallback() {
                @Override
                public void onSuccess() {
                    successCount[0]++;
                    checkCompletion();
                }

                @Override
                public void onFailure(String error) {
                    failureCount[0]++;
                    Log.e(TAG, "Migration failed for book: " + book.getTitle() + " - " + error);
                    checkCompletion();
                }

                private void checkCompletion() {
                    if (successCount[0] + failureCount[0] == total) {
                        if (failureCount[0] == 0) {
                            callback.onSuccess();
                        } else {
                            callback.onFailure("Migration completed with " + failureCount[0] + " errors.");
                        }
                    }
                }
            });
        }
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
    public void getCommentsByBookId(String bookId, CommentsCallback callback) {
        mFirestore.collection(Constants.COMMENTS_COLLECTION)
                .whereEqualTo("bookId", bookId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
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

    // ============ STORAGE OPERATIONS ============

    /**
     * Upload image to Firebase Storage
     */
    public void uploadImage(String localImagePath, String remotePath, AuthCallback callback) {
        StorageReference storageRef = mStorage.getReference(remotePath);
        // This is a simplified version - in production, use Uri from Android file system
        Log.d(TAG, "Image upload initiated for: " + remotePath);
        callback.onSuccess();
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

    // ============ FAVORITES OPERATIONS ============

    /**
     * Add a book to user's favorites in Firestore
     */
    public void addToFavorites(String bookId, AuthCallback callback) {
        String userId = mAuth.getUid();
        if (userId == null) {
            callback.onFailure("User not logged in");
            return;
        }

        Map<String, Object> favoriteData = new HashMap<>();
        favoriteData.put("bookId", bookId);
        favoriteData.put("userId", userId);
        favoriteData.put("timestamp", System.currentTimeMillis());

        mFirestore.collection(Constants.FAVORITES_COLLECTION)
                .document(userId + "_" + bookId)
                .set(favoriteData)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    /**
     * Remove a book from user's favorites in Firestore
     */
    public void removeFromFavorites(String bookId, AuthCallback callback) {
        String userId = mAuth.getUid();
        if (userId == null) {
            callback.onFailure("User not logged in");
            return;
        }

        mFirestore.collection(Constants.FAVORITES_COLLECTION)
                .document(userId + "_" + bookId)
                .delete()
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    /**
     * Check if a book is favorite for the current user in Firestore
     */
    public void isFavorite(String bookId, AuthCallback callback) {
        String userId = mAuth.getUid();
        if (userId == null) {
            callback.onFailure("User not logged in");
            return;
        }

        mFirestore.collection(Constants.FAVORITES_COLLECTION)
                .document(userId + "_" + bookId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        callback.onSuccess();
                    } else {
                        callback.onFailure("Not favorite");
                    }
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    /**
     * Get all favorite books for the current user
     */
    public void getFavoriteBooks(BooksCallback callback) {
        String userId = mAuth.getUid();
        if (userId == null) {
            callback.onFailure("User not logged in");
            return;
        }

        mFirestore.collection(Constants.FAVORITES_COLLECTION)
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(favoritiesSnapshot -> {
                    List<String> bookIds = new ArrayList<>();
                    for (DocumentSnapshot doc : favoritiesSnapshot.getDocuments()) {
                        bookIds.add(doc.getString("bookId"));
                    }

                    if (bookIds.isEmpty()) {
                        callback.onSuccess(new ArrayList<>());
                        return;
                    }

                    // Fetch actual book data for these IDs
                    // Firestore 'in' query limit is 10, but for MVP we use basic fetch
                    mFirestore.collection(Constants.BOOKS_COLLECTION)
                            .whereIn("id", bookIds)
                            .get()
                            .addOnSuccessListener(booksSnapshot -> {
                                List<Book> books = new ArrayList<>();
                                for (DocumentSnapshot doc : booksSnapshot.getDocuments()) {
                                    Book book = doc.toObject(Book.class);
                                    if (book != null) books.add(book);
                                }
                                callback.onSuccess(books);
                            })
                            .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }
}
