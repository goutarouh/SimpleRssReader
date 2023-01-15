package com.github.goutarouh.androidsampleapp.core.repository.workmanager

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.github.goutarouh.androidsampleapp.core.repository.RssRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class RssFetchWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val rssRepository: RssRepository
): CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val rssLink = inputData.getString(RSS_LINK) ?: return Result.failure()
        val changed = try {
            rssRepository.checkIfRssChanged(rssLink)
        } catch (e: Exception) {
            false
        }
        return Result.success()
    }

    companion object {
        const val RSS_LINK = "RSS_LINK"
    }
}