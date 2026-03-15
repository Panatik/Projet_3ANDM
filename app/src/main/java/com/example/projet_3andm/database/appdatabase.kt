package com.example.projet_3andm.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [ItemEntity::class, CategoryEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun itemDao(): ItemDao
    abstract fun categoryDao(): CategoryDao
}