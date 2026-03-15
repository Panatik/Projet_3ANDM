package com.example.projet_3andm.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey
    val idCategory: String,
    val name: String,
    val thumbnail: String
)