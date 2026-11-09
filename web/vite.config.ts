import { defineConfig, type Plugin } from 'vite';
import react from '@vitejs/plugin-react';

function generateFallbackHtml(targetUrl: string, status: number): string {
  let hostname = targetUrl;
  try {
    hostname = new URL(targetUrl).hostname;
  } catch {}

  return `<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Security Embed Protected</title>
  <style>
    body {
      margin: 0;
      font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
      background-color: #0d0f12;
      color: #f4f4f5;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      min-height: 100vh;
      padding: 24px;
      box-sizing: border-box;
      text-align: center;
    }
    .card {
      max-width: 440px;
      width: 100%;
      background: #181b21;
      border: 1px solid #272a34;
      border-radius: 20px;
      padding: 32px 24px;
      box-shadow: 0 12px 30px rgba(0, 0, 0, 0.4);
    }
    .badge {
      display: inline-block;
      font-size: 11px;
      font-weight: 700;
      text-transform: uppercase;
      letter-spacing: 0.05em;
      color: #38bdf8;
      background: rgba(56, 189, 248, 0.15);
      border: 1px solid rgba(56, 189, 248, 0.3);
      padding: 3px 10px;
      border-radius: 9999px;
      margin-bottom: 16px;
    }
    h2 {
      font-size: 18px;
      font-weight: 700;
      color: #ffffff;
      margin: 0 0 10px 0;
      line-height: 1.3;
    }
    p {
      font-size: 13px;
      color: #94a3b8;
      line-height: 1.6;
      margin: 0 0 24px 0;
    }
    .actions {
      display: flex;
      flex-direction: column;
      gap: 10px;
    }
    .btn {
      display: block;
      width: 100%;
      padding: 10px 16px;
      border-radius: 12px;
      font-size: 13px;
      font-weight: 600;
      text-decoration: none;
      cursor: pointer;
      box-sizing: border-box;
      transition: all 0.15s ease;
      border: none;
    }
    .btn-reader {
      background: #06b6d4;
      color: #ffffff;
    }
    .btn-reader:hover {
      background: #0891b2;
    }
    .btn-popout {
      background: #272a34;
      color: #e2e8f0;
      border: 1px solid #3f4455;
    }
    .btn-popout:hover {
      background: #333846;
    }
  </style>
</head>
<body>
  <div class="card">
    <div class="badge">Shield Protection Active</div>
    <h2>Frame Embedding Restricted</h2>
    <p>
      <strong>${hostname}</strong> protects its articles with anti-bot verification or restricts in-frame browsing.
      You can read the entire article in distraction-free Reader View or open it in a popup window.
    </p>
    <div class="actions">
      <button class="btn btn-reader" onclick="window.parent.postMessage({ type: 'SWITCH_TO_READER' }, '*')">
        Switch to Clean Reader View
      </button>
      <button class="btn btn-popout" onclick="window.open('${targetUrl}', '_blank', 'width=1100,height=850,resizable=yes,scrollbars=yes')">
        Open in Floating Window ↗
      </button>
    </div>
  </div>
</body>
</html>`;
}

function feedProxyPlugin(): Plugin {
  return {
    name: 'feed-proxy-plugin',
    configureServer(server) {
      server.middlewares.use('/api/feed', async (req, res) => {
        try {
          const reqUrl = new URL(req.url || '', `http://${req.headers.host || 'localhost'}`);
          const targetUrl = reqUrl.searchParams.get('url');
          const isWebView = reqUrl.searchParams.get('mode') === 'webview';

          if (!targetUrl) {
            res.statusCode = 400;
            res.setHeader('Content-Type', 'application/json');
            res.end(JSON.stringify({ error: 'Missing "url" query parameter' }));
            return;
          }

          const controller = new AbortController();
          const timeout = setTimeout(() => controller.abort(), 15000);

          const upstreamResponse = await fetch(targetUrl, {
            signal: controller.signal,
            headers: {
              'User-Agent':
                'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36 SereneSec/1.0',
              Accept: isWebView
                ? 'text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8'
                : 'application/rss+xml, application/atom+xml, application/xml, text/xml, text/html, */*',
              'Accept-Language': 'en-US,en;q=0.9',
            },
          });
          clearTimeout(timeout);

          if (!upstreamResponse.ok) {
            if (isWebView) {
              res.statusCode = 200;
              res.setHeader('Content-Type', 'text/html; charset=utf-8');
              res.setHeader('Access-Control-Allow-Origin', '*');
              res.end(generateFallbackHtml(targetUrl, upstreamResponse.status));
              return;
            }

            res.statusCode = upstreamResponse.status;
            res.setHeader('Content-Type', 'application/json');
            res.end(JSON.stringify({ error: `Upstream returned ${upstreamResponse.status}` }));
            return;
          }

          let bodyText = await upstreamResponse.text();
          const contentType = upstreamResponse.headers.get('content-type') || '';

          if (isWebView || contentType.includes('text/html')) {
            const baseTag = `<base href="${targetUrl}">`;
            const overrides = `
              <style>
                html, body, [class*="tl-wrapper"], [class*="entering"], [class*="transition"], [class*="fade"] {
                  opacity: 1 !important;
                  visibility: visible !important;
                  display: block !important;
                }
              </style>
              <script>
                (function() {
                  // Defuse frame-busting scripts from hijacking top window
                  try {
                    Object.defineProperty(window, 'top', { get: function() { return window.self; }, configurable: false });
                    Object.defineProperty(window, 'parent', { get: function() { return window.self; }, configurable: false });
                  } catch(e) {}

                  function cleanAndUncover() {
                    // Remove cookie banners and blocking dialogs
                    var selectors = [
                      '[class*="cookie"]', '[class*="consent"]', '[class*="gdpr"]',
                      '#onetrust-consent-sdk', '.cc-banner', '.cc-window',
                      '[class*="privacy-banner"]', '[class*="modal-overlay"]',
                      '[class*="newsletter-popup"]', '[class*="subscribe-popup"]'
                    ];
                    selectors.forEach(function(sel) {
                      try {
                        document.querySelectorAll(sel).forEach(function(el) { el.remove(); });
                      } catch(e) {}
                    });

                    // Force body visible and scrollable
                    document.body.style.opacity = '1';
                    document.body.style.visibility = 'visible';
                    document.body.style.overflow = 'auto';
                    document.documentElement.style.overflow = 'auto';
                    document.body.classList.remove('tl-wrapper-status--entering', 'modal-open', 'no-scroll');
                  }

                  if (document.readyState === 'loading') {
                    window.addEventListener('DOMContentLoaded', cleanAndUncover);
                  } else {
                    cleanAndUncover();
                  }
                  setTimeout(cleanAndUncover, 800);
                  setTimeout(cleanAndUncover, 2000);
                })();
              </script>
            `;

            if (bodyText.includes('<head>')) {
              bodyText = bodyText.replace('<head>', `<head>${baseTag}${overrides}`);
            } else if (bodyText.includes('<head ')) {
              bodyText = bodyText.replace(/<head[^>]*>/, `$&${baseTag}${overrides}`);
            } else {
              bodyText = baseTag + overrides + bodyText;
            }

            res.statusCode = 200;
            res.setHeader('Content-Type', 'text/html; charset=utf-8');
            res.setHeader('Access-Control-Allow-Origin', '*');
            res.setHeader('X-Frame-Options', 'ALLOWALL');
            res.end(bodyText);
            return;
          }

          res.statusCode = 200;
          res.setHeader('Content-Type', contentType || 'application/xml; charset=utf-8');
          res.setHeader('Access-Control-Allow-Origin', '*');
          res.end(bodyText);
        } catch (err: any) {
          if (isWebView && targetUrl) {
            res.statusCode = 200;
            res.setHeader('Content-Type', 'text/html; charset=utf-8');
            res.setHeader('Access-Control-Allow-Origin', '*');
            res.end(generateFallbackHtml(targetUrl, 502));
            return;
          }

          res.statusCode = 502;
          res.setHeader('Content-Type', 'application/json');
          res.end(JSON.stringify({ error: err?.message || 'Proxy error' }));
        }
      });
    },
  };
}

export default defineConfig({
  plugins: [react(), feedProxyPlugin()],
  server: {
    port: 3000,
  },
});
