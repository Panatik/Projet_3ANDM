package com.example.projet_3andm.api

import retrofit2.http.GET
import retrofit2.http.Query

interface RecipeApi {

    @GET("categories.php")
    suspend fun getCategories(): CategoriesResponse

    @GET("filter.php")
    suspend fun getRecipesByCategory(
        @Query("c") category: String
    ): MealsResponse

    @GET("lookup.php")
    suspend fun getRecipeById(
        @Query("i") id: String
    ): MealsResponse

    @GET("search.php")
    suspend fun searchRecipes(
        @Query("s") query: String
    ): MealsResponse
}