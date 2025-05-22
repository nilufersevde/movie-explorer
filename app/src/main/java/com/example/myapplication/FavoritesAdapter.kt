package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class FavoritesAdapter(
    private val onRemoveFavorite: (FavoriteMovie) -> Unit
) : ListAdapter<FavoriteMovie, FavoritesAdapter.FavoriteViewHolder>(FavoriteMovieDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.favorite_movie, parent, false)
        return FavoriteViewHolder(view)
    }

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class FavoriteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val posterImageView: ImageView = itemView.findViewById(R.id.posterImageView)
        private val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        private val releaseDateTextView: TextView = itemView.findViewById(R.id.releaseDateTextView)
        private val ratingTextView: TextView = itemView.findViewById(R.id.ratingTextView)
        private val removeButton: ImageButton = itemView.findViewById(R.id.removeButton)

        fun bind(favoriteMovie: FavoriteMovie) {
            titleTextView.text = favoriteMovie.title
            releaseDateTextView.text = favoriteMovie.releaseDate
            ratingTextView.text = "${favoriteMovie.rating}/10"

            // Load poster
            if (!favoriteMovie.posterPath.isNullOrEmpty()) {
                Glide.with(itemView.context)
                    .load("https://image.tmdb.org/t/p/w200${favoriteMovie.posterPath}")
                    .placeholder(R.drawable.ic_movie_placeholder)
                    .error(R.drawable.ic_movie_placeholder)
                    .into(posterImageView)
            } else {
                posterImageView.setImageResource(R.drawable.ic_movie_placeholder)
            }
            android.util.Log.d("FavoritesAdapter", "Remove button clicked for: ${favoriteMovie.title}")

            // Remove favorite button
            removeButton.setOnClickListener {
                onRemoveFavorite(favoriteMovie)
            }
        }
    }

    private class FavoriteMovieDiffCallback : DiffUtil.ItemCallback<FavoriteMovie>() {
        override fun areItemsTheSame(oldItem: FavoriteMovie, newItem: FavoriteMovie): Boolean {
            return oldItem.documentId == newItem.documentId
        }

        override fun areContentsTheSame(oldItem: FavoriteMovie, newItem: FavoriteMovie): Boolean {
            return oldItem == newItem
        }
    }
}