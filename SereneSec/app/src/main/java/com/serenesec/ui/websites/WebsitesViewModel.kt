package com.serenesec.ui.websites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.serenesec.data.local.dao.BookmarkDao
import com.serenesec.data.local.entity.BookmarkEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.security.MessageDigest
import javax.inject.Inject

@HiltViewModel
class WebsitesViewModel @Inject constructor(
    private val bookmarkDao: BookmarkDao
) : ViewModel() {
    
    val bookmarks: StateFlow<List<BookmarkEntity>> = bookmarkDao.getAllBookmarks()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    fun addBookmark(title: String, url: String, description: String?) {
        viewModelScope.launch {
            val id = generateId(url)
            val bookmark = BookmarkEntity(
                id = id,
                title = title,
                url = url,
                description = description
            )
            bookmarkDao.insertBookmark(bookmark)
        }
    }
    
    fun deleteBookmark(id: String) {
        viewModelScope.launch {
            bookmarkDao.deleteBookmarkById(id)
        }
    }
    
    fun updateLastVisited(id: String) {
        viewModelScope.launch {
            bookmarkDao.updateLastVisited(id, System.currentTimeMillis())
        }
    }
    
    private fun generateId(url: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(url.toByteArray())
        return bytes.take(16).joinToString("") { "%02x".format(it) }
    }
}
