package com.example.kitchenia;

import retrofit2.Call;
import retrofit2.http.GET;

public interface TheMealDbApi {
    @GET("api/json/v1/1/random.php")
    Call<MealsResponse> getRandomMeal();
}