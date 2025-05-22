package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.opencsv.CSVReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStreamReader

class MainActivity : AppCompatActivity() {
    private lateinit var movieDao: MovieDao
    private lateinit var adapter: MovieAdapter
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var viewModel: MainViewModel
    private var searchJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Initialize Firebase
        auth = Firebase.auth
        firestore = FirebaseFirestore.getInstance()

        // Check if user is logged in
        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // Initialize Room database
        val db = DatabaseProvider.getDatabase(this)
        movieDao = db.movieDao()

        // Set up RecyclerView
        val searchEditText = findViewById<EditText>(R.id.searchEditText)
        val searchButton = findViewById<Button>(R.id.searchButton)
        val nowPlayingButton = findViewById<Button>(R.id.nowPlayingButton)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)



        adapter = MovieAdapter(
            onFavoriteClick = { movie ->
                toggleFavorite(movie)
            },
                    onWatchedClick = { movie ->
                toggleWatched(movie)
            }
        )
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // Navigate to NowPlayingActivity
        nowPlayingButton.setOnClickListener {
            startActivity(Intent(this, NowPlayingActivity::class.java))
        }

        // Check if database is populated; if not, parse CSV
        lifecycleScope.launch {
            val movieCount = movieDao.getMovieCount()
            if (movieCount == 0) {
                Log.d("MainActivity", "Database empty, parsing CSV...")
                parseCsvAndCache(this@MainActivity, movieDao)
            } else {
                Log.d("MainActivity", "Database has $movieCount movies")
            }
            val allMovies = movieDao.getLimitedMovies(50)
            Log.d("MainActivity", "Loaded ${allMovies.size} movies initially")
            checkFavoritesStatus(allMovies)
        }

        // Handle search
        searchButton.setOnClickListener {
            val query = searchEditText.text.toString().trim()
            searchJob?.cancel() // Cancel previous search
            searchJob = lifecycleScope.launch {
                if (query.isNotEmpty()) {
                    Log.d("SEARCH_BUTTON", "Searching for: $query")
                    viewModel.getSearchResults(query).collectLatest { pagingData ->
                        adapter.submitData(pagingData)
                    }
                } else {
                    viewModel.moviePagingFlow.collectLatest { pagingData ->
                        adapter.submitData(pagingData)
                    }
                }
            }
        }

        // Initial data load
        lifecycleScope.launch {
            viewModel.moviePagingFlow.collectLatest { pagingData ->
                adapter.submitData(pagingData)
            }
        }
    }

    private fun checkFavoritesStatus(movies: List<Movie>) {
        val currentUser = auth.currentUser ?: return

        // Get user's favorites from Firestore
        firestore.collection("favorites")
            .whereEqualTo("userId", currentUser.uid)
            .get()
            .addOnSuccessListener { documents ->
                val favoriteIds = documents.documents.mapNotNull { doc ->
                    doc.getLong("movieId")?.toInt()
                }.toSet()

                // Mark favorites in the movie list and update UI
                val moviesWithFavoriteStatus = movies.map { movie ->
                    movie.copy(isFavorite = favoriteIds.contains(movie.id))
                }
                // Since we're using PagingData, we need to refresh the ViewModel or adapter data
                // For now, we'll rely on the initial load and search to update favorites
                // This is a limitation with PagingData; consider a custom solution if real-time updates are needed
            }
            .addOnFailureListener { e ->
                Log.e("MainActivity", "Error checking favorites", e)
                // Fallback to show movies without favorite status
                lifecycleScope.launch {
                    viewModel.moviePagingFlow.collectLatest { pagingData ->
                        adapter.submitData(pagingData)
                    }
                }
            }
    }

    private fun checkWatchedStatus(movies: List<Movie>) {
        val currentUser = auth.currentUser ?: return

        firestore.collection("watched")
            .whereEqualTo("userId", currentUser.uid)
            .get()
            .addOnSuccessListener { documents ->
                val watchedIds = documents.documents.mapNotNull { doc ->
                    doc.getLong("movieId")?.toInt()
                }.toSet()

                val moviesWithWatchedStatus = movies.map { movie ->
                    movie.copy(isWatched = watchedIds.contains(movie.id))
                }

                // Optional: update adapter if you're using non-paging
                // adapter.submitList(moviesWithWatchedStatus)

            }
            .addOnFailureListener { e ->
                Log.e("MainActivity", "Error checking watched status", e)
                lifecycleScope.launch {
                    viewModel.moviePagingFlow.collectLatest { pagingData ->
                        adapter.submitData(pagingData)
                    }
                }
            }
    }


    private fun toggleFavorite(movie: Movie) {
        val currentUser = auth.currentUser ?: return

        if (movie.isFavorite) {
            // Remove from favorites
            firestore.collection("favorites")
                .whereEqualTo("userId", currentUser.uid)
                .whereEqualTo("movieId", movie.id)
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        firestore.collection("favorites").document(document.id).delete()
                    }
                    Snackbar.make(
                        findViewById(android.R.id.content),
                        "${movie.title} removed from favorites",
                        Snackbar.LENGTH_SHORT
                    ).show()
                    // Update UI by refreshing the data
                    lifecycleScope.launch {
                        val updatedMovies = movieDao.getAllMovies()
                        checkFavoritesStatus(updatedMovies)
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("MainActivity", "Error removing favorite", e)
                }
        } else {
            // Add to favorites
            val favoriteMovie = FavoriteMovie(
                userId = currentUser.uid,
                movieId = movie.id,
                title = movie.title,
                posterPath = movie.posterPath,
                rating = movie.voteAverage,
                releaseDate = movie.releaseDate
            )

            firestore.collection("favorites")
                .add(favoriteMovie)
                .addOnSuccessListener {
                    Snackbar.make(
                        findViewById(android.R.id.content),
                        "${movie.title} added to favorites",
                        Snackbar.LENGTH_SHORT
                    ).show()
                    // Update UI by refreshing the data
                    lifecycleScope.launch {
                        val updatedMovies = movieDao.getAllMovies()
                        checkFavoritesStatus(updatedMovies)
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("MainActivity", "Error adding favorite", e)
                }
        }
    }

    private fun toggleWatched(movie: Movie) {
        val currentUser = auth.currentUser ?: return

        if (movie.isWatched) {
            // Remove from watched list
            firestore.collection("watched")
                .whereEqualTo("userId", currentUser.uid)
                .whereEqualTo("movieId", movie.id)
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        firestore.collection("watched").document(document.id).delete()
                    }
                    Snackbar.make(
                        findViewById(android.R.id.content),
                        "${movie.title} removed from watched list",
                        Snackbar.LENGTH_SHORT
                    ).show()
                    lifecycleScope.launch {
                        val updatedMovies = movieDao.getAllMovies()
                        checkWatchedStatus(updatedMovies)
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("MainActivity", "Error removing watched", e)
                }
        } else {
            // Add to watched list
            val watchedMovie = hashMapOf(
                "userId" to currentUser.uid,
                "movieId" to movie.id,
                "title" to movie.title,
                "posterPath" to movie.posterPath,
                "rating" to movie.voteAverage,
                "releaseDate" to movie.releaseDate,
                "timestamp" to System.currentTimeMillis()
            )

            firestore.collection("watched")
                .add(watchedMovie)
                .addOnSuccessListener {
                    Snackbar.make(
                        findViewById(android.R.id.content),
                        "${movie.title} marked as watched",
                        Snackbar.LENGTH_SHORT
                    ).show()
                    lifecycleScope.launch {
                        val updatedMovies = movieDao.getAllMovies()
                        checkWatchedStatus(updatedMovies)
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("MainActivity", "Error adding watched", e)
                }
        }
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_profile -> {
                startActivity(Intent(this, ProfileActivity::class.java))
                true
            }
            R.id.menu_logout -> {
                auth.signOut()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}

suspend fun parseCsvAndCache(context: Context, movieDao: MovieDao) {
    withContext(Dispatchers.IO) {
        try {
            context.assets.open("TMDB_movie_dataset_v11.csv").use { inputStream ->
                val reader = CSVReader(InputStreamReader(inputStream))
                reader.readNext() // Skip header row

                val batchSize = 20 // Process 100 records at a time
                var line: Array<String>?
                val batch = mutableListOf<Movie>()

                while (reader.readNext().also { line = it } != null) {
                    if (line!!.size >= 24) { // Ensure enough columns (24 fields in CSV)
                        val id = line!![0].toIntOrNull() ?: continue
                        val title = line!![1].ifEmpty { "Unknown Title" }
                        val voteAverage = line!![2].toDoubleOrNull() ?: 0.0
                        val releaseDate = line!![5].ifEmpty { "Unknown Date" }
                        val popularity = line!![16].toDoubleOrNull() ?: 0.0
                        val genres = line!![19].ifEmpty { "Unknown Genres" }
                        val overview = line!![15].ifEmpty { null }
                        val posterPath = line!![17].ifEmpty { null }
                        val originalTitle = line!![14].ifEmpty { null }
                        val tagline = line!![18].ifEmpty { null }

                        val movie = Movie(
                            id = id,
                            title = title,
                            releaseDate = releaseDate,
                            genres = genres,
                            popularity = popularity,
                            voteAverage = voteAverage,
                            overview = overview,
                            posterPath = posterPath,
                            originalTitle = originalTitle,
                            tagline = tagline
                        )
                        batch.add(movie)

                        // When batch reaches defined size, insert and clear
                        if (batch.size >= batchSize) {
                            movieDao.insertMovies(batch)
                            batch.clear()
                            // Force garbage collection (optional)
                            System.gc()
                        }
                    }
                }

                // Insert any remaining movies
                if (batch.isNotEmpty()) {
                    movieDao.insertMovies(batch)
                }
                reader.close()
            }
            Log.d("parseCsv", "Finished caching movies in database")
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("parseCsv", "Error parsing CSV: ${e.localizedMessage}")
        }
    }
}

object DatabaseProvider {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "movie_database"
            )
                .fallbackToDestructiveMigration() // Drop and recreate database on version change
                .build()
            INSTANCE = instance
            instance
        }
    }
}