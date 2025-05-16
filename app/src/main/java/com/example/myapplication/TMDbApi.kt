package com.example.myapplication

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

    interface TMDbApi {
        @GET("movie/now_playing")
        fun getNowPlayingMovies(
            @Query("api_key") apiKey: String,
            @Query("language") language: String,
            @Query("page") page: Int
        ): Call<MovieResponse>
    }
