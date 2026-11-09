/**
 * Cloudflare Pages Function: /api/feed
 * Zero-cost Edge CORS Proxy for RSS/Atom feeds and Reader WebView
 */

interface Env {
  // Add environment variables here if needed
}

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

export const onRequestGet = async (context: { request: Request; env: Env }) => {
  const reqUrl = new URL(context.request.url);
  const urlParam = reqUrl.searchParams.get('url');
  const isWebView = reqUrl.searchParams.get('mode') === 'webview';

  if (!urlParam) {
    return new Response(JSON.stringify({ error: 'Missing "url" query parameter' }), {
      status: 400,
      headers: {
        'Content-Type': 'application/json',
        'Access-Control-Allow-Origin': '*',
      },
    });
  }

  let targetUrl: URL;
  try {
    targetUrl = new URL(urlParam);
    if (targetUrl.protocol !== 'http:' && targetUrl.protocol !== 'https:') {
      throw new Error('Invalid protocol');
    }
  } catch (err) {
    return new Response(JSON.stringify({ error: 'Invalid URL provided' }), {
      status: 400,
      headers: {
        'Content-Type': 'application/json',
        'Access-Control-Allow-Origin': '*',
      },
    });
  }

  try {
    const upstreamResponse = await fetch(targetUrl.toString(), {
      headers: {
        'User-Agent':
          'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36 SereneSec/1.0',
        Accept: isWebView
          ? 'text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8'
          : 'application/rss+xml, application/atom+xml, application/xml, text/xml, text/html, */*',
        'Accept-Language': 'en-US,en;q=0.9',
      },
    });

    if (!upstreamResponse.ok) {
      if (isWebView) {
        return new Response(generateFallbackHtml(targetUrl.toString(), upstreamResponse.status), {
          status: 200,
          headers: {
            'Content-Type': 'text/html; charset=utf-8',
            'Access-Control-Allow-Origin': '*',
          },
        });
      }

      return new Response(
        JSON.stringify({
          error: `Upstream feed returned status ${upstreamResponse.status}: ${upstreamResponse.statusText}`,
        }),
        {
          status: upstreamResponse.status,
          headers: {
            'Content-Type': 'application/json',
            'Access-Control-Allow-Origin': '*',
          },
        }
      );
    }

    const contentType =
      upstreamResponse.headers.get('content-type') || 'application/xml; charset=utf-8';
    let textBody = await upstreamResponse.text();

    if (isWebView || contentType.includes('text/html')) {
      const baseTag = `<base href="${targetUrl.toString()}">`;
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

      if (textBody.includes('<head>')) {
        textBody = textBody.replace('<head>', `<head>${baseTag}${overrides}`);
      } else if (textBody.includes('<head ')) {
        textBody = textBody.replace(/<head[^>]*>/, `$&${baseTag}${overrides}`);
      } else {
        textBody = baseTag + overrides + textBody;
      }

      return new Response(textBody, {
        status: 200,
        headers: {
          'Content-Type': 'text/html; charset=utf-8',
          'Access-Control-Allow-Origin': '*',
        },
      });
    }

    return new Response(textBody, {
      status: 200,
      headers: {
        'Content-Type': contentType,
        'Access-Control-Allow-Origin': '*',
        'Cache-Control': 'public, max-age=300, stale-while-revalidate=600',
      },
    });
  } catch (err: any) {
    if (isWebView) {
      return new Response(generateFallbackHtml(targetUrl.toString(), 502), {
        status: 200,
        headers: {
          'Content-Type': 'text/html; charset=utf-8',
          'Access-Control-Allow-Origin': '*',
        },
      });
    }

    return new Response(
      JSON.stringify({ error: `Failed to fetch feed: ${err?.message || 'Unknown error'}` }),
      {
        status: 502,
        headers: {
          'Content-Type': 'application/json',
          'Access-Control-Allow-Origin': '*',
        },
      }
    );
  }
};

export const onRequestOptions = async () => {
  return new Response(null, {
    status: 204,
    headers: {
      'Access-Control-Allow-Origin': '*',
      'Access-Control-Allow-Methods': 'GET, OPTIONS',
      'Access-Control-Allow-Headers': '*',
    },
  });
};
