package com.example.myapplication

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import androidx.paging.PagingSource


@Dao
interface MovieDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovies(movies: List<Movie>)

    @Query("SELECT * FROM movies WHERE title LIKE '%' || :query || '%' COLLATE NOCASE")
    suspend fun searchMovies(query: String): List<Movie>

    @Query("SELECT * FROM movies")
    suspend fun getAllMovies(): List<Movie>

    @Query("SELECT COUNT(*) FROM movies")
    suspend fun getMovieCount(): Int

    @Query("SELECT * FROM movies LIMIT :count")
    suspend fun getLimitedMovies(count: Int): List<Movie>

    @Query("SELECT * FROM movies ORDER BY popularity DESC")
    fun getPagedMovies(): PagingSource<Int, Movie>

    @Query("SELECT * FROM movies WHERE title LIKE '%' || :query || '%' COLLATE NOCASE ORDER BY popularity DESC")
    fun searchMoviesPaged(query: String): PagingSource<Int, Movie>


}