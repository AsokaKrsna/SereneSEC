package com.serenesec.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.fillMaxSize
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.serenesec.domain.model.Source
import com.serenesec.ui.analytics.AnalyticsScreen
import com.serenesec.ui.collections.CollectionDetailScreen
import com.serenesec.ui.collections.CollectionsScreen
import com.serenesec.ui.inbox.InboxScreen
import com.serenesec.ui.reader.ReaderScreen
import com.serenesec.ui.settings.SettingsScreen
import com.serenesec.ui.notifications.NotificationSettingsScreen
import com.serenesec.ui.sources.AddSourceDialog
import com.serenesec.ui.sources.SourcesScreen
import com.serenesec.ui.sources.SourcesViewModel
import com.serenesec.ui.tags.TagsScreen
import com.serenesec.ui.websites.WebsitesScreen
import kotlinx.coroutines.launch
import java.net.URLDecoder
import java.net.URLEncoder
import java.security.MessageDigest

sealed class Screen(val route: String) {
    object Inbox : Screen("inbox")
    object Reader : Screen("reader/{articleId}") {
        fun createRoute(articleId: String) = "reader/$articleId"
    }
    object Settings : Screen("settings")
    object Sources : Screen("sources")
    object Websites : Screen("websites")
    object Analytics : Screen("analytics")
    object Tags : Screen("tags")
    object Notifications : Screen("notifications")
    object Collections : Screen("collections")
    object CollectionDetail : Screen("collection/{collectionId}") {
        fun createRoute(collectionId: String) = "collection/$collectionId"
    }
    object WebsiteReader : Screen("website_reader/{url}") {
        fun createRoute(url: String): String {
            val encoded = URLEncoder.encode(url, "UTF-8")
            return "website_reader/$encoded"
        }
    }
}

@Composable
fun SereneSecNavHost(
    navController: NavHostController,
    startDestination: String = Screen.Inbox.route
) {
    var showAddSourceDialog by remember { mutableStateOf(false) }
    val sourcesViewModel: SourcesViewModel = hiltViewModel()
    val scope = rememberCoroutineScope()
    
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Inbox.route) {
            InboxScreen(
                onArticleClick = { article ->
                    navController.navigate(Screen.Reader.createRoute(article.id))
                },
                onSettingsClick = {
                    navController.navigate(Screen.Settings.route)
                },
                onAddSourceClick = {
                    showAddSourceDialog = true
                },
                onCollectionsClick = {
                    navController.navigate(Screen.Collections.route)
                },
                onWebsitesClick = {
                    navController.navigate(Screen.Websites.route)
                }
            )
            
            // Show add source dialog overlay
            if (showAddSourceDialog) {
                AddSourceDialog(
                    onDismiss = { showAddSourceDialog = false },
                    onAddSource = { name, url, category, type ->
                        scope.launch {
                            val source = Source(
                                id = generateSourceId(url),
                                name = name,
                                url = url,
                                category = category,
                                type = type,
                                isBuiltIn = false,
                                isEnabled = true
                            )
                            sourcesViewModel.addSource(source)
                        }
                    }
                )
            }
        }
        
        composable(
            route = Screen.Reader.route,
            arguments = listOf(
                navArgument("articleId") { type = NavType.StringType }
            )
        ) {
            ReaderScreen(
                onBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.Settings.route) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onSourcesClick = { navController.navigate(Screen.Sources.route) },
                onWebsitesClick = { navController.navigate(Screen.Websites.route) },
                onAnalyticsClick = { navController.navigate(Screen.Analytics.route) },
                onTagsClick = { navController.navigate(Screen.Tags.route) },
                onNotificationsClick = { navController.navigate(Screen.Notifications.route) },
                onCollectionsClick = { navController.navigate(Screen.Collections.route) }
            )
        }
        
        composable(Screen.Sources.route) {
            SourcesScreen(
                onBack = { navController.popBackStack() },
                onAddSourceClick = { showAddSourceDialog = true }
            )
        }
        
        composable(Screen.Websites.route) {
            WebsitesScreen(
                onBack = { navController.popBackStack() },
                onWebsiteClick = { bookmark ->
                    navController.navigate(Screen.WebsiteReader.createRoute(bookmark.url))
                }
            )
        }
        
        composable(Screen.Analytics.route) {
            AnalyticsScreen(
                onBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.Tags.route) {
            TagsScreen(
                onBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.Notifications.route) {
            NotificationSettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.Collections.route) {
            CollectionsScreen(
                onBack = { navController.popBackStack() },
                onCollectionClick = { collection ->
                    navController.navigate(Screen.CollectionDetail.createRoute(collection.id))
                }
            )
        }
        
        composable(
            route = Screen.CollectionDetail.route,
            arguments = listOf(
                navArgument("collectionId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val collectionId = backStackEntry.arguments?.getString("collectionId") ?: ""
            // We need to get the collection entity - for now just use the ID
            CollectionDetailScreenWrapper(
                collectionId = collectionId,
                onBack = { navController.popBackStack() },
                onArticleClick = { article ->
                    navController.navigate(Screen.Reader.createRoute(article.id))
                }
            )
        }
        
        composable(
            route = Screen.WebsiteReader.route,
            arguments = listOf(
                navArgument("url") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val encodedUrl = backStackEntry.arguments?.getString("url") ?: ""
            val url = URLDecoder.decode(encodedUrl, "UTF-8")
            WebsiteReaderScreen(
                url = url,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

private fun generateSourceId(url: String): String {
    val bytes = MessageDigest.getInstance("SHA-256").digest(url.toByteArray())
    return "custom_" + bytes.take(8).joinToString("") { "%02x".format(it) }
}

// Simple WebView screen for non-RSS websites
@Composable
private fun WebsiteReaderScreen(
    url: String,
    onBack: () -> Unit
) {
    com.serenesec.ui.reader.SimpleWebViewScreen(
        url = url,
        onBack = onBack
    )
}

// Wrapper to fetch collection by ID for the detail screen
@Composable
private fun CollectionDetailScreenWrapper(
    collectionId: String,
    onBack: () -> Unit,
    onArticleClick: (com.serenesec.domain.model.Article) -> Unit
) {
    val viewModel: com.serenesec.ui.collections.CollectionsViewModel = hiltViewModel()
    val collections by viewModel.collections.collectAsState()
    val collection = collections.find { it.id == collectionId }
    
    if (collection != null) {
        CollectionDetailScreen(
            collection = collection,
            onBack = onBack,
            onArticleClick = onArticleClick
        )
    } else {
        // Loading or not found - just show back
        androidx.compose.foundation.layout.Box(
            modifier = androidx.compose.ui.Modifier.fillMaxSize(),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            androidx.compose.material3.CircularProgressIndicator()
        }
    }
}
