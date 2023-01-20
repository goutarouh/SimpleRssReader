package com.github.goutarouh.androidsampleapp.feature.rss.rssitemlist

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.goutarouh.androidsampleapp.core.repository.RssRepository
import com.github.goutarouh.androidsampleapp.core.util.string.decode64
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
    val rssItemLink = rssItemListNavArgs.rssLink.decode64()

    private val _uiState = MutableStateFlow<RssItemListScreenUiState>(RssItemListScreenUiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            updateUiState(rssItemLink)
        }
    }

    private suspend fun updateUiState(rssLink: String) {
        try {
            val rss = rssRepository.getRss(rssLink)
            _uiState.emit(RssItemListScreenUiState.Success(rss))
        } catch (e: Exception) {
            _uiState.emit(RssItemListScreenUiState.Error(e))
        }
    }

    fun setAutoFetch(rssLink: String, isAutoFetch: Boolean) {
        viewModelScope.launch {
            try {
                rssRepository.setAutoFetch(rssLink, isAutoFetch)
            } catch (e: Exception) {
                // TODO uiStateに送出するほどのエラーではない
            }

            val state = uiState.value
            if (state is RssItemListScreenUiState.Success) {
                if (isAutoFetch) {
                    rssRepository.registerWorker(state.rss.rssLink, state.rss.title)
                } else {
                    rssRepository.unRegisterWorker(state.rss.rssLink)
                }
            }
        }

    }

    fun updateRss(rssLink: String) {
        viewModelScope.launch {
            try {
                val rss = rssRepository.updateRss(rssLink, false)
                _uiState.emit(RssItemListScreenUiState.Success(rss))
            } catch (e: Exception) {
                _uiState.emit(RssItemListScreenUiState.Error(e))
            }
        }
    }
}

private const val rssLinkArg = "rssLink"
internal class RssItemListNavArgs(val rssLink: String) {
    constructor(savedStateHandle: SavedStateHandle): this(checkNotNull(savedStateHandle[rssLinkArg]) as String)
}