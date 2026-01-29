package com.serenesec.data.remote

import android.util.Log
import android.util.Xml
import com.serenesec.domain.model.Article
import com.serenesec.domain.model.ArticleState
import com.serenesec.domain.model.Source
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "FeedParser"

/**
 * Parser for RSS and Atom feeds using XmlPullParser for memory efficiency
 */
@Singleton
class FeedParser @Inject constructor() {
    
    private val dateFormats = listOf(
        // RFC 822 (RSS)
        SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.US),
        SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US),
        SimpleDateFormat("dd MMM yyyy HH:mm:ss Z", Locale.US),
        // ISO 8601 (Atom)
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply { 
            timeZone = TimeZone.getTimeZone("UTC") 
        },
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US),
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply { 
            timeZone = TimeZone.getTimeZone("UTC") 
        },
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US),
        SimpleDateFormat("yyyy-MM-dd", Locale.US)
    )
    
    /**
     * Parse a feed from an input stream
     */
    @Throws(XmlPullParserException::class, IOException::class)
    fun parseFeed(inputStream: InputStream, source: Source): List<Article> {
        return try {
            val parser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(inputStream, null)
            
            // Find the root element
            var eventType = parser.eventType
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    val result = when (parser.name.lowercase()) {
                        "rss" -> parseRssFeed(parser, source)
                        "feed" -> parseAtomFeed(parser, source)
                        "rdf:rdf", "rdf" -> parseRdfFeed(parser, source)
                        else -> {
                            eventType = parser.next()
                            continue
                        }
                    }
                    Log.d(TAG, "Parsed ${result.size} articles from ${source.name}")
                    return result
                }
                eventType = parser.next()
            }
            emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing feed for ${source.name}", e)
            emptyList()
        }
    }
    
    private fun parseRssFeed(parser: XmlPullParser, source: Source): List<Article> {
        val articles = mutableListOf<Article>()
        
        while (parser.next() != XmlPullParser.END_DOCUMENT) {
            if (parser.eventType == XmlPullParser.START_TAG) {
                if (parser.name.equals("item", ignoreCase = true)) {
                    parseRssItem(parser, source)?.let { articles.add(it) }
                }
            }
        }
        return articles
    }
    
    private fun parseRssItem(parser: XmlPullParser, source: Source): Article? {
        var title: String? = null
        var link: String? = null
        var description: String? = null
        var pubDate: String? = null
        var author: String? = null
        var imageUrl: String? = null
        var contentEncoded: String? = null
        
        while (parser.next() != XmlPullParser.END_DOCUMENT) {
            when (parser.eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name.lowercase()) {
                        "title" -> title = readTagText(parser, "title")
                        "link" -> link = readTagText(parser, "link")
                        "description" -> description = readTagText(parser, "description")
                        "content:encoded", "content" -> contentEncoded = readTagText(parser, parser.name)
                        "pubdate" -> pubDate = readTagText(parser, "pubdate")
                        "dc:date" -> pubDate = pubDate ?: readTagText(parser, "dc:date")
                        "author" -> author = readTagText(parser, "author")
                        "dc:creator" -> author = author ?: readTagText(parser, "dc:creator")
                        "enclosure" -> {
                            if (imageUrl == null) {
                                val url = parser.getAttributeValue(null, "url")
                                val type = parser.getAttributeValue(null, "type")
                                if (url != null && (type == null || type.startsWith("image/"))) {
                                    imageUrl = url
                                }
                            }
                            skipTag(parser)
                        }
                        "media:content", "media:thumbnail" -> {
                            if (imageUrl == null) {
                                imageUrl = parser.getAttributeValue(null, "url")
                            }
                            skipTag(parser)
                        }
                        else -> skipTag(parser)
                    }
                }
                XmlPullParser.END_TAG -> {
                    if (parser.name.equals("item", ignoreCase = true)) {
                        break
                    }
                }
            }
        }
        
        if (title.isNullOrBlank() || link.isNullOrBlank()) return null
        
        // Use content:encoded if available and longer than description
        val finalDescription = if (contentEncoded != null && (description == null || contentEncoded.length > description.length)) {
            contentEncoded
        } else {
            description
        }
        
        return Article(
            id = generateArticleId(link),
            sourceId = source.id,
            title = sanitizeText(title),
            summary = finalDescription?.let { sanitizeHtml(it) }?.take(500),
            contentUrl = link,
            publishedDate = parseDate(pubDate),
            state = ArticleState.UNREAD,
            category = source.category,
            author = author?.let { sanitizeText(it) },
            imageUrl = imageUrl
        )
    }
    
    private fun parseAtomFeed(parser: XmlPullParser, source: Source): List<Article> {
        val articles = mutableListOf<Article>()
        
        while (parser.next() != XmlPullParser.END_DOCUMENT) {
            if (parser.eventType == XmlPullParser.START_TAG) {
                if (parser.name.equals("entry", ignoreCase = true)) {
                    parseAtomEntry(parser, source)?.let { articles.add(it) }
                }
            }
        }
        return articles
    }
    
    private fun parseAtomEntry(parser: XmlPullParser, source: Source): Article? {
        var title: String? = null
        var link: String? = null
        var summary: String? = null
        var content: String? = null
        var updated: String? = null
        var published: String? = null
        var author: String? = null
        
        while (parser.next() != XmlPullParser.END_DOCUMENT) {
            when (parser.eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name.lowercase()) {
                        "title" -> title = readTagText(parser, "title")
                        "link" -> {
                            val rel = parser.getAttributeValue(null, "rel")
                            val href = parser.getAttributeValue(null, "href")
                            if (href != null && (link == null || rel == "alternate")) {
                                link = href
                            }
                            skipTag(parser)
                        }
                        "summary" -> summary = readTagText(parser, "summary")
                        "content" -> content = readTagText(parser, "content")
                        "updated" -> updated = readTagText(parser, "updated")
                        "published" -> published = readTagText(parser, "published")
                        "author" -> author = parseAuthorElement(parser)
                        else -> skipTag(parser)
                    }
                }
                XmlPullParser.END_TAG -> {
                    if (parser.name.equals("entry", ignoreCase = true)) {
                        break
                    }
                }
            }
        }
        
        if (title.isNullOrBlank() || link.isNullOrBlank()) return null
        
        val description = content ?: summary
        
        return Article(
            id = generateArticleId(link),
            sourceId = source.id,
            title = sanitizeText(title),
            summary = description?.let { sanitizeHtml(it) }?.take(500),
            contentUrl = link,
            publishedDate = parseDate(published ?: updated),
            state = ArticleState.UNREAD,
            category = source.category,
            author = author?.let { sanitizeText(it) }
        )
    }
    
    private fun parseAuthorElement(parser: XmlPullParser): String? {
        var name: String? = null
        
        while (parser.next() != XmlPullParser.END_DOCUMENT) {
            when (parser.eventType) {
                XmlPullParser.START_TAG -> {
                    if (parser.name.equals("name", ignoreCase = true)) {
                        name = readTagText(parser, "name")
                    } else {
                        skipTag(parser)
                    }
                }
                XmlPullParser.END_TAG -> {
                    if (parser.name.equals("author", ignoreCase = true)) {
                        break
                    }
                }
            }
        }
        return name
    }
    
    private fun parseRdfFeed(parser: XmlPullParser, source: Source): List<Article> {
        val articles = mutableListOf<Article>()
        
        while (parser.next() != XmlPullParser.END_DOCUMENT) {
            if (parser.eventType == XmlPullParser.START_TAG) {
                if (parser.name.equals("item", ignoreCase = true)) {
                    parseRssItem(parser, source)?.let { articles.add(it) }
                }
            }
        }
        return articles
    }
    
    /**
     * Read text content of a tag and ensure we end at the closing tag
     */
    private fun readTagText(parser: XmlPullParser, tagName: String): String {
        val result = StringBuilder()
        
        while (parser.next() != XmlPullParser.END_DOCUMENT) {
            when (parser.eventType) {
                XmlPullParser.TEXT, XmlPullParser.CDSECT -> {
                    result.append(parser.text)
                }
                XmlPullParser.END_TAG -> {
                    if (parser.name.equals(tagName, ignoreCase = true)) {
                        break
                    }
                }
                XmlPullParser.START_TAG -> {
                    // Nested tag - skip it but continue reading
                    skipTag(parser)
                }
            }
        }
        return result.toString().trim()
    }
    
    /**
     * Skip the current tag and all its children
     */
    private fun skipTag(parser: XmlPullParser) {
        if (parser.eventType != XmlPullParser.START_TAG) return
        
        var depth = 1
        while (depth > 0 && parser.next() != XmlPullParser.END_DOCUMENT) {
            when (parser.eventType) {
                XmlPullParser.START_TAG -> depth++
                XmlPullParser.END_TAG -> depth--
            }
        }
    }
    
    private fun parseDate(dateStr: String?): Long {
        if (dateStr.isNullOrBlank()) return System.currentTimeMillis()
        
        val cleaned = dateStr.trim()
        
        for (format in dateFormats) {
            try {
                return format.parse(cleaned)?.time ?: continue
            } catch (e: Exception) {
                continue
            }
        }
        
        // Try to extract a date with regex as fallback
        try {
            val isoMatch = Regex("(\\d{4}-\\d{2}-\\d{2})").find(cleaned)
            if (isoMatch != null) {
                return SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(isoMatch.value)?.time 
                    ?: System.currentTimeMillis()
            }
        } catch (e: Exception) {
            // Ignore
        }
        
        return System.currentTimeMillis()
    }
    
    private fun generateArticleId(url: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(url.toByteArray())
        return bytes.take(16).joinToString("") { "%02x".format(it) }
    }
    
    /**
     * Remove HTML tags and decode entities for plain text display
     */
    private fun sanitizeText(text: String): String {
        return text
            .replace(Regex("<[^>]+>"), "")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&apos;", "'")
            .replace("&#39;", "'")
            .replace("&#x27;", "'")
            .replace("&nbsp;", " ")
            .replace("&#160;", " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }
    
    /**
     * Sanitize HTML content for summary - strip scripts and dangerous elements
     */
    private fun sanitizeHtml(html: String): String {
        return html
            .replace(Regex("<script[^>]*>.*?</script>", RegexOption.DOT_MATCHES_ALL), "")
            .replace(Regex("<style[^>]*>.*?</style>", RegexOption.DOT_MATCHES_ALL), "")
            .replace(Regex("<iframe[^>]*>.*?</iframe>", RegexOption.DOT_MATCHES_ALL), "")
            .let { sanitizeText(it) }
    }
}
