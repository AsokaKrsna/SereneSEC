package com.serenesec.data.remote

import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for fetching feed content from URLs with realistic browser headers
 * to prevent 403/anti-bot blocks from Cloudflare, Akamai, and security portals.
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
                .header("Accept-Language", ACCEPT_LANGUAGE)
                .header("Sec-Fetch-Dest", "empty")
                .header("Sec-Fetch-Mode", "cors")
                .header("Sec-Fetch-Site", "cross-site")
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
     * Fetch HTML content for article caching or discovery
     */
    suspend fun fetchHtml(url: String): Result<String> {
        return try {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", USER_AGENT)
                .header("Accept", HTML_ACCEPT_HEADER)
                .header("Accept-Language", ACCEPT_LANGUAGE)
                .header("Sec-Fetch-Dest", "document")
                .header("Sec-Fetch-Mode", "navigate")
                .header("Sec-Fetch-Site", "none")
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
        // Modern Android Chrome User-Agent to avoid anti-bot / Cloudflare blocks
        private const val USER_AGENT = 
            "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Mobile Safari/537.36"
        private const val ACCEPT_HEADER = 
            "application/rss+xml, application/atom+xml, application/xml, text/xml, */*"
        private const val HTML_ACCEPT_HEADER = 
            "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8"
        private const val ACCEPT_LANGUAGE = "en-US,en;q=0.9"
    }
}
