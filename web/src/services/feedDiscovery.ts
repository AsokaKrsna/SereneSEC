import type { SourceType } from '../types';
import { fetchFeedXml } from './feedFetcher';

export interface DiscoveredFeed {
  feedUrl: string;
  title?: string;
  type: SourceType;
}

export async function discoverFeed(inputUrl: string): Promise<DiscoveredFeed> {
  let url = inputUrl.trim();
  if (!url.startsWith('http://') && !url.startsWith('https://')) {
    url = 'https://' + url;
  }

  const lower = url.toLowerCase();
  if (lower.endsWith('.xml') || lower.endsWith('.rss') || lower.endsWith('/feed') || lower.endsWith('/feed/')) {
    return {
      feedUrl: url,
      type: lower.includes('atom') ? 'ATOM' : 'RSS',
    };
  }

  // Handle GitHub repo URL: github.com/user/repo -> github.com/user/repo/releases.atom
  const githubMatch = url.match(/^https?:\/\/(www\.)?github\.com\/([^/]+)\/([^/]+)\/?$/i);
  if (githubMatch) {
    const [, , user, repo] = githubMatch;
    return {
      feedUrl: `https://github.com/${user}/${repo}/releases.atom`,
      title: `${repo} Releases`,
      type: 'GITHUB',
    };
  }

  try {
    const htmlText = await fetchFeedXml(url);
    const parser = new DOMParser();
    const doc = parser.parseFromString(htmlText, 'text/html');

    const pageTitle = doc.querySelector('title')?.textContent?.trim();

    // Look for <link rel="alternate" type="application/rss+xml" href="...">
    const linkTag =
      doc.querySelector('link[type="application/rss+xml"]') ||
      doc.querySelector('link[type="application/atom+xml"]');

    if (linkTag) {
      const rawHref = linkTag.getAttribute('href');
      if (rawHref) {
        const resolved = new URL(rawHref, url).toString();
        const tagTitle = linkTag.getAttribute('title')?.trim();
        const type =
          linkTag.getAttribute('type')?.includes('atom') || resolved.includes('atom')
            ? 'ATOM'
            : 'RSS';

        return {
          feedUrl: resolved,
          title: tagTitle || pageTitle,
          type,
        };
      }
    }

    return {
      feedUrl: url,
      title: pageTitle,
      type: 'RSS',
    };
  } catch (err) {
    return {
      feedUrl: url,
      type: 'RSS',
    };
  }
}
