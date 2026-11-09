import React, { useState, useEffect } from 'react';
import {
  X,
  Plus,
  Download,
  Upload,
  RefreshCw,
  CheckCircle2,
  AlertCircle,
  Trash2,
  Sparkles,
  Loader2,
} from 'lucide-react';
import type { Source, Category, SourceType } from '../types';
import { db } from '../db';
import { discoverFeed } from '../services/feedDiscovery';
import { exportToOpml, parseOpml } from '../services/opml';
import defaultSourcesData from '../assets/sources_default.json';

interface SourceManagerModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSourcesUpdated: () => void;
}

const CATEGORIES: Category[] = [
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

export const SourceManagerModal: React.FC<SourceManagerModalProps> = ({
  isOpen,
  onClose,
  onSourcesUpdated,
}) => {
  const [sources, setSources] = useState<Source[]>([]);
  const [activeTab, setActiveTab] = useState<'LIST' | 'ADD' | 'OPML'>('LIST');

  // Add source form state
  const [newUrl, setNewUrl] = useState('');
  const [newName, setNewName] = useState('');
  const [newCategory, setNewCategory] = useState<Category>('NEWS');
  const [newType, setNewType] = useState<SourceType>('RSS');
  const [isDiscovering, setIsDiscovering] = useState(false);
  const [discoveryMessage, setDiscoveryMessage] = useState<string | null>(null);

  // Status message
  const [statusMessage, setStatusMessage] = useState<string | null>(null);

  useEffect(() => {
    if (isOpen) {
      loadSources();
    }
  }, [isOpen]);

  const loadSources = async () => {
    const list = await db.sources.toArray();
    setSources(list.sort((a, b) => a.name.localeCompare(b.name)));
  };

  const handleToggleSource = async (source: Source) => {
    const updated = !source.isEnabled;
    await db.sources.update(source.id, { isEnabled: updated });
    setSources((prev) =>
      prev.map((s) => (s.id === source.id ? { ...s, isEnabled: updated } : s))
    );
    onSourcesUpdated();
  };

  const handleDeleteSource = async (sourceId: string) => {
    await db.sources.delete(sourceId);
    setSources((prev) => prev.filter((s) => s.id !== sourceId));
    onSourcesUpdated();
  };

  const handleClearAllErrors = async () => {
    const list = await db.sources.toArray();
    for (const s of list) {
      if (s.errorCount > 0 || s.lastErrorMessage) {
        await db.sources.update(s.id, {
          errorCount: 0,
          lastErrorMessage: undefined,
        });
      }
    }
    await loadSources();
    setStatusMessage('Cleared all error warnings.');
    onSourcesUpdated();
  };

  const handleAutoDiscover = async () => {
    if (!newUrl) return;
    setIsDiscovering(true);
    setDiscoveryMessage(null);

    try {
      const candidate = await discoverFeed(newUrl);
      if (candidate && candidate.feedUrl) {
        setNewUrl(candidate.feedUrl);
        if (!newName && candidate.title) {
          setNewName(candidate.title);
        }
        setNewType(candidate.type);
        setDiscoveryMessage(`Auto-detected: ${candidate.title || candidate.feedUrl} (${candidate.type})`);
      } else {
        setDiscoveryMessage('No explicit feed link tag found; will try direct URL.');
      }
    } catch {
      setDiscoveryMessage('Could not auto-detect; verify the URL.');
    } finally {
      setIsDiscovering(false);
    }
  };

  const handleAddSource = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newUrl || !newName) return;

    const newSource: Source = {
      id: `custom_${Date.now()}`,
      name: newName.trim(),
      url: newUrl.trim(),
      type: newType,
      category: newCategory,
      isEnabled: true,
      isBuiltIn: false,
      errorCount: 0,
      successCount: 0,
      totalArticlesFetched: 0,
    };

    await db.sources.add(newSource);
    setNewUrl('');
    setNewName('');
    setDiscoveryMessage(null);
    setStatusMessage(`Added "${newSource.name}" successfully.`);
    await loadSources();
    setActiveTab('LIST');
    onSourcesUpdated();
  };

  const handleExportOpml = async () => {
    const allSources = await db.sources.toArray();
    const xml = exportToOpml(allSources);
    const blob = new Blob([xml], { type: 'text/xml' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = 'serenesec_sources.opml';
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
    setStatusMessage('OPML export completed.');
  };

  const handleImportOpml = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    try {
      const text = await file.text();
      const parsed = parseOpml(text);
      if (parsed.length === 0) {
        setStatusMessage('No valid feeds found in OPML file.');
        return;
      }

      for (const item of parsed) {
        const existing = await db.sources.where('url').equals(item.url).first();
        if (!existing) {
          await db.sources.add({
            id: `opml_${Date.now()}_${Math.random().toString(36).substring(2, 7)}`,
            name: item.name,
            url: item.url,
            type: item.type,
            category: item.category,
            isEnabled: true,
            isBuiltIn: false,
            errorCount: 0,
            successCount: 0,
            totalArticlesFetched: 0,
          });
        }
      }

      await loadSources();
      setStatusMessage(`Imported ${parsed.length} feeds from OPML.`);
      onSourcesUpdated();
    } catch (err) {
      setStatusMessage('Failed to parse OPML file.');
      console.error(err);
    }
  };

  const handleResetDefaults = async () => {
    if (!window.confirm('Reset all sources to SereneSec defaults? Custom sources will be retained.')) {
      return;
    }

    const defaultIds = new Set(defaultSourcesData.sources.map((s: any) => s.id));
    const customSources = (await db.sources.toArray()).filter((s) => !defaultIds.has(s.id));

    await db.sources.clear();
    await db.initializeDefaults();

    if (customSources.length > 0) {
      await db.sources.bulkAdd(customSources);
    }

    await loadSources();
    setStatusMessage('Built-in sources restored to default.');
    onSourcesUpdated();
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 overflow-y-auto bg-slate-900/60 dark:bg-black/80 backdrop-blur-sm flex justify-center p-2 sm:p-4 md:p-6 animate-fadeIn">
      <div
        className="relative w-full max-w-2xl bg-white dark:bg-zinc-950 border border-slate-200 dark:border-zinc-800 rounded-2xl shadow-2xl overflow-hidden flex flex-col my-auto max-h-[90vh] transition-colors"
        onClick={(e) => e.stopPropagation()}
      >
        {/* Modal Header */}
        <div className="flex items-center justify-between px-5 py-4 bg-slate-50 dark:bg-zinc-900/90 border-b border-slate-200 dark:border-zinc-800">
          <div>
            <h2 className="text-base font-bold text-slate-900 dark:text-zinc-100">
              Source Manager
            </h2>
            <p className="text-xs text-slate-500 dark:text-zinc-400">
              Manage threat intelligence sources, feeds, and OPML
            </p>
          </div>
          <button
            onClick={onClose}
            className="p-1.5 rounded-lg text-slate-400 hover:text-slate-600 dark:text-zinc-400 dark:hover:text-zinc-200 hover:bg-slate-100 dark:hover:bg-zinc-800 transition"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Tab Navigation */}
        <div className="flex items-center space-x-2 px-5 py-2.5 bg-slate-100/70 dark:bg-zinc-900/40 border-b border-slate-200 dark:border-zinc-800/60 text-xs">
          <button
            onClick={() => setActiveTab('LIST')}
            className={`px-3 py-1.5 rounded-lg font-medium transition ${
              activeTab === 'LIST'
                ? 'bg-slate-900 text-white dark:bg-cyan-500/15 dark:text-cyan-400 dark:border dark:border-cyan-500/30 shadow-xs'
                : 'text-slate-600 dark:text-zinc-400 hover:text-slate-900 dark:hover:text-zinc-200'
            }`}
          >
            Active Sources ({sources.length})
          </button>
          <button
            onClick={() => setActiveTab('ADD')}
            className={`px-3 py-1.5 rounded-lg font-medium transition flex items-center space-x-1 ${
              activeTab === 'ADD'
                ? 'bg-slate-900 text-white dark:bg-cyan-500/15 dark:text-cyan-400 dark:border dark:border-cyan-500/30 shadow-xs'
                : 'text-slate-600 dark:text-zinc-400 hover:text-slate-900 dark:hover:text-zinc-200'
            }`}
          >
            <Plus className="w-3.5 h-3.5" />
            <span>Add Source</span>
          </button>
          <button
            onClick={() => setActiveTab('OPML')}
            className={`px-3 py-1.5 rounded-lg font-medium transition flex items-center space-x-1 ${
              activeTab === 'OPML'
                ? 'bg-slate-900 text-white dark:bg-cyan-500/15 dark:text-cyan-400 dark:border dark:border-cyan-500/30 shadow-xs'
                : 'text-slate-600 dark:text-zinc-400 hover:text-slate-900 dark:hover:text-zinc-200'
            }`}
          >
            <Download className="w-3.5 h-3.5" />
            <span>OPML & Backup</span>
          </button>
        </div>

        {/* Status Notification */}
        {statusMessage && (
          <div className="px-5 py-2 bg-cyan-50 dark:bg-cyan-950/40 border-b border-cyan-200 dark:border-cyan-800/40 text-xs text-cyan-800 dark:text-cyan-300 flex items-center justify-between">
            <span>{statusMessage}</span>
            <button
              onClick={() => setStatusMessage(null)}
              className="text-cyan-600 dark:text-cyan-400 hover:opacity-75"
            >
              <X className="w-3.5 h-3.5" />
            </button>
          </div>
        )}

        {/* Content Pane */}
        <div className="p-5 overflow-y-auto max-h-[calc(90vh-140px)]">
          {/* TAB 1: LIST */}
          {activeTab === 'LIST' && (
            <div className="space-y-2">
              <div className="flex items-center justify-between pb-1 text-xs text-slate-500 dark:text-zinc-400">
                <span>{sources.filter((s) => s.isEnabled).length} active of {sources.length} sources</span>
                {sources.some((s) => s.errorCount > 0) && (
                  <button
                    onClick={handleClearAllErrors}
                    className="text-[11px] text-amber-600 dark:text-amber-400 hover:underline font-medium"
                  >
                    Clear Warnings
                  </button>
                )}
              </div>
              {sources.map((source) => (
                <div
                  key={source.id}
                  className="flex items-center justify-between p-3 rounded-xl border border-slate-200 dark:border-zinc-800/80 bg-white dark:bg-zinc-900/50 hover:bg-slate-50 dark:hover:bg-zinc-900 transition"
                >
                  <div className="flex-1 min-w-0 pr-3">
                    <div className="flex items-center space-x-2">
                      <h4 className="text-sm font-semibold text-slate-800 dark:text-zinc-200 truncate">
                        {source.name}
                      </h4>
                      <span className="px-1.5 py-0.2 rounded text-[10px] font-mono uppercase bg-slate-100 dark:bg-zinc-800 text-slate-600 dark:text-zinc-400 border border-slate-200 dark:border-zinc-700/60">
                        {source.category}
                      </span>
                      {source.errorCount > 0 ? (
                        <span
                          className="flex items-center text-[10px] text-amber-600 dark:text-amber-400 bg-amber-50 dark:bg-amber-950/40 px-1.5 py-0.5 rounded border border-amber-200 dark:border-amber-800/40"
                          title={source.lastErrorMessage || 'Feed warning'}
                        >
                          <AlertCircle className="w-3 h-3 mr-1" />
                          <span>Warning</span>
                        </span>
                      ) : (
                        <span className="flex items-center text-[10px] text-emerald-600 dark:text-emerald-400/90 font-mono">
                          <CheckCircle2 className="w-3 h-3 mr-0.5" />
                          <span>Active</span>
                        </span>
                      )}
                    </div>
                    <p className="text-xs text-slate-400 dark:text-zinc-500 truncate mt-0.5 font-mono">
                      {source.url}
                    </p>
                  </div>

                  <div className="flex items-center space-x-2.5">
                    {/* Toggle Switch */}
                    <button
                      onClick={() => handleToggleSource(source)}
                      className={`relative inline-flex h-5 w-9 items-center rounded-full transition-colors ${
                        source.isEnabled ? 'bg-cyan-600 dark:bg-cyan-500' : 'bg-slate-300 dark:bg-zinc-700'
                      }`}
                    >
                      <span
                        className={`inline-block h-3.5 w-3.5 transform rounded-full bg-white transition-transform ${
                          source.isEnabled ? 'translate-x-4' : 'translate-x-1'
                        }`}
                      />
                    </button>

                    {/* Delete button (only for custom sources) */}
                    {!source.isBuiltIn && (
                      <button
                        onClick={() => handleDeleteSource(source.id)}
                        className="p-1 rounded text-slate-400 hover:text-red-600 dark:text-zinc-500 dark:hover:text-red-400 transition"
                        title="Delete custom source"
                      >
                        <Trash2 className="w-4 h-4" />
                      </button>
                    )}
                  </div>
                </div>
              ))}
            </div>
          )}

          {/* TAB 2: ADD SOURCE */}
          {activeTab === 'ADD' && (
            <form onSubmit={handleAddSource} className="space-y-4">
              <div>
                <label className="block text-xs font-medium text-slate-700 dark:text-zinc-300 mb-1">
                  Source Feed or Website URL
                </label>
                <div className="flex space-x-2">
                  <input
                    type="url"
                    required
                    value={newUrl}
                    onChange={(e) => setNewUrl(e.target.value)}
                    placeholder="https://example.com/feed.xml or website URL"
                    className="flex-1 bg-slate-50 dark:bg-zinc-900 border border-slate-300 dark:border-zinc-800 rounded-xl px-3 py-2 text-xs text-slate-900 dark:text-zinc-200 placeholder-slate-400 dark:placeholder-zinc-500 focus:outline-none focus:border-cyan-600 dark:focus:border-cyan-500"
                  />
                  <button
                    type="button"
                    onClick={handleAutoDiscover}
                    disabled={isDiscovering || !newUrl}
                    className="px-3 py-2 rounded-xl text-xs font-medium bg-slate-100 hover:bg-slate-200 dark:bg-zinc-800 dark:hover:bg-zinc-700 text-cyan-600 dark:text-cyan-400 border border-slate-300 dark:border-zinc-700/60 flex items-center space-x-1.5 transition disabled:opacity-50"
                  >
                    {isDiscovering ? (
                      <Loader2 className="w-3.5 h-3.5 animate-spin" />
                    ) : (
                      <Sparkles className="w-3.5 h-3.5" />
                    )}
                    <span>Auto-Detect</span>
                  </button>
                </div>
                {discoveryMessage && (
                  <p className="text-[11px] text-cyan-600 dark:text-cyan-400 mt-1.5">
                    {discoveryMessage}
                  </p>
                )}
              </div>

              <div>
                <label className="block text-xs font-medium text-slate-700 dark:text-zinc-300 mb-1">
                  Feed Title / Publication Name
                </label>
                <input
                  type="text"
                  required
                  value={newName}
                  onChange={(e) => setNewName(e.target.value)}
                  placeholder="e.g. Project Zero Research"
                  className="w-full bg-slate-50 dark:bg-zinc-900 border border-slate-300 dark:border-zinc-800 rounded-xl px-3 py-2 text-xs text-slate-900 dark:text-zinc-200 placeholder-slate-400 dark:placeholder-zinc-500 focus:outline-none focus:border-cyan-600 dark:focus:border-cyan-500"
                />
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-medium text-slate-700 dark:text-zinc-300 mb-1">
                    Category
                  </label>
                  <select
                    value={newCategory}
                    onChange={(e) => setNewCategory(e.target.value as Category)}
                    className="w-full bg-slate-50 dark:bg-zinc-900 border border-slate-300 dark:border-zinc-800 rounded-xl px-3 py-2 text-xs text-slate-900 dark:text-zinc-200 focus:outline-none focus:border-cyan-600 dark:focus:border-cyan-500"
                  >
                    {CATEGORIES.map((cat) => (
                      <option key={cat} value={cat}>
                        {cat}
                      </option>
                    ))}
                  </select>
                </div>

                <div>
                  <label className="block text-xs font-medium text-slate-700 dark:text-zinc-300 mb-1">
                    Type
                  </label>
                  <select
                    value={newType}
                    onChange={(e) => setNewType(e.target.value as SourceType)}
                    className="w-full bg-slate-50 dark:bg-zinc-900 border border-slate-300 dark:border-zinc-800 rounded-xl px-3 py-2 text-xs text-slate-900 dark:text-zinc-200 focus:outline-none focus:border-cyan-600 dark:focus:border-cyan-500"
                  >
                    <option value="RSS">RSS</option>
                    <option value="ATOM">Atom</option>
                    <option value="GITHUB">GitHub Releases</option>
                  </select>
                </div>
              </div>

              <div className="pt-2">
                <button
                  type="submit"
                  className="w-full py-2.5 rounded-xl bg-slate-900 hover:bg-slate-800 dark:bg-cyan-600 dark:hover:bg-cyan-500 text-white font-medium text-xs flex items-center justify-center space-x-1.5 transition shadow-sm"
                >
                  <Plus className="w-4 h-4" />
                  <span>Add Security Feed</span>
                </button>
              </div>
            </form>
          )}

          {/* TAB 3: OPML & BACKUP */}
          {activeTab === 'OPML' && (
            <div className="space-y-4">
              <div className="p-4 rounded-xl border border-slate-200 dark:border-zinc-800 bg-slate-50 dark:bg-zinc-900/50">
                <h4 className="text-sm font-semibold text-slate-900 dark:text-zinc-200 mb-1 flex items-center space-x-2">
                  <Download className="w-4 h-4 text-cyan-600 dark:text-cyan-400" />
                  <span>Export Feeds to OPML</span>
                </h4>
                <p className="text-xs text-slate-500 dark:text-zinc-400 mb-3">
                  Download an OPML 2.0 XML file containing all your configured sources. You can import this file directly into the SereneSec Android app or other RSS readers.
                </p>
                <button
                  onClick={handleExportOpml}
                  className="px-4 py-2 rounded-xl text-xs font-medium bg-white hover:bg-slate-100 text-slate-800 dark:bg-zinc-800 dark:hover:bg-zinc-700 dark:text-zinc-200 border border-slate-300 dark:border-zinc-700/60 inline-flex items-center space-x-1.5 transition shadow-xs"
                >
                  <Download className="w-3.5 h-3.5" />
                  <span>Export serenesec_sources.opml</span>
                </button>
              </div>

              <div className="p-4 rounded-xl border border-slate-200 dark:border-zinc-800 bg-slate-50 dark:bg-zinc-900/50">
                <h4 className="text-sm font-semibold text-slate-900 dark:text-zinc-200 mb-1 flex items-center space-x-2">
                  <Upload className="w-4 h-4 text-teal-600 dark:text-teal-400" />
                  <span>Import OPML File</span>
                </h4>
                <p className="text-xs text-slate-500 dark:text-zinc-400 mb-3">
                  Select an OPML or XML file exported from the Android app or another reader to bulk-import sources.
                </p>
                <input
                  type="file"
                  accept=".opml,.xml"
                  onChange={handleImportOpml}
                  className="block w-full text-xs text-slate-500 dark:text-zinc-400 file:mr-3 file:py-1.5 file:px-3 file:rounded-lg file:border-0 file:text-xs file:font-medium file:bg-slate-200 hover:file:bg-slate-300 dark:file:bg-zinc-800 dark:file:text-zinc-200 dark:hover:file:bg-zinc-700 cursor-pointer"
                />
              </div>

              <div className="p-4 rounded-xl border border-slate-200 dark:border-zinc-800 bg-slate-50 dark:bg-zinc-900/50">
                <h4 className="text-sm font-semibold text-slate-900 dark:text-zinc-200 mb-1 flex items-center space-x-2">
                  <RefreshCw className="w-4 h-4 text-amber-500" />
                  <span>Restore Built-in Sources</span>
                </h4>
                <p className="text-xs text-slate-500 dark:text-zinc-400 mb-3">
                  Reset built-in feeds to SereneSec's curated catalog (v7, including all Blockchain & Crypto feeds).
                </p>
                <button
                  onClick={handleResetDefaults}
                  className="px-4 py-2 rounded-xl text-xs font-medium bg-white hover:bg-amber-50 text-amber-700 border border-amber-300 dark:bg-zinc-800 dark:hover:bg-amber-950/40 dark:text-amber-300 dark:border-zinc-700/60 inline-flex items-center space-x-1.5 transition shadow-xs"
                >
                  <RefreshCw className="w-3.5 h-3.5" />
                  <span>Restore Default Curated Sources</span>
                </button>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
