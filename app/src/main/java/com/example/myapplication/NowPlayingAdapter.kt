package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class NowPlayingAdapter(
    private val onFavoriteClick: (Movie) -> Unit,
    private val onWatchedClick: (Movie) -> Unit // NEW
) : RecyclerView.Adapter<NowPlayingAdapter.MovieViewHolder>() {

    private var movies: List<Movie> = emptyList()

    fun submitList(list: List<Movie>) {
        movies = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_movie, parent, false)
        return MovieViewHolder(view)
    }

    override fun getItemCount(): Int = movies.size

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        holder.bind(movies[position])
    }

    inner class MovieViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        private val releaseDateTextView: TextView = itemView.findViewById(R.id.releaseDateTextView)
        private val genresTextView: TextView = itemView.findViewById(R.id.genresTextView)
        private val ratingTextView: TextView = itemView.findViewById(R.id.ratingTextView)
        private val posterImageView: ImageView = itemView.findViewById(R.id.posterImageView)
        private val favoriteButton: ImageButton = itemView.findViewById(R.id.favoriteButton)
        private val watchedButton: ImageButton = itemView.findViewById(R.id.watchedButton)

        fun bind(movie: Movie) {
            titleTextView.text = movie.title
            releaseDateTextView.text = movie.releaseDate
            genresTextView.text = movie.genres
            ratingTextView.text = "${movie.voteAverage}/10"

            if (movie.posterPath != null) {
                Glide.with(itemView.context)
                    .load("https://image.tmdb.org/t/p/w200${movie.posterPath}")
                    .placeholder(R.drawable.ic_movie_placeholder)
                    .error(R.drawable.ic_movie_placeholder)
                    .into(posterImageView)
            } else {
                posterImageView.setImageResource(R.drawable.ic_movie_placeholder)
            }

            val favoriteIcon = if (movie.isFavorite) {
                R.drawable.ic_favorite_filled
            } else {
                R.drawable.ic_favorite_border
            }
            favoriteButton.setImageResource(favoriteIcon)

            favoriteButton.setOnClickListener {
                onFavoriteClick(movie)
            }
            val watchedIcon = if (movie.isWatched) {
                R.drawable.favorite_24dp_1f1f1f
            } else {
                R.drawable.favorite_24dp_1f1f1f
            }
            watchedButton.setImageResource(watchedIcon)

            watchedButton.setOnClickListener {
                onWatchedClick(movie)
            }
        }
    }
}
