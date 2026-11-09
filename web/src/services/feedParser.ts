import type { Article, Source } from '../types';

function createHash(str: string): string {
  let hash = 0;
  for (let i = 0; i < str.length; i++) {
    const char = str.charCodeAt(i);
    hash = (hash << 5) - hash + char;
    hash |= 0;
  }
  return 'art-' + Math.abs(hash).toString(16);
}

function parseDate(dateStr: string | null): number {
  if (!dateStr) return Date.now();
  const parsed = Date.parse(dateStr);
  return isNaN(parsed) ? Date.now() : parsed;
}

function cleanText(text: string): string {
  return text
    .replace(/<!\[CDATA\[(.*?)\]\]>/gs, '$1')
    .replace(/<[^>]*>?/gm, '')
    .replace(/&amp;/g, '&')
    .replace(/&lt;/g, '<')
    .replace(/&gt;/g, '>')
    .replace(/&quot;/g, '"')
    .replace(/&#039;/g, "'")
    .replace(/&#8217;/g, "'")
    .replace(/&#8216;/g, "'")
    .replace(/&#8220;/g, '"')
    .replace(/&#8221;/g, '"')
    .replace(/&nbsp;/g, ' ')
    .trim();
}

export function parseFeedXml(xmlText: string, source: Source): Article[] {
  if (!xmlText || typeof xmlText !== 'string') return [];

  const parser = new DOMParser();
  let doc: Document;

  try {
    doc = parser.parseFromString(xmlText, 'text/xml');
    const hasParserError = doc.querySelector('parsererror');
    if (hasParserError && doc.querySelectorAll('item, entry').length === 0) {
      doc = parser.parseFromString(xmlText, 'text/html');
    }
  } catch {
    doc = parser.parseFromString(xmlText, 'text/html');
  }

  const articles: Article[] = [];

  // 1. Try RSS <item> tags
  const items = doc.querySelectorAll('item');
  if (items.length > 0) {
    items.forEach((item) => {
      const rawTitle = item.querySelector('title')?.textContent || '';
      const title = cleanText(rawTitle) || 'Untitled Intelligence';

      const linkElem = item.querySelector('link');
      const link =
        linkElem?.textContent?.trim() ||
        linkElem?.getAttribute('href')?.trim() ||
        item.querySelector('guid')?.textContent?.trim() ||
        '';

      const descElem =
        item.querySelector('description') ||
        item.querySelector('summary') ||
        item.getElementsByTagNameNS('*', 'encoded')[0];

      const rawSummary = descElem?.textContent || '';
      const summary = cleanText(rawSummary).slice(0, 280);

      const pubDate = parseDate(
        item.querySelector('pubDate')?.textContent ||
          item.querySelector('date')?.textContent ||
          item.querySelector('dc\\:date')?.textContent ||
          null
      );

      const author =
        cleanText(
          item.querySelector('author')?.textContent ||
            item.getElementsByTagNameNS('*', 'creator')[0]?.textContent ||
            ''
        ) || source.name;

      if (link && link.startsWith('http')) {
        articles.push({
          id: createHash(link + title),
          sourceId: source.id,
          sourceName: source.name,
          title,
          summary,
          contentUrl: link,
          publishedDate: pubDate,
          state: 'UNREAD',
          category: source.category,
          contentHtml: rawSummary,
          author,
          createdAt: Date.now(),
          isFavorite: false,
          isSavedForLater: false,
        });
      }
    });

    if (articles.length > 0) return articles;
  }

  // 2. Try Atom <entry> tags
  const entries = doc.querySelectorAll('entry');
  if (entries.length > 0) {
    entries.forEach((entry) => {
      const rawTitle = entry.querySelector('title')?.textContent || '';
      const title = cleanText(rawTitle) || 'Untitled Intelligence';

      const linkElem =
        entry.querySelector('link[rel="alternate"]') ||
        entry.querySelector('link[type*="html"]') ||
        entry.querySelector('link');

      let link =
        linkElem?.getAttribute('href')?.trim() ||
        linkElem?.textContent?.trim() ||
        entry.querySelector('id')?.textContent?.trim() ||
        '';

      const contentElem = entry.querySelector('content') || entry.querySelector('summary');
      const rawContent = contentElem?.textContent || '';
      const summary = cleanText(rawContent).slice(0, 280);

      const pubDate = parseDate(
        entry.querySelector('published')?.textContent ||
          entry.querySelector('updated')?.textContent ||
          null
      );

      const author =
        cleanText(entry.querySelector('author name, author')?.textContent || '') || source.name;

      if (link && link.startsWith('http')) {
        articles.push({
          id: createHash(link + title),
          sourceId: source.id,
          sourceName: source.name,
          title,
          summary,
          contentUrl: link,
          publishedDate: pubDate,
          state: 'UNREAD',
          category: source.category,
          contentHtml: rawContent,
          author,
          createdAt: Date.now(),
          isFavorite: false,
          isSavedForLater: false,
        });
      }
    });

    return articles;
  }

  return articles;
}
