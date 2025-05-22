package com.example.myapplication

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FavoriteMoviesActivity : AppCompatActivity() {

    private lateinit var adapter: FavoritesAdapter
    private lateinit var recyclerView: RecyclerView
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_movie_list)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.title = "Favorite Movies"
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        recyclerView = findViewById(R.id.movieListRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = FavoritesAdapter { favoriteMovie ->
            removeFavorite(favoriteMovie)
        }
        recyclerView.adapter = adapter

        fetchFavorites()
    }

    private fun fetchFavorites() {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("favorites")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { result ->
                val movies = result.documents.mapNotNull {
                    it.toObject(FavoriteMovie::class.java)?.copy(documentId = it.id)
                }
                adapter.submitList(movies)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load favorites", Toast.LENGTH_SHORT).show()
            }
    }

    private fun removeFavorite(movie: FavoriteMovie) {
        firestore.collection("favorites")
            .document(movie.documentId)
            .delete()
            .addOnSuccessListener {
                fetchFavorites()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to remove favorite", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
