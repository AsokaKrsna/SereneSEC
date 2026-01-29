package com.serenesec.data.sharing

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.net.Uri
import androidx.core.content.FileProvider
import com.serenesec.domain.model.Article
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Enhanced sharing manager with multiple sharing formats
 */
@Singleton
class ShareManager @Inject constructor(
    private val context: Context
) {
    
    enum class ShareFormat {
        PLAIN_TEXT,          // Title + URL
        MARKDOWN,            // Formatted markdown
        QUOTE_CARD,          // Image quote card
        TWITTER,             // Twitter-optimized
        LINKEDIN,            // LinkedIn format
        EMAIL                // Email-friendly format
    }
    
    /**
     * Share article with specified format
     */
    fun shareArticle(article: Article, format: ShareFormat): Intent {
        return when (format) {
            ShareFormat.PLAIN_TEXT -> createPlainTextShare(article)
            ShareFormat.MARKDOWN -> createMarkdownShare(article)
            ShareFormat.QUOTE_CARD -> createQuoteCardShare(article)
            ShareFormat.TWITTER -> createTwitterShare(article)
            ShareFormat.LINKEDIN -> createLinkedInShare(article)
            ShareFormat.EMAIL -> createEmailShare(article)
        }
    }
    
    /**
     * Copy article URL to clipboard
     */
    fun copyToClipboard(article: Article) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Article URL", article.contentUrl)
        clipboard.setPrimaryClip(clip)
    }
    
    /**
     * Copy formatted text to clipboard
     */
    fun copyFormattedToClipboard(article: Article) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val text = """
            |📰 ${article.title}
            |
            |${article.summary ?: ""}
            |
            |🔗 ${article.contentUrl}
            |
            |via SereneSec
        """.trimMargin()
        val clip = ClipData.newPlainText("Article", text)
        clipboard.setPrimaryClip(clip)
    }
    
    private fun createPlainTextShare(article: Article): Intent {
        val text = "${article.title}\n\n${article.contentUrl}"
        return Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
            putExtra(Intent.EXTRA_SUBJECT, article.title)
        }
    }
    
    private fun createMarkdownShare(article: Article): Intent {
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.US)
        val date = dateFormat.format(Date(article.publishedDate))
        
        val markdown = """
            |# ${article.title}
            |
            |**Category:** ${article.category.displayName}
            |**Published:** $date
            |
            |${article.summary ?: ""}
            |
            |[Read more](${article.contentUrl})
            |
            |---
            |*Shared via SereneSec*
        """.trimMargin()
        
        return Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, markdown)
            putExtra(Intent.EXTRA_SUBJECT, article.title)
        }
    }
    
    private fun createTwitterShare(article: Article): Intent {
        // Twitter has 280 char limit, optimize for that
        val categoryEmoji = getCategoryEmoji(article.category.name)
        val text = "$categoryEmoji ${article.title.take(200)}\n\n${article.contentUrl} #cybersecurity"
        
        return Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
    }
    
    private fun createLinkedInShare(article: Article): Intent {
        val text = """
            |🔐 ${article.title}
            |
            |${article.summary?.take(300) ?: ""}
            |
            |Read more: ${article.contentUrl}
            |
            |#cybersecurity #infosec #security #${article.category.name.lowercase()}
        """.trimMargin()
        
        return Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
    }
    
    private fun createEmailShare(article: Article): Intent {
        val dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.US)
        val date = dateFormat.format(Date(article.publishedDate))
        
        val body = """
            |Hi,
            |
            |I thought you might find this security article interesting:
            |
            |${article.title}
            |
            |${article.summary ?: ""}
            |
            |Read the full article here:
            |${article.contentUrl}
            |
            |Published: $date
            |Category: ${article.category.displayName}
            |
            |---
            |Shared via SereneSec - Cybersecurity News Reader
        """.trimMargin()
        
        return Intent(Intent.ACTION_SEND).apply {
            type = "message/rfc822"
            putExtra(Intent.EXTRA_SUBJECT, "Security Article: ${article.title}")
            putExtra(Intent.EXTRA_TEXT, body)
        }
    }
    
    private fun createQuoteCardShare(article: Article): Intent {
        // Generate a quote card image
        val bitmap = generateQuoteCard(article)
        val uri = saveBitmapToCache(bitmap, "share_card_${article.id}.png")
        
        if (uri != null) {
            return Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_TEXT, article.contentUrl)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        }
        
        // Fallback to text if image generation fails
        return createPlainTextShare(article)
    }
    
    private fun generateQuoteCard(article: Article): Bitmap {
        val width = 1080
        val height = 1080
        
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        // Background gradient (dark cyber theme)
        val backgroundPaint = Paint().apply {
            color = Color.parseColor("#0D0D1A")
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), backgroundPaint)
        
        // Gradient accent
        val accentPaint = Paint().apply {
            color = Color.parseColor("#00D9FF")
            alpha = 50
        }
        canvas.drawRoundRect(
            RectF(0f, 0f, width.toFloat(), 200f),
            0f, 0f, accentPaint
        )
        
        // Category tag
        val categoryPaint = Paint().apply {
            color = getCategoryColor(article.category.name)
            textSize = 48f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }
        canvas.drawText(
            article.category.displayName.uppercase(),
            80f, 120f, categoryPaint
        )
        
        // Title
        val titlePaint = Paint().apply {
            color = Color.WHITE
            textSize = 64f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            isAntiAlias = true
        }
        
        // Word wrap title
        val titleLines = wrapText(article.title, titlePaint, width - 160f).take(4)
        var y = 280f
        titleLines.forEach { line ->
            canvas.drawText(line, 80f, y, titlePaint)
            y += 80f
        }
        
        // Summary (if fits)
        if (article.summary != null && y < 700f) {
            val summaryPaint = Paint().apply {
                color = Color.parseColor("#88FFFFFF")
                textSize = 40f
                isAntiAlias = true
            }
            val summaryLines = wrapText(article.summary, summaryPaint, width - 160f).take(3)
            y += 40f
            summaryLines.forEach { line ->
                canvas.drawText(line, 80f, y, summaryPaint)
                y += 50f
            }
        }
        
        // Footer / branding
        val footerPaint = Paint().apply {
            color = Color.parseColor("#00D9FF")
            textSize = 36f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("🛡️ SereneSec", 80f, height - 80f, footerPaint)
        
        // URL hint
        val urlPaint = Paint().apply {
            color = Color.parseColor("#66FFFFFF")
            textSize = 28f
            isAntiAlias = true
        }
        canvas.drawText(
            article.contentUrl.take(50) + if (article.contentUrl.length > 50) "..." else "",
            80f, height - 140f, urlPaint
        )
        
        return bitmap
    }
    
    private fun wrapText(text: String, paint: Paint, maxWidth: Float): List<String> {
        val lines = mutableListOf<String>()
        val words = text.split(" ")
        var currentLine = ""
        
        for (word in words) {
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            val bounds = Rect()
            paint.getTextBounds(testLine, 0, testLine.length, bounds)
            
            if (bounds.width() > maxWidth && currentLine.isNotEmpty()) {
                lines.add(currentLine)
                currentLine = word
            } else {
                currentLine = testLine
            }
        }
        
        if (currentLine.isNotEmpty()) {
            lines.add(currentLine)
        }
        
        return lines
    }
    
    private fun saveBitmapToCache(bitmap: Bitmap, filename: String): Uri? {
        return try {
            val cacheDir = File(context.cacheDir, "share_images")
            cacheDir.mkdirs()
            val file = File(cacheDir, filename)
            
            FileOutputStream(file).use { fos ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            }
            
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            null
        }
    }
    
    private fun getCategoryEmoji(category: String): String {
        return when (category) {
            "NEWS" -> "📰"
            "CVE" -> "🔴"
            "MALWARE" -> "🦠"
            "TOOLS" -> "🛠️"
            "RESEARCH" -> "🔬"
            "ADVISORY" -> "⚠️"
            "INCIDENT" -> "🚨"
            else -> "🔐"
        }
    }
    
    private fun getCategoryColor(category: String): Int {
        return when (category) {
            "NEWS" -> Color.parseColor("#00D9FF")
            "CVE" -> Color.parseColor("#FF6B6B")
            "MALWARE" -> Color.parseColor("#FF8E53")
            "TOOLS" -> Color.parseColor("#4ECDC4")
            "RESEARCH" -> Color.parseColor("#9B59B6")
            "ADVISORY" -> Color.parseColor("#F39C12")
            "INCIDENT" -> Color.parseColor("#E74C3C")
            else -> Color.parseColor("#00D9FF")
        }
    }
}
