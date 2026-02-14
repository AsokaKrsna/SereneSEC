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
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.serenesec.MainActivity
import com.serenesec.data.local.SereneSecDatabase
import kotlinx.coroutines.flow.first

/**
 * Small widget showing unread article count
 */
class UnreadCountWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = UnreadCountWidget()
}

class UnreadCountWidget : GlanceAppWidget() {
    
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val database = androidx.room.Room.databaseBuilder(
            context,
            SereneSecDatabase::class.java,
            SereneSecDatabase.DATABASE_NAME
        ).fallbackToDestructiveMigration().build()
        
        val unreadCount = try {
            database.articleDao().getUnreadCount().first()
        } catch (e: Exception) {
            0
        }
        
        provideContent {
            GlanceTheme {
                UnreadCountContent(unreadCount)
            }
        }
    }
}

@Composable
private fun UnreadCountContent(count: Int) {
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
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🛡️",
                style = TextStyle(fontSize = 32.sp)
            )
            Spacer(modifier = GlanceModifier.height(8.dp))
            Text(
                text = count.toString(),
                style = TextStyle(
                    color = primaryColor,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = GlanceModifier.height(4.dp))
            Text(
                text = if (count == 1) "unread" else "unread",
                style = TextStyle(
                    color = subtleColor,
                    fontSize = 12.sp
                )
            )
        }
    }
}
