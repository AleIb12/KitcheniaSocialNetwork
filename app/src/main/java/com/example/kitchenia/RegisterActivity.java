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
 * Activity para el registro de usuarios.
 * Permite a los usuarios crear una nueva cuenta con correo electrónico y contraseña.
 */
public class RegisterActivity extends AppCompatActivity {

    // Elementos de la interfaz de usuario
    private EditText etCorreo, etNombre, etPassword, etPasswordConfirm;
    private Button btnCrearCuenta, googleButton;
    private TextView loginText;  // Agregado para el botón de navegación

    // Instancias de Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private static final int RC_SIGN_IN = 100;

    /**
     * Llamado cuando la actividad es creada por primera vez.
     * Inicializa los elementos de la interfaz de usuario y las instancias de Firebase.
     *
     * @param savedInstanceState Si la actividad está siendo re-inicializada después de
     * haber sido previamente cerrada, este Bundle contiene los datos más recientes
     * suministrados en onSaveInstanceState(Bundle). Nota: De lo contrario, es null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Inicializar elementos de la interfaz de usuario
        etCorreo = findViewById(R.id.etCorreo);
        etNombre = findViewById(R.id.etNombre);
        etPassword = findViewById(R.id.etPassword);
        etPasswordConfirm = findViewById(R.id.etPasswordConfirm);
        btnCrearCuenta = findViewById(R.id.btnCrearCuenta);
        loginText = findViewById(R.id.loginText); // Inicialización del nuevo botón

        // Inicializar instancias de Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Navegar a LoginActivity cuando el usuario haga clic en "loginText"
        loginText.setOnClickListener(v ->
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class))
        );

        // Configurar el listener del botón para la creación de cuenta
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
                                                        "Registro exitoso", Toast.LENGTH_SHORT).show()
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
                        "Todos los campos son obligatorios y las contraseñas deben coincidir",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Clase POJO simple para almacenar información del usuario en Firestore.
     */
    public static class User {
        private String username;

        public User() {}

        public User(String username) { this.username = username; }

        public String getUsername() { return username; }

        public void setUsername(String username) { this.username = username; }
    }

}