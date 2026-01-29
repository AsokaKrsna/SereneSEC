package com.serenesec.domain.model

/**
 * Domain model representing a content source
 */
data class Source(
    val id: String,
    val name: String,
    val url: String,
    val type: SourceType,
    val category: Category,
    val isEnabled: Boolean = true,
    val isBuiltIn: Boolean = false,
    val lastFetchTime: Long? = null,
    val lastSuccessTime: Long? = null,
    val lastErrorTime: Long? = null,
    val lastErrorMessage: String? = null,
    val errorCount: Int = 0,
    val successCount: Int = 0,
    val totalArticlesFetched: Int = 0,
    val description: String? = null,
    val iconUrl: String? = null
) {
    /**
     * Returns the actual feed URL, handling GitHub repo to releases.atom conversion
     */
    val feedUrl: String
        get() = when (type) {
            SourceType.GITHUB -> {
                // Convert github.com/user/repo to github.com/user/repo/releases.atom
                val normalized = url.trimEnd('/')
                if (normalized.endsWith("/releases.atom")) {
                    normalized
                } else {
                    "$normalized/releases.atom"
                }
            }
            else -> url
        }
    
    /**
     * Health status based on recent fetch results
     */
    val healthStatus: SourceHealth
        get() {
            if (successCount == 0 && errorCount == 0) return SourceHealth.UNKNOWN
            val totalAttempts = successCount + errorCount
            if (totalAttempts == 0) return SourceHealth.UNKNOWN
            
            val successRate = successCount.toDouble() / totalAttempts
            return when {
                successRate >= 0.9 -> SourceHealth.HEALTHY
                successRate >= 0.7 -> SourceHealth.DEGRADED
                successRate >= 0.3 -> SourceHealth.UNSTABLE
                else -> SourceHealth.FAILING
            }
        }
    
    /**
     * Success rate as percentage
     */
    val successRate: Int
        get() {
            val total = successCount + errorCount
            return if (total == 0) 100 else ((successCount.toDouble() / total) * 100).toInt()
        }
}

enum class SourceHealth(val displayName: String, val emoji: String) {
    HEALTHY("Healthy", "✅"),
    DEGRADED("Degraded", "⚠️"),
    UNSTABLE("Unstable", "🔶"),
    FAILING("Failing", "❌"),
    UNKNOWN("Unknown", "❓")
}
