package com.serenesec.ui.sharing

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TextFormat
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.serenesec.data.sharing.ShareManager.ShareFormat

/**
 * Bottom sheet with enhanced sharing options
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareBottomSheet(
    articleTitle: String,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onShareFormat: (ShareFormat) -> Unit,
    onCopyLink: () -> Unit,
    onCopyFormatted: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            // Header
            Text(
                text = "Share Article",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = articleTitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Share formats
            Text(
                text = "Share as",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Row 1: Quick share options
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ShareOptionButton(
                    icon = Icons.Default.Share,
                    label = "Quick",
                    color = MaterialTheme.colorScheme.primary,
                    onClick = { onShareFormat(ShareFormat.PLAIN_TEXT) }
                )
                
                ShareOptionButton(
                    icon = Icons.Default.FormatQuote,
                    label = "Quote Card",
                    color = Color(0xFF9B59B6),
                    onClick = { onShareFormat(ShareFormat.QUOTE_CARD) }
                )
                
                ShareOptionButton(
                    icon = Icons.Default.TextFormat,
                    label = "Markdown",
                    color = Color(0xFF3498DB),
                    onClick = { onShareFormat(ShareFormat.MARKDOWN) }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Row 2: Platform-specific
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ShareOptionButton(
                    icon = Icons.Default.Share,
                    label = "Twitter",
                    color = Color(0xFF1DA1F2),
                    onClick = { onShareFormat(ShareFormat.TWITTER) }
                )
                
                ShareOptionButton(
                    icon = Icons.Default.Share,
                    label = "LinkedIn",
                    color = Color(0xFF0A66C2),
                    onClick = { onShareFormat(ShareFormat.LINKEDIN) }
                )
                
                ShareOptionButton(
                    icon = Icons.Default.Email,
                    label = "Email",
                    color = Color(0xFFE74C3C),
                    onClick = { onShareFormat(ShareFormat.EMAIL) }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            HorizontalDivider()
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Copy options
            Text(
                text = "Copy",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            CopyOption(
                icon = Icons.Default.Link,
                label = "Copy Link",
                subtitle = "URL only",
                onClick = onCopyLink
            )
            
            CopyOption(
                icon = Icons.Default.ContentCopy,
                label = "Copy Formatted",
                subtitle = "Title + summary + link",
                onClick = onCopyFormatted
            )
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ShareOptionButton(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun CopyOption(
    icon: ImageVector,
    label: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
