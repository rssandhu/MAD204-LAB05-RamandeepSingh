/**
 * Course: MOBILE APP DEVELOPMENT - Lab 5
 * Student: Ramandeep Singh - A00194321
 * Date: December 09, 2025
 * Description: DAO for CRUD operations on favorite media.
 */

package com.example.medialibraryapp

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Delete

@Dao
interface FavoriteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(media: FavoriteMedia)

    @Query("SELECT * FROM favorite_media ORDER BY id DESC")
    suspend fun getAllFavorites(): List<FavoriteMedia>

    @Delete
    suspend fun delete(media: FavoriteMedia)

    @Query("DELETE FROM favorite_media WHERE id = :id")
    suspend fun deleteById(id: Long)
}
