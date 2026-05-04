package com.example.babelio.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.babelio.R;
import com.example.babelio.activities.BookDetailActivity;
import com.example.babelio.models.Book;

import java.util.List;

/**
 * Adapter for displaying books in RecyclerView
 */
public class BooksAdapter extends RecyclerView.Adapter<BooksAdapter.BookViewHolder> {
    private List<Book> booksList;
    private Context context;

    public BooksAdapter(List<Book> booksList) {
        this.booksList = booksList;
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_book, parent, false);
        return new BookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        Book book = booksList.get(position);
        holder.bindBook(book);
    }

    @Override
    public int getItemCount() {
        return booksList.size();
    }

    /**
     * Update the adapter with new data
     */
    public void updateBooks(List<Book> newBooks) {
        booksList.clear();
        booksList.addAll(newBooks);
        notifyDataSetChanged();
    }

    /**
     * ViewHolder for book items
     */
    class BookViewHolder extends RecyclerView.ViewHolder {
        ImageView bookCoverImageView;
        TextView titleTextView, authorTextView, ratingValue, genreTag;

        BookViewHolder(@NonNull View itemView) {
            super(itemView);
            bookCoverImageView = itemView.findViewById(R.id.bookCoverImageView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            authorTextView = itemView.findViewById(R.id.authorTextView);
            ratingValue = itemView.findViewById(R.id.ratingValue);
            genreTag = itemView.findViewById(R.id.genreTag);
        }

        void bindBook(Book book) {
            titleTextView.setText(book.getTitle());
            authorTextView.setText(book.getAuthor());
            if (ratingValue != null) {
                ratingValue.setText(String.format("%.1f", book.getRating()));
            }
            if (genreTag != null) {
                genreTag.setText(book.getGenre());
            }

            // Load image with Glide
            Glide.with(context)
                    .load(book.getImageUrl())
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_foreground)
                    .into(bookCoverImageView);

            // Click listener to open book details
            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, BookDetailActivity.class);
                intent.putExtra("book", book);
                context.startActivity(intent);
            });
        }
    }
}
