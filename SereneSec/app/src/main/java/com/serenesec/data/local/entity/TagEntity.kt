package com.serenesec.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Custom tag entity for user-created article tags
 */
@Entity(tableName = "tags")
data class TagEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val colorHex: Long,        // Color stored as ARGB long
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Cross-reference table for many-to-many relationship between articles and tags
 */
@Entity(
    tableName = "article_tags",
    primaryKeys = ["articleId", "tagId"]
)
data class ArticleTagCrossRef(
    val articleId: String,
    val tagId: String
)
