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
 * News Widget Receiver - entry point for the widget
 */
class NewsWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = NewsWidget()
}

/**
 * News Widget - displays latest security news on home screen
 */
class NewsWidget : GlanceAppWidget() {
    
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // Get latest articles from database
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
        
        provideContent {
            GlanceTheme {
                NewsWidgetContent(
                    articles = articles.map { 
                        WidgetArticle(
                            title = it.title,
                            source = it.sourceId,
                            category = it.category
                        )
                    }
                )
            }
        }
    }
}

data class WidgetArticle(
    val title: String,
    val source: String,
    val category: String
)

@Composable
private fun NewsWidgetContent(articles: List<WidgetArticle>) {
    val backgroundColor = ColorProvider(Color(0xFF1A1A2E))
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
                    style = TextStyle(fontSize = 18.sp)
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
                Text(
                    text = "${articles.size} new",
                    style = TextStyle(
                        color = subtleColor,
                        fontSize = 12.sp
                    )
                )
            }
            
            Spacer(modifier = GlanceModifier.height(8.dp))
            
            // Articles list
            if (articles.isEmpty()) {
                Box(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .defaultWeight(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No articles yet.\nTap to sync.",
                        style = TextStyle(
                            color = subtleColor,
                            fontSize = 14.sp
                        )
                    )
                }
            } else {
                Column(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .defaultWeight()
                ) {
                    articles.forEachIndexed { index, article ->
                        ArticleRow(
                            article = article,
                            primaryColor = primaryColor,
                            textColor = textColor,
                            subtleColor = subtleColor
                        )
                        if (index < articles.lastIndex) {
                            Spacer(modifier = GlanceModifier.height(6.dp))
                        }
                    }
                }
            }
            
            Spacer(modifier = GlanceModifier.height(4.dp))
            
            // Footer
            Text(
                text = "Tap to open app",
                style = TextStyle(
                    color = subtleColor,
                    fontSize = 11.sp
                )
            )
        }
    }
}

@Composable
private fun ArticleRow(
    article: WidgetArticle,
    primaryColor: ColorProvider,
    textColor: ColorProvider,
    subtleColor: ColorProvider
) {
    val categoryColor = when (article.category) {
        "CVE" -> ColorProvider(Color(0xFFFF6B6B))
        "MALWARE" -> ColorProvider(Color(0xFFFF8E53))
        "RESEARCH" -> ColorProvider(Color(0xFF4ECDC4))
        else -> primaryColor
    }
    
    Row(
        modifier = GlanceModifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Category indicator
        Box(
            modifier = GlanceModifier
                .size(4.dp, 32.dp)
                .background(categoryColor)
                .cornerRadius(2.dp)
        ) {
            // Empty - just a colored indicator
        }
        
        Spacer(modifier = GlanceModifier.width(8.dp))
        
        Column(modifier = GlanceModifier.defaultWeight()) {
            Text(
                text = article.title,
                style = TextStyle(
                    color = textColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                ),
                maxLines = 2
            )
        }
    }
}
