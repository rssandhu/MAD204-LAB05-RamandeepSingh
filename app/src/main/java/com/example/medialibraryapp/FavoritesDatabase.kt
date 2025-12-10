/**
 * Course: MOBILE APP DEVELOPMENT - Lab 5
 * Student: Ramandeep Singh - A00194321
 * Date: December 09, 2025
 * Description: Room database class for favorite media storage.
 */

package com.example.medialibraryapp

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [FavoriteMedia::class], version = 1, exportSchema = false)
abstract class FavoritesDatabase : RoomDatabase() {
    abstract fun favoriteDao(): FavoriteDao
}
