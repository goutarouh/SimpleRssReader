package com.github.goutarouh.androidsampleapp.core.repository

import com.github.goutarouh.androidsampleapp.core.network.service.ZennRssService
import com.github.goutarouh.androidsampleapp.core.repository.model.rss.Rss
import com.github.goutarouh.androidsampleapp.core.repository.model.rss.toRss
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface Data1Repository {
    suspend fun getRss(): Rss
}

internal class Data1RepositoryImpl(
    val zennRssService: ZennRssService,
): Data1Repository {
    override suspend fun getRss(): Rss = withContext(Dispatchers.IO) {
        return@withContext zennRssService.getData().toRss()
    }
}