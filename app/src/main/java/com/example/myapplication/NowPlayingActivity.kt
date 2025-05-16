package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NowPlayingActivity : AppCompatActivity() {
    private lateinit var adapter: NowPlayingAdapter
    private lateinit var auth: FirebaseAuth
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: View
    private lateinit var errorView: View
    private val apiKey = "e8d63bbf380e5732a0fd84173796a168"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_now_playing)

        // Set up toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Now Playing"

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()

        // Check if user is logged in
        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // Set up RecyclerView and UI elements
        recyclerView = findViewById(R.id.recyclerView)
        progressBar = findViewById(R.id.progressBar)
        errorView = findViewById(R.id.errorView)

        adapter = NowPlayingAdapter(
            onFavoriteClick = { movie ->
                // Temporarily disabled
            }
        )
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // Fetch now playing movies
        fetchNowPlayingMovies()
    }

    private fun fetchNowPlayingMovies() {
        // Show loading state
        progressBar.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        errorView.visibility = View.GONE

        // Check network availability
        if (!isNetworkAvailable(this)) {
            Log.e("NowPlayingActivity", "No internet connection")
            showError("No internet connection")
            return
        }

        val apiService = RetrofitClient.retrofitService
        val call = apiService.getNowPlayingMovies(apiKey, "en-US", 1)

        call.enqueue(object : Callback<MovieResponse> {
            override fun onResponse(call: Call<MovieResponse>, response: Response<MovieResponse>) {
                progressBar.visibility = View.GONE

                if (response.isSuccessful && response.body() != null) {
                    val tmdbMovies = response.body()!!.results
                    Log.d("NowPlayingActivity", "Fetched ${tmdbMovies.size} movies: $tmdbMovies")
                    val movies = tmdbMovies.map { mapTMDbMovieToLocal(it) }
                    if (movies.isEmpty()) {
                        Log.d("NowPlayingActivity", "Empty movie list after mapping")
                        showError("No movies available")
                    } else {
                        adapter.submitList(movies)
                        recyclerView.visibility = View.VISIBLE
                    }
                } else {
                    Log.e("NowPlayingActivity", "API error: ${response.code()} - ${response.message()}")
                    showError("Failed to load movies: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<MovieResponse>, t: Throwable) {
                progressBar.visibility = View.GONE
                Log.e("NowPlayingActivity", "Network error: ${t.message}", t)
                showError("Network error: ${t.message}")
            }
        })
    }

    private fun mapTMDbMovieToLocal(tmdbMovie: TMDbMovie): Movie {
        return Movie(
            id = tmdbMovie.id,
            title = tmdbMovie.title,
            releaseDate = tmdbMovie.releaseDate,
            genres = tmdbMovie.genres?.joinToString(", ") { it.name } ?: "",
            popularity = tmdbMovie.popularity,
            voteAverage = tmdbMovie.voteAverage,
            overview = tmdbMovie.overview,
            posterPath = tmdbMovie.posterPath,
            originalTitle = tmdbMovie.originalTitle,
            tagline = tmdbMovie.tagline,
            isFavorite = false
        )
    }

    private fun showError(message: String) {
        recyclerView.visibility = View.GONE
        errorView.visibility = View.VISIBLE
        Snackbar.make(
            findViewById(android.R.id.content),
            message,
            Snackbar.LENGTH_LONG
        ).show()
    }

    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}