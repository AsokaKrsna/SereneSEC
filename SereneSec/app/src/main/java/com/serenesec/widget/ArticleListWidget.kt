package com.serenesec.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.serenesec.MainActivity
import com.serenesec.R
import com.serenesec.data.local.SereneSecDatabase
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Large widget showing detailed article list
 */
class ArticleListWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = ArticleListWidget()
}

class ArticleListWidget : GlanceAppWidget() {
    
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val database = androidx.room.Room.databaseBuilder(
            context,
            SereneSecDatabase::class.java,
            SereneSecDatabase.DATABASE_NAME
        ).fallbackToDestructiveMigration().build()
        
        val articles = try {
            database.articleDao().getInboxArticles().first().take(5)
        } catch (e: Exception) {
            emptyList()
        }
        
        val unreadCount = try {
            database.articleDao().getUnreadCount().first()
        } catch (e: Exception) {
            0
        }
        
        provideContent {
            GlanceTheme {
                ArticleListContent(
                    articles = articles.map { 
                        DetailedArticle(
                            title = it.title,
                            source = it.sourceId,
                            category = it.category,
                            publishedDate = it.publishedDate,
                            isUnread = it.state == "UNREAD"
                        )
                    },
                    unreadCount = unreadCount
                )
            }
        }
    }
}

data class DetailedArticle(
    val title: String,
    val source: String,
    val category: String,
    val publishedDate: Long,
    val isUnread: Boolean
)

@Composable
private fun ArticleListContent(articles: List<DetailedArticle>, unreadCount: Int) {
    val backgroundColor = ColorProvider(Color(0xFF1A1A2E))
    val surfaceColor = ColorProvider(Color(0xFF1F2940))
    val primaryColor = ColorProvider(Color(0xFF00D9FF))
    val textColor = ColorProvider(Color.White)
    val subtleColor = ColorProvider(Color(0x88FFFFFF))
    
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(backgroundColor)
            .cornerRadius(20.dp)
            .padding(16.dp)
    ) {
        Column(
            modifier = GlanceModifier.fillMaxSize()
        ) {
            // Header with refresh button
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = GlanceModifier.defaultWeight()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "🛡️",
                            style = TextStyle(fontSize = 20.sp)
                        )
                        Spacer(modifier = GlanceModifier.width(8.dp))
                        Text(
                            text = "SereneSec",
                            style = TextStyle(
                                color = primaryColor,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    Spacer(modifier = GlanceModifier.height(2.dp))
                    Text(
                        text = "$unreadCount unread articles",
                        style = TextStyle(
                            color = subtleColor,
                            fontSize = 12.sp
                        )
                    )
                }
                
                Box(
                    modifier = GlanceModifier
                        .size(40.dp)
                        .background(surfaceColor)
                        .cornerRadius(12.dp)
                        .clickable(actionStartActivity<MainActivity>()),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        provider = ImageProvider(R.drawable.ic_launcher_foreground),
                        contentDescription = "Open app",
                        modifier = GlanceModifier.size(24.dp)
                    )
                }
            }
            
            Spacer(modifier = GlanceModifier.height(12.dp))
            
            // Articles list
            if (articles.isEmpty()) {
                Box(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .defaultWeight()
                        .background(surfaceColor)
                        .cornerRadius(12.dp)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "📭",
                            style = TextStyle(fontSize = 32.sp)
                        )
                        Spacer(modifier = GlanceModifier.height(8.dp))
                        Text(
                            text = "All caught up!",
                            style = TextStyle(
                                color = textColor,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )
                        Spacer(modifier = GlanceModifier.height(4.dp))
                        Text(
                            text = "No new articles",
                            style = TextStyle(
                                color = subtleColor,
                                fontSize = 13.sp
                            )
                        )
                    }
                }
            } else {
                Column(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .defaultWeight()
                ) {
                    articles.forEachIndexed { index, article ->
                        DetailedArticleCard(
                            article = article,
                            surfaceColor = surfaceColor,
                            primaryColor = primaryColor,
                            textColor = textColor,
                            subtleColor = subtleColor
                        )
                        if (index < articles.lastIndex) {
                            Spacer(modifier = GlanceModifier.height(8.dp))
                        }
                    }
                }
            }
            
            Spacer(modifier = GlanceModifier.height(8.dp))
            
            // Footer
            Text(
                text = "Last updated: ${getCurrentTime()}",
                style = TextStyle(
                    color = subtleColor,
                    fontSize = 10.sp
                )
            )
        }
    }
}

@Composable
private fun DetailedArticleCard(
    article: DetailedArticle,
    surfaceColor: ColorProvider,
    primaryColor: ColorProvider,
    textColor: ColorProvider,
    subtleColor: ColorProvider
) {
    val categoryColor = when (article.category) {
        "CVE" -> ColorProvider(Color(0xFFF85149))
        "MALWARE" -> ColorProvider(Color(0xFFDB6D28))
        "RESEARCH" -> ColorProvider(Color(0xFF3FB950))
        "THREAT_INTEL" -> ColorProvider(Color(0xFFFF7B72))
        "NEWS" -> ColorProvider(Color(0xFF58A6FF))
        "TOOLS" -> ColorProvider(Color(0xFFA371F7))
        else -> primaryColor
    }
    
    Box(
        modifier = GlanceModifier
            .fillMaxWidth()
            .background(surfaceColor)
            .cornerRadius(12.dp)
            .clickable(actionStartActivity<MainActivity>())
            .padding(12.dp)
    ) {
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            // Unread indicator
            if (article.isUnread) {
                Box(
                    modifier = GlanceModifier
                        .size(8.dp)
                        .background(primaryColor)
                        .cornerRadius(4.dp),
                    content = {}
                )
                Spacer(modifier = GlanceModifier.width(8.dp))
            }
            
            Column(modifier = GlanceModifier.defaultWeight()) {
                // Category badge
                Box(
                    modifier = GlanceModifier
                        .background(categoryColor)
                        .cornerRadius(4.dp)
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = article.category,
                        style = TextStyle(
                            color = ColorProvider(Color.White),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
                
                Spacer(modifier = GlanceModifier.height(6.dp))
                
                // Title
                Text(
                    text = article.title,
                    style = TextStyle(
                        color = textColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    maxLines = 2
                )
                
                Spacer(modifier = GlanceModifier.height(4.dp))
                
                // Source and time
                Text(
                    text = "${formatSource(article.source)} • ${getRelativeTime(article.publishedDate)}",
                    style = TextStyle(
                        color = subtleColor,
                        fontSize = 11.sp
                    ),
                    maxLines = 1
                )
            }
        }
    }
}

private fun formatSource(sourceId: String): String {
    return sourceId.split("-")
        .joinToString(" ") { it.replaceFirstChar { char -> 
            if (char.isLowerCase()) char.titlecase(Locale.getDefault()) else char.toString() 
        } }
        .take(20)
}

private fun getRelativeTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 60_000 -> "Just now"
        diff < 3600_000 -> "${diff / 60_000}m ago"
        diff < 86400_000 -> "${diff / 3600_000}h ago"
        diff < 604800_000 -> "${diff / 86400_000}d ago"
        else -> SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(timestamp))
    }
}

private fun getCurrentTime(): String {
    return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
}
