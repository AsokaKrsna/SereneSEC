package com.serenesec.domain.model

/**
 * Represents the type of content source
 */
enum class SourceType {
    RSS,      // Standard RSS/Atom feed
    GITHUB,   // GitHub releases (auto-converted to releases.atom)
    API       // Future: structured API sources
}

/**
 * Content categories for filtering
 */
enum class Category(val displayName: String) {
    NEWS("News"),
    TOOLS("Tools"),
    CVE("CVE"),
    RESEARCH("Research")
}

/**
 * Article lifecycle states
 */
enum class ArticleState {
    UNREAD,    // Fetched but not opened
    READ,      // Opened at least once
    ARCHIVED   // User-dismissed or completed
}
