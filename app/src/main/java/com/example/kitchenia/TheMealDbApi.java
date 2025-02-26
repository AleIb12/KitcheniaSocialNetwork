package com.example.kitchenia;

import retrofit2.Call;
import retrofit2.http.GET;

/**
 * Interface for The MealDB API endpoints.
 * Uses Retrofit annotations to define HTTP requests.
 */
public interface TheMealDbApi {

    /**
     * Fetches a random meal from The MealDB API.
     * Endpoint: api/json/v1/1/random.php
     *
     * @return Call object containing MealsResponse which includes the random meal data.
     * The response is automatically deserialized from JSON to MealsResponse object.
     */
    @GET("api/json/v1/1/random.php")
    Call<MealsResponse> getRandomMeal();
}