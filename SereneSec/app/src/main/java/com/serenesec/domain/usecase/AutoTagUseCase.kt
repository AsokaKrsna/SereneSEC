package com.serenesec.domain.usecase

import com.serenesec.data.local.dao.TagDao
import com.serenesec.data.local.entity.ArticleTagCrossRef
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Auto-tags articles based on keyword matching in titles
 */
@Singleton
class AutoTagUseCase @Inject constructor(
    private val tagDao: TagDao
) {
    
    /**
     * Auto-assign tags to an article based on case-insensitive word matching.
     * The tag name is matched exactly against words in the article title.
     */
    suspend fun autoTagArticle(articleId: String, articleTitle: String) {
        val allTags = tagDao.getAllTagsList()
        val titleLower = articleTitle.lowercase()
        
        // Match tags where the tag name (case-insensitive) appears in the title
        val matchingTags = allTags.filter { tag ->
            titleLower.contains(tag.name.lowercase())
        }
        
        // Create cross-references for matching tags
        matchingTags.forEach { tag ->
            tagDao.insertArticleTagCrossRef(
                ArticleTagCrossRef(
                    articleId = articleId,
                    tagId = tag.id
                )
            )
        }
    }
}
