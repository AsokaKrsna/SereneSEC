# SereneSec Web

> High-signal, distraction-free security & Web3 threat intelligence platform. Zero-cost, local-first, privacy-respecting client for desktop, mobile, and web.

## Architecture

- **Local-First & Zero-Cost**: Powered entirely by Dexie.js (IndexedDB). No backend database, zero telemetry, zero ongoing server bills.
- **Edge CORS Proxy**: Includes a Cloudflare Pages Function (`functions/api/feed.ts`) running at the edge to fetch XML/RSS/Atom feeds without browser CORS limitations.
- **Anti-Doomscroll Design**: Categorized into high-signal domains with an unread queue, anti-doomscroll empty states ("All caught up"), and threat badge pills (CVE identifiers, CISA advisories, 0-day alerts, DeFi exploits).
- **Distraction-Free Reader Mode**: Integrated Mozilla Readability engine with DOMPurify sanitization.
- **OPML 2.0 Interoperability**: 1-to-1 sync and backup compatibility with the SereneSec Android application.
- **PWA Ready**: Works offline once cached, installable as a native desktop or mobile PWA.

## Deployment to Cloudflare Pages (Free)

1. Connect your GitHub repository to **Cloudflare Pages**.
2. Configure the build settings:
   - **Framework preset**: `Vite`
   - **Root directory**: `web`
   - **Build command**: `npm run build`
   - **Build output directory**: `dist`
3. Click **Save and Deploy**. Cloudflare automatically activates the edge function at `/api/feed` for zero-cost RSS proxying within the free tier.

## Local Development

```bash
cd web
npm install
npm run dev
```
