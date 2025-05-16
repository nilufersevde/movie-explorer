package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class MovieAdapter(
    private val onFavoriteClick: (Movie) -> Unit
) : PagingDataAdapter<Movie, MovieAdapter.MovieViewHolder>(MovieDiffCallback()) {

    private var movieList: List<Movie> = emptyList()

    // For NowPlayingActivity to submit a List<Movie>
    fun submitList(movies: List<Movie>) {
        movieList = movies
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_movie, parent, false)
        return MovieViewHolder(view)
    }

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        val movie = getItem(position) ?: movieList.getOrNull(position)
        movie?.let { holder.bind(it) }
    }

    override fun getItemCount(): Int {
        return if (movieList.isNotEmpty()) movieList.size else super.getItemCount()
    }

    inner class MovieViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        private val releaseDateTextView: TextView = itemView.findViewById(R.id.releaseDateTextView)
        private val genresTextView: TextView = itemView.findViewById(R.id.genresTextView)
        private val ratingTextView: TextView = itemView.findViewById(R.id.ratingTextView)
        private val posterImageView: ImageView = itemView.findViewById(R.id.posterImageView)
        private val favoriteButton: ImageButton = itemView.findViewById(R.id.favoriteButton)

        fun bind(movie: Movie) {
            titleTextView.text = movie.title
            releaseDateTextView.text = movie.releaseDate
            genresTextView.text = movie.genres
            ratingTextView.text = "${movie.voteAverage}/10"

            // Load poster if available
            if (movie.posterPath != null) {
                Glide.with(itemView.context)
                    .load("https://image.tmdb.org/t/p/w200${movie.posterPath}")
                    .placeholder(R.drawable.ic_movie_placeholder)
                    .error(R.drawable.ic_movie_placeholder)
                    .into(posterImageView)
            } else {
                posterImageView.setImageResource(R.drawable.ic_movie_placeholder)
            }

            // Set favorite icon based on status
            val favoriteIcon = if (movie.isFavorite) {
                R.drawable.ic_favorite_filled
            } else {
                R.drawable.ic_favorite_border
            }
            favoriteButton.setImageResource(favoriteIcon)

            // Set click listener for favorite button
            favoriteButton.setOnClickListener {
                onFavoriteClick(movie)
            }
        }
    }

    private class MovieDiffCallback : DiffUtil.ItemCallback<Movie>() {
        override fun areItemsTheSame(oldItem: Movie, newItem: Movie): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Movie, newItem: Movie): Boolean {
            return oldItem == newItem // This checks all fields including isFavorite
        }
    }
}

//package com.example.myapplication
//
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.ImageButton
//import android.widget.ImageView
//import android.widget.TextView
//import androidx.recyclerview.widget.DiffUtil
//import androidx.recyclerview.widget.ListAdapter
//import androidx.recyclerview.widget.RecyclerView
//import com.bumptech.glide.Glide
//import androidx.paging.PagingDataAdapter
//
//class MovieAdapter(
//    private val onFavoriteClick: (Movie) -> Unit
//) : PagingDataAdapter<Movie, MovieAdapter.MovieViewHolder>(MovieDiffCallback()) {
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
//        val view = LayoutInflater.from(parent.context)
//            .inflate(R.layout.item_movie, parent, false)
//        return MovieViewHolder(view)
//    }
//
//    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
//        val movie = getItem(position)
//        movie?.let { holder.bind(it) }
//    }
//
//    inner class MovieViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        private val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
//        private val releaseDateTextView: TextView = itemView.findViewById(R.id.releaseDateTextView)
//        private val genresTextView: TextView = itemView.findViewById(R.id.genresTextView)
//        private val ratingTextView: TextView = itemView.findViewById(R.id.ratingTextView)
//        private val posterImageView: ImageView = itemView.findViewById(R.id.posterImageView)
//        private val favoriteButton: ImageButton = itemView.findViewById(R.id.favoriteButton)
//
//        fun bind(movie: Movie) {
//            titleTextView.text = movie.title
//            releaseDateTextView.text = movie.releaseDate
//            genresTextView.text = movie.genres
//            ratingTextView.text = "${movie.voteAverage}/10"
//
//            // Load poster if available
//            if (movie.posterPath != null) {
//                Glide.with(itemView.context)
//                    .load("https://image.tmdb.org/t/p/w200${movie.posterPath}")
//                    .placeholder(R.drawable.ic_movie_placeholder)
//                    .error(R.drawable.ic_movie_placeholder)
//                    .into(posterImageView)
//            } else {
//                posterImageView.setImageResource(R.drawable.ic_movie_placeholder)
//            }
//
//            // Set favorite icon based on status
//            val favoriteIcon = if (movie.isFavorite) {
//                R.drawable.ic_favorite_filled
//            } else {
//                R.drawable.ic_favorite_border
//            }
//            favoriteButton.setImageResource(favoriteIcon)
//
//            // Set click listener for favorite button
//            favoriteButton.setOnClickListener {
//                onFavoriteClick(movie)
//            }
//        }
//    }
//
//    private class MovieDiffCallback : DiffUtil.ItemCallback<Movie>() {
//        override fun areItemsTheSame(oldItem: Movie, newItem: Movie): Boolean {
//            return oldItem.id == newItem.id
//        }
//
//        override fun areContentsTheSame(oldItem: Movie, newItem: Movie): Boolean {
//            return oldItem == newItem // This checks all fields including isFavorite
//        }
//    }
//}