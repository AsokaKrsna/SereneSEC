package com.serenesec.ui.collections

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.serenesec.data.local.dao.ArticleDao
import com.serenesec.data.local.dao.CollectionDao
import com.serenesec.domain.model.Article
import com.serenesec.domain.repository.ArticleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CollectionDetailViewModel @Inject constructor(
    private val collectionDao: CollectionDao,
    private val articleRepository: ArticleRepository
) : ViewModel() {
    
    fun getArticlesInCollection(collectionId: String): Flow<List<Article>> {
        return collectionDao.getArticlesInCollection(collectionId).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    fun removeFromCollection(articleId: String, collectionId: String) {
        viewModelScope.launch {
            collectionDao.removeArticleFromCollectionById(articleId, collectionId)
        }
    }
    
    fun toggleFavorite(articleId: String, isFavorite: Boolean) {
        viewModelScope.launch {
            articleRepository.toggleFavorite(articleId, isFavorite)
        }
    }
    
    fun toggleSavedForLater(articleId: String, saved: Boolean) {
        viewModelScope.launch {
            articleRepository.toggleSavedForLater(articleId, saved)
        }
    }
}
