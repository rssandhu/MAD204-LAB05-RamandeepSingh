/**
 * Course: MOBILE APP DEVELOPMENT - Lab 5
 * Student: Ramandeep Singh - A00194321
 * Date: December 09, 2025
 * Description: Room Entity for favorite media items.
 */

package com.example.medialibraryapp

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_media")
data class FavoriteMedia(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val uri: String,
    val type: String  // "image" or "video"
)
