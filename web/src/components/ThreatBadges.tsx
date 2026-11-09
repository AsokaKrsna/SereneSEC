import React from 'react';
import type { Article, Category } from '../types';

interface ThreatBadgesProps {
  article: Article;
}

export const categoryColors: Record<
  Category,
  { bg: string; text: string; border: string; label: string }
> = {
  NEWS: {
    bg: 'bg-blue-50 dark:bg-blue-500/15',
    text: 'text-blue-700 dark:text-blue-400',
    border: 'border-blue-200 dark:border-blue-500/30',
    label: 'News',
  },
  TOOLS: {
    bg: 'bg-purple-50 dark:bg-purple-500/15',
    text: 'text-purple-700 dark:text-purple-400',
    border: 'border-purple-200 dark:border-purple-500/30',
    label: 'Tools',
  },
  CVE: {
    bg: 'bg-red-50 dark:bg-red-500/15',
    text: 'text-red-700 dark:text-red-400',
    border: 'border-red-200 dark:border-red-500/30',
    label: 'CVE',
  },
  RESEARCH: {
    bg: 'bg-emerald-50 dark:bg-emerald-500/15',
    text: 'text-emerald-700 dark:text-emerald-400',
    border: 'border-emerald-200 dark:border-emerald-500/30',
    label: 'Research',
  },
  THREAT_INTEL: {
    bg: 'bg-rose-50 dark:bg-rose-500/15',
    text: 'text-rose-700 dark:text-rose-400',
    border: 'border-rose-200 dark:border-rose-500/30',
    label: 'Threat Intel',
  },
  BLOCKCHAIN: {
    bg: 'bg-violet-50 dark:bg-purple-600/20',
    text: 'text-violet-700 dark:text-purple-300',
    border: 'border-violet-200 dark:border-purple-500/40',
    label: 'Blockchain',
  },
  GOV_ADVISORY: {
    bg: 'bg-sky-50 dark:bg-sky-500/15',
    text: 'text-sky-700 dark:text-sky-400',
    border: 'border-sky-200 dark:border-sky-500/30',
    label: 'Gov Advisory',
  },
  MALWARE: {
    bg: 'bg-orange-50 dark:bg-orange-500/15',
    text: 'text-orange-700 dark:text-orange-400',
    border: 'border-orange-200 dark:border-orange-500/30',
    label: 'Malware',
  },
  EXPLOITS: {
    bg: 'bg-amber-50 dark:bg-amber-500/15',
    text: 'text-amber-700 dark:text-amber-400',
    border: 'border-amber-200 dark:border-amber-500/30',
    label: 'Exploits',
  },
  PROGRAMS: {
    bg: 'bg-emerald-50 dark:bg-emerald-500/15',
    text: 'text-emerald-700 dark:text-emerald-400',
    border: 'border-emerald-200 dark:border-emerald-500/30',
    label: 'Programs',
  },
  WRITEUPS: {
    bg: 'bg-slate-100 dark:bg-gray-500/15',
    text: 'text-slate-700 dark:text-gray-400',
    border: 'border-slate-200 dark:border-gray-500/30',
    label: 'Writeups',
  },
  PODCAST: {
    bg: 'bg-fuchsia-50 dark:bg-fuchsia-500/15',
    text: 'text-fuchsia-700 dark:text-fuchsia-400',
    border: 'border-fuchsia-200 dark:border-fuchsia-500/30',
    label: 'Podcast',
  },
  LEARNING: {
    bg: 'bg-teal-50 dark:bg-teal-500/15',
    text: 'text-teal-700 dark:text-teal-400',
    border: 'border-teal-200 dark:border-teal-500/30',
    label: 'Learning',
  },
  DEFENSE: {
    bg: 'bg-indigo-50 dark:bg-indigo-500/15',
    text: 'text-indigo-700 dark:text-indigo-400',
    border: 'border-indigo-200 dark:border-indigo-500/30',
    label: 'Defense',
  },
  CLOUD: {
    bg: 'bg-cyan-50 dark:bg-cyan-500/15',
    text: 'text-cyan-700 dark:text-cyan-400',
    border: 'border-cyan-200 dark:border-cyan-500/30',
    label: 'Cloud',
  },
  TRADECRAFT: {
    bg: 'bg-red-50 dark:bg-red-600/15',
    text: 'text-red-700 dark:text-red-400',
    border: 'border-red-200 dark:border-red-600/30',
    label: 'Tradecraft',
  },
  INCIDENTS: {
    bg: 'bg-yellow-50 dark:bg-yellow-500/15',
    text: 'text-yellow-700 dark:text-yellow-400',
    border: 'border-yellow-200 dark:border-yellow-500/30',
    label: 'Incidents',
  },
  CONFERENCE: {
    bg: 'bg-violet-50 dark:bg-violet-500/15',
    text: 'text-violet-700 dark:text-violet-400',
    border: 'border-violet-200 dark:border-violet-500/30',
    label: 'Conference',
  },
};

export const ThreatBadges: React.FC<ThreatBadgesProps> = ({ article }) => {
  const cveRegex = /\bCVE-\d{4}-\d{4,}\b/gi;
  const inTitle = Array.from(article.title.matchAll(cveRegex), (m) => m[0].toUpperCase());
  const inSummary = article.summary
    ? Array.from(article.summary.matchAll(cveRegex), (m) => m[0].toUpperCase())
    : [];
  const cves = Array.from(new Set([...inTitle, ...inSummary])).slice(0, 2);

  const isCisa =
    article.sourceId.toLowerCase().includes('cisa') ||
    article.title.toLowerCase().includes('cisa');

  const isZeroDay =
    article.title.toLowerCase().includes('zero-day') ||
    article.title.toLowerCase().includes('0-day') ||
    article.title.toLowerCase().includes('zeroday');

  const isDeFiExploit =
    article.category === 'BLOCKCHAIN' &&
    (article.title.toLowerCase().includes('exploit') ||
      article.title.toLowerCase().includes('hack') ||
      article.title.toLowerCase().includes('drain') ||
      article.title.toLowerCase().includes('rekt'));

  const catStyle = categoryColors[article.category] || categoryColors.NEWS;

  return (
    <div className="flex flex-wrap items-center gap-1.5 mb-2">
      <span
        className={`px-2 py-0.5 rounded text-[11px] font-medium border ${catStyle.bg} ${catStyle.text} ${catStyle.border}`}
      >
        {catStyle.label}
      </span>

      {isCisa && (
        <span className="px-2 py-0.5 rounded text-[11px] font-bold bg-blue-50 dark:bg-blue-600/20 text-blue-700 dark:text-blue-400 border border-blue-200 dark:border-blue-500/30">
          CISA Alert
        </span>
      )}

      {isZeroDay && (
        <span className="px-2 py-0.5 rounded text-[11px] font-bold bg-red-50 dark:bg-red-600/20 text-red-700 dark:text-red-400 border border-red-200 dark:border-red-500/30">
          0-Day
        </span>
      )}

      {isDeFiExploit && (
        <span className="px-2 py-0.5 rounded text-[11px] font-bold bg-purple-50 dark:bg-purple-600/25 text-purple-700 dark:text-purple-300 border border-purple-200 dark:border-purple-500/40">
          ⚡ DeFi Exploit
        </span>
      )}

      {cves.map((cve) => (
        <span
          key={cve}
          className="px-2 py-0.5 rounded text-[11px] font-bold bg-rose-50 dark:bg-rose-600/20 text-rose-700 dark:text-rose-400 border border-rose-200 dark:border-rose-500/30 font-mono tracking-tight"
        >
          {cve}
        </span>
      ))}
    </div>
  );
};
