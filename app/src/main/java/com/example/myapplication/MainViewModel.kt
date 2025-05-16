package com.example.myapplication

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import kotlinx.coroutines.flow.Flow

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val movieDao = DatabaseProvider.getDatabase(application).movieDao()
    fun getSearchResults(query: String): Flow<PagingData<Movie>> {
        return Pager(
            config = PagingConfig(pageSize = 20),
            pagingSourceFactory = { movieDao.searchMoviesPaged(query) }
        ).flow.cachedIn(viewModelScope)
    }

    val moviePagingFlow = Pager(
        config = PagingConfig(pageSize = 20),
        pagingSourceFactory = { movieDao.getPagedMovies() }
    ).flow.cachedIn(viewModelScope)
}

