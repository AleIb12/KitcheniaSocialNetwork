package com.example.kitchenia;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Activity for user registration.
 * Allows users to create a new account with email and password.
 */
public class RegisterActivity extends AppCompatActivity {

    // UI elements
    private EditText etCorreo, etNombre, etPassword, etPasswordConfirm;
    private Button btnCrearCuenta, googleButton;
    private TextView loginText;  // Agregado para el botón de navegación

    // Firebase instances
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private static final int RC_SIGN_IN = 100;

    /**
     * Called when the activity is first created.
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
        googleButton = findViewById(R.id.googleButton);
        loginText = findViewById(R.id.loginText); // Inicialización del nuevo botón

        // Initialize Firebase instances
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Navegar a LoginActivity cuando el usuario haga clic en "loginText"
        loginText.setOnClickListener(v ->
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class))
        );

        // Set up the button click listener for account creation
        btnCrearCuenta.setOnClickListener(v -> {
            String correo = etCorreo.getText().toString().trim();
            String nombre = etNombre.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirmPassword = etPasswordConfirm.getText().toString().trim();

            if (password.equals(confirmPassword) && !correo.isEmpty()) {
                mAuth.createUserWithEmailAndPassword(correo, password)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                String userId = mAuth.getCurrentUser().getUid();
                                db.collection("users").document(userId)
                                        .set(new User(nombre))
                                        .addOnSuccessListener(aVoid ->
                                                Toast.makeText(RegisterActivity.this,
                                                        "Registration successful", Toast.LENGTH_SHORT).show()
                                        );
                                startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                                finish();
                            } else {
                                Toast.makeText(RegisterActivity.this,
                                        "Error: " + task.getException().getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                Toast.makeText(RegisterActivity.this,
                        "All fields are required and passwords must match",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Simple POJO class to store user information in Firestore.
     */
    public static class User {
        private String username;

        public User() {}

        public User(String username) { this.username = username; }

        public String getUsername() { return username; }

        public void setUsername(String username) { this.username = username; }
    }

}
