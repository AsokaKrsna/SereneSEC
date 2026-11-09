import React, { useState } from 'react';
import {
  Star,
  Bookmark,
  Archive,
  ExternalLink,
  Clock,
  Copy,
  Check,
  BookOpen,
} from 'lucide-react';
import type { Article } from '../types';
import { ThreatBadges } from './ThreatBadges';

interface ArticleCardProps {
  article: Article;
  onSelect: (article: Article) => void;
  onToggleFavorite: (article: Article) => void;
  onToggleSaveForLater: (article: Article) => void;
  onArchive: (article: Article) => void;
  onUnarchive?: (article: Article) => void;
  isCompact?: boolean;
}

function formatRelativeTime(timestamp: number): string {
  if (!timestamp) return '';
  const now = Date.now();
  const diffMs = now - timestamp;
  const diffSec = Math.floor(diffMs / 1000);
  const diffMin = Math.floor(diffSec / 60);
  const diffHours = Math.floor(diffMin / 60);
  const diffDays = Math.floor(diffHours / 24);

  if (diffMin < 1) return 'just now';
  if (diffMin < 60) return `${diffMin}m ago`;
  if (diffHours < 24) return `${diffHours}h ago`;
  if (diffDays === 1) return 'yesterday';
  if (diffDays < 7) return `${diffDays}d ago`;

  return new Date(timestamp).toLocaleDateString(undefined, {
    month: 'short',
    day: 'numeric',
  });
}

function estimateReadingTime(text?: string): string {
  if (!text) return '1m read';
  const words = text.trim().split(/\s+/).length;
  const mins = Math.max(1, Math.round(words / 150));
  return `${mins}m read`;
}

export const ArticleCard: React.FC<ArticleCardProps> = ({
  article,
  onSelect,
  onToggleFavorite,
  onToggleSaveForLater,
  onArchive,
  onUnarchive,
  isCompact = false,
}) => {
  const [copied, setCopied] = useState(false);
  const isUnread = article.state === 'UNREAD';
  const isArchived = article.state === 'ARCHIVED';

  const handleCopy = (e: React.MouseEvent) => {
    e.stopPropagation();
    if (article.contentUrl) {
      navigator.clipboard.writeText(article.contentUrl);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    }
  };

  // --- COMPACT ROW VIEW ---
  if (isCompact) {
    return (
      <div
        onClick={() => onSelect(article)}
        className={`group flex items-center justify-between p-3 rounded-xl border transition-all duration-150 cursor-pointer ${
          isUnread
            ? 'bg-white dark:bg-zinc-900/90 border-slate-300 dark:border-zinc-700/80 shadow-sm hover:border-cyan-600/50 dark:hover:border-cyan-500/50'
            : 'bg-slate-50/60 dark:bg-zinc-950/70 border-slate-200 dark:border-zinc-800/60 hover:bg-white dark:hover:bg-zinc-900/50'
        }`}
      >
        <div className="flex items-center space-x-3 min-w-0 pr-3">
          {/* Unread Indicator */}
          <span
            className={`w-2 h-2 rounded-full flex-shrink-0 ${
              isUnread
                ? 'bg-cyan-500 dark:bg-cyan-400 shadow-[0_0_8px_rgba(6,182,212,0.6)]'
                : 'bg-transparent'
            }`}
          />

          <div className="min-w-0">
            <div className="flex items-center space-x-2">
              <span className="text-[11px] font-semibold text-slate-500 dark:text-zinc-400 truncate max-w-[130px]">
                {article.sourceName || 'Feed'}
              </span>
              <ThreatBadges article={article} />
            </div>

            <h3
              className={`text-xs sm:text-sm font-semibold truncate transition-colors ${
                isUnread
                  ? 'text-slate-900 dark:text-zinc-100 group-hover:text-cyan-600 dark:group-hover:text-cyan-300'
                  : 'text-slate-700 dark:text-zinc-300 group-hover:text-slate-900 dark:group-hover:text-zinc-100'
              }`}
            >
              {article.title}
            </h3>
          </div>
        </div>

        {/* Right side controls */}
        <div
          className="flex items-center space-x-1.5 flex-shrink-0"
          onClick={(e) => e.stopPropagation()}
        >
          <span className="text-[11px] text-slate-400 dark:text-zinc-500 font-mono hidden md:inline">
            {formatRelativeTime(article.publishedDate)}
          </span>

          <button
            onClick={() => onToggleFavorite(article)}
            className={`p-1 rounded-md transition ${
              article.isFavorite
                ? 'text-amber-500 bg-amber-50 dark:bg-amber-400/10'
                : 'text-slate-400 dark:text-zinc-500 hover:text-slate-700 dark:hover:text-zinc-300 hover:bg-slate-100 dark:hover:bg-zinc-800'
            }`}
            title="Star"
          >
            <Star className="w-3.5 h-3.5" fill={article.isFavorite ? 'currentColor' : 'none'} />
          </button>

          <button
            onClick={() => onToggleSaveForLater(article)}
            className={`p-1 rounded-md transition ${
              article.isSavedForLater
                ? 'text-cyan-600 dark:text-cyan-400 bg-cyan-50 dark:bg-cyan-400/10'
                : 'text-slate-400 dark:text-zinc-500 hover:text-slate-700 dark:hover:text-zinc-300 hover:bg-slate-100 dark:hover:bg-zinc-800'
            }`}
            title="Save for later"
          >
            <Bookmark className="w-3.5 h-3.5" fill={article.isSavedForLater ? 'currentColor' : 'none'} />
          </button>

          <button
            onClick={() => (isArchived && onUnarchive ? onUnarchive(article) : onArchive(article))}
            className="p-1 rounded-md text-slate-400 dark:text-zinc-500 hover:text-slate-700 dark:hover:text-zinc-300 hover:bg-slate-100 dark:hover:bg-zinc-800 transition"
            title="Archive"
          >
            <Archive className="w-3.5 h-3.5" />
          </button>
        </div>
      </div>
    );
  }

  // --- COMFORTABLE CARD VIEW ---
  return (
    <article
      onClick={() => onSelect(article)}
      className={`group relative p-4 sm:p-5 rounded-2xl border transition-all duration-200 cursor-pointer ${
        isUnread
          ? 'bg-white dark:bg-zinc-900/90 border-slate-300 dark:border-zinc-700/80 hover:border-cyan-600/50 dark:hover:border-cyan-500/50 shadow-sm hover:shadow-md'
          : 'bg-slate-50/70 dark:bg-zinc-950/70 border-slate-200/90 dark:border-zinc-800/60 hover:border-slate-300 dark:hover:border-zinc-700 hover:bg-white dark:hover:bg-zinc-900/50'
      }`}
    >
      {/* Top Header: Source Name + Relative Time + Reading Time */}
      <div className="flex items-center justify-between text-xs text-slate-500 dark:text-zinc-400 mb-2">
        <div className="flex items-center space-x-2">
          {isUnread && (
            <span
              className="w-2 h-2 rounded-full bg-cyan-500 dark:bg-cyan-400 shadow-[0_0_8px_rgba(6,182,212,0.6)]"
              title="Unread"
            />
          )}
          <span className="font-semibold text-slate-700 dark:text-zinc-300">
            {article.sourceName || 'Security Feed'}
          </span>
          {article.author && (
            <>
              <span className="text-slate-400 dark:text-zinc-600">•</span>
              <span className="text-slate-500 dark:text-zinc-500 truncate max-w-[120px] sm:max-w-[180px]">
                {article.author}
              </span>
            </>
          )}
        </div>

        <div className="flex items-center space-x-2 text-slate-400 dark:text-zinc-500 font-mono text-[11px]">
          <span>{estimateReadingTime(article.summary)}</span>
          <span>•</span>
          <div className="flex items-center space-x-1">
            <Clock className="w-3 h-3" />
            <span>{formatRelativeTime(article.publishedDate)}</span>
          </div>
        </div>
      </div>

      {/* Threat and Category Badges */}
      <ThreatBadges article={article} />

      {/* Title */}
      <h3
        className={`text-base font-semibold leading-snug tracking-tight mb-2 transition-colors ${
          isUnread
            ? 'text-slate-900 dark:text-zinc-100 group-hover:text-cyan-600 dark:group-hover:text-cyan-300'
            : 'text-slate-700 dark:text-zinc-300 group-hover:text-slate-900 dark:group-hover:text-zinc-100'
        }`}
      >
        {article.title}
      </h3>

      {/* Summary Snippet */}
      {article.summary && (
        <p className="text-xs sm:text-sm text-slate-600 dark:text-zinc-400 line-clamp-2 leading-relaxed mb-4">
          {article.summary}
        </p>
      )}

      {/* Card Action Footer */}
      <div
        className="flex items-center justify-between pt-2 border-t border-slate-100 dark:border-zinc-800/40 text-xs"
        onClick={(e) => e.stopPropagation()}
      >
        <div className="flex items-center space-x-1.5 text-[11px] text-slate-400 dark:text-zinc-500 font-mono">
          <BookOpen className="w-3 h-3 text-cyan-500" />
          <span>Click to read</span>
        </div>

        <div className="flex items-center space-x-1 sm:space-x-1.5">
          {/* Copy link */}
          <button
            onClick={handleCopy}
            className="p-1.5 rounded-lg text-slate-400 dark:text-zinc-400 hover:text-slate-700 dark:hover:text-zinc-200 hover:bg-slate-100 dark:hover:bg-zinc-800/70 transition"
            title={copied ? 'Copied link!' : 'Copy link'}
          >
            {copied ? (
              <Check className="w-3.5 h-3.5 text-emerald-500" />
            ) : (
              <Copy className="w-3.5 h-3.5" />
            )}
          </button>

          {/* Star / Favorite */}
          <button
            onClick={() => onToggleFavorite(article)}
            className={`p-1.5 rounded-lg transition-colors ${
              article.isFavorite
                ? 'text-amber-500 bg-amber-50 dark:bg-amber-400/10'
                : 'text-slate-400 dark:text-zinc-400 hover:text-slate-700 dark:hover:text-zinc-200 hover:bg-slate-100 dark:hover:bg-zinc-800/70'
            }`}
            title={article.isFavorite ? 'Starred' : 'Star article'}
          >
            <Star
              className="w-3.5 h-3.5"
              fill={article.isFavorite ? 'currentColor' : 'none'}
            />
          </button>

          {/* Bookmark / Saved for later */}
          <button
            onClick={() => onToggleSaveForLater(article)}
            className={`p-1.5 rounded-lg transition-colors ${
              article.isSavedForLater
                ? 'text-cyan-600 dark:text-cyan-400 bg-cyan-50 dark:bg-cyan-400/10'
                : 'text-slate-400 dark:text-zinc-400 hover:text-slate-700 dark:hover:text-zinc-200 hover:bg-slate-100 dark:hover:bg-zinc-800/70'
            }`}
            title={article.isSavedForLater ? 'Saved for later' : 'Save for later'}
          >
            <Bookmark
              className="w-3.5 h-3.5"
              fill={article.isSavedForLater ? 'currentColor' : 'none'}
            />
          </button>

          {/* Archive / Unarchive */}
          <button
            onClick={() => (isArchived && onUnarchive ? onUnarchive(article) : onArchive(article))}
            className={`p-1.5 rounded-lg transition-colors ${
              isArchived
                ? 'text-slate-800 dark:text-zinc-200 bg-slate-200 dark:bg-zinc-800/90'
                : 'text-slate-400 dark:text-zinc-400 hover:text-slate-700 dark:hover:text-zinc-200 hover:bg-slate-100 dark:hover:bg-zinc-800/70'
            }`}
            title={isArchived ? 'Move back to Inbox' : 'Archive article'}
          >
            <Archive className="w-3.5 h-3.5" />
          </button>

          {/* External Link */}
          <a
            href={article.contentUrl}
            target="_blank"
            rel="noopener noreferrer"
            className="p-1.5 rounded-lg text-slate-400 dark:text-zinc-400 hover:text-slate-700 dark:hover:text-zinc-200 hover:bg-slate-100 dark:hover:bg-zinc-800/70 transition"
            title="Open original link in new tab"
          >
            <ExternalLink className="w-3.5 h-3.5" />
          </a>
        </div>
      </div>
    </article>
  );
};
