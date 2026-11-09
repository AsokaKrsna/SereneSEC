import React from 'react';
import type { Category } from '../types';
import { categoryColors } from './ThreatBadges';

interface CategoryFilterProps {
  selectedCategory: Category | 'ALL';
  onSelectCategory: (category: Category | 'ALL') => void;
  categoryCounts?: Partial<Record<Category | 'ALL', number>>;
}

const ALL_CATEGORIES: Category[] = [
  'NEWS',
  'BLOCKCHAIN',
  'CVE',
  'THREAT_INTEL',
  'RESEARCH',
  'TOOLS',
  'MALWARE',
  'EXPLOITS',
  'GOV_ADVISORY',
  'WRITEUPS',
  'CLOUD',
  'DEFENSE',
  'TRADECRAFT',
  'INCIDENTS',
  'PODCAST',
  'LEARNING',
  'PROGRAMS',
  'CONFERENCE',
];

export const CategoryFilter: React.FC<CategoryFilterProps> = ({
  selectedCategory,
  onSelectCategory,
  categoryCounts,
}) => {
  return (
    <div className="flex items-center space-x-2 overflow-x-auto py-2 px-1 scrollbar-none no-scrollbar">
      <button
        onClick={() => onSelectCategory('ALL')}
        className={`px-3.5 py-1.5 rounded-full text-xs font-medium whitespace-nowrap transition-colors flex items-center gap-1.5 border ${
          selectedCategory === 'ALL'
            ? 'bg-slate-900 text-white border-slate-900 dark:bg-cyan-500/20 dark:text-cyan-300 dark:border-cyan-500/40 shadow-sm'
            : 'bg-slate-100 hover:bg-slate-200/80 text-slate-700 border-slate-200/90 dark:bg-zinc-800/60 dark:text-zinc-400 dark:border-zinc-700/50 dark:hover:bg-zinc-800 dark:hover:text-zinc-300'
        }`}
      >
        <span>All Feeds</span>
        {categoryCounts?.['ALL'] !== undefined && (
          <span className="text-[10px] px-1.5 py-0.2 rounded-full bg-slate-200/80 text-slate-700 dark:bg-zinc-900/60 dark:text-zinc-400 font-mono">
            {categoryCounts['ALL']}
          </span>
        )}
      </button>

      {ALL_CATEGORIES.map((cat) => {
        const isSelected = selectedCategory === cat;
        const style = categoryColors[cat];
        const count = categoryCounts?.[cat];

        return (
          <button
            key={cat}
            onClick={() => onSelectCategory(cat)}
            className={`px-3 py-1.5 rounded-full text-xs font-medium whitespace-nowrap transition-colors flex items-center gap-1.5 border ${
              isSelected
                ? `${style.bg} ${style.text} border-current shadow-sm`
                : 'bg-slate-100 hover:bg-slate-200/80 text-slate-700 border-slate-200/90 dark:bg-zinc-800/60 dark:text-zinc-400 dark:border-zinc-700/40 dark:hover:bg-zinc-800 dark:hover:text-zinc-300'
            }`}
          >
            <span>{style.label}</span>
            {count !== undefined && count > 0 && (
              <span className="text-[10px] px-1.5 py-0.2 rounded-full bg-slate-200/80 text-slate-700 dark:bg-zinc-900/60 dark:text-zinc-300 font-mono opacity-90">
                {count}
              </span>
            )}
          </button>
        );
      })}
    </div>
  );
};
