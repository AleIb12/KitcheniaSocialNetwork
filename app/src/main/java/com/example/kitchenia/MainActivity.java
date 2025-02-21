// MainActivity.java
package com.example.kitchenia;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kitchenia.Meal;
import com.example.kitchenia.MealsResponse;
import com.example.kitchenia.TheMealDbApi;
import com.example.kitchenia.card.Carta;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private CartaAdapter cartaAdapter;
    private List<Carta> cartasList;
    private FirebaseFirestore db;
    private TheMealDbApi mealDbApi;
    private int apiResponses = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        db = FirebaseFirestore.getInstance();

        SearchView searchView = findViewById(R.id.searchView);
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        cartasList = new ArrayList<>();
        cartaAdapter = new CartaAdapter(cartasList);
        recyclerView.setAdapter(cartaAdapter);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://www.themealdb.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        mealDbApi = retrofit.create(TheMealDbApi.class);
        loadFirstThreeFromApi();

        bottomNavigationView.setSelectedItemId(R.id.nav_home);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) {
                return true;
            } else if (item.getItemId() == R.id.nav_upload) {
                startActivity(new Intent(MainActivity.this, UploadPhotoActivity.class));
                return true;
            } else if (item.getItemId() == R.id.nav_profile) {
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

    private void cargarTodasLasCartas() {
        db.collection("imagenes")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // Removed cartasList.clear()
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
                    cartaAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Log.e("MainActivity", "Error al cargar cartas", e));
    }

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
                                imagen.getUrl()
                        );
                        cartasList.add(carta);
                    }
                    cartaAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Log.e("MainActivity", "Error en la búsqueda", e));
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
                            String mealName = (meal.getStrMeal() != null) ? meal.getStrMeal() : "Unknown";
                            String mealThumb = (meal.getStrMealThumb() != null) ? meal.getStrMealThumb() : "";
                            Carta newCarta = new Carta(
                                    mealName,
                                    "Delicious meal",
                                    0,
                                    false,
                                    false,
                                    mealThumb
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

    private synchronized void checkIfComplete() {
        apiResponses++;
        if (apiResponses == 3) {
            runOnUiThread(this::cargarTodasLasCartas);
        }
    }
}