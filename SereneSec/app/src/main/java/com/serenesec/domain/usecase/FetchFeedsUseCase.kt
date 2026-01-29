package com.serenesec.domain.usecase

import android.util.Log
import com.serenesec.data.remote.FeedFetcher
import com.serenesec.data.remote.FeedParser
import com.serenesec.domain.model.Article
import com.serenesec.domain.model.Source
import com.serenesec.domain.repository.ArticleRepository
import com.serenesec.domain.repository.SourceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "FetchFeedsUseCase"

/**
 * Use case for fetching and parsing feeds from all enabled sources
 */
@Singleton
class FetchFeedsUseCase @Inject constructor(
    private val sourceRepository: SourceRepository,
    private val articleRepository: ArticleRepository,
    private val feedFetcher: FeedFetcher,
    private val feedParser: FeedParser,
    private val autoTagUseCase: AutoTagUseCase
) {
    
    /**
     * Fetch feeds from all enabled sources
     * @return Pair of (total new articles, total errors)
     */
    suspend operator fun invoke(): Result<FetchResult> = withContext(Dispatchers.IO) {
        try {
            val sources = sourceRepository.getEnabledSourcesList()
            Log.d(TAG, "Starting fetch for ${sources.size} enabled sources")
            
            if (sources.isEmpty()) {
                Log.w(TAG, "No enabled sources found")
                return@withContext Result.success(FetchResult(0, 0, 0, emptyList()))
            }
            
            var totalNew = 0
            var totalErrors = 0
            val sourceResults = mutableListOf<SourceFetchResult>()
            
            // Fetch feeds concurrently with supervision (one failure doesn't cancel others)
            supervisorScope {
                val results = sources.map { source ->
                    async {
                        fetchSingleSource(source)
                    }
                }.awaitAll()
                
                sources.forEachIndexed { index, source ->
                    val result = results[index]
                    when {
                        result.isSuccess -> {
                            val newCount = result.getOrDefault(0)
                            totalNew += newCount
                            Log.d(TAG, "✓ ${source.name}: $newCount articles")
                            sourceResults.add(SourceFetchResult(source.name, newCount, null))
                        }
                        result.isFailure -> {
                            totalErrors++
                            val error = result.exceptionOrNull()?.message ?: "Unknown error"
                            Log.e(TAG, "✗ ${source.name}: $error")
                            sourceResults.add(SourceFetchResult(source.name, 0, error))
                        }
                    }
                }
            }
            
            // Cleanup old archived articles
            articleRepository.deleteOldArchivedArticles()
            
            Log.d(TAG, "Fetch complete: $totalNew new articles from ${sources.size} sources ($totalErrors errors)")
            
            Result.success(FetchResult(
                sourcesCount = sources.size,
                newArticles = totalNew,
                errors = totalErrors,
                sourceResults = sourceResults
            ))
        } catch (e: Exception) {
            Log.e(TAG, "Fetch failed", e)
            Result.failure(e)
        }
    }
    
    private suspend fun fetchSingleSource(source: Source): Result<Int> {
        return try {
            Log.v(TAG, "Fetching: ${source.name} from ${source.feedUrl}")
            val feedResult = feedFetcher.fetchFeed(source.feedUrl)
            
            if (feedResult.isFailure) {
                val error = feedResult.exceptionOrNull() ?: Exception("Unknown error")
                sourceRepository.recordFailedFetch(source.id, error.message)
                Log.w(TAG, "Fetch failed for ${source.name}: ${error.message}")
                return Result.failure(error)
            }
            
            val inputStream = feedResult.getOrThrow()
            val articles = feedParser.parseFeed(inputStream, source)
            inputStream.close()
            
            Log.v(TAG, "Parsed ${articles.size} articles from ${source.name}")
            
            if (articles.isEmpty()) {
                sourceRepository.recordSuccessfulFetch(source.id, 0)
                return Result.success(0)
            }
            
            val insertedCount = articleRepository.insertArticles(articles)
            
            // Auto-tag newly inserted articles
            articles.forEach { article ->
                try {
                    autoTagUseCase.autoTagArticle(article.id, article.title)
                } catch (e: Exception) {
                    Log.w(TAG, "Auto-tag failed for ${article.id}: ${e.message}")
                }
            }
            
            sourceRepository.recordSuccessfulFetch(source.id, insertedCount)
            
            Log.v(TAG, "Inserted $insertedCount new articles from ${source.name}")
            Result.success(insertedCount)
        } catch (e: Exception) {
            sourceRepository.recordFailedFetch(source.id, e.message)
            Log.e(TAG, "Exception fetching ${source.name}", e)
            Result.failure(e)
        }
    }
    
    data class FetchResult(
        val sourcesCount: Int,
        val newArticles: Int,
        val errors: Int,
        val sourceResults: List<SourceFetchResult>
    )
    
    data class SourceFetchResult(
        val sourceName: String,
        val articleCount: Int,
        val error: String?
    )
}
