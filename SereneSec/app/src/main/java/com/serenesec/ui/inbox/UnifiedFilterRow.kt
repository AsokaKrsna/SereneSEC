package com.serenesec.ui.inbox

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.serenesec.domain.model.Category
import com.serenesec.ui.tags.TagsViewModel
import com.serenesec.ui.theme.CategoryCVE
import com.serenesec.ui.theme.CategoryNews
import com.serenesec.ui.theme.CategoryResearch
import com.serenesec.ui.theme.CategoryTools

@Composable
fun UnifiedFilterRow(
    selectedCategory: Category?,
    onCategorySelected: (Category?) -> Unit,
    selectedTagIds: Set<String>,
    onTagToggle: (String) -> Unit,
    tagsViewModel: TagsViewModel,
    modifier: Modifier = Modifier
) {
    val tags by tagsViewModel.tags.collectAsState()
    
    val categories = listOf(
        null to "All",
        Category.NEWS to "News",
        Category.TOOLS to "Tools",
        Category.CVE to "CVEs",
        Category.RESEARCH to "Research"
    )
    
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.padding(vertical = 8.dp)
    ) {
        // Built-in categories
        items(categories) { (category, label) ->
            val isSelected = selectedCategory == category
            val chipColor = when (category) {
                Category.NEWS -> CategoryNews
                Category.TOOLS -> CategoryTools
                Category.CVE -> CategoryCVE
                Category.RESEARCH -> CategoryResearch
                null -> MaterialTheme.colorScheme.primary
            }
            
            FilterChip(
                selected = isSelected,
                onClick = { onCategorySelected(category) },
                label = { Text(label) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = chipColor.copy(alpha = 0.2f),
                    selectedLabelColor = chipColor
                )
            )
        }
        
        // Custom tags
        items(tags, key = { it.id }) { tag ->
            val isSelected = tag.id in selectedTagIds
            val chipColor = Color(tag.colorHex)
            
            FilterChip(
                selected = isSelected,
                onClick = { onTagToggle(tag.id) },
                label = { Text(tag.name) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = chipColor.copy(alpha = 0.2f),
                    selectedLabelColor = chipColor
                )
            )
        }
    }
}
