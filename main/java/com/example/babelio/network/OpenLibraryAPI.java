package com.example.babelio.network;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Open Library API interface for fetching book data
 * No authentication required!
 */
public interface OpenLibraryAPI {
    
    /**
     * Search books by title
     * @param title Book title to search
     * @param limit Number of results (default 10)
     * @return SearchResponse containing book results
     */
    @GET("search.json")
    Call<BookSearchResponse> searchBooks(
        @Query("title") String title,
        @Query("limit") int limit
    );
    
    /**
     * Search books by author
     */
    @GET("search.json")
    Call<BookSearchResponse> searchByAuthor(
        @Query("author") String author,
        @Query("limit") int limit
    );
}
