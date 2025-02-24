package com.example.kitchenia;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

/**
 * Activity para subir fotos a Firebase Storage y guardar la información en Firestore.
 */
public class UploadPhotoActivity extends AppCompatActivity {

    private ImageView imageViewPreview;
    private EditText editTextDescripcion;
    private Uri imageUri;
    private FirebaseStorage storage;
    private FirebaseFirestore firestore;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ProgressDialog progressDialog;

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
        setContentView(R.layout.activity_upload_photo);

        storage = FirebaseStorage.getInstance();
        firestore = FirebaseFirestore.getInstance();
        Button buttonSelectImage = findViewById(R.id.buttonSelectImage);
        Button buttonSendPhoto = findViewById(R.id.buttonSendPhoto);
        imageViewPreview = findViewById(R.id.imageViewPreview);
        editTextDescripcion = findViewById(R.id.editTextDescripcion);

        // Inicializa ProgressDialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Subiendo imagen...");
        progressDialog.setCancelable(false);

        // Inicializa el ActivityResultLauncher para seleccionar imagen
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        imageUri = result.getData().getData();
                        imageViewPreview.setImageURI(imageUri);
                    } else {
                        Toast.makeText(this, "No se seleccionó ninguna imagen", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        buttonSelectImage.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            imagePickerLauncher.launch(Intent.createChooser(intent, "Seleccionar imagen"));
        });

        buttonSendPhoto.setOnClickListener(v -> {
            if (imageUri != null) {
                subirImagenAFirebase();
            } else {
                Toast.makeText(this, "Por favor selecciona una imagen", Toast.LENGTH_SHORT).show();
            }
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.nav_upload);
        bottomNavigationView.setOnItemSelectedListener(item -> {

            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(this, MainActivity.class));
                return true;
            } else if (itemId == R.id.nav_upload) {
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(this, UserProfileActivity.class));
                return true;
            }
            return false;
        });
    }

    /**
     * Sube la imagen seleccionada a Firebase Storage y guarda la información en Firestore.
     */
    private void subirImagenAFirebase() {
        progressDialog.show(); // Muestra el ProgressDialog antes de iniciar la subida

        StorageReference ref = storage.getReference().child("imagenes/" + System.currentTimeMillis() + ".jpg");
        ref.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot ->
                        ref.getDownloadUrl()
                                .addOnSuccessListener(uri -> {
                                    String descripcion = editTextDescripcion.getText().toString();
                                    firestore.collection("imagenes")
                                            .add(new Imagen(descripcion, uri.toString()))
                                            .addOnSuccessListener(documentReference -> {
                                                progressDialog.dismiss(); // Oculta el ProgressDialog
                                                Toast.makeText(UploadPhotoActivity.this, "Imagen subida exitosamente", Toast.LENGTH_SHORT).show();
                                            })
                                            .addOnFailureListener(e -> {
                                                progressDialog.dismiss();
                                                Toast.makeText(UploadPhotoActivity.this, "Error al guardar datos en Firestore", Toast.LENGTH_SHORT).show();
                                                Log.e("UploadPhotoActivity", "Error en Firestore", e);
                                            });
                                })
                                .addOnFailureListener(e -> {
                                    progressDialog.dismiss();
                                    Toast.makeText(UploadPhotoActivity.this, "Error al obtener URL de descarga", Toast.LENGTH_SHORT).show();
                                    Log.e("UploadPhotoActivity", "Error en getDownloadUrl", e);
                                }))
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(UploadPhotoActivity.this, "Error al subir imagen", Toast.LENGTH_SHORT).show();
                    Log.e("UploadPhotoActivity", "Error al subir imagen", e);
                });
    }

    /**
     * Clase modelo para la imagen.
     */
    public static class Imagen {
        private String descripcion;
        private String url;
        private String publicador;  // campo agregado

        public Imagen() {}

        public Imagen(String descripcion, String url) {
            this.descripcion = descripcion;
            this.url = url;

        }

        public String getDescripcion() {
            return descripcion;
        }

        public String getUrl() {
            return url;
        }

        public String getPublicador() {
            return publicador;
        }
    }
}