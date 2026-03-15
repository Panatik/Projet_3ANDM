package com.example.projet_3andm.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ItemDao {

    @Query("SELECT * FROM recipes")
    suspend fun getAllRecipes(): List<ItemEntity>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<ItemEntity>)

    @Query("SELECT * FROM recipes LIMIT 100")
    suspend fun getFirst100(): List<ItemEntity>

    @Query("SELECT COUNT(*) FROM recipes")
    suspend fun countRecipes(): Int
}