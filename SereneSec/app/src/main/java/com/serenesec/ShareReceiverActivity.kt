package com.serenesec

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.serenesec.domain.model.Article
import com.serenesec.domain.model.ArticleState
import com.serenesec.domain.model.Category
import com.serenesec.domain.repository.ArticleRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.security.MessageDigest
import javax.inject.Inject

/**
 * Activity to handle shared URLs from other apps
 */
@AndroidEntryPoint
class ShareReceiverActivity : ComponentActivity() {
    
    @Inject
    lateinit var articleRepository: ArticleRepository
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        handleIntent(intent)
    }
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }
    
    private fun handleIntent(intent: Intent?) {
        when (intent?.action) {
            Intent.ACTION_SEND -> {
                if (intent.type == "text/plain") {
                    handleSharedText(intent)
                }
            }
            Intent.ACTION_VIEW -> {
                intent.data?.toString()?.let { url ->
                    addArticleFromUrl(url)
                }
            }
        }
    }
    
    private fun handleSharedText(intent: Intent) {
        val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT) ?: return finish()
        
        // Extract URL from shared text
        val url = extractUrl(sharedText)
        if (url != null) {
            addArticleFromUrl(url)
        } else {
            Toast.makeText(this, "Could not find a valid URL", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
    
    private fun extractUrl(text: String): String? {
        // Simple URL regex
        val urlPattern = Regex("https?://[\\w\\-._~:/?#\\[\\]@!$&'()*+,;=%]+")
        return urlPattern.find(text)?.value
    }
    
    private fun addArticleFromUrl(url: String) {
        lifecycleScope.launch {
            try {
                // Check if already exists
                if (articleRepository.articleExists(url)) {
                    Toast.makeText(
                        this@ShareReceiverActivity,
                        "Article already in inbox",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                    return@launch
                }
                
                // Create article from URL
                val article = Article(
                    id = generateArticleId(url),
                    sourceId = "shared",
                    title = extractTitleFromUrl(url),
                    summary = null,
                    contentUrl = url,
                    publishedDate = System.currentTimeMillis(),
                    state = ArticleState.UNREAD,
                    category = Category.NEWS  // Default category for shared articles
                )
                
                val success = articleRepository.insertArticle(article)
                
                if (success) {
                    Toast.makeText(
                        this@ShareReceiverActivity,
                        "Article added to inbox",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        this@ShareReceiverActivity,
                        "Could not add article",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@ShareReceiverActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
            finish()
        }
    }
    
    private fun generateArticleId(url: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(url.toByteArray())
        return bytes.take(16).joinToString("") { "%02x".format(it) }
    }
    
    private fun extractTitleFromUrl(url: String): String {
        // Extract a readable title from URL path
        return try {
            val path = android.net.Uri.parse(url).path ?: url
            path.split("/")
                .lastOrNull { it.isNotBlank() }
                ?.replace("-", " ")
                ?.replace("_", " ")
                ?.replaceFirstChar { it.uppercase() }
                ?: "Shared Article"
        } catch (e: Exception) {
            "Shared Article"
        }
    }
}
