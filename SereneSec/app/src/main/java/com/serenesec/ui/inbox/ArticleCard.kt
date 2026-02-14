package com.serenesec.ui.inbox

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.serenesec.data.local.entity.TagEntity
import com.serenesec.domain.model.Article
import com.serenesec.domain.model.Category
import com.serenesec.ui.theme.CategoryCVE
import com.serenesec.ui.theme.CategoryNews
import com.serenesec.ui.theme.CategoryResearch
import com.serenesec.ui.theme.CategoryTools
import com.serenesec.ui.theme.CategoryThreatIntel
import com.serenesec.ui.theme.CategoryGovAdvisory
import com.serenesec.ui.theme.CategoryMalware
import com.serenesec.ui.theme.CategoryExploits
import com.serenesec.ui.theme.CategoryPrograms
import com.serenesec.ui.theme.CategoryWriteups
import com.serenesec.ui.theme.CategoryPodcast
import com.serenesec.ui.theme.CategoryLearning
import com.serenesec.ui.theme.CategoryDefense
import com.serenesec.ui.theme.CategoryCloud
import com.serenesec.ui.theme.CategoryTradecraft
import com.serenesec.ui.theme.CategoryIncidents
import com.serenesec.ui.theme.CategoryConference
import com.serenesec.ui.theme.Unread
import com.serenesec.ui.tags.TagsViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ArticleCard(
    article: Article,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    onArchive: () -> Unit,
    onFavoriteToggle: (Boolean) -> Unit,
    onSaveForLaterToggle: (Boolean) -> Unit,
    isArchived: Boolean = false,
    modifier: Modifier = Modifier
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onArchive()
                true
            } else {
                false
            }
        }
    )
    
    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        MaterialTheme.colorScheme.error.copy(alpha = 0.2f),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = if (isArchived) Icons.Default.Unarchive else Icons.Default.Archive,
                    contentDescription = if (isArchived) "Restore" else "Archive",
                    tint = if (isArchived) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            }
        },
        content = {
            ArticleCardContent(
                article = article,
                onClick = onClick,
                onLongClick = onLongClick,
                onFavoriteToggle = onFavoriteToggle,
                onSaveForLaterToggle = onSaveForLaterToggle,
                modifier = modifier
            )
        },
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ArticleCardContent(
    article: Article,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onFavoriteToggle: (Boolean) -> Unit,
    onSaveForLaterToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    tagsViewModel: TagsViewModel = hiltViewModel()
) {
    val favoriteColor by animateColorAsState(
        targetValue = if (article.isFavorite) Color(0xFFFF6B6B) else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "favoriteColor"
    )
    val saveColor by animateColorAsState(
        targetValue = if (article.isSavedForLater) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "saveColor"
    )
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Unread indicator
                if (article.isUnread) {
                    Box(
                        modifier = Modifier
                            .padding(top = 6.dp)
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Unread)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                }
                
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    // Category chip
                    CategoryChip(category = article.category)
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Title
                    Text(
                        text = article.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (article.isUnread) FontWeight.SemiBold else FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    // Summary
                    if (!article.summary.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = article.summary,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    
                    // Tags
                    ArticleTagsRow(
                        articleId = article.id,
                        tagsViewModel = tagsViewModel
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Action bar row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Meta info
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    if (!article.author.isNullOrBlank()) {
                        Text(
                            text = article.author,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Text(
                            text = " • ",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = formatTimeAgo(article.publishedDate),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Action buttons
                Row {
                    // Favorite button
                    IconButton(
                        onClick = { onFavoriteToggle(!article.isFavorite) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (article.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = if (article.isFavorite) "Remove from favorites" else "Add to favorites",
                            tint = favoriteColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    
                    // Save for later button
                    IconButton(
                        onClick = { onSaveForLaterToggle(!article.isSavedForLater) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (article.isSavedForLater) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                            contentDescription = if (article.isSavedForLater) "Remove from saved" else "Save for later",
                            tint = saveColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryChip(
    category: Category,
    modifier: Modifier = Modifier
) {
    val (color, label) = when (category) {
        Category.NEWS -> CategoryNews to "News"
        Category.TOOLS -> CategoryTools to "Tools"
        Category.CVE -> CategoryCVE to "CVE"
        Category.RESEARCH -> CategoryResearch to "Research"
        Category.THREAT_INTEL -> CategoryThreatIntel to "Threat Intel"
        Category.GOV_ADVISORY -> CategoryGovAdvisory to "Gov Advisory"
        Category.MALWARE -> CategoryMalware to "Malware"
        Category.EXPLOITS -> CategoryExploits to "Exploits"
        Category.PROGRAMS -> CategoryPrograms to "Programs"
        Category.WRITEUPS -> CategoryWriteups to "Writeups"
        Category.PODCAST -> CategoryPodcast to "Podcast"
        Category.LEARNING -> CategoryLearning to "Learning"
        Category.DEFENSE -> CategoryDefense to "Defense"
        Category.CLOUD -> CategoryCloud to "Cloud"
        Category.TRADECRAFT -> CategoryTradecraft to "Tradecraft"
        Category.INCIDENTS -> CategoryIncidents to "Incidents"
        Category.CONFERENCE -> CategoryConference to "Conference"
    }
    
    Box(
        modifier = modifier
            .background(
                color = color.copy(alpha = 0.15f),
                shape = RoundedCornerShape(4.dp)
            )
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun formatTimeAgo(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < TimeUnit.MINUTES.toMillis(1) -> "Just now"
        diff < TimeUnit.HOURS.toMillis(1) -> {
            val mins = TimeUnit.MILLISECONDS.toMinutes(diff)
            "${mins}m ago"
        }
        diff < TimeUnit.DAYS.toMillis(1) -> {
            val hours = TimeUnit.MILLISECONDS.toHours(diff)
            "${hours}h ago"
        }
        diff < TimeUnit.DAYS.toMillis(7) -> {
            val days = TimeUnit.MILLISECONDS.toDays(diff)
            "${days}d ago"
        }
        else -> {
            SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(timestamp))
        }
    }
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ArticleTagsRow(
    articleId: String,
    tagsViewModel: TagsViewModel
) {
    val tags = remember(articleId) {
        mutableStateOf<List<TagEntity>>(emptyList())
    }
    
    LaunchedEffect(articleId) {
        tags.value = tagsViewModel.getTagsForArticle(articleId)
    }
    
    if (tags.value.isNotEmpty()) {
        Spacer(modifier = Modifier.height(8.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            tags.value.forEach { tag ->
                Box(
                    modifier = Modifier
                        .background(
                            color = Color(tag.colorHex).copy(alpha = 0.15f),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = tag.name,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(tag.colorHex),
                        fontWeight = FontWeight.Medium,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}
