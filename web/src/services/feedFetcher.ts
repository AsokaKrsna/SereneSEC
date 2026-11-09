/**
 * Service to fetch feeds through Cloudflare Pages edge proxy or local Vite proxy
 */

export async function fetchFeedXml(url: string, isWebPage: boolean = false): Promise<string> {
  const proxyEndpoint = `/api/feed?url=${encodeURIComponent(url)}${isWebPage ? '&mode=webview' : ''}`;

  try {
    const res = await fetch(proxyEndpoint);
    if (res.ok) {
      return await res.text();
    }
    throw new Error(`Proxy error ${res.status}: ${res.statusText}`);
  } catch (err) {
    console.warn(`Edge proxy fetch failed for ${url}, trying fallback CORS proxy:`, err);
    try {
      const fallbackUrl = `https://api.allorigins.win/raw?url=${encodeURIComponent(url)}`;
      const fallbackRes = await fetch(fallbackUrl);
      if (fallbackRes.ok) {
        return await fallbackRes.text();
      }
    } catch (fallbackErr) {
      console.error('All fetch strategies failed:', fallbackErr);
    }
    throw err;
  }
}
