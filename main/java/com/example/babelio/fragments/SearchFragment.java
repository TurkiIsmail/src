package com.example.babelio.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.babelio.R;
import com.example.babelio.adapters.BooksAdapter;
import com.example.babelio.firebase.FirebaseHelper;
import com.example.babelio.models.Book;

import java.util.ArrayList;
import java.util.List;

/**
 * Search Fragment for searching books
 * Reads all books from Firestore Cloud
 */
public class SearchFragment extends Fragment {
    private EditText searchEditText;
    private RecyclerView searchResultsRecyclerView;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private BooksAdapter booksAdapter;
    private List<Book> allBooks = new ArrayList<>();
    private List<Book> filteredResults = new ArrayList<>();
    private FirebaseHelper firebaseHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize UI
        searchEditText = view.findViewById(R.id.searchEditText);
        searchResultsRecyclerView = view.findViewById(R.id.searchResultsRecyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        swipeRefreshLayout = view.findViewById(R.id.searchSwipeRefresh);

        // Setup RecyclerView with filtered list
        booksAdapter = new BooksAdapter(filteredResults);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(requireContext(), 2);
        searchResultsRecyclerView.setLayoutManager(gridLayoutManager);
        searchResultsRecyclerView.setAdapter(booksAdapter);

        firebaseHelper = new FirebaseHelper();

        swipeRefreshLayout.setOnRefreshListener(this::loadAllBooksFromFirestore);
        
        // Load all books from Firestore once
        loadAllBooksFromFirestore();

        // Setup search listener for local filtering
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterBooks(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadAllBooksFromFirestore() {
        progressBar.setVisibility(View.VISIBLE);
        firebaseHelper.getAllBooks(new FirebaseHelper.BooksCallback() {
            @Override
            public void onSuccess(List<Book> books) {
                if (isAdded()) {
                    allBooks.clear();
                    allBooks.addAll(books);
                    
                    // Initially show all
                    filteredResults.clear();
                    filteredResults.addAll(allBooks);
                    booksAdapter.notifyDataSetChanged();
                    progressBar.setVisibility(View.GONE);
                    swipeRefreshLayout.setRefreshing(false);
                }
            }

            @Override
            public void onFailure(String error) {
                if (isAdded()) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
        });
    }

    private void filterBooks(String query) {
        filteredResults.clear();
        if (query.isEmpty()) {
            filteredResults.addAll(allBooks);
        } else {
            String lowerQuery = query.toLowerCase().trim();
            for (Book book : allBooks) {
                if (book.getTitle().toLowerCase().contains(lowerQuery) || 
                    book.getAuthor().toLowerCase().contains(lowerQuery)) {
                    filteredResults.add(book);
                }
            }
        }
        booksAdapter.notifyDataSetChanged();
    }
}
