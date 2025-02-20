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
 * Activity for user login.
 */
public class LoginActivity extends AppCompatActivity {

    // Regular expressions for email and password validation
    private static final Pattern EMAIL_REGEX = Patterns.EMAIL_ADDRESS;
    private static final Pattern PASSWORD_REGEX =
            Pattern.compile("^(?=.*[0-9])(?=.*[a-zA-Z]).{6,}$");
    private static final int RC_SIGN_IN = 100;

    // UI elements
    private EditText emailEditText, passwordEditText;
    private Button loginButton, googleButton, facebookButton;
    private TextView forgotPasswordText, createAccountText;
    private ImageView logoImage;

    // Firebase and Google Sign-In instances
    private FirebaseAuth mAuth;
    private GoogleSignInClient googleSignInClient;

    /**
     * Called when the activity is first created.
     * Initializes the UI elements and sets up Firebase and Google Sign-In.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     * previously being shut down then this Bundle contains the data it most
     * recently supplied in onSaveInstanceState(Bundle). Note: Otherwise it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize UI elements
        logoImage = findViewById(R.id.logoImage);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        googleButton = findViewById(R.id.googleButton);
        facebookButton = findViewById(R.id.facebookButton);
        forgotPasswordText = findViewById(R.id.forgotPasswordText);
        createAccountText = findViewById(R.id.createAccountText);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        configureGoogleSignIn(); // Initialize Google Sign-In configuration

        // Set up login button click listener
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

        // Set up Google Sign-In button click listener
        googleButton.setOnClickListener(v -> signInWithGoogle());

        // Set up Facebook button click listener
        facebookButton.setOnClickListener(v ->
                Toast.makeText(this, "Facebook", Toast.LENGTH_SHORT).show()
        );

        // Set up forgot password text click listener
        forgotPasswordText.setOnClickListener(v -> {
            String url = "https://kitchenia-a5d3d.firebaseapp.com/__/auth/action?mode=action&oobCode=code";
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        });

        // Set up create account text click listener
        createAccountText.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class))
        );
    }

    /**
     * Configures Google Sign-In options.
     */
    private void configureGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    /**
     * Initiates Google Sign-In process.
     */
    private void signInWithGoogle() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    /**
     * Handles the result of the Google Sign-In intent.
     *
     * @param requestCode The request code passed to startActivityForResult().
     * @param resultCode The result code returned by the child activity.
     * @param data An Intent that carries the result data.
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
    @Override
    protected void onStart() {
        super.onStart();
        if (mAuth.getCurrentUser() != null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    /**
     * Called when the activity is started.
     * Checks if the user is already signed in and redirects to MainActivity if so.
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
     * Authenticates the user with Firebase using Google Sign-In credentials.
     *
     * @param account The GoogleSignInAccount obtained from the Google Sign-In intent.
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