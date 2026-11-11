# SereneSec Web

> High-signal, distraction-free security & Web3 threat intelligence platform. Zero-cost, local-first, privacy-respecting client for desktop, mobile, and web.

## Architecture

- **Local-First & Zero-Cost**: Powered entirely by Dexie.js (IndexedDB). No backend database, zero telemetry, zero ongoing server bills.
- **Edge CORS Proxy**: Includes native Edge Functions for **Netlify** (`netlify/edge-functions/feed.ts`), **Cloudflare Pages** (`functions/api/feed.ts`), and **Vercel** (`api/feed.ts`) running globally to fetch XML/RSS/Atom feeds and sanitize WebViews without browser CORS blocks.
- **Anti-Frame-Busting WebView**: Built-in anti-hijack proxy protection defuses frame-busting scripts (`window.top` overrides), uncloaks CSS, and presents a fallback shield card for bot-restricted sites.
- **Distraction-Free Reader Mode**: Integrated Mozilla Readability engine with DOMPurify sanitization.
- **Anti-Doomscroll Design**: Categorized into high-signal domains with an unread queue, calm empty states ("All caught up"), and threat badge pills (CVE identifiers, CISA advisories, 0-day alerts, DeFi exploits).
- **OPML 2.0 Interoperability**: 1-to-1 sync and backup compatibility with the SereneSec Android application.
- **Keyboard First**: Vim/Superhuman shortcuts (`j`/`k`, `Enter`, `s`, `b`, `e`, `/`, `t`, `v`, `?`).

---

## 🚀 One-Click Free Deployment

SereneSec Web is fully configured to deploy on **Netlify**, **Cloudflare Pages**, or **Vercel** for **$0/month**.

### Option A: Deploy to Netlify (Recommended)

1. Log in to [Netlify](https://app.netlify.com/) and click **"Add new site"** &rarr; **"Import an existing project"** &rarr; **GitHub**.
2. Select your **`AsokaKrsna/SereneSEC`** repository.
3. In the configuration:
   - **Base directory**: `web`
   - **Build command**: `npm run build` (auto-detected via `netlify.toml`)
   - **Publish directory**: `dist` (auto-detected via `netlify.toml`)
4. Click **Deploy SereneSec**.
5. Netlify will auto-deploy the site and activate the Deno-powered Edge Function (`/api/feed`) with **1,000,000 free invocations/month** and **100 GB free bandwidth**.

### Option B: Deploy to Cloudflare Pages

1. Go to the [Cloudflare Dashboard](https://dash.cloudflare.com/) &rarr; **Workers & Pages** &rarr; **Create application** &rarr; **Pages** &rarr; **Connect to Git**.
2. Select your `AsokaKrsna/SereneSEC` repository.
3. In the build settings:
   - **Root Directory**: `web`
   - **Framework preset**: `Vite`
   - **Build command**: `npm run build`
   - **Build output directory**: `dist`
4. Click **Save and Deploy**. Cloudflare automatically deploys `functions/api/feed.ts` with unlimited static asset storage and 100,000 free requests per day.

### Option C: Deploy to Vercel

1. Log in to [Vercel](https://vercel.com) and click **"Add New Project"** &rarr; **"Import"** next to `SereneSEC`.
2. Set **Root Directory** to `web` and click **Deploy**. Vercel uses `api/feed.ts`.

---

## Local Development

```bash
cd web
npm install
npm run dev
```

Open [http://localhost:3000](http://localhost:3000) in your browser. Local Vite dev middleware transparently mocks the edge feed proxy.
