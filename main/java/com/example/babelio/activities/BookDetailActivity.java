package com.example.babelio.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.babelio.R;
import com.example.babelio.adapters.CommentsAdapter;
import com.example.babelio.database.DatabaseHelper;
import com.example.babelio.firebase.FirebaseHelper;
import com.example.babelio.models.Book;
import com.example.babelio.models.Comment;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

/**
 * Book Detail Activity showing full book information
 * Cloud Connected
 */
public class BookDetailActivity extends AppCompatActivity {
    private ImageView bookCoverImageView;
    private TextView titleTextView, authorTextView, descriptionTextView, genreTextView;
    private RatingBar ratingBar;
    private MaterialButton addToFavoritesButton, addCommentButton;
    private RecyclerView commentsRecyclerView;
    private ProgressBar progressBar;

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
        commentsRecyclerView = findViewById(R.id.commentsRecyclerView);
        progressBar = findViewById(R.id.progressBar);

        // Setup comments RecyclerView
        commentsAdapter = new CommentsAdapter(commentsList);
        commentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        commentsRecyclerView.setAdapter(commentsAdapter);

        // Button listeners
        addToFavoritesButton.setOnClickListener(v -> toggleFavorite());
        addCommentButton.setOnClickListener(v -> {
            // Future: Show dialog to add comment to Firestore
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
        }
    }

    private void loadComments() {
        commentsList.clear();
        commentsList.addAll(dbHelper.getCommentsForBook(currentBook.getId()));
        
        // If no comments in DB, load initial mock ones and save them
        if (commentsList.isEmpty()) {
            addInitialComments();
        } else {
            commentsAdapter.notifyDataSetChanged();
        }
    }

    private void addInitialComments() {
        Comment comment1 = new Comment(currentBook.getId(), "user1", "Emma Wilson", "Amazing read! Couldn't put it down.", 5.0);
        Comment comment2 = new Comment(currentBook.getId(), "user2", "John Smith", "Great story with memorable characters.", 4.5);
        
        dbHelper.addComment(comment1);
        dbHelper.addComment(comment2);
        
        commentsList.add(comment1);
        commentsList.add(comment2);
        commentsAdapter.notifyDataSetChanged();
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
