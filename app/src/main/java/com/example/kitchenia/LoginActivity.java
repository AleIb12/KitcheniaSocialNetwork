package com.example.kitchenia;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.regex.Pattern;

/**
 * Activity para el inicio de sesión de usuarios.
 */
public class LoginActivity extends AppCompatActivity {

    // Expresiones regulares para la validación de correo y contraseña
    private static final Pattern EMAIL_REGEX = Patterns.EMAIL_ADDRESS;
    private static final Pattern PASSWORD_REGEX =
            Pattern.compile("^(?=.*[0-9])(?=.*[a-zA-Z]).{6,}$");
    private static final int RC_SIGN_IN = 100;

    // Elementos de la interfaz de usuario
    private EditText emailEditText, passwordEditText;
    private Button loginButton, googleButton;
    private TextView forgotPasswordText, createAccountText;
    private ImageView logoImage;

    // Instancias de Firebase y Google Sign-In
    private FirebaseAuth mAuth;
    private GoogleSignInClient googleSignInClient;

    /**
     * Llamado cuando la actividad es creada por primera vez.
     * Inicializa los elementos de la interfaz de usuario y configura Firebase y Google Sign-In.
     *
     * @param savedInstanceState Si la actividad está siendo re-inicializada después de
     * haber sido previamente cerrada, este Bundle contiene los datos más recientes
     * suministrados en onSaveInstanceState(Bundle). Nota: De lo contrario, es null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Inicializar elementos de la interfaz de usuario
        logoImage = findViewById(R.id.logoImage);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        googleButton = findViewById(R.id.googleButton);
        forgotPasswordText = findViewById(R.id.forgotPasswordText);
        createAccountText = findViewById(R.id.createAccountText);

        // Inicializar Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        configureGoogleSignIn(); // Inicializar configuración de Google Sign-In

        // Configurar listener para el botón de inicio de sesión
        loginButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (!EMAIL_REGEX.matcher(email).matches()) {
                Toast.makeText(this, "Formato de correo inválido", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!PASSWORD_REGEX.matcher(password).matches()) {
                Toast.makeText(this, "La contraseña debe contener letras y dígitos, mínimo 6 caracteres",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, "Inicio de sesión exitoso",
                                    Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish();
                        } else {
                            Toast.makeText(LoginActivity.this,
                                    "Error: " + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        // Configurar listener para el botón de Google Sign-In
        googleButton.setOnClickListener(v -> signInWithGoogle());

        // Configurar listener para el texto de "Olvidé mi contraseña"
        forgotPasswordText.setOnClickListener(v -> {
            String url = "https://kitchenia-a5d3d.firebaseapp.com/__/auth/action?mode=action&oobCode=code";
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        });

        // Configurar listener para el texto de "Crear cuenta"
        createAccountText.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class))
        );
    }

    /**
     * Configura las opciones de Google Sign-In.
     */
    private void configureGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    /**
     * Inicia el proceso de Google Sign-In.
     */
    private void signInWithGoogle() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    /**
     * Maneja el resultado del intent de Google Sign-In.
     *
     * @param requestCode El código de solicitud pasado a startActivityForResult().
     * @param resultCode El código de resultado devuelto por la actividad hija.
     * @param data Un Intent que lleva los datos del resultado.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                Toast.makeText(this, "Error al iniciar sesión con Google", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Llamado cuando la actividad es iniciada.
     * Verifica si el usuario ya ha iniciado sesión y redirige a MainActivity si es así.
     */
    @Override
    protected void onStart() {
        super.onStart();
        if (mAuth.getCurrentUser() != null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    /**
     * Autentica al usuario con Firebase usando las credenciales de Google Sign-In.
     *
     * @param account La cuenta de GoogleSignInAccount obtenida del intent de Google Sign-In.
     */
    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Inicio de sesión exitoso",
                        Toast.LENGTH_SHORT).show();
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
            } else {
                Toast.makeText(this, "Error al iniciar sesión con Google: " +
                                task.getException().getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}