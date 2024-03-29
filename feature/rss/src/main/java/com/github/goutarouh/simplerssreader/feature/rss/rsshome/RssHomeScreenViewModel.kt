package com.github.goutarouh.simplerssreader.feature.rss.rsshome

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.goutarouh.simplerssreader.core.repository.RssRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RssHomeScreenViewModel @Inject constructor(
    private val rssRepository: RssRepository
): ViewModel() {

    private val rssFavoriteList = rssRepository.getRssListFlow()

    private val _uiState = MutableStateFlow<RssHomeScreenUiState>(RssHomeScreenUiState.Initial)
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            rssFavoriteList.collect {
                _uiState.emit(RssHomeScreenUiState.Success(it))
            }
        }
    }

    fun deleteRss(rssLink: String) {
        viewModelScope.launch {
            rssRepository.deleteAndUnregisterRss(rssLink)
        }
    }
}