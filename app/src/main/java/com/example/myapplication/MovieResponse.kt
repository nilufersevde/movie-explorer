package com.example.myapplication
data class MovieResponse(
    val results: List<TMDbMovie>,
    val page: Int,
    val total_pages: Int
)