package com.serenesec.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.serenesec.data.local.entity.ArticleCollectionCrossRef
import com.serenesec.data.local.entity.ArticleEntity
import com.serenesec.data.local.entity.CollectionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CollectionDao {
    
    // Collection CRUD operations
    @Query("SELECT * FROM collections ORDER BY updatedAt DESC")
    fun getAllCollections(): Flow<List<CollectionEntity>>
    
    @Query("SELECT * FROM collections WHERE id = :id")
    suspend fun getCollectionById(id: String): CollectionEntity?
    
    @Query("SELECT * FROM collections WHERE name = :name")
    suspend fun getCollectionByName(name: String): CollectionEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCollection(collection: CollectionEntity)
    
    @Update
    suspend fun updateCollection(collection: CollectionEntity)
    
    @Delete
    suspend fun deleteCollection(collection: CollectionEntity)
    
    @Query("DELETE FROM collections WHERE id = :id")
    suspend fun deleteCollectionById(id: String)
    
    @Query("SELECT COUNT(*) FROM collections")
    fun getCollectionCount(): Flow<Int>
    
    // Article-Collection relationship operations
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addArticleToCollection(crossRef: ArticleCollectionCrossRef)
    
    @Delete
    suspend fun removeArticleFromCollection(crossRef: ArticleCollectionCrossRef)
    
    @Query("DELETE FROM article_collections WHERE articleId = :articleId AND collectionId = :collectionId")
    suspend fun removeArticleFromCollectionById(articleId: String, collectionId: String)
    
    @Query("DELETE FROM article_collections WHERE collectionId = :collectionId")
    suspend fun removeAllArticlesFromCollection(collectionId: String)
    
    @Query("DELETE FROM article_collections WHERE articleId = :articleId")
    suspend fun removeArticleFromAllCollections(articleId: String)
    
    // Get collections for an article
    @Query("""
        SELECT c.* FROM collections c
        INNER JOIN article_collections ac ON c.id = ac.collectionId
        WHERE ac.articleId = :articleId
        ORDER BY c.name ASC
    """)
    fun getCollectionsForArticle(articleId: String): Flow<List<CollectionEntity>>
    
    @Query("""
        SELECT c.* FROM collections c
        INNER JOIN article_collections ac ON c.id = ac.collectionId
        WHERE ac.articleId = :articleId
        ORDER BY c.name ASC
    """)
    suspend fun getCollectionsForArticleList(articleId: String): List<CollectionEntity>
    
    // Get articles in a collection
    @Query("""
        SELECT a.* FROM articles a
        INNER JOIN article_collections ac ON a.id = ac.articleId
        WHERE ac.collectionId = :collectionId
        ORDER BY ac.addedAt DESC
    """)
    fun getArticlesInCollection(collectionId: String): Flow<List<ArticleEntity>>
    
    // Get article count for a collection
    @Query("SELECT COUNT(*) FROM article_collections WHERE collectionId = :collectionId")
    fun getArticleCountForCollection(collectionId: String): Flow<Int>
    
    @Query("SELECT COUNT(*) FROM article_collections WHERE collectionId = :collectionId")
    suspend fun getArticleCountForCollectionSync(collectionId: String): Int
    
    // Check if article is in a specific collection
    @Query("SELECT EXISTS(SELECT 1 FROM article_collections WHERE articleId = :articleId AND collectionId = :collectionId)")
    suspend fun isArticleInCollection(articleId: String, collectionId: String): Boolean
    
    // Transaction to delete collection and all its associations
    @Transaction
    suspend fun deleteCollectionWithArticles(collectionId: String) {
        removeAllArticlesFromCollection(collectionId)
        deleteCollectionById(collectionId)
    }
    
    // Update collection's updatedAt timestamp
    @Query("UPDATE collections SET updatedAt = :timestamp WHERE id = :id")
    suspend fun touchCollection(id: String, timestamp: Long = System.currentTimeMillis())
}
