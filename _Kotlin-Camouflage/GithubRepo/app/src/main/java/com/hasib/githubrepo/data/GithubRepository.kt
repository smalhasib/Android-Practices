package com.hasib.githubrepo.data

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.hasib.githubrepo.api.GithubService
import com.hasib.githubrepo.db.RepoDatabase
import com.hasib.githubrepo.model.Repo
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Repository class that works with local and remote data sources.
 */
@ViewModelScoped
class GithubRepository @Inject constructor(
    private val service: GithubService,
    private val database: RepoDatabase
) {
    /**
     * Search repositories whose names match the query, exposed as a stream of data that will emit
     * every time we get more data from the network.
     */
    fun getSearchResultStream(query: String): Flow<PagingData<Repo>> {
        // appending '%' so we can allow other characters to be before and after the query string
        val dbQuery = "%${query.replace(' ', '%')}%"
        val pagingSourceFactory =  { database.reposDao().reposByName(dbQuery)}

        @OptIn(ExperimentalPagingApi::class)
        return Pager(
            config = PagingConfig(
                pageSize = NETWORK_PAGE_SIZE,
                enablePlaceholders = false
            ),
            remoteMediator = GithubRemoteMediator(
                query,
                service,
                database
            ),
            pagingSourceFactory = pagingSourceFactory
        ).flow
    }

    companion object {
        const val NETWORK_PAGE_SIZE = 30
    }
}
