import Dexie, { type Table } from 'dexie';
import type { Article, Source, Tag, Bookmark } from '../types';
import defaultSourcesData from '../assets/sources_default.json';

export class SereneSecDatabase extends Dexie {
  articles!: Table<Article, string>;
  sources!: Table<Source, string>;
  tags!: Table<Tag, string>;
  bookmarks!: Table<Bookmark, string>;

  constructor() {
    super('SereneSecDatabase');

    this.version(1).stores({
      articles: 'id, sourceId, category, state, publishedDate, isFavorite, isSavedForLater, createdAt',
      sources: 'id, category, isEnabled, isBuiltIn',
      tags: 'id, name',
      bookmarks: 'id, category, createdAt',
    });
  }

  async initializeDefaults() {
    const count = await this.sources.count();
    if (count > 0) return;

    try {
      const sourcesToInsert: Source[] = defaultSourcesData.sources.map((raw: any) => ({
        id: raw.id,
        name: raw.name,
        url: raw.url,
        type: raw.type as Source['type'],
        category: raw.category as Source['category'],
        isEnabled: true,
        isBuiltIn: true,
        errorCount: 0,
        successCount: 0,
        totalArticlesFetched: 0,
        description: raw.description,
        iconUrl: raw.iconUrl,
      }));

      await this.sources.bulkAdd(sourcesToInsert);
      console.log(`Initialized ${sourcesToInsert.length} built-in security sources into IndexedDB`);
    } catch (err) {
      console.error('Failed to initialize default sources into IndexedDB:', err);
    }
  }
}

export const db = new SereneSecDatabase();
