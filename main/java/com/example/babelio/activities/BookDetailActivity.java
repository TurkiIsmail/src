package com.example.babelio.activities;

import android.net.Uri;
import android.view.LayoutInflater;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.example.babelio.R;
import com.example.babelio.adapters.CommentsAdapter;
import com.example.babelio.database.DatabaseHelper;
import com.example.babelio.firebase.FirebaseHelper;
import com.example.babelio.models.Book;
import com.example.babelio.models.Comment;
import com.example.babelio.models.User;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Book Detail Activity showing full book information
 * Cloud Connected
 */
public class BookDetailActivity extends AppCompatActivity {
    private ImageView bookCoverImageView;
    private TextView titleTextView, authorTextView, descriptionTextView, genreTextView;
    private RatingBar ratingBar;
    private MaterialButton addToFavoritesButton, addCommentButton, editBookButton, deleteBookButton;
    private RecyclerView commentsRecyclerView;
    private ProgressBar progressBar;
    private View emptyCommentsLayout;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ActivityResultLauncher<String> coverPickerLauncher;

    private CommentsAdapter commentsAdapter;
    private List<Comment> commentsList = new ArrayList<>();

    private Book currentBook;
    private boolean isFavorite = false;
    private DatabaseHelper dbHelper;
    private FirebaseHelper firebaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_detail);

        dbHelper = new DatabaseHelper(this);
        firebaseHelper = new FirebaseHelper();
        coverPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null && currentBook != null) {
                        uploadNewCover(uri);
                    }
                }
        );
        initializeUI();
        loadBookData();
    }

    /**
     * Initialize UI components
     */
    private void initializeUI() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        bookCoverImageView = findViewById(R.id.bookCoverImageView);
        titleTextView = findViewById(R.id.titleTextView);
        authorTextView = findViewById(R.id.authorTextView);
        descriptionTextView = findViewById(R.id.descriptionTextView);
        genreTextView = findViewById(R.id.genreTextView);
        ratingBar = findViewById(R.id.ratingBar);
        addToFavoritesButton = findViewById(R.id.addToFavoritesButton);
        addCommentButton = findViewById(R.id.addCommentButton);
        editBookButton = findViewById(R.id.editBookButton);
        deleteBookButton = findViewById(R.id.deleteBookButton);
        commentsRecyclerView = findViewById(R.id.commentsRecyclerView);
        progressBar = findViewById(R.id.progressBar);
        emptyCommentsLayout = findViewById(R.id.emptyCommentsLayout);
        swipeRefreshLayout = findViewById(R.id.bookDetailSwipeRefresh);

        // Setup comments RecyclerView
        commentsAdapter = new CommentsAdapter(commentsList);
        commentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        commentsRecyclerView.setAdapter(commentsAdapter);

        // Button listeners
        addToFavoritesButton.setOnClickListener(v -> toggleFavorite());
        addCommentButton.setOnClickListener(v -> showAddCommentDialog());
        editBookButton.setOnClickListener(v -> showEditBookDialog());
        deleteBookButton.setOnClickListener(v -> confirmDeleteBook());

        swipeRefreshLayout.setOnRefreshListener(this::refreshBookDetails);
    }

    /**
     * Show dialog to add a comment
     */
    private void showAddCommentDialog() {
        if (!firebaseHelper.isUserLoggedIn()) {
            Toast.makeText(this, "Please login to leave a review", Toast.LENGTH_SHORT).show();
            return;
        }

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_comment, null);
        RatingBar dialogRatingBar = dialogView.findViewById(R.id.dialogRatingBar);
        TextInputEditText commentEditText = dialogView.findViewById(R.id.commentEditText);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton("Post", (d, which) -> {
                    String commentText = commentEditText.getText().toString().trim();
                    float rating = dialogRatingBar.getRating();

                    if (commentText.isEmpty()) {
                        Toast.makeText(this, "Review cannot be empty", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    postComment(commentText, rating);
                })
                .setNegativeButton("Cancel", null)
                .create();

        dialog.show();
    }

    private void postComment(String text, float rating) {
        String uid = firebaseHelper.getCurrentUser().getUid();
        
        // Fetch current user name before posting
        firebaseHelper.getUserFromFirestore(uid, new FirebaseHelper.UserCallback() {
            @Override
            public void onSuccess(User user) {
                Comment comment = new Comment(
                        currentBook.getId(),
                        uid,
                        user.getName(),
                        text,
                        rating
                );
                comment.setUserProfileImage(user.getProfileImageUrl());

                firebaseHelper.addComment(comment, new FirebaseHelper.AuthCallback() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(BookDetailActivity.this, "Review posted!", Toast.LENGTH_SHORT).show();
                        firebaseHelper.updateBookRatingFromComments(currentBook.getId(), new FirebaseHelper.AuthCallback() {
                            @Override
                            public void onSuccess() {
                                refreshBookDetails();
                            }

                            @Override
                            public void onFailure(String error) {
                                loadComments();
                            }
                        });
                    }

                    @Override
                    public void onFailure(String error) {
                        Toast.makeText(BookDetailActivity.this, "Failed to post: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(BookDetailActivity.this, "Error fetching user info", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Load book data from intent extras
     */
    private void loadBookData() {
        currentBook = (Book) getIntent().getSerializableExtra("book");
        
        if (currentBook != null) {
            progressBar.setVisibility(android.view.View.GONE);
            
            // Check favorite status in Firestore
            firebaseHelper.isFavorite(currentBook.getId(), new FirebaseHelper.AuthCallback() {
                @Override
                public void onSuccess() {
                    isFavorite = true;
                    updateFavoriteButton();
                }

                @Override
                public void onFailure(String error) {
                    isFavorite = false;
                    updateFavoriteButton();
                }
            });

            displayBookInfo();
            loadComments();
            loadAdminActions();
        }
    }

    private void refreshBookDetails() {
        if (currentBook == null) {
            stopRefreshing();
            return;
        }

        firebaseHelper.getBookById(currentBook.getId(), new FirebaseHelper.SingleBookCallback() {
            @Override
            public void onSuccess(Book book) {
                if (book != null) {
                    currentBook = book;
                    displayBookInfo();
                }
                refreshFavoriteState();
                loadComments();
            }

            @Override
            public void onFailure(String error) {
                refreshFavoriteState();
                loadComments();
            }
        });
    }

    private void refreshFavoriteState() {
        firebaseHelper.isFavorite(currentBook.getId(), new FirebaseHelper.AuthCallback() {
            @Override
            public void onSuccess() {
                isFavorite = true;
                updateFavoriteButton();
            }

            @Override
            public void onFailure(String error) {
                isFavorite = false;
                updateFavoriteButton();
            }
        });
    }

    private void loadAdminActions() {
        if (!firebaseHelper.isUserLoggedIn()) {
            setAdminButtonsVisible(false);
            return;
        }

        String uid = firebaseHelper.getCurrentUser().getUid();
        firebaseHelper.getUserFromFirestore(uid, new FirebaseHelper.UserCallback() {
            @Override
            public void onSuccess(User user) {
                boolean isAdmin = user != null && "admin".equalsIgnoreCase(user.getRole());
                setAdminButtonsVisible(isAdmin);
            }

            @Override
            public void onFailure(String error) {
                setAdminButtonsVisible(false);
            }
        });
    }

    private void setAdminButtonsVisible(boolean visible) {
        int visibility = visible ? View.VISIBLE : View.GONE;
        editBookButton.setVisibility(visibility);
        deleteBookButton.setVisibility(visibility);
    }

    private void showEditBookDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_book, null);
        TextInputEditText titleEditText = dialogView.findViewById(R.id.editBookTitle);
        TextInputEditText authorEditText = dialogView.findViewById(R.id.editBookAuthor);
        TextInputEditText genreEditText = dialogView.findViewById(R.id.editBookGenre);
        TextInputEditText descriptionEditText = dialogView.findViewById(R.id.editBookDescription);
        MaterialButton changeCoverButton = dialogView.findViewById(R.id.changeCoverButton);

        titleEditText.setText(currentBook.getTitle());
        authorEditText.setText(currentBook.getAuthor());
        genreEditText.setText(currentBook.getGenre());
        descriptionEditText.setText(currentBook.getDescription());
        changeCoverButton.setOnClickListener(v -> coverPickerLauncher.launch("image/*"));

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton("Save", (d, which) -> {
                    String title = titleEditText.getText() != null ? titleEditText.getText().toString().trim() : "";
                    String author = authorEditText.getText() != null ? authorEditText.getText().toString().trim() : "";
                    String genre = genreEditText.getText() != null ? genreEditText.getText().toString().trim() : "";
                    String description = descriptionEditText.getText() != null ? descriptionEditText.getText().toString().trim() : "";

                    if (title.isEmpty() || author.isEmpty()) {
                        Toast.makeText(this, "Title and author are required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    updateBookDetails(title, author, genre, description);
                })
                .setNegativeButton("Cancel", null)
                .create();

        dialog.show();
    }

    private void updateBookDetails(String title, String author, String genre, String description) {
        java.util.Map<String, Object> updates = new java.util.HashMap<>();
        updates.put("title", title);
        updates.put("author", author);
        updates.put("genre", genre);
        updates.put("description", description);

        firebaseHelper.updateBook(currentBook.getId(), updates, new FirebaseHelper.AuthCallback() {
            @Override
            public void onSuccess() {
                currentBook.setTitle(title);
                currentBook.setAuthor(author);
                currentBook.setGenre(genre);
                currentBook.setDescription(description);
                displayBookInfo();
                Toast.makeText(BookDetailActivity.this, "Book updated", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(BookDetailActivity.this, "Update failed: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void confirmDeleteBook() {
        new AlertDialog.Builder(this)
                .setTitle("Delete book")
                .setMessage("This will remove the book and its reviews. Continue?")
                .setPositiveButton("Delete", (dialog, which) -> deleteBook())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteBook() {
        firebaseHelper.deleteBook(currentBook.getId(), new FirebaseHelper.AuthCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(BookDetailActivity.this, "Book deleted", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(BookDetailActivity.this, "Delete failed: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadComments() {
        progressBar.setVisibility(View.VISIBLE);
        firebaseHelper.getCommentsForBook(currentBook.getId(), new FirebaseHelper.CommentsCallback() {
            @Override
            public void onSuccess(List<Comment> cloudComments) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    List<Comment> sortedComments = new ArrayList<>(cloudComments);
                    Collections.sort(sortedComments, new Comparator<Comment>() {
                        @Override
                        public int compare(Comment left, Comment right) {
                            return Long.compare(right.getTimestamp(), left.getTimestamp());
                        }
                    });

                    commentsList.clear();

                    if (sortedComments.isEmpty()) {
                        // For empty cloud, show empty state layout
                        progressBar.setVisibility(View.GONE);
                        emptyCommentsLayout.setVisibility(View.VISIBLE);
                        // Reset rating to book default if no reviews
                        ratingBar.setRating((float) currentBook.getRating());
                    } else {
                        emptyCommentsLayout.setVisibility(View.GONE);
                        commentsList.addAll(sortedComments);
                        commentsAdapter.notifyDataSetChanged();
                        calculateAverageRating(sortedComments);
                    }
                    stopRefreshing();
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    // On failure, show empty state instead of legacy local dummy data
                    emptyCommentsLayout.setVisibility(View.VISIBLE);
                    stopRefreshing();
                });
            }
        });
    }

    private void stopRefreshing() {
        if (swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    private void uploadNewCover(Uri coverUri) {
        progressBar.setVisibility(View.VISIBLE);
        firebaseHelper.uploadBookCover(coverUri, currentBook.getId(), new FirebaseHelper.AuthCallback() {
            @Override
            public void onSuccess() {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(BookDetailActivity.this, "Cover updated", Toast.LENGTH_SHORT).show();
                refreshBookDetails();
            }

            @Override
            public void onFailure(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(BookDetailActivity.this, "Cover update failed: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void calculateAverageRating(List<Comment> comments) {
        if (comments == null || comments.isEmpty()) return;
        
        float totalRating = 0;
        for (Comment comment : comments) {
            totalRating += comment.getRating();
        }
        float average = totalRating / comments.size();
        ratingBar.setRating(average);
    }

    private void toggleFavorite() {
        if (isFavorite) {
            firebaseHelper.removeFromFavorites(currentBook.getId(), new FirebaseHelper.AuthCallback() {
                @Override
                public void onSuccess() {
                    isFavorite = false;
                    updateFavoriteButton();
                }

                @Override
                public void onFailure(String error) {}
            });
        } else {
            firebaseHelper.addToFavorites(currentBook.getId(), new FirebaseHelper.AuthCallback() {
                @Override
                public void onSuccess() {
                    isFavorite = true;
                    updateFavoriteButton();
                }

                @Override
                public void onFailure(String error) {}
            });
        }
    }

    private void updateFavoriteButton() {
        if (isFavorite) {
            addToFavoritesButton.setText("Remove from Favorites");
        } else {
            addToFavoritesButton.setText("Add to Favorites");
        }
    }

    /**
     * Display book information on UI
     */
    private void displayBookInfo() {
        titleTextView.setText(currentBook.getTitle());
        authorTextView.setText("By " + currentBook.getAuthor());
        descriptionTextView.setText(currentBook.getDescription());
        genreTextView.setText(currentBook.getGenre());
        ratingBar.setRating((float) currentBook.getRating());

        // Load book cover image
        Glide.with(this)
                .load(currentBook.getImageUrl())
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_foreground)
                .into(bookCoverImageView);

        // Update favorite button
        updateFavoriteButton();
    }
}
