package com.example.babelio.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.babelio.R;
import com.example.babelio.adapters.BooksAdapter;
import com.example.babelio.database.DatabaseHelper;
import com.example.babelio.firebase.FirebaseHelper;
import com.example.babelio.models.Book;

import java.util.ArrayList;
import java.util.List;

/**
 * Favorites Fragment showing user's favorite books
 * Loads real favorites from Firestore Cloud
 */
public class FavoritesFragment extends Fragment {
    private RecyclerView favoriteBooksRecyclerView;
    private ProgressBar progressBar;
    private BooksAdapter booksAdapter;
    private List<Book> favoriteBooks = new ArrayList<>();
    private FirebaseHelper firebaseHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_favorites, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize UI
        favoriteBooksRecyclerView = view.findViewById(R.id.favoriteBooksRecyclerView);
        progressBar = view.findViewById(R.id.progressBar);

        // Setup RecyclerView with GridLayout (2 columns)
        booksAdapter = new BooksAdapter(favoriteBooks);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(requireContext(), 2);
        favoriteBooksRecyclerView.setLayoutManager(gridLayoutManager);
        favoriteBooksRecyclerView.setAdapter(booksAdapter);

        firebaseHelper = new FirebaseHelper();

        // Load favorites from Firestore
        loadFavoritesFromFirestore();
    }

    /**
     * Load favorites from Firestore Cloud
     */
    private void loadFavoritesFromFirestore() {
        progressBar.setVisibility(View.VISIBLE);
        firebaseHelper.getFavoriteBooks(new FirebaseHelper.BooksCallback() {
            @Override
            public void onSuccess(List<Book> books) {
                if (isAdded()) {
                    favoriteBooks.clear();
                    favoriteBooks.addAll(books);
                    booksAdapter.notifyDataSetChanged();
                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(String error) {
                if (isAdded()) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
