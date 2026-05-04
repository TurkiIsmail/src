package com.example.babelio.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.babelio.R;
import com.example.babelio.activities.LoginActivity;
import com.example.babelio.database.DatabaseHelper;
import com.example.babelio.firebase.FirebaseHelper;
import com.example.babelio.models.Book;
import com.example.babelio.models.User;

import java.util.List;

/**
 * Profile Fragment showing user profile information
 * Fully Cloud Connected
 */
public class ProfileFragment extends Fragment {
    private ImageView profileImageView;
    private TextView nameTextView, emailTextView, bioTextView, favoritesCountTextView, reviewsCountTextView;
    private Button logoutButton;
    private ProgressBar progressBar;
    private FirebaseHelper firebaseHelper;
    private DatabaseHelper dbHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firebaseHelper = new FirebaseHelper();
        dbHelper = new DatabaseHelper(getContext());

        // Initialize UI
        profileImageView = view.findViewById(R.id.profileImageView);
        nameTextView = view.findViewById(R.id.nameTextView);
        emailTextView = view.findViewById(R.id.emailTextView);
        bioTextView = view.findViewById(R.id.bioTextView);
        favoritesCountTextView = view.findViewById(R.id.favoritesCountTextView);
        reviewsCountTextView = view.findViewById(R.id.reviewsCountTextView);
        logoutButton = view.findViewById(R.id.logoutButton);
        progressBar = view.findViewById(R.id.progressBar);

        // Setup logout button
        logoutButton.setOnClickListener(v -> handleLogout());

        // Load real user profile from Firestore
        loadUserProfile();
    }

    private void handleLogout() {
        firebaseHelper.logoutUser();
        Intent intent = new Intent(requireContext(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    /**
     * Load user profile from Firestore
     */
    private void loadUserProfile() {
        if (!firebaseHelper.isUserLoggedIn()) return;

        progressBar.setVisibility(View.VISIBLE);
        String uid = firebaseHelper.getCurrentUser().getUid();

        firebaseHelper.getUserFromFirestore(uid, new FirebaseHelper.UserCallback() {
            @Override
            public void onSuccess(User user) {
                if (isAdded()) {
                    progressBar.setVisibility(View.GONE);
                    displayUserProfile(user);
                }
            }

            @Override
            public void onFailure(String error) {
                if (isAdded()) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Error loading profile: " + error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Display user profile information
     */
    private void displayUserProfile(User user) {
        nameTextView.setText(user.getName());
        emailTextView.setText(user.getEmail());
        bioTextView.setText(user.getBio() == null || user.getBio().isEmpty() ? "No bio added yet" : user.getBio());
        favoritesCountTextView.setText(String.valueOf(user.getFavoritesCount()));
        reviewsCountTextView.setText(String.valueOf(user.getReviewsCount()));

        // Load profile image
        if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(user.getProfileImageUrl())
                    .placeholder(R.drawable.person)
                    .error(R.drawable.person)
                    .into(profileImageView);
        } else {
            profileImageView.setImageResource(R.drawable.person);
        }
    }
}
