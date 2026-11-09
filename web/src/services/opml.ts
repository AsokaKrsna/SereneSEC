import type { Source, Category, SourceType } from '../types';

export function exportToOpml(sources: Source[]): string {
  const grouped = sources.reduce((acc, source) => {
    const cat = source.category || 'NEWS';
    if (!acc[cat]) acc[cat] = [];
    acc[cat].push(source);
    return acc;
  }, {} as Record<Category, Source[]>);

  let xml = `<?xml version="1.0" encoding="UTF-8"?>\n`;
  xml += `<opml version="2.0">\n`;
  xml += `  <head>\n`;
  xml += `    <title>SereneSec Feeds Export</title>\n`;
  xml += `  </head>\n`;
  xml += `  <body>\n`;

  for (const [category, catSources] of Object.entries(grouped)) {
    xml += `    <outline text="${escapeXml(category)}" title="${escapeXml(category)}">\n`;
    for (const source of catSources) {
      const type = source.type === 'GITHUB' ? 'rss' : source.type.toLowerCase();
      xml += `      <outline type="${type}" text="${escapeXml(source.name)}" title="${escapeXml(source.name)}" xmlUrl="${escapeXml(source.url)}" htmlUrl="${escapeXml(source.url)}"/>\n`;
    }
    xml += `    </outline>\n`;
  }

  xml += `  </body>\n`;
  xml += `</opml>`;

  return xml;
}

export function parseOpml(xmlText: string): Source[] {
  const parser = new DOMParser();
  const doc = parser.parseFromString(xmlText, 'text/xml');
  const sources: Source[] = [];

  const outlines = doc.querySelectorAll('outline[xmlUrl]');
  outlines.forEach((outline) => {
    const xmlUrl = outline.getAttribute('xmlUrl');
    const text = outline.getAttribute('text') || outline.getAttribute('title') || 'Unnamed Source';
    const parentOutline = outline.parentElement;
    const categoryName =
      (parentOutline?.getAttribute('text') || parentOutline?.getAttribute('title') || 'NEWS').toUpperCase() as Category;

    if (xmlUrl) {
      let type: SourceType = 'RSS';
      if (xmlUrl.includes('github.com') && xmlUrl.includes('releases.atom')) {
        type = 'GITHUB';
      } else if (xmlUrl.includes('atom')) {
        type = 'ATOM';
      }

      sources.push({
        id: 'custom-' + Math.random().toString(36).substring(2, 10),
        name: text.trim(),
        url: xmlUrl.trim(),
        type,
        category: categoryName,
        isEnabled: true,
        isBuiltIn: false,
        errorCount: 0,
        successCount: 0,
        totalArticlesFetched: 0,
      });
    }
  });

  return sources;
}

function escapeXml(unsafe: string): string {
  return unsafe.replace(/[<>&'"]/g, (c) => {
    switch (c) {
      case '<':
        return '&lt;';
      case '>':
        return '&gt;';
      case '&':
        return '&amp;';
      case '\'':
        return '&apos;';
      case '"':
        return '&quot;';
      default:
        return c;
    }
  });
}
