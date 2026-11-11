# SereneSec Web

> High-signal, distraction-free security & Web3 threat intelligence platform. Zero-cost, local-first, privacy-respecting client for desktop, mobile, and web.

## Architecture

- **Local-First & Zero-Cost**: Powered entirely by Dexie.js (IndexedDB). No backend database, zero telemetry, zero ongoing server bills.
- **Edge CORS Proxy**: Includes an Edge Function for both **Vercel** (`api/feed.ts`) and **Cloudflare Pages** (`functions/api/feed.ts`) running globally to fetch XML/RSS/Atom feeds and sanitize WebViews without browser CORS blocks.
- **Anti-Frame-Busting WebView**: Built-in anti-hijack proxy protection defuses frame-busting scripts (`window.top` overrides), uncloaks CSS, and presents a fallback shield card for bot-restricted sites.
- **Distraction-Free Reader Mode**: Integrated Mozilla Readability engine with DOMPurify sanitization.
- **Anti-Doomscroll Design**: Categorized into high-signal domains with an unread queue, calm empty states ("All caught up"), and threat badge pills (CVE identifiers, CISA advisories, 0-day alerts, DeFi exploits).
- **OPML 2.0 Interoperability**: 1-to-1 sync and backup compatibility with the SereneSec Android application.
- **Keyboard First**: Vim/Superhuman shortcuts (`j`/`k`, `Enter`, `s`, `b`, `e`, `/`, `t`, `v`, `?`).

---

## 🚀 One-Click Free Deployment

SereneSec Web is fully configured to deploy on **Vercel** or **Cloudflare Pages** for **$0/month**.

### Option A: Deploy to Vercel (Recommended)

1. Log in to [Vercel](https://vercel.com) and click **"Add New Project"** &rarr; **"Import"** next to your `SereneSEC` GitHub repository.
2. In the project configuration:
   - **Root Directory**: Click **Edit** and select `web`.
   - **Framework Preset**: Vercel will auto-detect `Vite`.
   - **Build Command**: `npm run build` (auto-detected).
   - **Output Directory**: `dist` (auto-detected).
3. Click **Deploy**.
4. That's it! Vercel automatically compiles the Vite frontend and deploys `/api/feed` as a globally distributed Vercel Edge Function with 1,000,000 free invocations per month.

### Option B: Deploy to Cloudflare Pages

1. Go to the [Cloudflare Dashboard](https://dash.cloudflare.com/) &rarr; **Workers & Pages** &rarr; **Create application** &rarr; **Pages** &rarr; **Connect to Git**.
2. Select your `SereneSEC` repository.
3. In the build settings:
   - **Root Directory**: `web`
   - **Framework preset**: `Vite`
   - **Build command**: `npm run build`
   - **Build output directory**: `dist`
4. Click **Save and Deploy**. Cloudflare automatically deploys `functions/api/feed.ts` at the edge with 100,000 free requests per day.

---

## Local Development

```bash
cd web
npm install
npm run dev
```

Open [http://localhost:3000](http://localhost:3000) in your browser. Local Vite dev middleware transparently mocks the edge feed proxy.
