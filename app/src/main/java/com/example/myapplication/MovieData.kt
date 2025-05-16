package com.example.myapplication

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "movies")
data class Movie(
    @PrimaryKey val id: Int,
    val title: String,
    val releaseDate: String,
    val genres: String,
    val popularity: Double,
    val voteAverage: Double,
    val overview: String? = null,
    val posterPath: String? = null,
    val originalTitle: String? = null,
    val tagline: String? = null,
    // Not stored in database, used for UI
    val isFavorite: Boolean = false
)