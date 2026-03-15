package com.example.projet_3andm.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recipes")
data class ItemEntity(
    @PrimaryKey
    val idMeal: String,
    val title: String,
    val description: String,
    val image: String,
    val category: String
)