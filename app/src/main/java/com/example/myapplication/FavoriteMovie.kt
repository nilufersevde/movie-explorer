package com.example.myapplication

import com.google.firebase.firestore.DocumentId

data class FavoriteMovie(
    @DocumentId val documentId: String = "", // Firestore document ID
    val userId: String = "",  // Firebase Auth user ID
    val movieId: Int = 0,
    val title: String = "",
    val posterPath: String? = null,
    val rating: Double = 0.0,
    val releaseDate: String = "",
    val timestamp: Long = System.currentTimeMillis() // When it was added to favorites
)