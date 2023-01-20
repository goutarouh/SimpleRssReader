package com.github.goutarouh.androidsampleapp.feature.rss.rsshome

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.goutarouh.androidsampleapp.core.repository.RssRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combineTransform
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
                val (favoriteList, unFavoriteList) = it.partition { it.isFavorite }
                _uiState.emit(RssHomeScreenUiState.Success(favoriteList, unFavoriteList))
            }
        }
    }

    fun deleteRss(rssLink: String) {
        viewModelScope.launch {
            rssRepository.deleteRss(rssLink)
        }
    }
}