package com.example.babelio.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.babelio.R;
import com.example.babelio.network.BookSearchResponse;
import com.example.babelio.network.OpenLibraryAPI;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Test Activity to debug Open Library API
 * Remove this after testing!
 */
public class TestAPIActivity extends AppCompatActivity {
    private static final String TAG = "TestAPI";
    private TextView resultTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_api);

        resultTextView = findViewById(R.id.resultTextView);
        Button testButton = findViewById(R.id.testButton);

        testButton.setOnClickListener(v -> testAPI());
    }

    private void testAPI() {
        resultTextView.setText("Testing API...\n");

        // Create Retrofit instance
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://openlibrary.org/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        OpenLibraryAPI api = retrofit.create(OpenLibraryAPI.class);

        // Test search for "Harry Potter"
        Call<BookSearchResponse> call = api.searchBooks("Harry Potter", 5);

        call.enqueue(new Callback<BookSearchResponse>() {
            @Override
            public void onResponse(Call<BookSearchResponse> call, Response<BookSearchResponse> response) {
                Log.d(TAG, "Response Code: " + response.code());
                resultTextView.append("Response Code: " + response.code() + "\n\n");

                if (response.isSuccessful()) {
                    BookSearchResponse searchResponse = response.body();
                    
                    if (searchResponse != null && searchResponse.docs != null) {
                        resultTextView.append("Books Found: " + searchResponse.docs.size() + "\n\n");
                        Log.d(TAG, "Books Found: " + searchResponse.docs.size());

                        for (int i = 0; i < Math.min(3, searchResponse.docs.size()); i++) {
                            BookSearchResponse.BookResult book = searchResponse.docs.get(i);
                            String info = "Book " + (i+1) + ":\n" +
                                    "Title: " + book.title + "\n" +
                                    "Author: " + book.getAuthorName() + "\n" +
                                    "Cover URL: " + book.getCoverUrl() + "\n\n";
                            resultTextView.append(info);
                            Log.d(TAG, info);
                        }
                        Toast.makeText(TestAPIActivity.this, "API Works! Found " + searchResponse.docs.size() + " books", Toast.LENGTH_LONG).show();
                    } else {
                        resultTextView.append("Response body is null!");
                        Log.d(TAG, "Response body is null");
                    }
                } else {
                    resultTextView.append("Response failed: " + response.message() + "\n");
                    Log.d(TAG, "Response failed: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<BookSearchResponse> call, Throwable t) {
                String errorMsg = "API Call Failed: " + t.getMessage();
                resultTextView.append(errorMsg + "\n");
                Log.e(TAG, errorMsg, t);
                Toast.makeText(TestAPIActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
