package com.example.kitchenia;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.SearchView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.kitchenia.card.Carta;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * La actividad principal de la aplicación, que muestra una lista de cartas y permite
 * realizar búsquedas y navegar entre diferentes secciones.
 */

public class MainActivity extends AppCompatActivity {
    private FirebaseStorage storage;
    private CartaAdapter cartaAdapter;
    private List<Carta> cartasList;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private TheMealDbApi mealDbApi;
    private int apiResponses = 0;

    @Override
    protected void onCreate(@NonNull Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        SearchView searchView = findViewById(R.id.searchView);
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        cartasList = new ArrayList<>();
        cartaAdapter = new CartaAdapter(cartasList);
        recyclerView.setAdapter(cartaAdapter);

        cartaAdapter.setOnItemDeleteListener((carta, position) -> {
            String documentId = carta.getNombre().replace("Receta ", "");
            db.collection("imagenes").document(documentId)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        cartasList.remove(position);
                        cartaAdapter.notifyItemRemoved(position);
                        Toast.makeText(MainActivity.this, "Publicación eliminada", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> Log.e("MainActivity", "Error al eliminar la publicación", e));
        });

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://www.themealdb.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        mealDbApi = retrofit.create(TheMealDbApi.class);
        loadFirstThreeFromApi();

        bottomNavigationView.setSelectedItemId(R.id.nav_home);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) return true;
            if (itemId == R.id.nav_upload) {
                startActivity(new Intent(MainActivity.this, UploadPhotoActivity.class));
                return true;
            }
            if (itemId == R.id.nav_profile) {
                startActivity(new Intent(MainActivity.this, UserProfileActivity.class));
                return true;
            }
            return false;
        });

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
                } else {
                    buscarCartas(newText);
                }
                return true;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarTodasLasCartas();
    }

    private void loadFirstThreeFromApi() {
        for (int i = 0; i < 3; i++) {
            mealDbApi.getRandomMeal().enqueue(new Callback<MealsResponse>() {
                @Override
                public void onResponse(@NonNull Call<MealsResponse> call, @NonNull Response<MealsResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        MealsResponse data = response.body();
                        if (data.getMeals() != null && !data.getMeals().isEmpty()) {
                            Meal meal = data.getMeals().get(0);
                            Carta newCarta = new Carta(meal.getStrMeal(), meal.getStrMeal(), 0, false, false, meal.getStrMealThumb(), "chef");
                            runOnUiThread(() -> {
                                cartasList.add(newCarta);
                                cartaAdapter.notifyItemInserted(cartasList.size() - 1);
                            });
                        }
                    }
                    checkIfComplete();
                }
                @Override
                public void onFailure(@NonNull Call<MealsResponse> call, @NonNull Throwable t) {
                    Log.e("Retrofit Error", "Error al cargar comida", t);
                    checkIfComplete();
                }
            });
        }
    }

    private void cargarTodasLasCartas() {
        Log.d("MainActivity", "Cargando todas las cartas...");
        List<Carta> apiCartas = new ArrayList<>();
        for (Carta c : cartasList) if ("chef".equals(c.getPublicador())) apiCartas.add(c);

        db.collection("imagenes").get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<Carta> firestoreCartas = new ArrayList<>();
            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                UploadPhotoActivity.Imagen imagen = document.toObject(UploadPhotoActivity.Imagen.class);
                firestoreCartas.add(new Carta("Receta " + document.getId(), imagen.getDescripcion(), 0, false, false, imagen.getUrl(), imagen.getPublicador()));
            }
            cartasList.clear();
            cartasList.addAll(apiCartas);
            cartasList.addAll(firestoreCartas);
            cartaAdapter.notifyDataSetChanged();
        }).addOnFailureListener(e -> Log.e("MainActivity", "Error al cargar cartas", e));
    }

    /**
     * Busca cartas en Firestore que contengan la consulta dada en su descripción.
     *
     * @param query La consulta de búsqueda.
     */
    private void buscarCartas(String query) {
        Log.d("MainActivity", "Buscando: " + query);

        String queryLower = query.toLowerCase().trim();
        List<Carta> buscarCartas = new ArrayList<>();

        // 1️⃣ Buscar en la API (consultando de nuevo por si aún no estaban cargadas)
        mealDbApi.getRandomMeal().enqueue(new Callback<MealsResponse>() {
            @Override
            public void onResponse(@NonNull Call<MealsResponse> call, @NonNull Response<MealsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    MealsResponse data = response.body();
                    if (data.getMeals() != null && !data.getMeals().isEmpty()) {
                        for (Meal meal : data.getMeals()) {
                            String mealNameLower = meal.getStrMeal().toLowerCase().trim();
                            if (mealNameLower.equals(queryLower)) { // Coincidencia exacta
                                Log.d("MainActivity", "Coincidencia en API: " + meal.getStrMeal());
                                Carta newCarta = new Carta(
                                        meal.getStrMeal(),
                                        meal.getStrMeal(),
                                        0,
                                        false,
                                        false,
                                        meal.getStrMealThumb(),
                                        "chef"
                                );
                                buscarCartas.add(newCarta);
                            }
                        }
                    }
                }
                // Pasamos a la segunda búsqueda en Firestore después de consultar la API
                buscarEnFirestore(queryLower, buscarCartas);
            }

            @Override
            public void onFailure(@NonNull Call<MealsResponse> call, @NonNull Throwable t) {
                Log.e("MainActivity", "Error al buscar en API", t);
                buscarEnFirestore(queryLower, buscarCartas); // Continuar con Firestore aunque falle la API
            }
        });
    }

    /**
     * Busca en Firestore y actualiza la lista.
     */
    private void buscarEnFirestore(String queryLower, List<Carta> buscarCartas) {
        db.collection("imagenes")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        UploadPhotoActivity.Imagen imagen = document.toObject(UploadPhotoActivity.Imagen.class);
                        if (imagen.getDescripcion() != null) {
                            String descripcionLower = imagen.getDescripcion().toLowerCase().trim();
                            if (descripcionLower.equals(queryLower)) { // Coincidencia exacta
                                Log.d("MainActivity", "Coincidencia en Firestore: " + imagen.getDescripcion());
                                Carta carta = new Carta(
                                        "Receta " + document.getId(),
                                        imagen.getDescripcion(),
                                        0,
                                        false,
                                        false,
                                        imagen.getUrl(),
                                        imagen.getPublicador()
                                );
                                buscarCartas.add(carta);
                            }
                        }
                    }

                    // Si no se encontró nada, mostrar mensaje
                    if (buscarCartas.isEmpty()) {
                        Log.d("MainActivity", "No se encontraron coincidencias exactas");
                        Toast.makeText(MainActivity.this, "No se encontraron resultados", Toast.LENGTH_SHORT).show();
                    }

                    // Actualizar la lista del adaptador con las cartas encontradas
                    cartasList.clear();
                    cartasList.addAll(buscarCartas);
                    cartaAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Log.e("MainActivity", "Error en la búsqueda", e));
    }






    /**
     * Verifica si se han completado todas las respuestas de la API.
     * Si es así, carga todas las cartas.
     */
    private synchronized void checkIfComplete() {
        apiResponses++;
        if (apiResponses == 3) {
            runOnUiThread(this::cargarTodasLasCartas);
        }
    }
}



