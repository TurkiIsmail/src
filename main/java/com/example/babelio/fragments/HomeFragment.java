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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.babelio.R;
import com.example.babelio.adapters.BooksAdapter;
import com.example.babelio.database.DatabaseHelper;
import com.example.babelio.models.Book;
import com.example.babelio.network.BookSearchResponse;
import com.example.babelio.network.OpenLibraryAPI;

import com.example.babelio.firebase.FirebaseHelper;
import com.example.babelio.utils.Constants;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Home Fragment showing list of books
 * Cloud Connected
 */
public class HomeFragment extends Fragment {
    private RecyclerView booksRecyclerView;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private BooksAdapter booksAdapter;
    private List<Book> booksList = new ArrayList<>();
    private DatabaseHelper dbHelper;
    private FirebaseHelper firebaseHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize UI
        booksRecyclerView = view.findViewById(R.id.booksRecyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        swipeRefreshLayout = view.findViewById(R.id.homeSwipeRefresh);

        // Setup RecyclerView with GridLayout (2 columns)
        booksAdapter = new BooksAdapter(booksList);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(requireContext(), 2);
        booksRecyclerView.setLayoutManager(gridLayoutManager);
        booksRecyclerView.setAdapter(booksAdapter);

        dbHelper = new DatabaseHelper(requireContext());
        firebaseHelper = new FirebaseHelper();

        swipeRefreshLayout.setOnRefreshListener(this::loadBooksFromFirestore);

        // Load books from Firestore
        loadBooksFromFirestore();
    }

    /**
     * Load books from Firebase Firestore
     */
    private void loadBooksFromFirestore() {
        progressBar.setVisibility(View.VISIBLE);
        firebaseHelper.getAllBooks(new FirebaseHelper.BooksCallback() {
            @Override
            public void onSuccess(List<Book> books) {
                booksList.clear();
                booksList.addAll(books);
                
                // If cloud is empty, fall back to SQLite and migrate
                if (booksList.isEmpty()) {
                    loadBooksFromSQLite();
                    migrateToCloud();
                } else {
                    booksAdapter.notifyDataSetChanged();
                    progressBar.setVisibility(View.GONE);
                    swipeRefreshLayout.setRefreshing(false);
                }
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(requireContext(), "Cloud Sync Failed: " + error, Toast.LENGTH_SHORT).show();
                loadBooksFromSQLite();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    /**
     * Migrate local data to cloud
     */
    private void migrateToCloud() {
        List<Book> localBooks = dbHelper.getAllBooks();
        if (!localBooks.isEmpty()) {
            firebaseHelper.massMigrateBooks(localBooks, new FirebaseHelper.AuthCallback() {
                @Override
                public void onSuccess() {
                    android.util.Log.d("HomeFragment", "Local data migrated to Firestore");
                }

                @Override
                public void onFailure(String error) {
                    android.util.Log.e("HomeFragment", "Migration failed: " + error);
                }
            });
        }
    }

    /**
     * Load books from SQLite database (Fallback)
     */
    private void loadBooksFromSQLite() {
        booksList.clear();
        booksList.addAll(dbHelper.getAllBooks());
        booksAdapter.notifyDataSetChanged();
        progressBar.setVisibility(View.GONE);
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    /**
     * Load popular books from Open Library API
     * Loads all books without filter - no limits!
     */
    private void loadBooksFromAPI() {
        progressBar.setVisibility(View.VISIBLE);
        booksList.clear();
        booksAdapter.notifyDataSetChanged();
        
        android.util.Log.d("HomeFragment", "Starting API call for books...");
        
        // Create Retrofit instance
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://openlibrary.org/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        
        OpenLibraryAPI api = retrofit.create(OpenLibraryAPI.class);
        
        // Search for "the" - returns many popular books
        api.searchBooks("the", 50).enqueue(new Callback<BookSearchResponse>() {
            @Override
            public void onResponse(Call<BookSearchResponse> call, Response<BookSearchResponse> response) {
                android.util.Log.d("HomeFragment", "API Response received. Code: " + response.code());
                
                if (response.isSuccessful()) {
                    BookSearchResponse searchResponse = response.body();
                    
                    if (searchResponse != null) {
                        android.util.Log.d("HomeFragment", "Response body exists");
                        
                        if (searchResponse.docs != null) {
                            android.util.Log.d("HomeFragment", "Docs list size: " + searchResponse.docs.size());
                            
                            if (searchResponse.docs.isEmpty()) {
                                android.util.Log.e("HomeFragment", "Docs list is EMPTY!");
                                loadFallbackBooks();
                            } else {
                                // Add ALL books - no filter!
                                for (BookSearchResponse.BookResult result : searchResponse.docs) {
                                    Book book = new Book(
                                            result.getUniqueId(),
                                            result.title,
                                            result.getAuthorName(),
                                            "Popular book from Open Library",
                                            result.getCoverUrl(),
                                            4.0f + (float)(Math.random() * 0.8f),
                                            (int)(500 + Math.random() * 1500)
                                    );
                                    booksList.add(book);
                                    android.util.Log.d("HomeFragment", "Added book #" + booksList.size() + ": " + book.getTitle());
                                }
                                android.util.Log.d("HomeFragment", "SUCCESS! Total books loaded: " + booksList.size());
                            }
                        } else {
                            android.util.Log.e("HomeFragment", "Docs list is NULL!");
                            loadFallbackBooks();
                        }
                    } else {
                        android.util.Log.e("HomeFragment", "Response body is NULL!");
                        loadFallbackBooks();
                    }
                } else {
                    android.util.Log.e("HomeFragment", "API Response not successful: " + response.message());
                    loadFallbackBooks();
                }
                
                booksAdapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Call<BookSearchResponse> call, Throwable t) {
                android.util.Log.e("HomeFragment", "API FAILED: " + t.getMessage(), t);
                progressBar.setVisibility(View.GONE);
                loadFallbackBooks();
            }
        });
    }
    
    /**
     * Fallback to mock books if API fails
     * Uses real cover URLs from Open Library
     */
    private void loadFallbackBooks() {
        android.util.Log.d("HomeFragment", "Loading fallback mock books...");
        booksList.clear();
        
        // Using test cover URL for all books
        String testCoverUrl = "https://res.cloudinary.com/deptrtc9n/image/upload/image_2026-04-30_132810898_qrqpc7.png";
        booksList.add(new Book("1", "The Great Gatsby", "F. Scott Fitzgerald", 
            "A classic novel about wealth and love", testCoverUrl, 4.5f, 1200));
        booksList.add(new Book("2", "To Kill a Mockingbird", "Harper Lee", 
            "A gripping tale of racial injustice", testCoverUrl, 4.8f, 950));
        booksList.add(new Book("3", "1984", "George Orwell", 
            "A dystopian novel of totalitarianism", testCoverUrl, 4.6f, 1100));
        booksList.add(new Book("4", "Pride and Prejudice", "Jane Austen", 
            "A romantic novel of manners and marriage", testCoverUrl, 4.7f, 800));
        booksList.add(new Book("5", "The Catcher in the Rye", "J.D. Salinger", 
            "A story of teenage alienation", testCoverUrl, 4.3f, 750));
        booksList.add(new Book("6", "Brave New World", "Aldous Huxley", 
            "A science fiction vision of the future", testCoverUrl, 4.4f, 890));
        
        android.util.Log.d("HomeFragment", "Fallback books loaded: " + booksList.size());
        booksAdapter.notifyDataSetChanged();
    }
}
