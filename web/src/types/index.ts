export type SourceType = 'RSS' | 'ATOM' | 'GITHUB' | 'API';

export type Category =
  | 'NEWS'
  | 'TOOLS'
  | 'CVE'
  | 'RESEARCH'
  | 'THREAT_INTEL'
  | 'BLOCKCHAIN'
  | 'GOV_ADVISORY'
  | 'MALWARE'
  | 'EXPLOITS'
  | 'PROGRAMS'
  | 'WRITEUPS'
  | 'PODCAST'
  | 'LEARNING'
  | 'DEFENSE'
  | 'CLOUD'
  | 'TRADECRAFT'
  | 'INCIDENTS'
  | 'CONFERENCE';

export type ArticleState = 'UNREAD' | 'READ' | 'ARCHIVED';

export interface Source {
  id: string;
  name: string;
  url: string;
  type: SourceType;
  category: Category;
  isEnabled: boolean;
  isBuiltIn: boolean;
  lastFetchTime?: number;
  lastSuccessTime?: number;
  lastErrorTime?: number;
  lastErrorMessage?: string;
  errorCount: number;
  successCount: number;
  totalArticlesFetched: number;
  description?: string;
  iconUrl?: string;
}

export interface Article {
  id: string;
  sourceId: string;
  sourceName?: string;
  title: string;
  summary?: string;
  contentUrl: string;
  publishedDate: number;
  state: ArticleState;
  category: Category;
  contentHtml?: string;
  author?: string;
  imageUrl?: string;
  createdAt: number;
  isFavorite: boolean;
  isSavedForLater: boolean;
}

export interface Tag {
  id: string;
  name: string;
  colorHex: string;
}

export interface Bookmark {
  id: string;
  title: string;
  url: string;
  description?: string;
  category: Category;
  createdAt: number;
}

export type TimeRange = 'TODAY' | 'THIS_WEEK' | 'THIS_MONTH' | 'ALL_TIME';
export type InboxTab = 'INBOX' | 'FAVORITES' | 'SAVED' | 'ARCHIVE';
export type SortOption = 'NEWEST' | 'OLDEST' | 'TITLE_AZ' | 'BY_SOURCE';
