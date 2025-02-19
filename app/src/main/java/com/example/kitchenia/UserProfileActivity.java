package com.example.kitchenia;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.kitchenia.card.Carta;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class UserProfileActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView ivProfileImage;
    private EditText etDescription;
    private Button btnFollow, btnChangeProfileImage, btnEditDescription, btnSaveDescription;
    private TextView tvUsername, tvFollowersCount, tvPostCount, tvFollowingCount;
    private SharedPreferences preferences;
    private FirebaseAuth mAuth;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private boolean isFollowing;
    private int followersCount;
    private FirebaseFirestore db;

    // New fields for the RecyclerView
    private RecyclerView rvUserImages;
    private List<Carta> userCartasList;
    private CartaAdapter userCartaAdapter;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference().child("profile_images");

        ivProfileImage = findViewById(R.id.ivProfileImage);
        etDescription = findViewById(R.id.etDescription);
        btnEditDescription = findViewById(R.id.btnEditBio);
        btnSaveDescription = findViewById(R.id.btnSaveDescription);
        btnFollow = findViewById(R.id.btnFollow);
        btnChangeProfileImage = findViewById(R.id.btnChangeProfileImage);
        tvUsername = findViewById(R.id.tvUsername);
        tvFollowersCount = findViewById(R.id.tvFollowersCount);
        tvPostCount = findViewById(R.id.tvPostCount);
        tvFollowingCount = findViewById(R.id.tvFollowingCount);

        preferences = getSharedPreferences("USER_DATA", MODE_PRIVATE);
        String userDescription = preferences.getString("USER_DESCRIPTION", "Sin descripción");
        etDescription.setText(userDescription);

        followersCount = preferences.getInt("FOLLOWERS_COUNT", 0);
        isFollowing = preferences.getBoolean("IS_FOLLOWING", false);
        tvFollowersCount.setText(String.valueOf(followersCount));

        if (mAuth.getCurrentUser() != null) {
            String userId = mAuth.getCurrentUser().getUid();
            db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String nombre = documentSnapshot.getString("username");
                        tvUsername.setText(nombre);
                    }
                })
                .addOnFailureListener(e ->
                    Toast.makeText(this, "Error loading username", Toast.LENGTH_SHORT).show()
                );
        }

        actualizarBotonSeguir();
        cargarImagenPerfil();

        // Initialize RecyclerView for user cards
        rvUserImages = findViewById(R.id.rvUserImages);
        rvUserImages.setLayoutManager(new LinearLayoutManager(this));
        userCartasList = new ArrayList<>();
        userCartaAdapter = new CartaAdapter(userCartasList);
        rvUserImages.setAdapter(userCartaAdapter);

        // Load user's cards
        cargarCartasDelUsuario();

        btnEditDescription.setOnClickListener(v -> {
            etDescription.setEnabled(true);
            btnSaveDescription.setVisibility(View.VISIBLE);
            btnEditDescription.setVisibility(View.GONE);
        });

        btnSaveDescription.setOnClickListener(v -> {
            String nuevaDescripcion = etDescription.getText().toString().trim();
            etDescription.setEnabled(false);
            btnSaveDescription.setVisibility(View.GONE);
            btnEditDescription.setVisibility(View.VISIBLE);
            preferences.edit().putString("USER_DESCRIPTION", nuevaDescripcion).apply();
            Toast.makeText(this, "Descripción actualizada", Toast.LENGTH_SHORT).show();
        });

        btnFollow.setOnClickListener(v -> toggleFollow());
        btnChangeProfileImage.setOnClickListener(v -> openFileChooser());

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(this, MainActivity.class));
                return true;
            } else if (itemId == R.id.nav_upload) {
                startActivity(new Intent(this, UploadPhotoActivity.class));
                return true;
            } else if (itemId == R.id.nav_profile) {
                return true;
            }
            return false;
        });
    }

    // Load user's cards from Firestore
    private void cargarCartasDelUsuario() {
        if (mAuth.getCurrentUser() != null) {
            String userId = mAuth.getCurrentUser().getUid();
            db.collection("imagenes")
                    .whereEqualTo("userId", userId)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        userCartasList.clear();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            UploadPhotoActivity.Imagen imagen =
                                    document.toObject(UploadPhotoActivity.Imagen.class);
                            Carta carta = new Carta(
                                "Receta " + document.getId(),
                                imagen.getDescripcion(),
                                0,
                                false,
                                false,
                                imagen.getUrl()
                            );
                            userCartasList.add(carta);
                        }
                        userCartaAdapter.notifyDataSetChanged();
                    })
                    .addOnFailureListener(e ->
                        Toast.makeText(this, "Error loading user cards", Toast.LENGTH_SHORT).show()
                    );
        }
    }

    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK &&
                data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            ivProfileImage.setImageURI(imageUri);
            subirImagenAFirebase(imageUri);
        }
    }

    private void subirImagenAFirebase(Uri imageUri) {
        if (mAuth.getCurrentUser() == null) return;
        String userId = mAuth.getCurrentUser().getUid();
        StorageReference fileRef = storageRef.child(userId + ".jpg");
        fileRef.putFile(imageUri).addOnSuccessListener(taskSnapshot ->
            fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                preferences.edit().putString("USER_IMAGE_URL", uri.toString()).apply();
                Toast.makeText(this, "Imagen subida", Toast.LENGTH_SHORT).show();
            })
        ).addOnFailureListener(e ->
            Toast.makeText(this, "Error al subir imagen: " + e.getMessage(), Toast.LENGTH_SHORT).show()
        );
    }

    private void cargarImagenPerfil() {
        String imageUrl = preferences.getString("USER_IMAGE_URL", null);
        if (imageUrl != null) {
            Glide.with(this).load(imageUrl).into(ivProfileImage);
        } else {
            ivProfileImage.setImageBitmap(
                BitmapFactory.decodeResource(getResources(), R.drawable.default_profile)
            );
        }
    }

    private void toggleFollow() {
        if (isFollowing && followersCount > 0) {
            followersCount--;
            isFollowing = false;
        } else {
            followersCount++;
            isFollowing = true;
        }
        preferences.edit()
                .putBoolean("IS_FOLLOWING", isFollowing)
                .putInt("FOLLOWERS_COUNT", followersCount)
                .apply();
        tvFollowersCount.setText(String.valueOf(followersCount));
        actualizarBotonSeguir();
    }

    private void actualizarBotonSeguir() {
        btnFollow.setText(isFollowing ? "Dejar de seguir" : "Seguir");
    }
}
