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

    /**
     * Llamado cuando la actividad es creada por primera vez.
     * Inicializa los componentes de la UI y las instancias de Firebase.
     *
     * @param savedInstanceState Si la actividad está siendo re-inicializada después de
     * haber sido previamente cerrada, este Bundle contiene los datos más recientes
     * suministrados en onSaveInstanceState(Bundle). Nota: De lo contrario, es null.
     */
    @Override
    protected void onCreate(@NonNull Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializar instancias de Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Inicializar componentes de la UI
        SearchView searchView = findViewById(R.id.searchView);
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);

        // Configurar RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        cartasList = new ArrayList<>();
        cartaAdapter = new CartaAdapter(cartasList);
        recyclerView.setAdapter(cartaAdapter);

        // Configurar listener para el borrado de publicaciones
        cartaAdapter.setOnItemDeleteListener((carta, position) -> {
            String documentId = carta.getNombre().replace("Receta ", "");
            db.collection("imagenes").document(documentId)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        cartasList.remove(position);
                        cartaAdapter.notifyItemRemoved(position);
                        Toast.makeText(MainActivity.this, "Publicación eliminada", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Log.e("MainActivity", "Error al eliminar la publicación", e);
                        Toast.makeText(MainActivity.this, "Error al eliminar la publicación", Toast.LENGTH_SHORT).show();
                    });
        });

        // Configurar Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://www.themealdb.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        mealDbApi = retrofit.create(TheMealDbApi.class);
        loadFirstThreeFromApi();

        // Configurar bottom navigation
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

        // Configurar funcionalidad de búsqueda
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
    }

    /**
     * Llamado cuando la actividad se reanuda.
     * Carga todas las cartas desde Firestore.
     */
    @Override
    protected void onResume() {
        super.onResume();
        cargarTodasLasCartas();
    }

    /**
     * Carga tres imágenes aleatorias desde la API y las agrega a la lista.
     */
    private void loadFirstThreeFromApi() {
        for (int i = 0; i < 3; i++) {
            mealDbApi.getRandomMeal().enqueue(new Callback<MealsResponse>() {
                @Override
                public void onResponse(@NonNull Call<MealsResponse> call, @NonNull Response<MealsResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        MealsResponse data = response.body();
                        if (data.getMeals() != null && !data.getMeals().isEmpty()) {
                            Meal meal = data.getMeals().get(0);
                            String mealName = meal.getStrMeal() != null ? meal.getStrMeal() : "Unknown";
                            String mealThumb = meal.getStrMealThumb() != null ? meal.getStrMealThumb() : "";
                            Carta newCarta = new Carta(
                                    mealName,
                                    mealName,
                                    0,
                                    false,
                                    false,
                                    mealThumb,
                                    "chef"
                            );
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
                    Log.e("Retrofit Error", "Could not load meal: ", t);
                    checkIfComplete();
                }
            });
        }
    }

    /**
     * Carga las cartas de Firestore y combina con las imágenes de la API.
     */
    private void cargarTodasLasCartas() {
        // Conservar las cartas de API (con publicador "chef")
        List<Carta> apiCartas = new ArrayList<>();
        for (Carta c : cartasList) {
            if ("chef".equals(c.getPublicador())) {
                apiCartas.add(c);
            }
        }
        db.collection("imagenes")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Carta> firestoreCartas = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        UploadPhotoActivity.Imagen imagen = document.toObject(UploadPhotoActivity.Imagen.class);
                        Carta carta = new Carta(
                                "Receta " + document.getId(),
                                imagen.getDescripcion(),
                                0,
                                false,
                                false,
                                imagen.getUrl(),
                                imagen.getPublicador()
                        );
                        firestoreCartas.add(carta);
                    }
                    cartasList.clear();
                    cartasList.addAll(apiCartas);
                    cartasList.addAll(firestoreCartas);
                    cartaAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Log.e("MainActivity", "Error al cargar cartas", e));
    }

    /**
     * Busca cartas en Firestore que coincidan con la consulta dada.
     *
     * @param query La consulta de búsqueda.
     */
    private void buscarCartas(String query) {
        db.collection("imagenes")
                .whereGreaterThanOrEqualTo("descripcion", query)
                .whereLessThanOrEqualTo("descripcion", query + "\uf8ff")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Carta> buscarCartas = new ArrayList<>();
                    // Conservar las cartas de API cargadas
                    for (Carta c : cartasList) {
                        if ("chef".equals(c.getPublicador())) {
                            buscarCartas.add(c);
                        }
                    }
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        UploadPhotoActivity.Imagen imagen = document.toObject(UploadPhotoActivity.Imagen.class);
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