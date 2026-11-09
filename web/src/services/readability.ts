import { Readability } from '@mozilla/readability';
import DOMPurify from 'dompurify';
import { fetchFeedXml } from './feedFetcher';

export interface CleanArticleResult {
  title: string;
  byline?: string;
  content: string;
  textContent: string;
  excerpt?: string;
  siteName?: string;
}

export async function extractReadableArticle(
  url: string,
  fallbackHtml?: string
): Promise<CleanArticleResult> {
  let htmlToParse = fallbackHtml || '';

  if (!htmlToParse || htmlToParse.length < 500) {
    try {
      htmlToParse = await fetchFeedXml(url, true);
    } catch (err) {
      console.warn('Could not fetch upstream page for readability, using fallback:', err);
    }
  }

  const parser = new DOMParser();
  const doc = parser.parseFromString(htmlToParse, 'text/html');

  // Strip cookie banners, script tags, consent dialogs before parsing
  const junkSelectors = [
    '.cookie',
    '#cookie',
    '[class*="consent"]',
    '[class*="cookie"]',
    '.modal-overlay',
    '#onetrust-consent-sdk',
    'script',
    'style',
    'noscript',
  ];
  junkSelectors.forEach((sel) => {
    doc.querySelectorAll(sel).forEach((el) => el.remove());
  });

  const reader = new Readability(doc);
  const parsed = reader.parse();

  if (parsed && parsed.content) {
    const cleanContent = DOMPurify.sanitize(parsed.content, {
      ALLOWED_TAGS: [
        'h1',
        'h2',
        'h3',
        'h4',
        'p',
        'b',
        'i',
        'strong',
        'em',
        'a',
        'pre',
        'code',
        'blockquote',
        'ul',
        'ol',
        'li',
        'img',
        'table',
        'thead',
        'tbody',
        'tr',
        'th',
        'td',
      ],
      ALLOWED_ATTR: ['href', 'src', 'alt', 'title', 'target', 'class'],
    });

    return {
      title: parsed.title,
      byline: parsed.byline || undefined,
      content: cleanContent,
      textContent: parsed.textContent,
      excerpt: parsed.excerpt || undefined,
      siteName: parsed.siteName || undefined,
    };
  }

  // Fallback: sanitized raw HTML
  return {
    title: doc.title || 'Article View',
    content: DOMPurify.sanitize(htmlToParse),
    textContent: doc.body.textContent || '',
  };
}
