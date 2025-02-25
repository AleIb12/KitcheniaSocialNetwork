package com.example.kitchenia;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {

    private static final Pattern EMAIL_REGEX = Patterns.EMAIL_ADDRESS;
    private static final Pattern PASSWORD_REGEX = Pattern.compile("^(?=.*[0-9])(?=.*[a-zA-Z]).{6,}$");

    private EditText emailEditText, passwordEditText;
    private ProgressDialog progressDialog;
    private FirebaseAuth mAuth;
    private GoogleSignInClient googleSignInClient;

    // ActivityResultLauncher para el inicio de sesión con Google
    private ActivityResultLauncher<Intent> googleSignInLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initViews();
        setupFirebase();
        configureGoogleSignIn();
        setupListeners();
        registerGoogleSignInLauncher();
    }

    private void initViews() {
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
    }

    private void setupFirebase() {
        mAuth = FirebaseAuth.getInstance();
    }

    private void configureGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void setupListeners() {
        Button loginButton = findViewById(R.id.loginButton);
        Button googleButton = findViewById(R.id.googleButton);
        TextView forgotPasswordText = findViewById(R.id.forgotPasswordText);
        TextView createAccountText = findViewById(R.id.createAccountText);

        loginButton.setOnClickListener(v -> attemptEmailPasswordLogin());
        googleButton.setOnClickListener(v -> signInWithGoogle());
        forgotPasswordText.setOnClickListener(v -> showForgotPasswordDialog());
        createAccountText.setOnClickListener(v -> navigateToRegister());
    }

    private void registerGoogleSignInLauncher() {
        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        handleGoogleSignInResult(result.getData());
                    }
                }
        );
    }

    private void attemptEmailPasswordLogin() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (!validateEmail(email) || !validatePassword(password)) {
            return;
        }

        showProgressDialog();
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    hideProgressDialog();
                    if (task.isSuccessful()) {
                        navigateToMain();
                    } else {
                        handleLoginError(task.getException());
                    }
                });
    }

    private boolean validateEmail(String email) {
        if (!EMAIL_REGEX.matcher(email).matches()) {
            showError(getString(R.string.invalid_email_format));
            return false;
        }
        return true;
    }

    private boolean validatePassword(String password) {
        if (!PASSWORD_REGEX.matcher(password).matches()) {
            showError(getString(R.string.invalid_password));
            return false;
        }
        return true;
    }

    private void signInWithGoogle() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        googleSignInLauncher.launch(signInIntent);
    }

    private void showForgotPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.reset_password);
        builder.setMessage(R.string.enter_email_reset);

        final EditText input = new EditText(this);
        // Se agregan banderas para asegurar el tipo de entrada (correo)
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        builder.setView(input);

        builder.setPositiveButton(R.string.send, (dialog, which) -> {
            String email = input.getText().toString().trim();
            if (EMAIL_REGEX.matcher(email).matches()) {
                sendPasswordResetEmail(email);
            } else {
                showError(getString(R.string.invalid_email_format));
            }
        });
        builder.setNegativeButton(R.string.cancel, null);
        builder.show();
    }

    private void sendPasswordResetEmail(String email) {
        showProgressDialog();
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    hideProgressDialog();
                    if (task.isSuccessful()) {
                        showError(getString(R.string.reset_email_sent));
                    } else {
                        showError(getString(R.string.reset_email_error));
                    }
                });
    }

    private void handleGoogleSignInResult(Intent data) {
        try {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            GoogleSignInAccount account = task.getResult(ApiException.class);
            if (account != null) {
                firebaseAuthWithGoogle(account);
            } else {
                showError(getString(R.string.google_login_error));
            }
        } catch (ApiException e) {
            showError(getString(R.string.google_login_error));
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        showProgressDialog();
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    hideProgressDialog();
                    if (task.isSuccessful()) {
                        navigateToMain();
                    } else {
                        handleLoginError(task.getException());
                    }
                });
    }

    private void navigateToMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void navigateToRegister() {
        startActivity(new Intent(this, RegisterActivity.class));
    }

    private void handleLoginError(@Nullable Exception exception) {
        String error = getString(R.string.login_error);
        if (exception instanceof FirebaseAuthInvalidUserException) {
            error = getString(R.string.error_user_not_found);
        } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
            error = getString(R.string.error_invalid_credentials);
        } else if (exception != null) {
            error = exception.getMessage();
        }
        showError(error);
    }

    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage(getString(R.string.please_wait));
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(false);
        }
        progressDialog.show();
    }

    private void hideProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mAuth.getCurrentUser() != null) {
            navigateToMain();
        }
    }
}
