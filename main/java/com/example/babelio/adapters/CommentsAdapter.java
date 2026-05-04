package com.example.babelio.adapters;

import android.content.Context;
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
import com.example.babelio.models.Comment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for displaying comments/reviews in RecyclerView
 */
public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.CommentViewHolder> {
    private List<Comment> commentsList;
    private Context context;

    public CommentsAdapter(List<Comment> commentsList) {
        this.commentsList = commentsList;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = commentsList.get(position);
        holder.bindComment(comment);
    }

    @Override
    public int getItemCount() {
        return commentsList.size();
    }

    /**
     * ViewHolder for comment items
     */
    class CommentViewHolder extends RecyclerView.ViewHolder {
        ImageView userProfileImageView;
        TextView userNameTextView, commentTextTextView, dateTextView;
        RatingBar ratingBar;

        CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            userProfileImageView = itemView.findViewById(R.id.userProfileImageView);
            userNameTextView = itemView.findViewById(R.id.userNameTextView);
            commentTextTextView = itemView.findViewById(R.id.commentTextTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            ratingBar = itemView.findViewById(R.id.ratingBar);
        }

        void bindComment(Comment comment) {
            userNameTextView.setText(comment.getUserName());
            commentTextTextView.setText(comment.getText());
            ratingBar.setRating((float) comment.getRating());

            // Format timestamp
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            String dateString = sdf.format(new Date(comment.getTimestamp()));
            dateTextView.setText(dateString);

            // Load user profile image
            if (comment.getUserProfileImage() != null && !comment.getUserProfileImage().isEmpty()) {
                Glide.with(context)
                        .load(comment.getUserProfileImage())
                        .placeholder(R.drawable.person)
                        .error(R.drawable.person)
                        .into(userProfileImageView);
            } else {
                userProfileImageView.setImageResource(R.drawable.person);
            }
        }
    }
}
