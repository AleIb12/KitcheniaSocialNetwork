package com.example.kitchenia;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;



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
    private Button btnSaveDescription, btnFollow;
    private TextView tvUsername, tvFollowersCount, tvPostCount, tvFollowingCount;
    private SharedPreferences preferences;
    private FirebaseAuth mAuth;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private boolean isFollowing;
    private int followersCount;
    private FirebaseFirestore db;

    // RecyclerView para imágenes del usuario
    private RecyclerView rvUserImages;
    private List<Carta> userCartasList;
    private CartaAdapter userCartaAdapter;

    // Guardar la descripción original para comparar cambios
    private String originalDescription;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference().child("profile_images");

        ivProfileImage = findViewById(R.id.ivProfileImage);
        etDescription = findViewById(R.id.etDescription);
        btnSaveDescription = findViewById(R.id.btnSaveDescription);
        btnFollow = findViewById(R.id.btnFollow);
        tvUsername = findViewById(R.id.tvUsername);
        tvFollowersCount = findViewById(R.id.tvFollowersCount);
        tvPostCount = findViewById(R.id.tvPostCount);
        tvFollowingCount = findViewById(R.id.tvFollowingCount);

        // Inicialmente, desactivamos la edición de la biografía usando inputType="none"
        etDescription.setInputType(InputType.TYPE_NULL);

        preferences = getSharedPreferences("USER_DATA", MODE_PRIVATE);
        originalDescription = preferences.getString("USER_DESCRIPTION", "Sin descripción");
        etDescription.setText(originalDescription);

        // Al tocar el cuadro de biografía se habilita la edición
        etDescription.setOnClickListener(v -> {
            // Activamos la edición: reestablecemos el inputType a texto multilínea
            etDescription.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
            etDescription.setSelection(etDescription.getText().length());
            etDescription.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(etDescription, InputMethodManager.SHOW_IMPLICIT);
            }
        });

        // TextWatcher para mostrar el botón "Guardar" solo si se modificó la biografía
        etDescription.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Sin acción
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String currentText = s.toString().trim();
                if (!currentText.equals(originalDescription)) {
                    btnSaveDescription.setVisibility(Button.VISIBLE);
                } else {
                    btnSaveDescription.setVisibility(Button.GONE);
                }
            }
            @Override
            public void afterTextChanged(Editable s) {
                // Sin acción
            }
        });

        // Permitir guardar con el teclado (acción "Done" o Enter)
        etDescription.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                saveDescription();
                return true;
            }
            return false;
        });

        // Acción del botón "Guardar"
        btnSaveDescription.setOnClickListener(v -> saveDescription());

        // Configuración del botón "Seguir"
        btnFollow.setOnClickListener(v -> toggleFollow());

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

        // Inicializar RecyclerView para las imágenes del usuario
        rvUserImages = findViewById(R.id.rvUserImages);
        rvUserImages.setLayoutManager(new LinearLayoutManager(this));
        userCartasList = new ArrayList<>();
        userCartaAdapter = new CartaAdapter(userCartasList);
        rvUserImages.setAdapter(userCartaAdapter);
        cargarCartasDelUsuario();

        // Configurar Bottom Navigation
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

        // Hacer que la imagen de perfil sea interactiva para cambiarla
        ivProfileImage.setClickable(true);
        ivProfileImage.setFocusable(true);
        ivProfileImage.setOnClickListener(v -> openFileChooser());
    }

    // Método para guardar la descripción actualizada
    private void saveDescription() {
        String newDescription = etDescription.getText().toString().trim();
        if (!newDescription.equals(originalDescription)) {
            preferences.edit().putString("USER_DESCRIPTION", newDescription).apply();
            Toast.makeText(this, "Descripción actualizada", Toast.LENGTH_SHORT).show();
            originalDescription = newDescription;
        }
        // Desactivar la edición volviendo a inputType null
        etDescription.setInputType(InputType.TYPE_NULL);
        btnSaveDescription.setVisibility(Button.GONE);
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(etDescription.getWindowToken(), 0);
        }
    }

    // Maneja la acción de seguir/dejar de seguir
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

    // Abre el selector de imágenes para cambiar la imagen de perfil
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

    // Sube la imagen seleccionada a Firebase Storage
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

    // Carga la imagen de perfil almacenada (si existe)
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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            // Cerrar sesión y redirigir a LoginActivity
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Carga las cartas (imágenes) del usuario desde Firestore
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

}
