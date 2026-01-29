package com.serenesec.data.remote

import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for fetching feed content from URLs
 */
@Singleton
class FeedFetcher @Inject constructor(
    private val okHttpClient: OkHttpClient
) {
    
    /**
     * Fetch feed content as an InputStream
     * Returns null if fetch fails
     */
    suspend fun fetchFeed(url: String): Result<InputStream> {
        return try {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", USER_AGENT)
                .header("Accept", ACCEPT_HEADER)
                .build()
            
            val response = okHttpClient.newCall(request).execute()
            
            if (response.isSuccessful) {
                response.body?.byteStream()?.let { 
                    Result.success(it) 
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                Result.failure(Exception("HTTP ${response.code}: ${response.message}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Fetch HTML content for article caching
     */
    suspend fun fetchHtml(url: String): Result<String> {
        return try {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", USER_AGENT)
                .header("Accept", "text/html,application/xhtml+xml")
                .build()
            
            val response = okHttpClient.newCall(request).execute()
            
            if (response.isSuccessful) {
                response.body?.string()?.let { 
                    Result.success(it) 
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                Result.failure(Exception("HTTP ${response.code}: ${response.message}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    companion object {
        private const val USER_AGENT = "SereneSec/1.0 (Cybersecurity Reader; https://github.com/serenesec)"
        private const val ACCEPT_HEADER = "application/rss+xml, application/atom+xml, application/xml, text/xml, */*"
    }
}
