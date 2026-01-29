package com.serenesec.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.serenesec.data.local.entity.ArticleTagCrossRef
import com.serenesec.data.local.entity.TagEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {
    
    // Tag operations
    @Query("SELECT * FROM tags ORDER BY name ASC")
    fun getAllTags(): Flow<List<TagEntity>>
    
    @Query("SELECT * FROM tags ORDER BY name ASC")
    suspend fun getAllTagsList(): List<TagEntity>
    
    @Query("SELECT * FROM tags WHERE id = :id")
    suspend fun getTagById(id: String): TagEntity?
    
    @Query("SELECT * FROM tags WHERE name = :name")
    suspend fun getTagByName(name: String): TagEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTag(tag: TagEntity)
    
    @Update
    suspend fun updateTag(tag: TagEntity)
    
    @Delete
    suspend fun deleteTag(tag: TagEntity)
    
    @Query("DELETE FROM tags WHERE id = :id")
    suspend fun deleteTagById(id: String)
    
    @Query("SELECT COUNT(*) FROM tags")
    fun getTagCount(): Flow<Int>
    
    // Article-Tag relationship operations
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addTagToArticle(crossRef: ArticleTagCrossRef)
    
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertArticleTagCrossRef(crossRef: ArticleTagCrossRef)
    
    @Delete
    suspend fun removeTagFromArticle(crossRef: ArticleTagCrossRef)
    
    @Query("DELETE FROM article_tags WHERE articleId = :articleId AND tagId = :tagId")
    suspend fun removeTagFromArticleById(articleId: String, tagId: String)
    
    @Query("DELETE FROM article_tags WHERE tagId = :tagId")
    suspend fun removeAllArticlesFromTag(tagId: String)
    
    @Query("DELETE FROM article_tags WHERE articleId = :articleId")
    suspend fun removeAllTagsFromArticle(articleId: String)
    
    // Get tags for an article
    @Query("""
        SELECT t.* FROM tags t
        INNER JOIN article_tags at ON t.id = at.tagId
        WHERE at.articleId = :articleId
        ORDER BY t.name ASC
    """)
    fun getTagsForArticle(articleId: String): Flow<List<TagEntity>>
    
    @Query("""
        SELECT t.* FROM tags t
        INNER JOIN article_tags at ON t.id = at.tagId
        WHERE at.articleId = :articleId
        ORDER BY t.name ASC
    """)
    suspend fun getTagsForArticleList(articleId: String): List<TagEntity>
    
    // Get article IDs for a tag
    @Query("SELECT articleId FROM article_tags WHERE tagId = :tagId")
    fun getArticleIdsForTag(tagId: String): Flow<List<String>>
    
    // Get count of articles with a tag
    @Query("SELECT COUNT(*) FROM article_tags WHERE tagId = :tagId")
    fun getArticleCountForTag(tagId: String): Flow<Int>
    
    // Check if article has a specific tag
    @Query("SELECT EXISTS(SELECT 1 FROM article_tags WHERE articleId = :articleId AND tagId = :tagId)")
    suspend fun articleHasTag(articleId: String, tagId: String): Boolean
    
    // Transaction to delete tag and all its associations
    @Transaction
    suspend fun deleteTagWithAssociations(tagId: String) {
        removeAllArticlesFromTag(tagId)
        deleteTagById(tagId)
    }
}
