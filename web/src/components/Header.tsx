import React from 'react';
import {
  Shield,
  RefreshCw,
  SlidersHorizontal,
  Search,
  X,
  Inbox,
  Star,
  Bookmark,
  Archive,
  Sun,
  Moon,
  LayoutGrid,
  List,
  Keyboard,
} from 'lucide-react';
import type { InboxTab } from '../types';

interface HeaderProps {
  currentTab: InboxTab;
  onSelectTab: (tab: InboxTab) => void;
  unreadCount: number;
  favoriteCount: number;
  savedCount: number;
  isSyncing: boolean;
  onSync: () => void;
  onOpenSourceManager: () => void;
  searchQuery: string;
  onSearchChange: (query: string) => void;
  theme: 'dark' | 'light';
  onToggleTheme: () => void;
  isCompact: boolean;
  onToggleCompact: () => void;
  onOpenShortcuts: () => void;
}

export const Header: React.FC<HeaderProps> = ({
  currentTab,
  onSelectTab,
  unreadCount,
  favoriteCount,
  savedCount,
  isSyncing,
  onSync,
  onOpenSourceManager,
  searchQuery,
  onSearchChange,
  theme,
  onToggleTheme,
  isCompact,
  onToggleCompact,
  onOpenShortcuts,
}) => {
  return (
    <header className="sticky top-0 z-30 bg-white/95 dark:bg-zinc-950/95 backdrop-blur-md border-b border-slate-200/90 dark:border-zinc-800/80 transition-colors">
      <div className="max-w-6xl mx-auto px-4 sm:px-6 py-3">
        {/* Top Tier: Logo, App Title & Primary Actions */}
        <div className="flex items-center justify-between gap-4">
          {/* Brand */}
          <div className="flex items-center space-x-3">
            <div className="w-9 h-9 rounded-xl bg-slate-900 dark:bg-cyan-500/20 text-cyan-400 border border-slate-800 dark:border-cyan-500/40 flex items-center justify-center shadow-sm">
              <Shield className="w-5 h-5 text-white dark:text-cyan-400" />
            </div>
            <div>
              <div className="flex items-center space-x-2">
                <h1 className="text-lg font-bold tracking-tight text-slate-900 dark:text-zinc-100">
                  SereneSec
                </h1>
                <span className="text-[10px] font-mono px-1.5 py-0.5 rounded bg-slate-100 text-slate-600 border border-slate-200 dark:bg-zinc-900 dark:border-zinc-800 dark:text-cyan-400">
                  v1.1
                </span>
              </div>
              <p className="text-xs text-slate-500 dark:text-zinc-500 hidden sm:block">
                Distraction-Free Security & Web3 Threat Intelligence
              </p>
            </div>
          </div>

          {/* Right Header Actions */}
          <div className="flex items-center space-x-1.5 sm:space-x-2">
            {/* View Mode Toggle (Compact vs Grid) */}
            <button
              onClick={onToggleCompact}
              className="p-2 rounded-xl text-slate-500 hover:text-slate-900 dark:text-zinc-400 dark:hover:text-zinc-200 bg-slate-100 hover:bg-slate-200/80 dark:bg-zinc-900 dark:hover:bg-zinc-800 border border-slate-200/80 dark:border-zinc-700/60 transition"
              title={isCompact ? 'Switch to Card view' : 'Switch to Compact list view'}
            >
              {isCompact ? <LayoutGrid className="w-4 h-4" /> : <List className="w-4 h-4" />}
            </button>

            {/* Theme Toggle (Dark / Light) */}
            <button
              onClick={onToggleTheme}
              className="p-2 rounded-xl text-slate-500 hover:text-slate-900 dark:text-zinc-400 dark:hover:text-zinc-200 bg-slate-100 hover:bg-slate-200/80 dark:bg-zinc-900 dark:hover:bg-zinc-800 border border-slate-200/80 dark:border-zinc-700/60 transition"
              title={`Switch to ${theme === 'dark' ? 'Light' : 'Dark'} mode`}
            >
              {theme === 'dark' ? (
                <Sun className="w-4 h-4 text-amber-400" />
              ) : (
                <Moon className="w-4 h-4 text-slate-700" />
              )}
            </button>

            {/* Keyboard Shortcuts Dialog */}
            <button
              onClick={onOpenShortcuts}
              className="p-2 rounded-xl text-slate-500 hover:text-slate-900 dark:text-zinc-400 dark:hover:text-zinc-200 bg-slate-100 hover:bg-slate-200/80 dark:bg-zinc-900 dark:hover:bg-zinc-800 border border-slate-200/80 dark:border-zinc-700/60 transition hidden md:flex"
              title="Keyboard shortcuts (?)"
            >
              <Keyboard className="w-4 h-4" />
            </button>

            {/* Sync Feeds Button */}
            <button
              onClick={onSync}
              disabled={isSyncing}
              className={`px-3 py-1.5 rounded-xl text-xs font-medium flex items-center space-x-1.5 transition ${
                isSyncing
                  ? 'bg-slate-100 dark:bg-zinc-800 text-slate-400 dark:text-zinc-500 cursor-not-allowed'
                  : 'bg-slate-900 text-white hover:bg-slate-800 dark:bg-zinc-800 dark:hover:bg-zinc-700 dark:text-zinc-200 border border-slate-900 dark:border-zinc-700/60 shadow-sm'
              }`}
              title="Sync all enabled security sources"
            >
              <RefreshCw
                className={`w-3.5 h-3.5 ${
                  isSyncing ? 'animate-spin' : ''
                } text-cyan-400`}
              />
              <span className="hidden sm:inline">
                {isSyncing ? 'Syncing...' : 'Sync'}
              </span>
            </button>

            {/* Manage Feeds Button */}
            <button
              onClick={onOpenSourceManager}
              className="px-3 py-1.5 rounded-xl text-xs font-medium bg-slate-100 hover:bg-slate-200/80 text-slate-700 dark:bg-zinc-800 dark:hover:bg-zinc-700 dark:text-zinc-200 border border-slate-200 dark:border-zinc-700/60 flex items-center space-x-1.5 transition"
              title="Manage security sources and OPML"
            >
              <SlidersHorizontal className="w-3.5 h-3.5 text-slate-500 dark:text-zinc-400" />
              <span className="hidden sm:inline">Sources</span>
            </button>
          </div>
        </div>

        {/* Lower Tier: Navigation Tabs & Search Input */}
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 mt-3 pt-2 border-t border-slate-200/60 dark:border-zinc-800/50">
          {/* Navigation Tabs */}
          <nav className="flex items-center space-x-1 overflow-x-auto scrollbar-none no-scrollbar">
            {/* Inbox Tab */}
            <button
              onClick={() => onSelectTab('INBOX')}
              className={`px-3 py-1.5 rounded-lg text-xs font-medium flex items-center space-x-1.5 transition ${
                currentTab === 'INBOX'
                  ? 'bg-slate-900 text-white dark:bg-cyan-500/15 dark:text-cyan-400 dark:border dark:border-cyan-500/30 shadow-sm'
                  : 'text-slate-600 dark:text-zinc-400 hover:text-slate-900 dark:hover:text-zinc-200 hover:bg-slate-100 dark:hover:bg-zinc-800/60'
              }`}
            >
              <Inbox className="w-3.5 h-3.5" />
              <span>Inbox</span>
              {unreadCount > 0 && (
                <span className="px-1.5 py-0.2 rounded-full text-[10px] bg-slate-800 text-slate-200 dark:bg-cyan-900/60 dark:text-cyan-300 font-mono">
                  {unreadCount}
                </span>
              )}
            </button>

            {/* Favorites Tab */}
            <button
              onClick={() => onSelectTab('FAVORITES')}
              className={`px-3 py-1.5 rounded-lg text-xs font-medium flex items-center space-x-1.5 transition ${
                currentTab === 'FAVORITES'
                  ? 'bg-amber-100 text-amber-900 border border-amber-300 dark:bg-amber-500/15 dark:text-amber-400 dark:border-amber-500/30'
                  : 'text-slate-600 dark:text-zinc-400 hover:text-slate-900 dark:hover:text-zinc-200 hover:bg-slate-100 dark:hover:bg-zinc-800/60'
              }`}
            >
              <Star className="w-3.5 h-3.5" />
              <span>Favorites</span>
              {favoriteCount > 0 && (
                <span className="px-1.5 py-0.2 rounded-full text-[10px] bg-amber-200 text-amber-900 dark:bg-amber-900/60 dark:text-amber-300 font-mono">
                  {favoriteCount}
                </span>
              )}
            </button>

            {/* Saved Tab */}
            <button
              onClick={() => onSelectTab('SAVED')}
              className={`px-3 py-1.5 rounded-lg text-xs font-medium flex items-center space-x-1.5 transition ${
                currentTab === 'SAVED'
                  ? 'bg-teal-100 text-teal-900 border border-teal-300 dark:bg-teal-500/15 dark:text-teal-400 dark:border-teal-500/30'
                  : 'text-slate-600 dark:text-zinc-400 hover:text-slate-900 dark:hover:text-zinc-200 hover:bg-slate-100 dark:hover:bg-zinc-800/60'
              }`}
            >
              <Bookmark className="w-3.5 h-3.5" />
              <span>Saved</span>
              {savedCount > 0 && (
                <span className="px-1.5 py-0.2 rounded-full text-[10px] bg-teal-200 text-teal-900 dark:bg-teal-900/60 dark:text-teal-300 font-mono">
                  {savedCount}
                </span>
              )}
            </button>

            {/* Archive Tab */}
            <button
              onClick={() => onSelectTab('ARCHIVE')}
              className={`px-3 py-1.5 rounded-lg text-xs font-medium flex items-center space-x-1.5 transition ${
                currentTab === 'ARCHIVE'
                  ? 'bg-slate-200 text-slate-900 dark:bg-zinc-800 dark:text-zinc-100 border border-slate-300 dark:border-zinc-700'
                  : 'text-slate-600 dark:text-zinc-400 hover:text-slate-900 dark:hover:text-zinc-200 hover:bg-slate-100 dark:hover:bg-zinc-800/60'
              }`}
            >
              <Archive className="w-3.5 h-3.5" />
              <span>Archive</span>
            </button>
          </nav>

          {/* Quick Search */}
          <div className="relative w-full sm:w-64">
            <Search className="w-3.5 h-3.5 absolute left-3 top-1/2 -translate-y-1/2 text-slate-400 dark:text-zinc-400 pointer-events-none" />
            <input
              type="text"
              value={searchQuery}
              onChange={(e) => onSearchChange(e.target.value)}
              placeholder="Filter by CVE, title, keyword... (/)"
              className="w-full bg-slate-100 dark:bg-zinc-900/80 border border-slate-200 dark:border-zinc-700/60 rounded-xl pl-8 pr-7 py-1.5 text-xs text-slate-800 dark:text-zinc-200 placeholder-slate-400 dark:placeholder-zinc-500 focus:outline-none focus:border-cyan-600 dark:focus:border-cyan-500 focus:ring-1 focus:ring-cyan-500/30 transition"
            />
            {searchQuery && (
              <button
                onClick={() => onSearchChange('')}
                className="absolute right-2.5 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600 dark:text-zinc-400 dark:hover:text-zinc-200"
              >
                <X className="w-3.5 h-3.5" />
              </button>
            )}
          </div>
        </div>
      </div>
    </header>
  );
};
