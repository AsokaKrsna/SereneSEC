import React, { useState, useEffect, useMemo, useCallback, useRef } from 'react';
import type { Article, Category, InboxTab, Source } from './types';
import { db } from './db';
import { fetchFeedXml } from './services/feedFetcher';
import { parseFeedXml } from './services/feedParser';
import { Header } from './components/Header';
import { CategoryFilter } from './components/CategoryFilter';
import { ArticleCard } from './components/ArticleCard';
import { ReaderModal } from './components/ReaderModal';
import { SourceManagerModal } from './components/SourceManagerModal';
import { ShortcutsModal } from './components/ShortcutsModal';
import { EmptyState } from './components/EmptyState';
import { CheckCheck, ShieldCheck } from 'lucide-react';

export const App: React.FC = () => {
  const [articles, setArticles] = useState<Article[]>([]);
  const [sources, setSources] = useState<Source[]>([]);
  const [currentTab, setCurrentTab] = useState<InboxTab>('INBOX');
  const [selectedCategory, setSelectedCategory] = useState<Category | 'ALL'>('ALL');
  const [searchQuery, setSearchQuery] = useState('');
  const [isSyncing, setIsSyncing] = useState(false);
  const [syncStatus, setSyncStatus] = useState<string | null>(null);
  const [selectedArticle, setSelectedArticle] = useState<Article | null>(null);
  const [isSourceManagerOpen, setIsSourceManagerOpen] = useState(false);
  const [isShortcutsOpen, setIsShortcutsOpen] = useState(false);

  // Theme state: dark / light
  const [theme, setTheme] = useState<'dark' | 'light'>(() => {
    const saved = localStorage.getItem('serenesec_theme');
    if (saved === 'dark' || saved === 'light') return saved;
    return 'dark';
  });

  // Layout density: comfortable (cards) vs compact (list)
  const [isCompact, setIsCompact] = useState<boolean>(() => {
    return localStorage.getItem('serenesec_compact') === 'true';
  });

  // Active highlighted article index for keyboard navigation
  const [focusedIndex, setFocusedIndex] = useState<number>(-1);
  const articleRefs = useRef<(HTMLElement | null)[]>([]);

  // Apply theme class to root
  useEffect(() => {
    if (theme === 'dark') {
      document.documentElement.classList.add('dark');
    } else {
      document.documentElement.classList.remove('dark');
    }
    localStorage.setItem('serenesec_theme', theme);
  }, [theme]);

  // Persist compact mode
  useEffect(() => {
    localStorage.setItem('serenesec_compact', String(isCompact));
  }, [isCompact]);

  // Load articles from Dexie IndexedDB
  const reloadArticles = useCallback(async () => {
    const list = await db.articles.toArray();
    list.sort((a, b) => b.publishedDate - a.publishedDate);
    setArticles(list);
  }, []);

  // Load sources from Dexie IndexedDB
  const reloadSources = useCallback(async () => {
    const list = await db.sources.toArray();
    setSources(list);
  }, []);

  // Feed Synchronization Engine
  const syncFeeds = useCallback(async () => {
    setIsSyncing(true);
    setSyncStatus('Starting security feed sync...');

    try {
      const allSources = await db.sources.toArray();
      const activeSources = allSources.filter((s) => s.isEnabled);

      let totalNewArticles = 0;
      let completedCount = 0;

      const batchSize = 6;
      for (let i = 0; i < activeSources.length; i += batchSize) {
        const batch = activeSources.slice(i, i + batchSize);

        setSyncStatus(
          `Syncing feeds (${completedCount}/${activeSources.length})...`
        );

        await Promise.allSettled(
          batch.map(async (source) => {
            try {
              const xml = await fetchFeedXml(source.url);
              const parsed = parseFeedXml(xml, source);

              if (parsed.length > 0) {
                const ids = parsed.map((a) => a.id);
                const existing = await db.articles.where('id').anyOf(ids).keys();
                const existingSet = new Set(existing);
                const newToAdd = parsed.filter((a) => !existingSet.has(a.id));

                if (newToAdd.length > 0) {
                  await db.articles.bulkAdd(newToAdd);
                  totalNewArticles += newToAdd.length;
                }

                await db.sources.update(source.id, {
                  lastSuccessTime: Date.now(),
                  successCount: (source.successCount || 0) + 1,
                  totalArticlesFetched: (source.totalArticlesFetched || 0) + newToAdd.length,
                  errorCount: 0,
                  lastErrorMessage: undefined,
                });
              }
            } catch (err: any) {
              console.warn(`Sync failed for ${source.name}:`, err);
              await db.sources.update(source.id, {
                lastErrorTime: Date.now(),
                errorCount: (source.errorCount || 0) + 1,
                lastErrorMessage: err?.message || 'Network/feed error',
              });
            } finally {
              completedCount++;
            }
          })
        );

        // Update live state as batches finish
        if (totalNewArticles > 0) {
          await reloadArticles();
        }
      }

      await reloadArticles();
      await reloadSources();
      setSyncStatus(
        totalNewArticles > 0
          ? `Synced ${totalNewArticles} new security articles.`
          : 'Feeds are up to date.'
      );
      setTimeout(() => setSyncStatus(null), 5000);
    } catch (err: any) {
      console.error('Sync failed:', err);
      setSyncStatus('Sync encountered an issue.');
      setTimeout(() => setSyncStatus(null), 4000);
    } finally {
      setIsSyncing(false);
    }
  }, [reloadArticles, reloadSources]);

  // Initial initialization
  useEffect(() => {
    const init = async () => {
      await db.initializeDefaults();

      // Clean up any stale malformed XML errors from previous proxy runs
      const allSrcs = await db.sources.toArray();
      for (const s of allSrcs) {
        if (s.lastErrorMessage && s.lastErrorMessage.includes('Malformed')) {
          await db.sources.update(s.id, {
            errorCount: 0,
            lastErrorMessage: undefined,
          });
        }
      }

      await reloadSources();
      const existingArticles = await db.articles.count();
      if (existingArticles === 0) {
        // Initial sync on first launch
        await syncFeeds();
      } else {
        await reloadArticles();
      }
    };
    init();
  }, [reloadSources, reloadArticles, syncFeeds]);

  // Tab & count calculations
  const unreadCount = useMemo(
    () => articles.filter((a) => a.state === 'UNREAD').length,
    [articles]
  );
  const favoriteCount = useMemo(
    () => articles.filter((a) => a.isFavorite).length,
    [articles]
  );
  const savedCount = useMemo(
    () => articles.filter((a) => a.isSavedForLater).length,
    [articles]
  );

  // Category counts within current tab
  const categoryCounts = useMemo(() => {
    let baseList = articles;
    if (currentTab === 'INBOX') {
      baseList = articles.filter((a) => a.state !== 'ARCHIVED');
    } else if (currentTab === 'FAVORITES') {
      baseList = articles.filter((a) => a.isFavorite);
    } else if (currentTab === 'SAVED') {
      baseList = articles.filter((a) => a.isSavedForLater);
    } else if (currentTab === 'ARCHIVE') {
      baseList = articles.filter((a) => a.state === 'ARCHIVED');
    }

    const counts: Partial<Record<Category | 'ALL', number>> = {
      ALL: baseList.length,
    };

    baseList.forEach((a) => {
      counts[a.category] = (counts[a.category] || 0) + 1;
    });

    return counts;
  }, [articles, currentTab]);

  // Filtered and sorted article stream
  const filteredArticles = useMemo(() => {
    return articles.filter((a) => {
      // 1. Tab filter
      if (currentTab === 'INBOX' && a.state === 'ARCHIVED') return false;
      if (currentTab === 'FAVORITES' && !a.isFavorite) return false;
      if (currentTab === 'SAVED' && !a.isSavedForLater) return false;
      if (currentTab === 'ARCHIVE' && a.state !== 'ARCHIVED') return false;

      // 2. Category filter
      if (selectedCategory !== 'ALL' && a.category !== selectedCategory) {
        return false;
      }

      // 3. Search query filter
      if (searchQuery.trim()) {
        const query = searchQuery.toLowerCase().trim();
        const matchesTitle = a.title.toLowerCase().includes(query);
        const matchesSummary = a.summary?.toLowerCase().includes(query);
        const matchesAuthor = a.author?.toLowerCase().includes(query);
        const matchesSource = a.sourceName?.toLowerCase().includes(query);
        const matchesCategory = a.category.toLowerCase().includes(query);
        return matchesTitle || matchesSummary || matchesAuthor || matchesSource || matchesCategory;
      }

      return true;
    });
  }, [articles, currentTab, selectedCategory, searchQuery]);

  // Actions
  const handleToggleFavorite = async (article: Article) => {
    const updated = !article.isFavorite;
    await db.articles.update(article.id, { isFavorite: updated });
    setArticles((prev) =>
      prev.map((a) => (a.id === article.id ? { ...a, isFavorite: updated } : a))
    );
    if (selectedArticle && selectedArticle.id === article.id) {
      setSelectedArticle({ ...selectedArticle, isFavorite: updated });
    }
  };

  const handleToggleSaveForLater = async (article: Article) => {
    const updated = !article.isSavedForLater;
    await db.articles.update(article.id, { isSavedForLater: updated });
    setArticles((prev) =>
      prev.map((a) => (a.id === article.id ? { ...a, isSavedForLater: updated } : a))
    );
    if (selectedArticle && selectedArticle.id === article.id) {
      setSelectedArticle({ ...selectedArticle, isSavedForLater: updated });
    }
  };

  const handleArchive = async (article: Article) => {
    await db.articles.update(article.id, { state: 'ARCHIVED' });
    setArticles((prev) =>
      prev.map((a) => (a.id === article.id ? { ...a, state: 'ARCHIVED' } : a))
    );
    if (selectedArticle && selectedArticle.id === article.id) {
      setSelectedArticle(null);
    }
  };

  const handleUnarchive = async (article: Article) => {
    await db.articles.update(article.id, { state: 'READ' });
    setArticles((prev) =>
      prev.map((a) => (a.id === article.id ? { ...a, state: 'READ' } : a))
    );
  };

  const handleToggleReadState = async (article: Article) => {
    const nextState = article.state === 'READ' ? 'UNREAD' : 'READ';
    await db.articles.update(article.id, { state: nextState });
    setArticles((prev) =>
      prev.map((a) => (a.id === article.id ? { ...a, state: nextState } : a))
    );
    if (selectedArticle && selectedArticle.id === article.id) {
      setSelectedArticle({ ...selectedArticle, state: nextState });
    }
  };

  const handleSelectArticle = async (article: Article) => {
    if (article.state === 'UNREAD') {
      await db.articles.update(article.id, { state: 'READ' });
      const updated = { ...article, state: 'READ' as const };
      setArticles((prev) =>
        prev.map((a) => (a.id === article.id ? updated : a))
      );
      setSelectedArticle(updated);
    } else {
      setSelectedArticle(article);
    }
  };

  const handleMarkAllRead = async () => {
    const unreadIds = articles.filter((a) => a.state === 'UNREAD').map((a) => a.id);
    if (unreadIds.length === 0) return;

    for (const id of unreadIds) {
      await db.articles.update(id, { state: 'READ' });
    }
    setArticles((prev) =>
      prev.map((a) => (a.state === 'UNREAD' ? { ...a, state: 'READ' } : a))
    );
  };

  const handleUpdateArticleContent = async (articleId: string, contentHtml: string) => {
    await db.articles.update(articleId, { contentHtml });
    setArticles((prev) =>
      prev.map((a) => (a.id === articleId ? { ...a, contentHtml } : a))
    );
  };

  // Keyboard navigation shortcuts
  useEffect(() => {
    const handleGlobalKeyDown = (e: KeyboardEvent) => {
      // Do not capture if typing in an input or textarea
      const target = e.target as HTMLElement;
      if (target.tagName === 'INPUT' || target.tagName === 'TEXTAREA' || target.tagName === 'SELECT') {
        return;
      }
      if (selectedArticle || isSourceManagerOpen || isShortcutsOpen) {
        return;
      }

      if (e.key === 'j' || e.key === 'ArrowDown') {
        e.preventDefault();
        setFocusedIndex((prev) => {
          const next = Math.min(prev + 1, filteredArticles.length - 1);
          articleRefs.current[next]?.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
          return next;
        });
      } else if (e.key === 'k' || e.key === 'ArrowUp') {
        e.preventDefault();
        setFocusedIndex((prev) => {
          const next = Math.max(prev - 1, 0);
          articleRefs.current[next]?.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
          return next;
        });
      } else if (e.key === 'Enter' || e.key === 'o') {
        if (focusedIndex >= 0 && focusedIndex < filteredArticles.length) {
          handleSelectArticle(filteredArticles[focusedIndex]);
        }
      } else if (e.key === 's') {
        if (focusedIndex >= 0 && focusedIndex < filteredArticles.length) {
          handleToggleFavorite(filteredArticles[focusedIndex]);
        }
      } else if (e.key === 'b') {
        if (focusedIndex >= 0 && focusedIndex < filteredArticles.length) {
          handleToggleSaveForLater(filteredArticles[focusedIndex]);
        }
      } else if (e.key === 'e') {
        if (focusedIndex >= 0 && focusedIndex < filteredArticles.length) {
          handleArchive(filteredArticles[focusedIndex]);
        }
      } else if (e.key === 't') {
        setTheme((prev) => (prev === 'dark' ? 'light' : 'dark'));
      } else if (e.key === 'v') {
        setIsCompact((prev) => !prev);
      } else if (e.key === '?') {
        setIsShortcutsOpen(true);
      }
    };

    window.addEventListener('keydown', handleGlobalKeyDown);
    return () => window.removeEventListener('keydown', handleGlobalKeyDown);
  }, [
    filteredArticles,
    focusedIndex,
    selectedArticle,
    isSourceManagerOpen,
    isShortcutsOpen,
  ]);

  return (
    <div className="min-h-screen bg-[#f8fafc] dark:bg-[#0d0f12] text-slate-900 dark:text-zinc-100 flex flex-col selection:bg-cyan-500/30 selection:text-cyan-900 dark:selection:text-cyan-200 transition-colors">
      {/* Top Header */}
      <Header
        currentTab={currentTab}
        onSelectTab={setCurrentTab}
        unreadCount={unreadCount}
        favoriteCount={favoriteCount}
        savedCount={savedCount}
        isSyncing={isSyncing}
        onSync={syncFeeds}
        onOpenSourceManager={() => setIsSourceManagerOpen(true)}
        searchQuery={searchQuery}
        onSearchChange={setSearchQuery}
        theme={theme}
        onToggleTheme={() => setTheme((prev) => (prev === 'dark' ? 'light' : 'dark'))}
        isCompact={isCompact}
        onToggleCompact={() => setIsCompact((prev) => !prev)}
        onOpenShortcuts={() => setIsShortcutsOpen(true)}
      />

      {/* Main Body */}
      <main className="flex-1 max-w-6xl w-full mx-auto px-4 sm:px-6 py-4">
        {/* Sync Status Toast/Notification */}
        {syncStatus && (
          <div className="mb-4 px-4 py-2 rounded-xl bg-cyan-50 dark:bg-cyan-950/60 border border-cyan-200 dark:border-cyan-800/40 text-xs text-cyan-800 dark:text-cyan-300 flex items-center space-x-2 animate-fadeIn">
            <ShieldCheck className="w-4 h-4 text-cyan-600 dark:text-cyan-400" />
            <span>{syncStatus}</span>
          </div>
        )}

        {/* Category Filter Pills */}
        <div className="mb-3">
          <CategoryFilter
            selectedCategory={selectedCategory}
            onSelectCategory={setSelectedCategory}
            categoryCounts={categoryCounts}
          />
        </div>

        {/* Sub-bar: Stream info & Mark all read */}
        <div className="flex items-center justify-between py-2 mb-3 text-xs text-slate-500 dark:text-zinc-500 border-b border-slate-200/80 dark:border-zinc-900">
          <div className="flex items-center space-x-2">
            <span>Showing {filteredArticles.length} items</span>
            {selectedCategory !== 'ALL' && (
              <span className="font-semibold text-cyan-600 dark:text-cyan-400">
                in {selectedCategory}
              </span>
            )}
            <span className="text-slate-300 dark:text-zinc-700 hidden sm:inline">|</span>
            <span className="text-[11px] text-slate-400 dark:text-zinc-600 hidden sm:inline">
              Press <kbd className="px-1 rounded bg-slate-100 dark:bg-zinc-800 border border-slate-200 dark:border-zinc-700 font-mono">?</kbd> for shortcuts
            </span>
          </div>

          <div className="flex items-center space-x-3">
            {currentTab === 'INBOX' && unreadCount > 0 && (
              <button
                onClick={handleMarkAllRead}
                className="inline-flex items-center space-x-1 text-slate-600 hover:text-cyan-600 dark:text-zinc-400 dark:hover:text-cyan-400 font-medium transition"
                title="Mark all unread items as read"
              >
                <CheckCheck className="w-3.5 h-3.5" />
                <span>Mark all read</span>
              </button>
            )}
          </div>
        </div>

        {/* Article Cards Grid / List */}
        {filteredArticles.length === 0 ? (
          <EmptyState
            tab={currentTab}
            isSearchActive={searchQuery.length > 0}
            searchQuery={searchQuery}
            onClearSearch={() => setSearchQuery('')}
          />
        ) : (
          <div
            className={
              isCompact
                ? 'space-y-2'
                : 'grid grid-cols-1 md:grid-cols-2 gap-3.5 sm:gap-4'
            }
          >
            {filteredArticles.map((article, idx) => (
              <div
                key={article.id}
                ref={(el) => (articleRefs.current[idx] = el)}
                className={`transition-all ${
                  focusedIndex === idx
                    ? 'ring-2 ring-cyan-500 rounded-2xl'
                    : ''
                }`}
              >
                <ArticleCard
                  article={article}
                  onSelect={handleSelectArticle}
                  onToggleFavorite={handleToggleFavorite}
                  onToggleSaveForLater={handleToggleSaveForLater}
                  onArchive={handleArchive}
                  onUnarchive={handleUnarchive}
                  isCompact={isCompact}
                />
              </div>
            ))}
          </div>
        )}
      </main>

      {/* Reader Modal */}
      <ReaderModal
        article={selectedArticle}
        onClose={() => setSelectedArticle(null)}
        onToggleFavorite={handleToggleFavorite}
        onToggleSaveForLater={handleToggleSaveForLater}
        onArchive={handleArchive}
        onToggleReadState={handleToggleReadState}
        onUpdateArticleContent={handleUpdateArticleContent}
      />

      {/* Source Manager Modal */}
      <SourceManagerModal
        isOpen={isSourceManagerOpen}
        onClose={() => setIsSourceManagerOpen(false)}
        onSourcesUpdated={() => {
          reloadSources();
          reloadArticles();
        }}
      />

      {/* Keyboard Shortcuts Sheet */}
      <ShortcutsModal
        isOpen={isShortcutsOpen}
        onClose={() => setIsShortcutsOpen(false)}
      />
    </div>
  );
};

export default App;
