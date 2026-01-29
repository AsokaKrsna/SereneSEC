package com.serenesec.domain.model

/**
 * Domain model representing an article
 */
data class Article(
    val id: String,
    val sourceId: String,
    val title: String,
    val summary: String?,
    val contentUrl: String,
    val publishedDate: Long,
    val state: ArticleState = ArticleState.UNREAD,
    val category: Category,
    val contentHtml: String? = null,
    val author: String? = null,
    val imageUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val isSavedForLater: Boolean = false
) {
    val isUnread: Boolean get() = state == ArticleState.UNREAD
    val isRead: Boolean get() = state == ArticleState.READ
    val isArchived: Boolean get() = state == ArticleState.ARCHIVED
}
