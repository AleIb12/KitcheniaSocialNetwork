package com.example.kitchenia;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
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

import androidx.annotation.NonNull;
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

/**
 * Activity para mostrar y editar el perfil del usuario.
 * Permite cambiar la imagen de perfil, editar la descripción y ver las publicaciones del usuario.
 */
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

    // Additional variables
    private ProgressDialog progressDialog;
    private Uri imageUri;
    private EditText editTextDescripcion;
    private FirebaseFirestore firestore;

    /**
     * Llamado cuando la actividad es creada por primera vez.
     * Inicializa los elementos de la interfaz de usuario y las instancias de Firebase.
     *
     * @param savedInstanceState Si la actividad está siendo re-inicializada después de
     * haber sido previamente cerrada, este Bundle contiene los datos más recientes
     * suministrados en onSaveInstanceState(Bundle). Nota: De lo contrario, es null.
     */
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //Quita el nombre de la aplicción de la barra de herramientas
        getSupportActionBar().setDisplayShowTitleEnabled(false);

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
        bottomNavigationView.setSelectedItemId(R.id.nav_profile);
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

    /**
     * Guarda la descripción actualizada del usuario.
     * Desactiva la edición y oculta el teclado.
     */
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

    /**
     * Maneja la acción de seguir o dejar de seguir a un usuario.
     * Actualiza el contador de seguidores y el estado de seguimiento.
     */
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

    /**
     * Actualiza el texto del botón de seguir según el estado de seguimiento.
     */
    private void actualizarBotonSeguir() {
        btnFollow.setText(isFollowing ? "Dejar de seguir" : "Seguir");
    }

    /**
     * Abre el selector de archivos para cambiar la imagen de perfil.
     */
    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    /**
     * Maneja el resultado de la actividad de selección de imagen.
     *
     * @param requestCode El código de solicitud pasado a startActivityForResult().
     * @param resultCode El código de resultado devuelto por la actividad hija.
     * @param data Un Intent que lleva los datos del resultado.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK &&
                data != null && data.getData() != null) {
            imageUri = data.getData();
            ivProfileImage.setImageURI(imageUri);
            subirImagenAFirebase(imageUri);
        }
    }

    /**
     * Sube la imagen seleccionada a Firebase Storage.
     *
     * @param imageUri La URI de la imagen seleccionada.
     */
   private void subirImagenAFirebase(Uri imageUri) {
       if (progressDialog == null) {
           progressDialog = new ProgressDialog(this);
           progressDialog.setMessage("Actualizando imagen de perfil...");
       }
       progressDialog.show();

       String userId = mAuth.getCurrentUser().getUid();
       StorageReference ref = storageRef.child(userId + ".jpg");

       ref.putFile(imageUri)
           .addOnSuccessListener(taskSnapshot -> {
               ref.getDownloadUrl().addOnSuccessListener(uri -> {
                   // Actualizar URL en SharedPreferences
                   preferences.edit()
                       .putString("USER_IMAGE_URL", uri.toString())
                       .apply();

                   // Actualizar URL en Firestore
                   db.collection("users")
                       .document(userId)
                       .update("profileImageUrl", uri.toString())
                       .addOnSuccessListener(unused -> {
                           progressDialog.dismiss();
                           Toast.makeText(UserProfileActivity.this,
                               "Imagen de perfil actualizada", Toast.LENGTH_SHORT).show();
                           cargarImagenPerfil(); // Recargar la imagen
                       })
                       .addOnFailureListener(e -> {
                           progressDialog.dismiss();
                           Toast.makeText(UserProfileActivity.this,
                               "Error al actualizar imagen", Toast.LENGTH_SHORT).show();
                       });
               });
           })
           .addOnFailureListener(e -> {
               progressDialog.dismiss();
               Toast.makeText(UserProfileActivity.this,
                   "Error al subir imagen", Toast.LENGTH_SHORT).show();
           });
   }

    /**
     * Carga la imagen de perfil almacenada (si existe).
     */
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

    /**
     * Crea el menú de opciones en la barra de herramientas.
     *
     * @param menu El menú de opciones en el que se inflará el menú.
     * @return true si el menú se creó con éxito.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_profile, menu);
        return true;
    }

    /**
     * Maneja las selecciones de elementos del menú de opciones.
     *
     * @param item El elemento del menú seleccionado.
     * @return true si la selección se manejó con éxito.
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            FirebaseAuth.getInstance().signOut();  // Cerrar sesión en Firebase
            Intent intent = new Intent(this, LoginActivity.class); // Ir a la pantalla de login
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish(); // Cerrar la actividad actual
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Carga las cartas (imágenes) del usuario desde Firestore.
     */
    private void cargarCartasDelUsuario() {
        if (mAuth.getCurrentUser() != null) {
            String currentUserEmail = mAuth.getCurrentUser().getEmail();
            db.collection("imagenes")
                .whereEqualTo("publicador", currentUserEmail)
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
                                imagen.getUrl(),
                                currentUserEmail
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

    /**
     * Clase modelo para la imagen.
     */
    public static class Imagen {
        private String descripcion;
        private String url;
        private String publicador;

        public Imagen() {}

        public Imagen(String descripcion, String url) {
            this.descripcion = descripcion;
            this.url = url;
        }

        public String getDescripcion() { return descripcion; }

        public String getUrl() { return url; }

        public String getPublicador() { return publicador; }

        public void setPublicador(String publicador) {
            this.publicador = publicador;
        }
    }
}