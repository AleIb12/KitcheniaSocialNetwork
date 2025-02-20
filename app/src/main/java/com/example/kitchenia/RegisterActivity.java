package com.example.kitchenia;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Activity for user registration.
 * Allows users to create a new account with email and password.
 */
public class RegisterActivity extends AppCompatActivity {

    // UI elements
    private EditText etCorreo, etNombre, etPassword, etPasswordConfirm;
    private Button btnCrearCuenta;

    // Firebase instances
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    /**
     * Called when the activity is first created.
     * Initializes the UI elements and sets up the Firebase instances.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     * previously being shut down then this Bundle contains the data it most
     * recently supplied in onSaveInstanceState(Bundle). Note: Otherwise it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize UI elements
        etCorreo = findViewById(R.id.etCorreo);
        etNombre = findViewById(R.id.etNombre);
        etPassword = findViewById(R.id.etPassword);
        etPasswordConfirm = findViewById(R.id.etPasswordConfirm);
        btnCrearCuenta = findViewById(R.id.btnCrearCuenta);

        // Initialize Firebase instances
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Set up the button click listener for account creation
        btnCrearCuenta.setOnClickListener(v -> {
            String correo = etCorreo.getText().toString().trim();
            String nombre = etNombre.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirmPassword = etPasswordConfirm.getText().toString().trim();

            // Validate input and create a new user
            if (password.equals(confirmPassword) && !correo.isEmpty()) {
                mAuth.createUserWithEmailAndPassword(correo, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            String userId = mAuth.getCurrentUser().getUid();
                            db.collection("users").document(userId)
                                .set(new User(nombre))
                                .addOnSuccessListener(aVoid ->
                                    Toast.makeText(
                                        RegisterActivity.this,
                                        "Registration successful",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                );
                            startActivity(new Intent(
                                RegisterActivity.this, MainActivity.class));
                            finish();
                        } else {
                            Toast.makeText(
                                RegisterActivity.this,
                                "Error: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT
                            ).show();
                        }
                    });
            } else {
                Toast.makeText(
                    RegisterActivity.this,
                    "All fields are required and passwords must match",
                    Toast.LENGTH_SHORT
                ).show();
            }
        });
    }

    /**
     * Simple POJO class to store user information in Firestore.
     */
    public static class User {
        private String username;

        // Default constructor required for calls to DataSnapshot.getValue(User.class)
        public User() {}

        /**
         * Constructor to initialize the username.
         *
         * @param username The username of the user.
         */
        public User(String username) { this.username = username; }

        /**
         * Gets the username.
         *
         * @return The username of the user.
         */
        public String getUsername() { return username; }

        /**
         * Sets the username.
         *
         * @param username The username to set.
         */
        public void setUsername(String username) { this.username = username; }
    }
}