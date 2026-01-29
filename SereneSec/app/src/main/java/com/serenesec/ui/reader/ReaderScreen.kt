package com.serenesec.ui.reader

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import java.io.ByteArrayInputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    onBack: () -> Unit,
    viewModel: ReaderViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val article by viewModel.article.collectAsState()
    val isFocusMode by viewModel.isFocusMode.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    var webViewProgress by remember { mutableIntStateOf(0) }
    var isWebViewLoading by remember { mutableStateOf(true) }
    var showExternalLinkDialog by remember { mutableStateOf<String?>(null) }
    
    // WebView reference for JS injection
    var webView by remember { mutableStateOf<WebView?>(null) }
    
    // Toggle focus mode
    LaunchedEffect(isFocusMode) {
        webView?.let { wv ->
            if (isFocusMode) {
                injectFocusMode(context, wv)
            } else {
                // Reload original page
                article?.contentUrl?.let { wv.loadUrl(it) }
            }
        }
    }
    
    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = article?.title ?: "Loading...",
                                style = MaterialTheme.typography.titleSmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            article?.contentUrl?.let { url ->
                                Text(
                                    text = Uri.parse(url).host ?: url,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    },
                    actions = {
                        // Floating window button
                        IconButton(onClick = {
                            article?.let { art ->
                                if (com.serenesec.util.FloatingWindowPermission.canDrawOverlays(context)) {
                                    com.serenesec.service.FloatingWindowService.startFloating(
                                        context, art.contentUrl, art.title
                                    )
                                } else {
                                    val intent = com.serenesec.util.FloatingWindowPermission.getOverlayPermissionIntent(context)
                                    context.startActivity(intent)
                                }
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.OpenInBrowser,
                                contentDescription = "Floating window"
                            )
                        }
                        // Archive button
                        IconButton(onClick = {
                            viewModel.archiveArticle()
                            onBack()
                        }) {
                            Icon(
                                imageVector = Icons.Default.Archive,
                                contentDescription = "Archive"
                            )
                        }
                        // Open in browser
                        IconButton(onClick = {
                            article?.contentUrl?.let { url ->
                                showExternalLinkDialog = url
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.OpenInNew,
                                contentDescription = "Open in browser"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
                
                // Progress indicator
                if (isWebViewLoading && webViewProgress < 100) {
                    LinearProgressIndicator(
                        progress = { webViewProgress / 100f },
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        floatingActionButton = {
            // Focus Mode FAB
            FloatingActionButton(
                onClick = { viewModel.toggleFocusMode() },
                containerColor = if (isFocusMode) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isFocusMode) Icons.Default.Visibility else Icons.Default.MenuBook,
                        contentDescription = if (isFocusMode) "Exit Focus Mode" else "Focus Mode",
                        tint = if (isFocusMode) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isFocusMode) "Normal" else "Focus",
                        color = if (isFocusMode) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                article?.contentUrl?.let { url ->
                    WebViewContent(
                        url = url,
                        onWebViewCreated = { webView = it },
                        onProgressChanged = { webViewProgress = it },
                        onLoadingChanged = { isWebViewLoading = it },
                        onExternalLink = { showExternalLinkDialog = it }
                    )
                }
            }
        }
    }
    
    // External link dialog
    showExternalLinkDialog?.let { url ->
        ExternalLinkDialog(
            url = url,
            onDismiss = { showExternalLinkDialog = null },
            onConfirm = {
                openInBrowser(context, url)
                showExternalLinkDialog = null
            }
        )
    }
}

// Ad/tracker domains to block at network level
private val blockedDomains = setOf(
    // Ads
    "doubleclick.net",
    "googlesyndication.com",
    "googleadservices.com",
    "google-analytics.com",
    "googletagmanager.com",
    "googletagservices.com",
    "adservice.google.com",
    "pagead2.googlesyndication.com",
    "adsense.google.com",
    "facebook.net",
    "connect.facebook.net",
    "facebook.com/tr",
    "analytics.twitter.com",
    "platform.twitter.com",
    "ads.twitter.com",
    "amazon-adsystem.com",
    "advertising.com",
    "adsrvr.org",
    "adnxs.com",
    "criteo.com",
    "criteo.net",
    "outbrain.com",
    "taboola.com",
    "mgid.com",
    "revcontent.com",
    "adblade.com",
    "adroll.com",
    "rubiconproject.com",
    "casalemedia.com",
    "pubmatic.com",
    "openx.net",
    "bidswitch.net",
    "lijit.com",
    "sharethrough.com",
    "bidgear.com",
    // Trackers
    "scorecardresearch.com",
    "quantserve.com",
    "bluekai.com",
    "krxd.net",
    "exelator.com",
    "rlcdn.com",
    "demdex.net",
    "omtrdc.net",
    "everesttech.net",
    "mookie1.com",
    "dotomi.com",
    "chartbeat.com",
    "newrelic.com",
    "nr-data.net",
    "segment.io",
    "segment.com",
    "mixpanel.com",
    "amplitude.com",
    "hotjar.com",
    "fullstory.com",
    "mouseflow.com",
    "crazyegg.com",
    "luckyorange.com",
    // Cookie consent popups (if loaded externally)
    "cookielaw.org",
    "onetrust.com",
    "trustarc.com",
    "cookiebot.com",
    "evidon.com",
    "usercentrics.eu",
    "osano.com",
    "iubenda.com",
    "termly.io"
)

private val blockedPathPatterns = listOf(
    "/ads/",
    "/ad/",
    "/pixel",
    "/track",
    "/beacon",
    "/analytics",
    "/collect",
    "/log?",
    "/event?",
    "/__utm",
    "/gtm.js",
    "/gtag/",
    "/fbevents",
    "/tr?"
)

private fun shouldBlockUrl(url: String): Boolean {
    val lowerUrl = url.lowercase()
    val host = try {
        Uri.parse(url).host?.lowercase() ?: ""
    } catch (e: Exception) {
        ""
    }
    
    // Check domain blocklist
    if (blockedDomains.any { host.contains(it) }) {
        return true
    }
    
    // Check path patterns
    if (blockedPathPatterns.any { lowerUrl.contains(it) }) {
        return true
    }
    
    return false
}

// Empty response to return for blocked requests
private fun createEmptyResponse(): WebResourceResponse {
    return WebResourceResponse(
        "text/plain",
        "UTF-8",
        ByteArrayInputStream(ByteArray(0))
    )
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun WebViewContent(
    url: String,
    onWebViewCreated: (WebView) -> Unit,
    onProgressChanged: (Int) -> Unit,
    onLoadingChanged: (Boolean) -> Unit,
    onExternalLink: (String) -> Unit
) {
    val context = LocalContext.current
    val originalHost = remember(url) { Uri.parse(url).host }
    
    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    loadWithOverviewMode = true
                    useWideViewPort = true
                    builtInZoomControls = true
                    displayZoomControls = false
                    mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
                    cacheMode = WebSettings.LOAD_DEFAULT
                    // Disable third-party cookies
                    @Suppress("DEPRECATION")
                    setSupportMultipleWindows(false)
                    blockNetworkImage = false
                    loadsImagesAutomatically = true
                }
                
                webViewClient = object : WebViewClient() {
                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                        super.onPageStarted(view, url, favicon)
                        onLoadingChanged(true)
                    }
                    
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        onLoadingChanged(false)
                        
                        // Inject cleanup script after page loads
                        view?.evaluateJavascript(getCleanupScript(), null)
                    }
                    
                    override fun shouldOverrideUrlLoading(
                        view: WebView?,
                        request: WebResourceRequest?
                    ): Boolean {
                        val requestUrl = request?.url?.toString() ?: return false
                        val requestHost = Uri.parse(requestUrl).host
                        
                        // Allow same-domain navigation
                        if (requestHost == originalHost) {
                            return false
                        }
                        
                        // Block external links and show dialog
                        onExternalLink(requestUrl)
                        return true
                    }
                    
                    // Block ads and trackers at network level
                    override fun shouldInterceptRequest(
                        view: WebView?,
                        request: WebResourceRequest?
                    ): WebResourceResponse? {
                        val requestUrl = request?.url?.toString() ?: return null
                        
                        if (shouldBlockUrl(requestUrl)) {
                            return createEmptyResponse()
                        }
                        
                        return super.shouldInterceptRequest(view, request)
                    }
                }
                
                webChromeClient = object : WebChromeClient() {
                    override fun onProgressChanged(view: WebView?, newProgress: Int) {
                        super.onProgressChanged(view, newProgress)
                        onProgressChanged(newProgress)
                    }
                }
                
                loadUrl(url)
                onWebViewCreated(this)
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}

/**
 * JavaScript to clean up the page after it loads
 * Removes cookie notices, popups, and enables scrolling
 */
private fun getCleanupScript(): String {
    return """
        (function() {
            // Remove cookie notices and overlays
            var selectors = [
                '[class*="cookie"]', '[class*="consent"]', '[class*="gdpr"]',
                '[id*="cookie"]', '[id*="consent"]', '#onetrust-consent-sdk',
                '.cc-banner', '.cc-window', '[class*="privacy-banner"]',
                '[class*="popup"]', '[class*="newsletter-popup"]',
                '[class*="subscribe-popup"]', '[class*="modal-overlay"]'
            ];
            selectors.forEach(function(sel) {
                try {
                    document.querySelectorAll(sel).forEach(function(el) {
                        el.remove();
                    });
                } catch(e) {}
            });
            
            // Enable scrolling
            document.body.style.overflow = 'auto';
            document.documentElement.style.overflow = 'auto';
            document.body.classList.remove('modal-open', 'no-scroll');
            
            // Remove fixed position from potential floating elements
            document.querySelectorAll('[style*="position: fixed"]').forEach(function(el) {
                if (el.innerText && el.innerText.toLowerCase().includes('cookie')) {
                    el.remove();
                }
            });
        })();
    """.trimIndent()
}

@Composable
private fun ExternalLinkDialog(
    url: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("External Link") },
        text = {
            Column {
                Text("This link will open in your default browser:")
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = url,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Open")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun openInBrowser(context: Context, url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    } catch (e: Exception) {
        // Handle silently
    }
}

private fun injectFocusMode(context: Context, webView: WebView) {
    try {
        val readabilityJs = context.assets.open("readability.js")
            .bufferedReader()
            .use { it.readText() }
        val focusModeJs = context.assets.open("focus_mode.js")
            .bufferedReader()
            .use { it.readText() }
        
        webView.evaluateJavascript(readabilityJs, null)
        webView.evaluateJavascript(focusModeJs, null)
    } catch (e: Exception) {
        // Fallback to basic focus mode
        val basicFocusJs = """
            (function() {
                // Detect theme
                var isDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
                var bgColor = isDark ? '#1A1A2E' : '#FFFFFF';
                var textColor = isDark ? '#E8E8E8' : '#1A1A2E';
                
                var article = document.querySelector('article') || document.body;
                document.body.innerHTML = '<div style="max-width: 680px; margin: 0 auto; padding: 24px; font-family: system-ui; line-height: 1.75; font-size: 18px;">' + article.innerHTML + '</div>';
                document.body.style.backgroundColor = bgColor;
                document.body.style.color = textColor;
            })();
        """.trimIndent()
        webView.evaluateJavascript(basicFocusJs, null)
    }
}
