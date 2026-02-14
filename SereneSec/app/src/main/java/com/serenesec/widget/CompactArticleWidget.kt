package com.serenesec.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
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
import com.serenesec.data.local.SereneSecDatabase
import kotlinx.coroutines.flow.first

/**
 * Compact widget showing top 3 articles
 */
class CompactArticleWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = CompactArticleWidget()
}

class CompactArticleWidget : GlanceAppWidget() {
    
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val database = androidx.room.Room.databaseBuilder(
            context,
            SereneSecDatabase::class.java,
            SereneSecDatabase.DATABASE_NAME
        ).fallbackToDestructiveMigration().build()
        
        val articles = try {
            database.articleDao().getInboxArticles().first().take(3)
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
                CompactArticleContent(
                    articles = articles.map { 
                        CompactArticle(
                            title = it.title,
                            category = it.category,
                            isUnread = it.state == "UNREAD"
                        )
                    },
                    unreadCount = unreadCount
                )
            }
        }
    }
}

data class CompactArticle(
    val title: String,
    val category: String,
    val isUnread: Boolean
)

@Composable
private fun CompactArticleContent(articles: List<CompactArticle>, unreadCount: Int) {
    val backgroundColor = ColorProvider(Color(0xFF1A1A2E))
    val surfaceColor = ColorProvider(Color(0xFF1F2940))
    val primaryColor = ColorProvider(Color(0xFF00D9FF))
    val textColor = ColorProvider(Color.White)
    val subtleColor = ColorProvider(Color(0x88FFFFFF))
    
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(backgroundColor)
            .cornerRadius(16.dp)
            .clickable(actionStartActivity<MainActivity>())
            .padding(12.dp)
    ) {
        Column(
            modifier = GlanceModifier.fillMaxSize()
        ) {
            // Header
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🛡️",
                    style = TextStyle(fontSize = 16.sp)
                )
                Spacer(modifier = GlanceModifier.width(6.dp))
                Text(
                    text = "SereneSec",
                    style = TextStyle(
                        color = primaryColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = GlanceModifier.defaultWeight())
                
                // Unread badge
                if (unreadCount > 0) {
                    Box(
                        modifier = GlanceModifier
                            .background(primaryColor)
                            .cornerRadius(8.dp)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = unreadCount.toString(),
                            style = TextStyle(
                                color = ColorProvider(Color(0xFF1A1A2E)),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
            
            Spacer(modifier = GlanceModifier.height(10.dp))
            
            // Articles list
            if (articles.isEmpty()) {
                Box(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .defaultWeight(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "✓",
                            style = TextStyle(
                                color = primaryColor,
                                fontSize = 24.sp
                            )
                        )
                        Spacer(modifier = GlanceModifier.height(4.dp))
                        Text(
                            text = "All caught up",
                            style = TextStyle(
                                color = subtleColor,
                                fontSize = 12.sp
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
                        CompactArticleRow(
                            article = article,
                            surfaceColor = surfaceColor,
                            primaryColor = primaryColor,
                            textColor = textColor
                        )
                        if (index < articles.lastIndex) {
                            Spacer(modifier = GlanceModifier.height(6.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CompactArticleRow(
    article: CompactArticle,
    surfaceColor: ColorProvider,
    primaryColor: ColorProvider,
    textColor: ColorProvider
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
    
    Row(
        modifier = GlanceModifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Category indicator
        Box(
            modifier = GlanceModifier
                .size(3.dp, 28.dp)
                .background(categoryColor)
                .cornerRadius(2.dp),
            content = {}
        )
        
        Spacer(modifier = GlanceModifier.width(8.dp))
        
        // Unread dot
        if (article.isUnread) {
            Box(
                modifier = GlanceModifier
                    .size(6.dp)
                    .background(primaryColor)
                    .cornerRadius(3.dp),
                content = {}
            )
            Spacer(modifier = GlanceModifier.width(6.dp))
        }
        
        Column(modifier = GlanceModifier.defaultWeight()) {
            Text(
                text = article.title,
                style = TextStyle(
                    color = textColor,
                    fontSize = 12.sp,
                    fontWeight = if (article.isUnread) FontWeight.Medium else FontWeight.Normal
                ),
                maxLines = 2
            )
        }
    }
}
