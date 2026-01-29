package com.serenesec.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entity for non-RSS website bookmarks
 * These are websites that don't have RSS feeds but user wants quick access to
 */
@Entity(
    tableName = "bookmarks",
    indices = [
        Index(value = ["url"], unique = true)
    ]
)
data class BookmarkEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val url: String,
    val description: String? = null,
    val faviconUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val lastVisited: Long? = null
)
