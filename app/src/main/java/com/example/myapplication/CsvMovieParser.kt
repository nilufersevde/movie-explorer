package com.example.myapplication

import android.content.Context
import com.opencsv.CSVReader
import java.io.InputStreamReader

object CsvMovieParser {
    fun parseCsv(context: Context): List<Movie> {
        val movies = mutableListOf<Movie>()
        val inputStream = context.assets.open("TMDB_movie_dataset_v11.csv")
        val reader = CSVReader(InputStreamReader(inputStream))
        val rows = reader.readAll()
        val header = rows.first()
        val dataRows = rows.drop(1)

        for (row in dataRows) {
            try {
                val id = row[0].toInt()
                val title = row[1]
                val voteAverage = row[2].toDouble()
                val releaseDate = row[5]
                val overview = row[11]
                val posterPath = row[13]
                val genres = row[15]
                val popularity = row[10].toDouble()

                val movie = Movie(
                    id = id,
                    title = title,
                    releaseDate = releaseDate,
                    genres = genres,
                    popularity = popularity,
                    voteAverage = voteAverage,
                    overview = overview,
                    posterPath = posterPath
                )
                movies.add(movie)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return movies
    }
}
