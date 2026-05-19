package com.example.babelio.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.babelio.R;
import com.example.babelio.activities.LoginActivity;
import com.example.babelio.firebase.FirebaseHelper;
import com.example.babelio.models.User;

/**
 * Profile Fragment showing user profile information
 * Fully Connected with Cloudinary Uploads
 */
public class ProfileFragment extends Fragment {
    private static final String TAG = "ProfileFragment";
    private ImageView profileImageView;
    private TextView nameTextView, bioTextView;
    private Button logoutButton;
    private ProgressBar progressBar;
    private FirebaseHelper firebaseHelper;
    private ActivityResultLauncher<String> pickerLauncher;
    private Uri selectedImageUri;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firebaseHelper = new FirebaseHelper();
        
        pickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null && isAdded()) {
                        selectedImageUri = uri;
                        uploadNewProfileImage();
                    }
                }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize UI
        profileImageView = view.findViewById(R.id.profileImageView);
        nameTextView = view.findViewById(R.id.nameTextView);
        bioTextView = view.findViewById(R.id.bioTextView);
        logoutButton = view.findViewById(R.id.logoutButton);
        progressBar = view.findViewById(R.id.progressBar);

        // Setup image click to change profile picture
        profileImageView.setOnClickListener(v -> {
            if (firebaseHelper.isUserLoggedIn()) {
                pickerLauncher.launch("image/*");
            } else {
                Toast.makeText(requireContext(), "Please login to change profile image", Toast.LENGTH_SHORT).show();
            }
        });

        logoutButton.setOnClickListener(v -> {
            firebaseHelper.logoutUser();
            Intent intent = new Intent(requireContext(), LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            requireActivity().finish();
        });

        // Load real user data if logged in
        if (firebaseHelper.isUserLoggedIn()) {
            loadUserProfile();
        } else {
            loadMockUserProfile();
        }
    }

    /**
     * Upload selected image to Cloudinary
     */
    private void uploadNewProfileImage() {
        if (selectedImageUri == null || !firebaseHelper.isUserLoggedIn()) return;

        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        String userId = firebaseHelper.getCurrentUser().getUid();

        firebaseHelper.uploadProfileImage(selectedImageUri, userId, new FirebaseHelper.AuthCallback() {
            @Override
            public void onSuccess() {
                if (isAdded()) {
                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }
                    Toast.makeText(getContext(), "Profile image updated!", Toast.LENGTH_SHORT).show();
                    loadUserProfile(); // Refresh data
                }
            }

            @Override
            public void onFailure(String error) {
                if (isAdded()) {
                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }
                    Toast.makeText(getContext(), "Upload failed: " + error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Load real user profile from Firestore
     */
    private void loadUserProfile() {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        String userId = firebaseHelper.getCurrentUser().getUid();

        firebaseHelper.getUserFromFirestore(userId, new FirebaseHelper.UserCallback() {
            @Override
            public void onSuccess(User user) {
                if (isAdded()) {
                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }
                    displayUserProfile(user);
                }
            }

            @Override
            public void onFailure(String error) {
                if (isAdded()) {
                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }
                    loadMockUserProfile();
                }
            }
        });
    }

    /**
     * Load mock user profile
     */
    private void loadMockUserProfile() {
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
        
        User user = new User();
        user.setUid("123");
        user.setName("John Reader");
        user.setEmail("reader@example.com");
        user.setBio("Book lover and adventure seeker");
        user.setFavoritesCount(8);
        user.setReviewsCount(15);
        user.setProfileImageUrl("https://via.placeholder.com/120?text=Profile");
        
        displayUserProfile(user);
    }

    /**
     * Display user profile information
     */
    private void displayUserProfile(User user) {
        nameTextView.setText(user.getName());
        bioTextView.setText(user.getBio().isEmpty() ? "No bio added yet" : user.getBio());
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
