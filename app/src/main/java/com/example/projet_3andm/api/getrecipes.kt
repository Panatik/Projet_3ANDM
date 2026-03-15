package com.example.projet_3andm.api

import com.example.projet_3andm.database.ItemDao
import com.example.projet_3andm.database.ItemEntity
import com.example.projet_3andm.database.CategoryDao
import com.example.projet_3andm.database.CategoryEntity

object RecipeSeeder {

    suspend fun loadMoreRecipesByCategory(
        itemDao: ItemDao,
        category: String,
        limit: Int = 10
    ): Int {
        val existingIds = itemDao.getAllRecipeIds().toMutableSet()
        val newRecipes = mutableListOf<ItemEntity>()

        val meals = RetrofitInstance.api
            .getRecipesByCategory(category)
            .meals
            ?: return 0

        for (meal in meals) {
            if (newRecipes.size >= limit) break
            if (meal.idMeal in existingIds) continue

            val details = RetrofitInstance.api
                .getRecipeById(meal.idMeal)
                .meals
                ?.firstOrNull() ?: continue

            newRecipes.add(
                ItemEntity(
                    idMeal = details.idMeal,
                    title = details.strMeal,
                    description = details.strInstructions ?: "",
                    image = details.strMealThumb ?: "",
                    category = details.strCategory ?: category
                )
            )
        }

        if (newRecipes.isNotEmpty()) {
            itemDao.insertAll(newRecipes)
        }

        return newRecipes.size
    }

    suspend fun seedCategories(categoryDao: CategoryDao) {
        val categories = RetrofitInstance.api.getCategories().categories

        val entities = categories.map { category ->
            CategoryEntity(
                idCategory = category.idCategory,
                name = category.strCategory,
                thumbnail = category.strCategoryThumb
            )
        }

        categoryDao.insertAll(entities)
    }

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

    suspend fun loadMoreRecipes(itemDao: ItemDao, limit: Int = 10): Int {
        val existingIds = itemDao.getAllRecipeIds().toMutableSet()
        val newRecipes = mutableListOf<ItemEntity>()

        val categories = RetrofitInstance.api.getCategories().categories

        for (category in categories) {
            val meals = RetrofitInstance.api
                .getRecipesByCategory(category.strCategory)
                .meals
                ?: emptyList()

            for (meal in meals) {
                if (newRecipes.size >= limit) break
                if (meal.idMeal in existingIds) continue

                val details = RetrofitInstance.api
                    .getRecipeById(meal.idMeal)
                    .meals
                    ?.firstOrNull()

                if (details != null) {
                    val recipe = ItemEntity(
                        idMeal = details.idMeal,
                        title = details.strMeal,
                        description = details.strInstructions ?: "",
                        image = details.strMealThumb ?: "",
                        category = details.strCategory ?: category.strCategory
                    )

                    newRecipes.add(recipe)
                    existingIds.add(details.idMeal)
                }
            }

            if (newRecipes.size >= limit) break
        }

        if (newRecipes.isNotEmpty()) {
            itemDao.insertAll(newRecipes)
        }

        return newRecipes.size
    }



}