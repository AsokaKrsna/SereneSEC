package com.serenesec.data.remote

import com.serenesec.domain.model.SourceType
import java.net.URI
import java.util.regex.Pattern
import javax.inject.Inject
import javax.inject.Singleton

data class DiscoveredFeed(
    val feedUrl: String,
    val title: String?,
    val type: SourceType
)

@Singleton
class FeedDiscovery @Inject constructor(
    private val feedFetcher: FeedFetcher
) {
    private val linkPattern = Pattern.compile(
        """<link[^>]+rel=["']alternate["'][^>]+>|<link[^>]+type=["']application/(rss|atom)\+xml["'][^>]+>""",
        Pattern.CASE_INSENSITIVE
    )
    private val hrefPattern = Pattern.compile("""href=["']([^"']+)["']""", Pattern.CASE_INSENSITIVE)
    private val typePattern = Pattern.compile("""type=["']([^"']+)["']""", Pattern.CASE_INSENSITIVE)
    private val titlePattern = Pattern.compile("""title=["']([^"']+)["']""", Pattern.CASE_INSENSITIVE)
    private val htmlTitlePattern = Pattern.compile("""<title[^>]*>(.*?)</title>""", Pattern.CASE_INSENSITIVE or Pattern.DOTALL)

    /**
     * Resolves a given URL to its actual RSS/Atom feed URL.
     * If the URL is already a feed or auto-discovery finds a feed link in HTML, returns DiscoveredFeed.
     */
    suspend fun discoverFeed(inputUrl: String): DiscoveredFeed {
        val trimmed = inputUrl.trim()
        val normalizedUrl = if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://")) {
            "https://$trimmed"
        } else {
            trimmed
        }

        // Direct feed checks
        val lower = normalizedUrl.lowercase()
        if (lower.endsWith(".rss") || lower.endsWith(".xml") || lower.endsWith("/feed/") || lower.endsWith("/feed")) {
            return DiscoveredFeed(
                feedUrl = normalizedUrl,
                title = null,
                type = if (lower.contains("atom")) SourceType.ATOM else SourceType.RSS
            )
        }

        // Fetch HTML to parse <link rel="alternate">
        val htmlResult = feedFetcher.fetchHtml(normalizedUrl)
        val html = htmlResult.getOrNull() ?: return DiscoveredFeed(
            feedUrl = normalizedUrl,
            title = null,
            type = SourceType.RSS
        )

        // Try extracting site title
        val pageTitle = htmlTitlePattern.matcher(html).let { matcher ->
            if (matcher.find()) matcher.group(1)?.trim() else null
        }

        val matcher = linkPattern.matcher(html)
        while (matcher.find()) {
            val linkTag = matcher.group()
            val hrefMatcher = hrefPattern.matcher(linkTag)
            if (hrefMatcher.find()) {
                val rawHref = hrefMatcher.group(1) ?: continue
                val resolvedFeedUrl = resolveUrl(normalizedUrl, rawHref)

                val tagType = typePattern.matcher(linkTag).let { m ->
                    if (m.find()) m.group(1)?.lowercase() else ""
                } ?: ""

                val tagTitle = titlePattern.matcher(linkTag).let { m ->
                    if (m.find()) m.group(1)?.trim() else pageTitle
                }

                val sourceType = if (tagType.contains("atom") || resolvedFeedUrl.contains("atom")) {
                    SourceType.ATOM
                } else {
                    SourceType.RSS
                }

                return DiscoveredFeed(
                    feedUrl = resolvedFeedUrl,
                    title = tagTitle ?: pageTitle,
                    type = sourceType
                )
            }
        }

        // If no link tag found, fallback to trying /feed or returning original URL
        return DiscoveredFeed(
            feedUrl = normalizedUrl,
            title = pageTitle,
            type = SourceType.RSS
        )
    }

    private fun resolveUrl(baseUrl: String, relativeOrAbsolute: String): String {
        return try {
            val baseUri = URI(baseUrl)
            baseUri.resolve(relativeOrAbsolute).toString()
        } catch (e: Exception) {
            if (relativeOrAbsolute.startsWith("http://") || relativeOrAbsolute.startsWith("https://")) {
                relativeOrAbsolute
            } else {
                "${baseUrl.trimEnd('/')}/${relativeOrAbsolute.trimStart('/')}"
            }
        }
    }
}
