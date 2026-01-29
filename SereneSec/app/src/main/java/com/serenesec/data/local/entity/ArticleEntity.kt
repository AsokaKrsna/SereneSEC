package com.serenesec.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.serenesec.domain.model.Article
import com.serenesec.domain.model.ArticleState
import com.serenesec.domain.model.Category

/**
 * Room entity for articles
 */
@Entity(
    tableName = "articles",
    foreignKeys = [
        ForeignKey(
            entity = SourceEntity::class,
            parentColumns = ["id"],
            childColumns = ["sourceId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["sourceId"]),
        Index(value = ["state"]),
        Index(value = ["publishedDate"]),
        Index(value = ["contentUrl"], unique = true),
        Index(value = ["isFavorite"]),
        Index(value = ["isSavedForLater"])
    ]
)
data class ArticleEntity(
    @PrimaryKey
    val id: String,
    val sourceId: String,
    val title: String,
    val summary: String?,
    val contentUrl: String,
    val publishedDate: Long,
    val state: String,
    val category: String,
    val contentHtml: String? = null,
    val author: String? = null,
    val imageUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val isSavedForLater: Boolean = false
) {
    fun toDomain(): Article = Article(
        id = id,
        sourceId = sourceId,
        title = title,
        summary = summary,
        contentUrl = contentUrl,
        publishedDate = publishedDate,
        state = ArticleState.valueOf(state),
        category = Category.valueOf(category),
        contentHtml = contentHtml,
        author = author,
        imageUrl = imageUrl,
        createdAt = createdAt,
        isFavorite = isFavorite,
        isSavedForLater = isSavedForLater
    )

    companion object {
        fun fromDomain(article: Article): ArticleEntity = ArticleEntity(
            id = article.id,
            sourceId = article.sourceId,
            title = article.title,
            summary = article.summary,
            contentUrl = article.contentUrl,
            publishedDate = article.publishedDate,
            state = article.state.name,
            category = article.category.name,
            contentHtml = article.contentHtml,
            author = article.author,
            imageUrl = article.imageUrl,
            createdAt = article.createdAt,
            isFavorite = article.isFavorite,
            isSavedForLater = article.isSavedForLater
        )
    }
}
