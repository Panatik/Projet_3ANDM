package com.example.projet_3andm.api

data class CategoriesResponse(
    val categories: List<CategoryDto>
)

data class CategoryDto(
    val idCategory: String,
    val strCategory: String,
    val strCategoryThumb: String
)

data class MealsResponse(
    val meals: List<MealDto>?
)

data class MealDto(
    val idMeal: String,
    val strMeal: String,
    val strCategory: String?,
    val strInstructions: String?,
    val strMealThumb: String?
)