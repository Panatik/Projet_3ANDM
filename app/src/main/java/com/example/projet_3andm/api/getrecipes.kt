package com.example.projet_3andm.api

import com.example.projet_3andm.database.ItemDao
import com.example.projet_3andm.database.ItemEntity

object RecipeSeeder {

    suspend fun seedDatabase(itemDao: ItemDao) {
        if (itemDao.countRecipes() >= 100) return

        val categories = RetrofitInstance.api.getCategories().categories
        val recipesMap = mutableMapOf<String, ItemEntity>()

        for (category in categories) {
            val meals = RetrofitInstance.api
                .getRecipesByCategory(category.strCategory)
                .meals
                ?: emptyList()

            for (meal in meals) {
                if (recipesMap.size >= 100) break
                if (recipesMap.containsKey(meal.idMeal)) continue

                val details = RetrofitInstance.api
                    .getRecipeById(meal.idMeal)
                    .meals
                    ?.firstOrNull()

                if (details != null) {
                    recipesMap[details.idMeal] = ItemEntity(
                        idMeal = details.idMeal,
                        title = details.strMeal,
                        description = details.strInstructions ?: "",
                        image = details.strMealThumb ?: "",
                        category = details.strCategory ?: category.strCategory
                    )
                }
            }

            if (recipesMap.size >= 100) break
        }

        itemDao.insertAll(recipesMap.values.toList())
    }
}