package com.github.goutarouh.simplerssreader.feature.rss.rssitemlist

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.goutarouh.simplerssreader.core.repository.RssRepository
import com.github.goutarouh.simplerssreader.core.util.data.Result
import com.github.goutarouh.simplerssreader.core.util.navigation.UrlNavArg
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RssItemListScreenViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val rssRepository: RssRepository
): ViewModel() {

    private val rssItemListNavArgs = RssItemListNavArgs(savedStateHandle)
    private val rssItemLink = with(UrlNavArg) { rssItemListNavArgs.rssLink.navArgDecode() }

    private val _uiState = MutableStateFlow<RssItemListScreenUiState>(RssItemListScreenUiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            updateUiState(rssItemLink)
            rssRepository.setUnReadItemCount(rssItemLink, 0)
        }
    }

    private suspend fun updateUiState(rssLink: String) {
        when (val result = rssRepository.getRss(rssLink)) {
            is Result.Success -> {
                _uiState.emit(RssItemListScreenUiState.Success(result.data, null))
            }
            is Result.Error -> {
                _uiState.emit(RssItemListScreenUiState.Error(result.e))
            }
        }
    }

    fun setAutoFetch(rssLink: String, isAutoFetch: Boolean) {
        viewModelScope.launch {
            try {
                rssRepository.setAutoFetch(rssLink, isAutoFetch)
            } catch (e: Exception) {
                when (val state = _uiState.value) {
                    is RssItemListScreenUiState.Success -> {
                        _uiState.emit(state.copy(workerEvent = if (isAutoFetch) SetWorkerEvent.RegisteredFailed else SetWorkerEvent.UnRegisteredFailed))
                    }
                    else -> {}
                }
                // TODO uiStateに送出するほどのエラーではない
            }
            updateUiState(rssLink)

            val setWorkerEvent = setAutoFetchWorker(isAutoFetch)
            when (val state = _uiState.value) {
                is RssItemListScreenUiState.Success -> {
                    _uiState.emit(state.copy(workerEvent = setWorkerEvent))
                }
                else -> {}
            }
        }
    }

    fun setPushNotification(rssLink: String, isPushNotification: Boolean) {
        viewModelScope.launch {
            try {
                rssRepository.setPushNotification(rssLink, isPushNotification)
            } catch (e: Exception) {
                // TODO uiStateに送出するほどのエラーではない
            }
            updateUiState(rssLink)
        }
    }

    private fun setAutoFetchWorker(isAutoFetch: Boolean): SetWorkerEvent? {
        val state = uiState.value
        return if (state is RssItemListScreenUiState.Success) {
            if (isAutoFetch) {
                val result = rssRepository.registerWorker(state.rss.rssLink, state.rss.title)
                if (result) SetWorkerEvent.RegisteredSuccess else SetWorkerEvent.RegisteredFailed
            } else {
                val result = rssRepository.unRegisterWorker(state.rss.rssLink)
                if (result) SetWorkerEvent.UnRegisteredSuccess else SetWorkerEvent.UnRegisteredFailed
            }
        } else {
            null
        }
    }

    fun updateRss(rssLink: String) {
        viewModelScope.launch {
            when (val result = rssRepository.updateRss(rssLink, false)) {
                is Result.Success -> {
                    _uiState.emit(RssItemListScreenUiState.Success(result.data, null))
                }
                is Result.Error -> {
                    _uiState.emit(RssItemListScreenUiState.Error(result.e))
                }
            }
        }
    }

    fun setWorkerEventDone() {
        viewModelScope.launch {
            when (val state = _uiState.value) {
                is RssItemListScreenUiState.Success -> {
                    _uiState.emit(state.copy(workerEvent = null))
                }
                else -> {}
            }
        }
    }
}

private const val rssLinkArg = "rssLink"
internal class RssItemListNavArgs(val rssLink: String) {
    constructor(savedStateHandle: SavedStateHandle): this(checkNotNull(savedStateHandle[rssLinkArg]) as String)
}