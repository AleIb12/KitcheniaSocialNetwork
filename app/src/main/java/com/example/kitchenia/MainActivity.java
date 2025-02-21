package com.example.kitchenia;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.SearchView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kitchenia.card.Carta;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Main activity of the application.
 * Displays a list of Carta objects and provides search functionality.
 */
public class MainActivity extends AppCompatActivity {

    private CartaAdapter cartaAdapter;
    private List<Carta> cartasList;
    private FirebaseFirestore db;

    /**
     * Called when the activity is first created.
     * Initializes the UI elements and sets up Firebase.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     * previously being shut down then this Bundle contains the data it most
     * recently supplied in onSaveInstanceState(Bundle). Note: Otherwise it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();

        // Initialize views
        SearchView searchView = findViewById(R.id.searchView);
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        cartasList = new ArrayList<>();

        // Setup search functionality
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                buscarCartas(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    cargarTodasLasCartas();
                }
                return true;
            }
        });

        // Load initial data
        cargarTodasLasCartas();

        bottomNavigationView.setSelectedItemId(R.id.nav_home);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                return true;
            } else if (itemId == R.id.nav_upload) {
                startActivity(new Intent(MainActivity.this, UploadPhotoActivity.class));
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(MainActivity.this, UserProfileActivity.class));
                return true;
            }
            return false;
        });
    }

    /**
     * Called when the activity is resumed.
     * Reloads all Carta objects.
     */
    @Override
    protected void onResume() {
        super.onResume();
        cargarTodasLasCartas();
    }

    /**
     * Loads all Carta objects from Firestore and updates the RecyclerView.
     */

    private void cargarTodasLasCartas() {
        db.collection("imagenes")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    cartasList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        UploadPhotoActivity.Imagen imagen = document.toObject(UploadPhotoActivity.Imagen.class);
                        Carta carta = new Carta(
                                "Receta " + document.getId(),
                                imagen.getDescripcion(),
                                0,
                                false,
                                false,
                                imagen.getUrl()
                        );
                        cartasList.add(carta);
                    }
                    // Verifica que el adaptador no sea null antes de notificar cambios
                    if (cartaAdapter != null) {
                        cartaAdapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> Log.e("MainActivity", "Error al cargar cartas", e));
    }


    /**
     * Searches for Carta objects in Firestore based on the query and updates the RecyclerView.
     *
     * @param query The search query.
     */
    private void buscarCartas(String query) {
        db.collection("imagenes")
            .whereGreaterThanOrEqualTo("descripcion", query)
            .whereLessThanOrEqualTo("descripcion", query + "\uf8ff")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                cartasList.clear();
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    UploadPhotoActivity.Imagen imagen = document.toObject(UploadPhotoActivity.Imagen.class);
                    Carta carta = new Carta(
                        "Receta " + document.getId(),
                        imagen.getDescripcion(),
                        0,
                        false,
                        false,
                        imagen.getUrl() // Pass the URL from Firestore
                    );
                    cartasList.add(carta);
                }
                cartaAdapter.notifyDataSetChanged();
            })
            .addOnFailureListener(e -> Log.e("MainActivity", "Error en la búsqueda", e));
    }
}