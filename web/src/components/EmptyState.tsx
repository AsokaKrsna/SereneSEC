import React from 'react';
import { CheckCircle2, Bookmark, Star, Archive, Search } from 'lucide-react';
import type { InboxTab } from '../types';

interface EmptyStateProps {
  tab: InboxTab;
  isSearchActive: boolean;
  searchQuery?: string;
  onClearSearch?: () => void;
}

export const EmptyState: React.FC<EmptyStateProps> = ({
  tab,
  isSearchActive,
  searchQuery,
  onClearSearch,
}) => {
  if (isSearchActive) {
    return (
      <div className="flex flex-col items-center justify-center p-12 text-center my-8 rounded-2xl border border-slate-200 dark:border-zinc-800/80 bg-white dark:bg-zinc-900/40 shadow-xs">
        <div className="w-12 h-12 rounded-full bg-slate-100 dark:bg-zinc-800/80 flex items-center justify-center text-slate-500 dark:text-zinc-400 mb-4">
          <Search className="w-6 h-6" />
        </div>
        <h3 className="text-base font-semibold text-slate-900 dark:text-zinc-200 mb-1">
          No matches found
        </h3>
        <p className="text-sm text-slate-500 dark:text-zinc-400 max-w-sm mb-4">
          We couldn't find any intelligence or CVE matching "{searchQuery}".
        </p>
        {onClearSearch && (
          <button
            onClick={onClearSearch}
            className="px-4 py-1.5 rounded-lg text-xs font-medium bg-slate-100 hover:bg-slate-200 text-slate-800 dark:bg-zinc-800 dark:text-zinc-300 dark:hover:bg-zinc-700 transition"
          >
            Clear Search
          </button>
        )}
      </div>
    );
  }

  if (tab === 'INBOX') {
    return (
      <div className="flex flex-col items-center justify-center p-12 text-center my-8 rounded-2xl border border-slate-200 dark:border-zinc-800/80 bg-white dark:bg-zinc-900/40 shadow-xs">
        <div className="w-14 h-14 rounded-full bg-cyan-50 dark:bg-cyan-950/60 border border-cyan-200 dark:border-cyan-800/40 flex items-center justify-center text-cyan-600 dark:text-cyan-400 mb-4">
          <CheckCircle2 className="w-7 h-7" />
        </div>
        <h3 className="text-lg font-semibold text-slate-900 dark:text-zinc-100 mb-1.5">
          All caught up
        </h3>
        <p className="text-sm text-slate-500 dark:text-zinc-400 max-w-md">
          You've reviewed all unread intelligence. SereneSec is designed to prevent doomscrolling—take a break, reflect, and stay focused on signal.
        </p>
      </div>
    );
  }

  if (tab === 'FAVORITES') {
    return (
      <div className="flex flex-col items-center justify-center p-12 text-center my-8 rounded-2xl border border-slate-200 dark:border-zinc-800/80 bg-white dark:bg-zinc-900/40 shadow-xs">
        <div className="w-12 h-12 rounded-full bg-amber-50 dark:bg-zinc-800/80 flex items-center justify-center text-amber-500 dark:text-amber-400 mb-4">
          <Star className="w-6 h-6" />
        </div>
        <h3 className="text-base font-semibold text-slate-900 dark:text-zinc-200 mb-1">
          No starred articles
        </h3>
        <p className="text-sm text-slate-500 dark:text-zinc-400 max-w-sm">
          Star high-impact research, advisories, or post-mortems to keep them easily accessible in your reference library.
        </p>
      </div>
    );
  }

  if (tab === 'SAVED') {
    return (
      <div className="flex flex-col items-center justify-center p-12 text-center my-8 rounded-2xl border border-slate-200 dark:border-zinc-800/80 bg-white dark:bg-zinc-900/40 shadow-xs">
        <div className="w-12 h-12 rounded-full bg-cyan-50 dark:bg-zinc-800/80 flex items-center justify-center text-cyan-600 dark:text-cyan-400 mb-4">
          <Bookmark className="w-6 h-6" />
        </div>
        <h3 className="text-base font-semibold text-slate-900 dark:text-zinc-200 mb-1">
          No saved articles
        </h3>
        <p className="text-sm text-slate-500 dark:text-zinc-400 max-w-sm">
          Save articles for later reading during your scheduled threat review or deep work blocks.
        </p>
      </div>
    );
  }

  return (
    <div className="flex flex-col items-center justify-center p-12 text-center my-8 rounded-2xl border border-slate-200 dark:border-zinc-800/80 bg-white dark:bg-zinc-900/40 shadow-xs">
      <div className="w-12 h-12 rounded-full bg-slate-100 dark:bg-zinc-800/80 flex items-center justify-center text-slate-500 dark:text-zinc-400 mb-4">
        <Archive className="w-6 h-6" />
      </div>
      <h3 className="text-base font-semibold text-slate-900 dark:text-zinc-200 mb-1">
        Archive is empty
      </h3>
      <p className="text-sm text-slate-500 dark:text-zinc-400 max-w-sm">
        Articles you archive after reading will appear here for historical reference.
      </p>
    </div>
  );
};
