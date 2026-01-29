package com.serenesec.ui.reader

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.serenesec.data.remote.FeedFetcher
import com.serenesec.domain.model.Article
import com.serenesec.domain.repository.ArticleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReaderViewModel @Inject constructor(
    private val articleRepository: ArticleRepository,
    private val feedFetcher: FeedFetcher,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    private val articleId: String = savedStateHandle["articleId"] ?: ""
    
    private val _article = MutableStateFlow<Article?>(null)
    val article: StateFlow<Article?> = _article.asStateFlow()
    
    private val _isFocusMode = MutableStateFlow(false)
    val isFocusMode: StateFlow<Boolean> = _isFocusMode.asStateFlow()
    
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _cachedHtml = MutableStateFlow<String?>(null)
    val cachedHtml: StateFlow<String?> = _cachedHtml.asStateFlow()
    
    init {
        loadArticle()
    }
    
    private fun loadArticle() {
        viewModelScope.launch {
            val loadedArticle = articleRepository.getArticleById(articleId)
            _article.value = loadedArticle
            _cachedHtml.value = loadedArticle?.contentHtml
            
            // Mark as read
            if (loadedArticle != null && loadedArticle.isUnread) {
                articleRepository.markAsRead(articleId)
            }
            
            _isLoading.value = false
        }
    }
    
    fun toggleFocusMode() {
        _isFocusMode.value = !_isFocusMode.value
    }
    
    fun exitFocusMode() {
        _isFocusMode.value = false
    }
    
    fun archiveArticle() {
        viewModelScope.launch {
            articleRepository.archiveArticle(articleId)
        }
    }
    
    fun cacheCurrentHtml(html: String) {
        viewModelScope.launch {
            articleRepository.cacheArticleHtml(articleId, html)
            _cachedHtml.value = html
        }
    }
}
