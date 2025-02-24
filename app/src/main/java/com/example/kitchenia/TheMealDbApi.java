package com.example.kitchenia;

import retrofit2.Call;
import retrofit2.http.GET;

/**
 * Interfaz para interactuar con la API de TheMealDb.
 */
public interface TheMealDbApi {

    /**
     * Realiza una solicitud GET para obtener una comida aleatoria.
     *
     * @return Un objeto Call que se puede usar para ejecutar la solicitud.
     */
    @GET("api/json/v1/1/random.php")
    Call<MealsResponse> getRandomMeal();
}