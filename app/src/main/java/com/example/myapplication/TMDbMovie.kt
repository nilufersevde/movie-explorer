package com.example.myapplication

import com.google.gson.annotations.SerializedName

data class TMDbMovie(
    val id: Int,
    val title: String,
    @SerializedName("release_date") val releaseDate: String,
    val genres: List<Genre>,
    val popularity: Double,
    @SerializedName("vote_average") val voteAverage: Double,
    val overview: String?,
    @SerializedName("poster_path") val posterPath: String?,
    @SerializedName("original_title") val originalTitle: String?,
    val tagline: String?
)

data class Genre(
    val id: Int,
    val name: String
)