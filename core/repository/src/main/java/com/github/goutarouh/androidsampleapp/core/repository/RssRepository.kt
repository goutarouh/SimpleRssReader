package com.github.goutarouh.androidsampleapp.core.repository

import com.github.goutarouh.androidsampleapp.core.database.TransactionProcessExecutor
import com.github.goutarouh.androidsampleapp.core.database.dao.RssDao
import com.github.goutarouh.androidsampleapp.core.database.model.rss.RssFavoriteEntity
import com.github.goutarouh.androidsampleapp.core.database.model.rss.RssWrapperData
import com.github.goutarouh.androidsampleapp.core.network.data.rss.RssApiModel
import com.github.goutarouh.androidsampleapp.core.network.service.ZennRssService
import com.github.goutarouh.androidsampleapp.core.repository.model.rss.*
import com.github.goutarouh.androidsampleapp.core.repository.model.rss.toRss
import com.github.goutarouh.androidsampleapp.core.repository.model.rss.toRssEntity
import com.github.goutarouh.androidsampleapp.core.repository.model.rss.toRssItemEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

interface RssRepository {
    fun getRssListFlow(): Flow<List<Rss>>
    suspend fun updateRss(rssLink: String, isInit: Boolean): Rss
    suspend fun getRss(rssLink: String): Rss
    suspend fun changeFavorite(rssLink: String, isFavorite: Boolean)
}

internal class RssRepositoryImpl(
    val transactionProcessExecutor: TransactionProcessExecutor,
    val zennRssService: ZennRssService,
    val rssDao: RssDao
): RssRepository {

    override fun getRssListFlow(): Flow<List<Rss>> {
        val rssList = rssDao.getRssWrapperDataListFlow()
        return rssList.map {
            it.map { it.toRss() }
        }
    }

    override suspend fun updateRss(rssLink: String, isInit: Boolean): Rss = withContext(Dispatchers.IO) {
        val rssApiModel = zennRssService.getRss(rssLink)

        if (rssApiModel.items.isEmpty()) {
            throw NoRssItemException(rssLink)
        }

        val rssWrapperData = writeRssToDb(rssLink, rssApiModel, isInit)
        return@withContext rssWrapperData.toRss()
    }

    override suspend fun getRss(rssLink: String): Rss = withContext(Dispatchers.IO) {

        val hasRss = rssDao.hasRssEntity(rssLink)
        return@withContext if (hasRss) {
            val rssWrapperData = rssDao.getRssWrapperData(rssLink)
            rssWrapperData.toRss()
        } else {
            updateRss(rssLink, true)
        }
    }

    override suspend fun changeFavorite(rssLink: String, isFavorite: Boolean) = withContext(Dispatchers.IO) {
        rssDao.updateRssFavoriteEntity(RssFavoriteEntity(rssLink, isFavorite))
    }

    private suspend fun writeRssToDb(
        rssLink: String,
        rssApiModel: RssApiModel,
        isInit: Boolean
    ): RssWrapperData {
        val rssEntity = rssApiModel.toRssEntity(rssLink)
        val rssItemEntityList = rssApiModel.items.map {
            it.toRssItemEntity(rssLink)
        }
        val rssFavoriteEntity = RssFavoriteEntity(rssLink)

        transactionProcessExecutor.doTransactionProcess {
            rssDao.insertRssEntity(rssEntity)
            rssDao.insertRssItemEntityList(rssItemEntityList)
            if (isInit) {
                rssDao.insertRssFavoriteEntity(rssFavoriteEntity)
            }
        }
        return rssDao.getRssWrapperData(rssLink)
    }

}

