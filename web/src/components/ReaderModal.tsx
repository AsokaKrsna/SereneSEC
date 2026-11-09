import React, { useEffect, useState, useRef } from 'react';
import {
  X,
  Star,
  Bookmark,
  Archive,
  ExternalLink,
  BookOpen,
  Globe,
  Loader2,
  CheckCircle2,
  Copy,
  Check,
  Maximize2,
  Minimize2,
  Share2,
} from 'lucide-react';
import DOMPurify from 'dompurify';
import type { Article } from '../types';
import { ThreatBadges } from './ThreatBadges';
import { extractReadableArticle } from '../services/readability';

interface ReaderModalProps {
  article: Article | null;
  onClose: () => void;
  onToggleFavorite: (article: Article) => void;
  onToggleSaveForLater: (article: Article) => void;
  onArchive: (article: Article) => void;
  onToggleReadState: (article: Article) => void;
  onUpdateArticleContent?: (articleId: string, contentHtml: string) => void;
}

export const ReaderModal: React.FC<ReaderModalProps> = ({
  article,
  onClose,
  onToggleFavorite,
  onToggleSaveForLater,
  onArchive,
  onToggleReadState,
  onUpdateArticleContent,
}) => {
  // Mode: 'WEBVIEW' (Full live embedded page like mobile app) vs 'FOCUS' (Clean Reader view)
  const [viewMode, setViewMode] = useState<'WEBVIEW' | 'FOCUS'>('WEBVIEW');
  const [isMaximized, setIsMaximized] = useState(false);
  const [isIframeLoading, setIsIframeLoading] = useState(true);

  // Focus mode states
  const [fontSize, setFontSize] = useState<'sm' | 'base' | 'lg'>('base');
  const [fontFamily, setFontFamily] = useState<'sans' | 'serif'>('serif');
  const [isLoadingReader, setIsLoadingReader] = useState(false);
  const [cleanContent, setCleanContent] = useState<string | null>(null);
  const [copied, setCopied] = useState(false);
  const [scrollProgress, setScrollProgress] = useState(0);
  const contentScrollRef = useRef<HTMLDivElement>(null);

  // Close on Escape key & listen for fallback message from iframe
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Escape') {
        onClose();
      }
    };
    const handleMessage = (e: MessageEvent) => {
      if (e.data?.type === 'SWITCH_TO_READER') {
        setViewMode('FOCUS');
      }
    };
    window.addEventListener('keydown', handleKeyDown);
    window.addEventListener('message', handleMessage);
    return () => {
      window.removeEventListener('keydown', handleKeyDown);
      window.removeEventListener('message', handleMessage);
    };
  }, [onClose]);

  useEffect(() => {
    if (article) {
      setIsIframeLoading(true);
      setCleanContent(article.contentHtml || null);
      setScrollProgress(0);

      // Pre-fetch or extract readable article content if not already stored
      if (!article.contentHtml || article.contentHtml.length < 500) {
        fetchReadableContent();
      }
    }
  }, [article]);

  const fetchReadableContent = async () => {
    if (!article?.contentUrl) return;
    setIsLoadingReader(true);
    try {
      const parsed = await extractReadableArticle(article.contentUrl);
      if (parsed && parsed.content) {
        setCleanContent(parsed.content);
        if (onUpdateArticleContent) {
          onUpdateArticleContent(article.id, parsed.content);
        }
      }
    } catch (err) {
      console.error('Failed to parse full article text:', err);
    } finally {
      setIsLoadingReader(false);
    }
  };

  const handleScroll = () => {
    if (contentScrollRef.current) {
      const { scrollTop, scrollHeight, clientHeight } = contentScrollRef.current;
      const progress = (scrollTop / (scrollHeight - clientHeight)) * 100;
      setScrollProgress(isNaN(progress) ? 0 : progress);
    }
  };

  const handlePopoutWindow = () => {
    if (!article?.contentUrl) return;
    const width = 1100;
    const height = 850;
    const left = window.screen.width / 2 - width / 2;
    const top = window.screen.height / 2 - height / 2;
    window.open(
      article.contentUrl,
      'SereneSecReaderWindow',
      `width=${width},height=${height},top=${top},left=${left},resizable=yes,scrollbars=yes,status=no`
    );
  };

  const handleCopyLink = () => {
    if (article?.contentUrl) {
      navigator.clipboard.writeText(article.contentUrl);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    }
  };

  if (!article) return null;

  const isRead = article.state === 'READ';
  const iframeSrc = `/api/feed?url=${encodeURIComponent(article.contentUrl)}&mode=webview`;

  const fontClasses = {
    sm: 'text-sm leading-relaxed',
    base: 'text-base leading-relaxed',
    lg: 'text-lg leading-loose',
  }[fontSize];

  return (
    <div
      className={`fixed inset-0 z-50 flex items-center justify-center bg-slate-950/75 backdrop-blur-md transition-all duration-200 animate-fadeIn ${
        isMaximized ? 'p-0' : 'p-2 sm:p-4 md:p-6'
      }`}
    >
      <div
        className={`relative w-full bg-white dark:bg-zinc-950 border border-slate-200 dark:border-zinc-800 shadow-2xl flex flex-col overflow-hidden transition-all duration-200 ${
          isMaximized
            ? 'h-screen w-screen rounded-none'
            : 'max-w-5xl h-[92vh] rounded-2xl'
        }`}
        onClick={(e) => e.stopPropagation()}
      >
        {/* Top Control Bar / Window Header */}
        <div className="flex items-center justify-between px-3 sm:px-5 py-2.5 bg-slate-50 dark:bg-zinc-900 border-b border-slate-200 dark:border-zinc-800 transition-colors flex-shrink-0">
          {/* Left: Window actions & Article quick info */}
          <div className="flex items-center space-x-1 sm:space-x-2 min-w-0 pr-2">
            <button
              onClick={() => onToggleReadState(article)}
              className={`px-2.5 py-1 rounded-lg text-xs font-medium flex items-center space-x-1.5 transition ${
                isRead
                  ? 'bg-slate-200/80 text-slate-700 dark:bg-zinc-800 dark:text-zinc-300 hover:bg-slate-300'
                  : 'bg-cyan-600 text-white dark:bg-cyan-500/20 dark:text-cyan-300 dark:border dark:border-cyan-500/40'
              }`}
              title={isRead ? 'Mark as Unread' : 'Mark as Read'}
            >
              <CheckCircle2 className="w-3.5 h-3.5" />
              <span className="hidden sm:inline">{isRead ? 'Read' : 'Mark Read'}</span>
            </button>

            <button
              onClick={() => onToggleFavorite(article)}
              className={`p-1.5 rounded-lg transition ${
                article.isFavorite
                  ? 'text-amber-500 bg-amber-50 dark:bg-amber-400/10'
                  : 'text-slate-400 hover:text-slate-700 dark:text-zinc-400 dark:hover:text-zinc-200 hover:bg-slate-200/60 dark:hover:bg-zinc-800'
              }`}
              title={article.isFavorite ? 'Starred' : 'Star'}
            >
              <Star className="w-4 h-4" fill={article.isFavorite ? 'currentColor' : 'none'} />
            </button>

            <button
              onClick={() => onToggleSaveForLater(article)}
              className={`p-1.5 rounded-lg transition ${
                article.isSavedForLater
                  ? 'text-cyan-600 dark:text-cyan-400 bg-cyan-50 dark:bg-cyan-400/10'
                  : 'text-slate-400 hover:text-slate-700 dark:text-zinc-400 dark:hover:text-zinc-200 hover:bg-slate-200/60 dark:hover:bg-zinc-800'
              }`}
              title={article.isSavedForLater ? 'Saved' : 'Save for later'}
            >
              <Bookmark className="w-4 h-4" fill={article.isSavedForLater ? 'currentColor' : 'none'} />
            </button>

            <button
              onClick={() => {
                onArchive(article);
                onClose();
              }}
              className="p-1.5 rounded-lg text-slate-400 hover:text-slate-700 dark:text-zinc-400 dark:hover:text-zinc-200 hover:bg-slate-200/60 dark:hover:bg-zinc-800 transition"
              title="Archive"
            >
              <Archive className="w-4 h-4" />
            </button>

            <div className="h-4 w-px bg-slate-300 dark:bg-zinc-800 mx-1 hidden sm:block" />

            {/* Source & Title in Header */}
            <div className="min-w-0 hidden md:block">
              <span className="text-xs font-semibold text-slate-800 dark:text-zinc-200 truncate block max-w-[280px]">
                {article.title}
              </span>
              <span className="text-[10px] text-slate-500 dark:text-zinc-400 font-mono block truncate">
                {article.sourceName || 'Feed'}
              </span>
            </div>
          </div>

          {/* Center: View Switcher (Web View vs Focus View) */}
          <div className="flex items-center bg-slate-200/80 dark:bg-zinc-800/80 p-0.5 rounded-xl border border-slate-300/80 dark:border-zinc-700/60">
            <button
              onClick={() => setViewMode('WEBVIEW')}
              className={`px-3 py-1 rounded-lg text-xs font-semibold flex items-center space-x-1.5 transition ${
                viewMode === 'WEBVIEW'
                  ? 'bg-white dark:bg-zinc-950 text-slate-900 dark:text-cyan-400 shadow-xs'
                  : 'text-slate-600 dark:text-zinc-400 hover:text-slate-900 dark:hover:text-zinc-200'
              }`}
              title="Open full live website inside app window"
            >
              <Globe className="w-3.5 h-3.5" />
              <span>Web View</span>
            </button>

            <button
              onClick={() => {
                setViewMode('FOCUS');
                if (!cleanContent || cleanContent.length < 500) {
                  fetchReadableContent();
                }
              }}
              className={`px-3 py-1 rounded-lg text-xs font-semibold flex items-center space-x-1.5 transition ${
                viewMode === 'FOCUS'
                  ? 'bg-white dark:bg-zinc-950 text-slate-900 dark:text-cyan-400 shadow-xs'
                  : 'text-slate-600 dark:text-zinc-400 hover:text-slate-900 dark:hover:text-zinc-200'
              }`}
              title="Switch to distraction-free Reader View"
            >
              <BookOpen className="w-3.5 h-3.5" />
              <span>Reader View</span>
            </button>
          </div>

          {/* Right: Window Controls */}
          <div className="flex items-center space-x-1">
            {/* Pop-out floating window */}
            <button
              onClick={handlePopoutWindow}
              className="p-1.5 rounded-lg text-slate-500 hover:text-slate-800 dark:text-zinc-400 dark:hover:text-zinc-200 hover:bg-slate-200/60 dark:hover:bg-zinc-800 transition hidden sm:inline-flex"
              title="Open in floating popup window"
            >
              <ExternalLink className="w-4 h-4" />
            </button>

            {/* Maximize / Restore */}
            <button
              onClick={() => setIsMaximized(!isMaximized)}
              className="p-1.5 rounded-lg text-slate-500 hover:text-slate-800 dark:text-zinc-400 dark:hover:text-zinc-200 hover:bg-slate-200/60 dark:hover:bg-zinc-800 transition hidden sm:inline-flex"
              title={isMaximized ? 'Restore size' : 'Maximize window'}
            >
              {isMaximized ? <Minimize2 className="w-4 h-4" /> : <Maximize2 className="w-4 h-4" />}
            </button>

            {/* Close */}
            <button
              onClick={onClose}
              className="p-1.5 rounded-lg text-slate-500 hover:text-slate-900 dark:text-zinc-400 dark:hover:text-zinc-100 hover:bg-slate-200/60 dark:hover:bg-zinc-800 transition"
              title="Close (Esc)"
            >
              <X className="w-5 h-5" />
            </button>
          </div>
        </div>

        {/* MODE 1: FULL LIVE IN-APP WEBVIEW (Matches mobile app WebView) */}
        {viewMode === 'WEBVIEW' && (
          <div className="relative flex-1 w-full h-full bg-slate-100 dark:bg-zinc-900 overflow-hidden flex flex-col">
            {/* Loading progress indicator */}
            {isIframeLoading && (
              <div className="absolute top-0 left-0 right-0 z-20 h-1 bg-slate-200 dark:bg-zinc-800 overflow-hidden">
                <div className="h-full bg-cyan-500 animate-pulse w-3/4" />
              </div>
            )}

            <iframe
              src={iframeSrc}
              title={article.title}
              onLoad={() => setIsIframeLoading(false)}
              className="w-full flex-1 border-0 bg-white"
              sandbox="allow-scripts allow-same-origin allow-forms allow-popups"
            />
          </div>
        )}

        {/* MODE 2: CLEAN FOCUS READER VIEW */}
        {viewMode === 'FOCUS' && (
          <div className="flex-1 flex flex-col overflow-hidden">
            {/* Scroll progress */}
            <div className="w-full h-1 bg-slate-100 dark:bg-zinc-900">
              <div
                className="h-full bg-cyan-500 transition-all duration-100"
                style={{ width: `${scrollProgress}%` }}
              />
            </div>

            {/* Focus Sub-header controls */}
            <div className="flex items-center justify-between px-6 py-2 border-b border-slate-200/80 dark:border-zinc-800/80 text-xs bg-slate-50/50 dark:bg-zinc-900/40">
              <div className="flex items-center space-x-2">
                <button
                  onClick={() => setFontFamily(fontFamily === 'serif' ? 'sans' : 'serif')}
                  className="px-2 py-1 rounded bg-slate-100 dark:bg-zinc-800 font-semibold text-slate-700 dark:text-zinc-300"
                >
                  {fontFamily === 'serif' ? 'Serif' : 'Sans'}
                </button>
                <div className="flex items-center bg-slate-100 dark:bg-zinc-800 rounded p-0.5">
                  <button
                    onClick={() => setFontSize('sm')}
                    className={`px-1.5 py-0.5 rounded ${fontSize === 'sm' ? 'bg-white dark:bg-zinc-700' : ''}`}
                  >
                    A-
                  </button>
                  <button
                    onClick={() => setFontSize('base')}
                    className={`px-1.5 py-0.5 rounded ${fontSize === 'base' ? 'bg-white dark:bg-zinc-700' : ''}`}
                  >
                    A
                  </button>
                  <button
                    onClick={() => setFontSize('lg')}
                    className={`px-1.5 py-0.5 rounded ${fontSize === 'lg' ? 'bg-white dark:bg-zinc-700' : ''}`}
                  >
                    A+
                  </button>
                </div>
              </div>

              <div className="flex items-center space-x-2">
                <button
                  onClick={handleCopyLink}
                  className="inline-flex items-center space-x-1 text-slate-600 dark:text-zinc-400 hover:text-cyan-600"
                >
                  {copied ? <Check className="w-3.5 h-3.5 text-emerald-500" /> : <Copy className="w-3.5 h-3.5" />}
                  <span>{copied ? 'Copied' : 'Share'}</span>
                </button>
                <a
                  href={article.contentUrl}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="text-cyan-600 dark:text-cyan-400 hover:underline flex items-center space-x-1"
                >
                  <span>Direct link</span>
                  <ExternalLink className="w-3.5 h-3.5" />
                </a>
              </div>
            </div>

            {/* Content pane */}
            <div
              ref={contentScrollRef}
              onScroll={handleScroll}
              className="flex-1 overflow-y-auto p-6 sm:p-10"
            >
              <div className="max-w-[72ch] mx-auto">
                <ThreatBadges article={article} />
                <h1 className="text-xl sm:text-2xl md:text-3xl font-bold tracking-tight text-slate-900 dark:text-zinc-100 mb-4 leading-snug">
                  {article.title}
                </h1>
                <div className="text-xs text-slate-500 dark:text-zinc-400 mb-6 pb-4 border-b border-slate-200 dark:border-zinc-800">
                  <span className="font-semibold">{article.sourceName}</span> •{' '}
                  {new Date(article.publishedDate).toLocaleDateString()}
                  {article.author && ` • By ${article.author}`}
                </div>

                <div
                  className={`text-slate-800 dark:text-zinc-300 ${fontClasses} ${
                    fontFamily === 'serif' ? 'font-serif' : 'font-sans'
                  }`}
                >
                  {isLoadingReader ? (
                    <div className="py-16 text-center">
                      <Loader2 className="w-8 h-8 animate-spin text-cyan-500 mx-auto mb-2" />
                      <p className="text-xs text-slate-500">Extracting readable article...</p>
                    </div>
                  ) : cleanContent ? (
                    <div
                      className="prose prose-slate dark:prose-invert max-w-none"
                      dangerouslySetInnerHTML={{
                        __html: DOMPurify.sanitize(cleanContent),
                      }}
                    />
                  ) : (
                    <p className="text-base text-slate-700 dark:text-zinc-300 leading-relaxed">
                      {article.summary}
                    </p>
                  )}
                </div>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};
