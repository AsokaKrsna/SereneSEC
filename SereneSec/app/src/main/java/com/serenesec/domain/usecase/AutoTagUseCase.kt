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
     * Auto-assign tags to an article based on word-boundary matching.
     * Prevents partial substring false-positives (e.g. 'apt' matching 'adaptation').
     */
    suspend fun autoTagArticle(articleId: String, articleTitle: String) {
        val allTags = tagDao.getAllTagsList()
        if (allTags.isEmpty() || articleTitle.isBlank()) return

        // Match tags using word boundaries to ensure whole-token matching
        val matchingTags = allTags.filter { tag ->
            val tagName = tag.name.trim()
            if (tagName.isEmpty()) return@filter false
            val regex = Regex("\\b${Regex.escape(tagName)}\\b", RegexOption.IGNORE_CASE)
            regex.containsMatchIn(articleTitle)
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
