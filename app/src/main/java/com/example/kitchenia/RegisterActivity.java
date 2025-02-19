// `RegisterActivity.java`
package com.example.kitchenia;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class RegisterActivity extends AppCompatActivity {
    private EditText etCorreo, etNombre, etPassword, etPasswordConfirm;
    private Button btnCrearCuenta;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etCorreo = findViewById(R.id.etCorreo);
        etNombre = findViewById(R.id.etNombre);
        etPassword = findViewById(R.id.etPassword);
        etPasswordConfirm = findViewById(R.id.etPasswordConfirm);
        btnCrearCuenta = findViewById(R.id.btnCrearCuenta);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

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

    // Simple POJO class to store in Firestore
    public static class User {
        private String username;
        public User() {}
        public User(String username) { this.username = username; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
    }
}