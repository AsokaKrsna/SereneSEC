package com.serenesec.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Collection entity for organizing articles into folders
 */
@Entity(tableName = "collections")
data class CollectionEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String? = null,
    val colorHex: Long,              // Accent color for the collection
    val iconName: String = "folder", // Material icon name
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Cross-reference table for many-to-many relationship between articles and collections
 * An article can be in multiple collections
 */
@Entity(
    tableName = "article_collections",
    primaryKeys = ["articleId", "collectionId"]
)
data class ArticleCollectionCrossRef(
    val articleId: String,
    val collectionId: String,
    val addedAt: Long = System.currentTimeMillis()
)
