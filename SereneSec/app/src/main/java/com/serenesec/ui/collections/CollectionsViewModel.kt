package com.serenesec.ui.collections

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.serenesec.data.local.dao.CollectionDao
import com.serenesec.data.local.entity.ArticleCollectionCrossRef
import com.serenesec.data.local.entity.CollectionEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

// Preset collection colors
val collectionColorOptions = listOf(
    Color(0xFF5C6BC0),  // Indigo
    Color(0xFF42A5F5),  // Blue
    Color(0xFF26C6DA),  // Cyan
    Color(0xFF26A69A),  // Teal
    Color(0xFF66BB6A),  // Green
    Color(0xFFFFCA28),  // Amber
    Color(0xFFFFA726),  // Orange
    Color(0xFFEF5350),  // Red
    Color(0xFFEC407A),  // Pink
    Color(0xFFAB47BC),  // Purple
    Color(0xFF8D6E63),  // Brown
    Color(0xFF78909C),  // Blue Grey
)

// Collection icon options
val collectionIconOptions = listOf(
    "folder" to "📁",
    "star" to "⭐",
    "bookmark" to "🔖",
    "flag" to "🚩",
    "lock" to "🔒",
    "fire" to "🔥",
    "target" to "🎯",
    "lightning" to "⚡",
    "shield" to "🛡️",
    "bug" to "🐛",
    "code" to "💻",
    "warning" to "⚠️"
)

@HiltViewModel
class CollectionsViewModel @Inject constructor(
    private val collectionDao: CollectionDao
) : ViewModel() {
    
    val collections: StateFlow<List<CollectionEntity>> = collectionDao.getAllCollections()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    private val _editingCollection = MutableStateFlow<CollectionEntity?>(null)
    val editingCollection: StateFlow<CollectionEntity?> = _editingCollection.asStateFlow()
    
    fun createCollection(name: String, description: String?, colorHex: Long, iconName: String) {
        viewModelScope.launch {
            val collection = CollectionEntity(
                id = UUID.randomUUID().toString(),
                name = name.trim(),
                description = description?.trim()?.takeIf { it.isNotEmpty() },
                colorHex = colorHex,
                iconName = iconName
            )
            collectionDao.insertCollection(collection)
        }
    }
    
    fun updateCollection(collection: CollectionEntity) {
        viewModelScope.launch {
            collectionDao.updateCollection(collection.copy(updatedAt = System.currentTimeMillis()))
        }
    }
    
    fun deleteCollection(collectionId: String) {
        viewModelScope.launch {
            collectionDao.deleteCollectionWithArticles(collectionId)
        }
    }
    
    fun setEditingCollection(collection: CollectionEntity?) {
        _editingCollection.value = collection
    }
    
    // For article management
    fun addArticleToCollection(articleId: String, collectionId: String) {
        viewModelScope.launch {
            collectionDao.addArticleToCollection(ArticleCollectionCrossRef(articleId, collectionId))
            collectionDao.touchCollection(collectionId)
        }
    }
    
    fun removeArticleFromCollection(articleId: String, collectionId: String) {
        viewModelScope.launch {
            collectionDao.removeArticleFromCollectionById(articleId, collectionId)
        }
    }
    
    suspend fun getCollectionsForArticle(articleId: String): List<CollectionEntity> {
        return collectionDao.getCollectionsForArticleList(articleId)
    }
    
    suspend fun isArticleInCollection(articleId: String, collectionId: String): Boolean {
        return collectionDao.isArticleInCollection(articleId, collectionId)
    }
    
    suspend fun getArticleCount(collectionId: String): Int {
        return collectionDao.getArticleCountForCollectionSync(collectionId)
    }
}
