import React, { useEffect } from 'react';
import { X, Keyboard } from 'lucide-react';

interface ShortcutsModalProps {
  isOpen: boolean;
  onClose: () => void;
}

const SHORTCUTS = [
  { key: 'j / ↓', description: 'Navigate to next article' },
  { key: 'k / ↑', description: 'Navigate to previous article' },
  { key: 'Enter / o', description: 'Open article in Focus Reader' },
  { key: 's', description: 'Toggle Star / Favorite' },
  { key: 'b', description: 'Toggle Bookmark / Save for later' },
  { key: 'e', description: 'Archive article' },
  { key: '/', description: 'Focus search bar' },
  { key: 't', description: 'Toggle Light / Dark mode' },
  { key: 'v', description: 'Toggle Card / Compact view' },
  { key: 'Esc', description: 'Close modal or dismiss reader' },
  { key: '?', description: 'Show keyboard shortcuts' },
];

export const ShortcutsModal: React.FC<ShortcutsModalProps> = ({ isOpen, onClose }) => {
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Escape') onClose();
    };
    if (isOpen) {
      window.addEventListener('keydown', handleKeyDown);
      return () => window.removeEventListener('keydown', handleKeyDown);
    }
  }, [isOpen, onClose]);

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 overflow-y-auto bg-slate-900/60 dark:bg-black/80 backdrop-blur-sm flex justify-center p-4 animate-fadeIn">
      <div
        className="relative w-full max-w-md bg-white dark:bg-zinc-950 border border-slate-200 dark:border-zinc-800 rounded-2xl shadow-2xl overflow-hidden my-auto p-5 transition-colors"
        onClick={(e) => e.stopPropagation()}
      >
        <div className="flex items-center justify-between pb-3 border-b border-slate-200 dark:border-zinc-800">
          <div className="flex items-center space-x-2">
            <Keyboard className="w-5 h-5 text-cyan-600 dark:text-cyan-400" />
            <h3 className="text-sm font-bold text-slate-900 dark:text-zinc-100">
              Keyboard Shortcuts
            </h3>
          </div>
          <button
            onClick={onClose}
            className="p-1 rounded-lg text-slate-400 hover:text-slate-600 dark:text-zinc-400 dark:hover:text-zinc-200"
          >
            <X className="w-4 h-4" />
          </button>
        </div>

        <div className="mt-4 space-y-2.5">
          {SHORTCUTS.map((sc) => (
            <div
              key={sc.key}
              className="flex items-center justify-between text-xs py-1"
            >
              <span className="text-slate-600 dark:text-zinc-400">
                {sc.description}
              </span>
              <kbd className="px-2 py-0.5 rounded bg-slate-100 dark:bg-zinc-800 border border-slate-300 dark:border-zinc-700 text-slate-800 dark:text-zinc-200 font-mono font-semibold text-[11px] shadow-xs">
                {sc.key}
              </kbd>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};
