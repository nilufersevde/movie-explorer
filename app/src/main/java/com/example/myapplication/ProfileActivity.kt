package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.myapplication.databinding.ActivityProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var favoritesAdapter: FavoritesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Initialize Firebase
        auth = Firebase.auth
        firestore = FirebaseFirestore.getInstance()

        // Setup user info
        setupUserInfo()

        // Setup favorites RecyclerView
        setupFavoritesRecyclerView()

        // Load user's favorite movies
        loadFavoriteMovies()

        // Logout button
        binding.logoutButton.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun setupUserInfo() {
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            binding.userEmailTextView.text = user.email
            // You can add profile image loading with Glide here if user has a profile photo
        }
    }

    private fun setupFavoritesRecyclerView() {
        favoritesAdapter = FavoritesAdapter(
            onRemoveFavorite = { favoriteMovie ->
                removeFavoriteMovie(favoriteMovie)
            }
        )
        binding.favoritesRecyclerView.apply {
            layoutManager = GridLayoutManager(this@ProfileActivity, 2)
            adapter = favoritesAdapter
        }
    }

    private fun loadFavoriteMovies() {
        val currentUser = auth.currentUser ?: return

        firestore.collection("favorites")
            .whereEqualTo("userId", currentUser.uid)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                val favoriteMovies = documents.map { it.toObject(FavoriteMovie::class.java) }

                if (favoriteMovies.isEmpty()) {
                    binding.emptyFavoritesTextView.visibility = View.VISIBLE
                } else {
                    binding.emptyFavoritesTextView.visibility = View.GONE
                    favoritesAdapter.submitList(favoriteMovies)
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error loading favorites", e)
            }
    }

    private fun removeFavoriteMovie(favoriteMovie: FavoriteMovie) {
        firestore.collection("favorites").document(favoriteMovie.documentId)
            .delete()
            .addOnSuccessListener {
                // Refresh the favorites list
                loadFavoriteMovies()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error removing favorite", e)
            }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        private const val TAG = "ProfileActivity"
    }
}