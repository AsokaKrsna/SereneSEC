/**
 * SereneSec Focus Mode - Premium Reader Experience
 * - Extracts article content using Readability
 * - Respects app theme (light/dark)
 * - Blocks ads, cookie notices, and trackers
 * - Typography inspired by Edge/Safari Reader Mode
 */
(function () {
    'use strict';

    // ===========================================
    // PHASE 1: Remove annoying elements FIRST
    // ===========================================
    function removeAnnoyances() {
        // Cookie consent dialogs - common class names and IDs
        const cookieSelectors = [
            '[class*="cookie"]',
            '[class*="consent"]',
            '[class*="gdpr"]',
            '[class*="privacy-banner"]',
            '[id*="cookie"]',
            '[id*="consent"]',
            '[id*="gdpr"]',
            '[class*="CookieConsent"]',
            '[class*="cookie-banner"]',
            '[class*="cookie-notice"]',
            '[class*="cookie-popup"]',
            '[class*="cookie-modal"]',
            '[data-testid*="cookie"]',
            '[aria-label*="cookie"]',
            '.cc-banner',
            '.cc-window',
            '.cc_container',
            '#cookiebanner',
            '#cookie-law',
            '.qc-cmp2-container',
            '.evidon-banner',
            '#onetrust-consent-sdk',
            '#usercentrics-root',
            '.osano-cm-window',
            '.js-consent-banner',
            '#sp_message_container',
            '[class*="trustarc"]',
            '[id*="trustarc"]'
        ];

        // Ad containers and tracking elements
        const adSelectors = [
            '[class*="ad-"]',
            '[class*="-ad"]',
            '[class*="advertisement"]',
            '[class*="sponsor"]',
            '[id*="google_ads"]',
            '[id*="dfp"]',
            '[class*="dfp"]',
            'iframe[src*="doubleclick"]',
            'iframe[src*="googlesyndication"]',
            'iframe[src*="facebook.com/plugins"]',
            'iframe[src*="twitter.com/widgets"]',
            '[data-ad]',
            '[data-google-query-id]',
            '.adsbygoogle',
            '.ad-container',
            '.ad-wrapper',
            '.ad-slot',
            '.advertisement',
            '.sidebar-ad',
            '.inline-ad',
            '.sponsored-content',
            '.promoted-content',
            '[class*="outbrain"]',
            '[class*="taboola"]',
            '[id*="taboola"]',
            '[class*="native-ad"]',
            '[class*="promo-box"]'
        ];

        // Popups, modals, overlays
        const popupSelectors = [
            '[class*="popup"]',
            '[class*="modal"]:not(.focus-mode-modal)',
            '[class*="overlay"]:not(.focus-mode-overlay)',
            '[class*="subscribe"]',
            '[class*="newsletter"]',
            '[class*="signup"]',
            '[class*="paywall"]',
            '[class*="signin-prompt"]',
            '[class*="reg-wall"]',
            '[class*="login-prompt"]',
            '.fixed[class*="banner"]',
            '[class*="sticky-header"]',
            '[class*="floating-header"]',
            '[class*="notification-bar"]',
            '[class*="alert-banner"]'
        ];

        // Social sharing bars
        const socialSelectors = [
            '[class*="social-share"]',
            '[class*="share-buttons"]',
            '[class*="sharing-buttons"]',
            '.addthis_toolbox',
            '.sharethis-inline',
            '[class*="follow-us"]'
        ];

        // Navigation and sidebars (we're in reader mode)
        const navSelectors = [
            'nav',
            'header:not(article header)',
            'footer:not(article footer)',
            'aside',
            '[class*="sidebar"]',
            '[class*="related-posts"]',
            '[class*="recommended"]',
            '[class*="comments"]',
            '[id*="comments"]',
            '[class*="author-bio"]:not(article [class*="author"])',
            '[class*="breadcrumb"]',
            '[class*="pagination"]'
        ];

        const allSelectors = [
            ...cookieSelectors,
            ...adSelectors,
            ...popupSelectors,
            ...socialSelectors,
            ...navSelectors
        ];

        allSelectors.forEach(selector => {
            try {
                document.querySelectorAll(selector).forEach(el => {
                    // Don't remove if it's inside the main article
                    if (!el.closest('article') || el.matches('[class*="ad"]') || el.matches('[class*="cookie"]')) {
                        el.remove();
                    }
                });
            } catch (e) { /* Ignore selector errors */ }
        });

        // Remove fixed/sticky positioning to prevent floating elements
        document.querySelectorAll('*').forEach(el => {
            const style = window.getComputedStyle(el);
            if (style.position === 'fixed' || style.position === 'sticky') {
                if (!el.closest('.focus-mode-container')) {
                    el.style.position = 'relative';
                    el.style.top = 'auto';
                    el.style.bottom = 'auto';
                }
            }
        });

        // Enable scrolling (often disabled by popups)
        document.body.style.overflow = 'auto';
        document.documentElement.style.overflow = 'auto';
        document.body.classList.remove('modal-open', 'no-scroll', 'overflow-hidden');
    }

    // ===========================================
    // PHASE 2: Detect theme preference
    // ===========================================
    function detectTheme() {
        // Check if system prefers dark mode
        if (window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches) {
            return 'dark';
        }
        return 'light';
    }

    // ===========================================
    // PHASE 3: Apply focus mode styling
    // ===========================================
    function applyFocusMode(article) {
        const theme = detectTheme();
        const isDark = theme === 'dark';

        // Theme colors (soft, not harsh AMOLED black)
        const colors = isDark ? {
            bg: '#1A1A2E',           // Soft dark blue-gray
            bgSecondary: '#16213E',   // Slightly lighter for contrast
            text: '#E8E8E8',          // Soft white, not pure white
            textSecondary: '#A0A0A0',
            accent: '#4FC3F7',        // Light blue accent
            link: '#64B5F6',
            border: '#2D3748',
            codeBg: '#0F0F1A',
            codeText: '#A5D6FF',
            blockquoteBorder: '#4FC3F7',
            blockquoteBg: 'rgba(79, 195, 247, 0.1)'
        } : {
            bg: '#FFFFFF',
            bgSecondary: '#F8F9FA',
            text: '#1A1A2E',
            textSecondary: '#5A6270',
            accent: '#0066CC',
            link: '#0066CC',
            border: '#E2E8F0',
            codeBg: '#F1F5F9',
            codeText: '#1E293B',
            blockquoteBorder: '#0066CC',
            blockquoteBg: 'rgba(0, 102, 204, 0.05)'
        };

        // Extract metadata
        const title = article.title || document.title;
        const byline = article.byline || '';
        const siteName = article.siteName || window.location.hostname;
        const content = article.content;
        const excerpt = article.excerpt || '';

        // Calculate reading time
        const wordCount = article.textContent ? article.textContent.split(/\s+/).length : 500;
        const readingTime = Math.max(1, Math.round(wordCount / 200));

        // Build the reader view HTML
        const readerHTML = `
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>${title}</title>
                <style>
                    * {
                        box-sizing: border-box;
                    }
                    
                    html {
                        scroll-behavior: smooth;
                    }
                    
                    body {
                        font-family: 'Segoe UI', system-ui, -apple-system, BlinkMacSystemFont, sans-serif;
                        line-height: 1.75;
                        margin: 0;
                        padding: 0;
                        background: ${colors.bg};
                        color: ${colors.text};
                        -webkit-font-smoothing: antialiased;
                        -moz-osx-font-smoothing: grayscale;
                    }
                    
                    .reader-container {
                        max-width: 680px;
                        margin: 0 auto;
                        padding: 24px 20px 60px;
                    }
                    
                    /* Header styling */
                    .reader-header {
                        margin-bottom: 32px;
                        padding-bottom: 24px;
                        border-bottom: 1px solid ${colors.border};
                    }
                    
                    .reader-site {
                        font-size: 13px;
                        color: ${colors.accent};
                        text-transform: uppercase;
                        letter-spacing: 0.5px;
                        margin-bottom: 12px;
                        font-weight: 600;
                    }
                    
                    .reader-title {
                        font-size: 28px;
                        font-weight: 700;
                        line-height: 1.3;
                        margin: 0 0 16px;
                        color: ${colors.text};
                        letter-spacing: -0.5px;
                    }
                    
                    .reader-excerpt {
                        font-size: 18px;
                        color: ${colors.textSecondary};
                        line-height: 1.6;
                        margin-bottom: 16px;
                        font-style: italic;
                    }
                    
                    .reader-meta {
                        display: flex;
                        flex-wrap: wrap;
                        gap: 16px;
                        font-size: 14px;
                        color: ${colors.textSecondary};
                    }
                    
                    .reader-meta span {
                        display: flex;
                        align-items: center;
                        gap: 6px;
                    }
                    
                    /* Article content */
                    .reader-content {
                        font-size: 18px;
                        line-height: 1.8;
                    }
                    
                    .reader-content p {
                        margin: 0 0 1.5em;
                    }
                    
                    .reader-content h1,
                    .reader-content h2,
                    .reader-content h3,
                    .reader-content h4 {
                        font-weight: 600;
                        line-height: 1.4;
                        margin: 2em 0 0.75em;
                        color: ${colors.text};
                    }
                    
                    .reader-content h1 { font-size: 1.75em; }
                    .reader-content h2 { font-size: 1.5em; }
                    .reader-content h3 { font-size: 1.25em; }
                    .reader-content h4 { font-size: 1.1em; }
                    
                    /* Links */
                    .reader-content a {
                        color: ${colors.link};
                        text-decoration: none;
                        border-bottom: 1px solid transparent;
                        transition: border-color 0.2s;
                    }
                    
                    .reader-content a:hover {
                        border-bottom-color: ${colors.link};
                    }
                    
                    /* Images */
                    .reader-content img {
                        max-width: 100%;
                        height: auto;
                        border-radius: 8px;
                        margin: 1.5em 0;
                        display: block;
                    }
                    
                    .reader-content figure {
                        margin: 1.5em 0;
                    }
                    
                    .reader-content figcaption {
                        font-size: 14px;
                        color: ${colors.textSecondary};
                        text-align: center;
                        margin-top: 8px;
                        font-style: italic;
                    }
                    
                    /* Blockquotes */
                    .reader-content blockquote {
                        margin: 1.5em 0;
                        padding: 16px 24px;
                        border-left: 4px solid ${colors.blockquoteBorder};
                        background: ${colors.blockquoteBg};
                        border-radius: 0 8px 8px 0;
                        font-style: italic;
                    }
                    
                    .reader-content blockquote p:last-child {
                        margin-bottom: 0;
                    }
                    
                    /* Code blocks */
                    .reader-content pre {
                        background: ${colors.codeBg};
                        border: 1px solid ${colors.border};
                        border-radius: 8px;
                        padding: 16px 20px;
                        overflow-x: auto;
                        margin: 1.5em 0;
                        font-size: 14px;
                        line-height: 1.6;
                    }
                    
                    .reader-content pre code {
                        background: none;
                        padding: 0;
                        font-size: inherit;
                        color: ${colors.codeText};
                    }
                    
                    .reader-content code {
                        font-family: 'SF Mono', 'Cascadia Code', 'Fira Code', Consolas, monospace;
                        background: ${colors.codeBg};
                        padding: 2px 6px;
                        border-radius: 4px;
                        font-size: 0.9em;
                        color: ${colors.codeText};
                    }
                    
                    /* Lists */
                    .reader-content ul,
                    .reader-content ol {
                        margin: 1em 0;
                        padding-left: 1.5em;
                    }
                    
                    .reader-content li {
                        margin: 0.5em 0;
                    }
                    
                    /* Tables */
                    .reader-content table {
                        width: 100%;
                        border-collapse: collapse;
                        margin: 1.5em 0;
                        font-size: 16px;
                    }
                    
                    .reader-content th,
                    .reader-content td {
                        padding: 12px;
                        border: 1px solid ${colors.border};
                        text-align: left;
                    }
                    
                    .reader-content th {
                        background: ${colors.bgSecondary};
                        font-weight: 600;
                    }
                    
                    /* Horizontal rule */
                    .reader-content hr {
                        border: none;
                        height: 1px;
                        background: ${colors.border};
                        margin: 2em 0;
                    }
                    
                    /* Selection */
                    ::selection {
                        background: ${colors.accent};
                        color: ${isDark ? '#000' : '#fff'};
                    }
                    
                    /* Remove all possible ad remnants */
                    [class*="ad-"], [class*="advertisement"], [id*="ad-"],
                    iframe, .adsbygoogle, [data-ad] {
                        display: none !important;
                    }
                </style>
            </head>
            <body>
                <article class="reader-container">
                    <header class="reader-header">
                        <div class="reader-site">${siteName}</div>
                        <h1 class="reader-title">${title}</h1>
                        ${excerpt ? `<p class="reader-excerpt">${excerpt}</p>` : ''}
                        <div class="reader-meta">
                            ${byline ? `<span>✍️ ${byline}</span>` : ''}
                            <span>📖 ${readingTime} min read</span>
                            <span>📝 ${wordCount.toLocaleString()} words</span>
                        </div>
                    </header>
                    <div class="reader-content">
                        ${content}
                    </div>
                </article>
            </body>
            </html>
        `;

        // Replace entire document
        document.open();
        document.write(readerHTML);
        document.close();
    }

    // ===========================================
    // MAIN EXECUTION
    // ===========================================
    try {
        // Step 1: Remove annoyances first (even before Readability)
        removeAnnoyances();

        // Step 2: Check if Readability is available
        if (typeof Readability !== 'undefined') {
            // Clone document for Readability
            const documentClone = document.cloneNode(true);
            const reader = new Readability(documentClone);
            const article = reader.parse();

            if (article && article.content) {
                applyFocusMode(article);
            } else {
                // Fallback: just style the current page
                console.log('Readability could not extract article, applying basic cleanup');
            }
        } else {
            console.log('Readability not loaded');
        }
    } catch (e) {
        console.error('Focus mode error:', e);
    }
})();
