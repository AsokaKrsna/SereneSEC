package com.serenesec.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.serenesec.data.local.dao.ArticleDao
import com.serenesec.data.local.dao.BookmarkDao
import com.serenesec.data.local.dao.CollectionDao
import com.serenesec.data.local.dao.SourceDao
import com.serenesec.data.local.dao.TagDao
import com.serenesec.data.local.entity.ArticleCollectionCrossRef
import com.serenesec.data.local.entity.ArticleEntity
import com.serenesec.data.local.entity.ArticleTagCrossRef
import com.serenesec.data.local.entity.BookmarkEntity
import com.serenesec.data.local.entity.CollectionEntity
import com.serenesec.data.local.entity.SourceEntity
import com.serenesec.data.local.entity.TagEntity

@Database(
    entities = [
        SourceEntity::class,
        ArticleEntity::class,
        BookmarkEntity::class,
        TagEntity::class,
        ArticleTagCrossRef::class,
        CollectionEntity::class,
        ArticleCollectionCrossRef::class
    ],
    version = 4,
    exportSchema = true
)
abstract class SereneSecDatabase : RoomDatabase() {
    abstract fun sourceDao(): SourceDao
    abstract fun articleDao(): ArticleDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun tagDao(): TagDao
    abstract fun collectionDao(): CollectionDao
    
    companion object {
        const val DATABASE_NAME = "serenesec_database"
    }
}
