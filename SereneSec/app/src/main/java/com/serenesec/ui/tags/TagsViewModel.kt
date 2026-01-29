package com.serenesec.ui.tags

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.serenesec.data.local.dao.TagDao
import com.serenesec.data.local.entity.ArticleTagCrossRef
import com.serenesec.data.local.entity.TagEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class Tag(
    val id: String,
    val name: String,
    val color: Color,
    val articleCount: Int = 0
)

// Preset tag color options
val tagColorOptions = listOf(
    Color(0xFFEF5350),  // Red
    Color(0xFFEC407A),  // Pink
    Color(0xFFAB47BC),  // Purple
    Color(0xFF7E57C2),  // Deep Purple
    Color(0xFF5C6BC0),  // Indigo
    Color(0xFF42A5F5),  // Blue
    Color(0xFF29B6F6),  // Light Blue
    Color(0xFF26C6DA),  // Cyan
    Color(0xFF26A69A),  // Teal
    Color(0xFF66BB6A),  // Green
    Color(0xFF9CCC65),  // Light Green
    Color(0xFFD4E157),  // Lime
    Color(0xFFFFCA28),  // Amber
    Color(0xFFFFA726),  // Orange
    Color(0xFFFF7043),  // Deep Orange
    Color(0xFF8D6E63),  // Brown
)

@HiltViewModel
class TagsViewModel @Inject constructor(
    private val tagDao: TagDao
) : ViewModel() {
    
    val tags: StateFlow<List<TagEntity>> = tagDao.getAllTags()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    private val _editingTag = MutableStateFlow<TagEntity?>(null)
    val editingTag: StateFlow<TagEntity?> = _editingTag.asStateFlow()
    
    fun createTag(name: String, colorHex: Long) {
        viewModelScope.launch {
            val tag = TagEntity(
                id = UUID.randomUUID().toString(),
                name = name.trim(),
                colorHex = colorHex
            )
            tagDao.insertTag(tag)
        }
    }
    
    fun updateTag(tag: TagEntity) {
        viewModelScope.launch {
            tagDao.updateTag(tag)
        }
    }
    
    fun deleteTag(tagId: String) {
        viewModelScope.launch {
            tagDao.deleteTagWithAssociations(tagId)
        }
    }
    
    fun setEditingTag(tag: TagEntity?) {
        _editingTag.value = tag
    }
    
    // For article tagging
    fun addTagToArticle(articleId: String, tagId: String) {
        viewModelScope.launch {
            tagDao.addTagToArticle(ArticleTagCrossRef(articleId, tagId))
        }
    }
    
    fun removeTagFromArticle(articleId: String, tagId: String) {
        viewModelScope.launch {
            tagDao.removeTagFromArticleById(articleId, tagId)
        }
    }
    
    suspend fun getTagsForArticle(articleId: String): List<TagEntity> {
        return tagDao.getTagsForArticleList(articleId)
    }
}
