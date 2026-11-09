# SereneSec — Future Development Roadmap (`FUTUREPLAN.md`)

> A strategic roadmap for expanding SereneSec's anti-doomscroll threat intelligence ecosystem across Android and Web with zero-cost, local-first, privacy-respecting architecture.

---

## Phase 1: Offline Superpowers & Analytical Tooling (Near Term)

### 1.1 Full PWA Offline Service Worker (Fly-Safe Reading)
- **Objective**: Ensure the web platform opens instantly and allows reading all cached, saved, and starred intelligence with zero network connectivity (e.g., during flights, air-gapped security operations, or transit).
- **Implementation**:
  - Integrate a Workbox-powered Service Worker (`sw.js`).
  - Cache app shell (HTML, CSS, JS, fonts, icons) with a `CacheFirst` strategy.
  - Pair with Dexie.js IndexedDB storage so full article bodies and metadata persist indefinitely offline.
  - Enable "Install SereneSec" desktop and mobile home screen prompt.

### 1.2 "Export to Markdown / Obsidian" Threat Analyst Exporter
- **Objective**: Streamline the workflow for security analysts, penetration testers, and smart contract auditors who maintain personal knowledge graphs in Obsidian, Logseq, or Notion.
- **Implementation**:
  - One-click "Copy as Markdown Note" in Reader View.
  - Pre-formatted frontmatter template:
    ```markdown
    ---
    title: "Sanctioned Canadian Crypto OTCs Served Iran and Drug Cartels"
    source: "Chainalysis Blog"
    date: 2026-11-10
    url: "https://www.chainalysis.com/blog/ofac-canadian-otcs/"
    category: Blockchain
    tags: [crypto, sanctions, ofac, threat-intel]
    cve: []
    ---

    # Executive Summary
    ...
    ```

---

## Phase 2: Web3 & Threat Intelligence Deepening (Mid Term)

### 2.1 CVE Severity & Exploitability Intel Popover
- **Objective**: Provide instantaneous context when a security researcher spots a CVE badge.
- **Implementation**:
  - When clicking a `CVE-XXXX-XXXX` pill, display an interactive threat card:
    - **NVD CVSS Base Score** (e.g. Critical 9.8 / High 7.5).
    - **EPSS (Exploit Prediction Scoring System)** percentage.
    - **CISA KEV Status**: Badge indicating whether the vulnerability is actively weaponized in the wild.
  - Fetch directly on demand or via local cached lookup table.

### 2.2 Blockchain & Web3 Chain/Vector Auto-Tagging
- **Objective**: Enable smart contract auditors to filter vulnerability disclosures relevant to their current engagement.
- **Implementation**:
  - Auto-tag detected blockchain ecosystems: `#Ethereum`, `#Solana`, `#Arbitrum`, `#Base`, `#Cosmos`, `#Bitcoin`.
  - Auto-tag attack vectors: `#Reentrancy`, `#OracleManipulation`, `#BridgeExploit`, `#AccessControl`, `#FlashLoan`.
  - Add quick sub-filter chips when the `BLOCKCHAIN` category is active.

### 2.3 Integrated Distraction-Free Podcast Player
- **Objective**: Listen to premier audio intelligence (*Darknet Diaries*, *Risky Business*, *Smashing Security*, *Security Now*) without switching apps.
- **Implementation**:
  - Detect `<enclosure type="audio/mpeg">` in RSS payloads.
  - Docked mini-player with Play/Pause, 15s scrub, 1.25x / 1.5x / 2x speed controls, and background playback.

---

## Phase 3: Zero-Cost, Zero-Knowledge Cross-Device Sync (Long Term)

### 3.1 P2P Local Network / QR Code Transfer (No Servers)
- **Objective**: Seamlessly transfer Starred articles, Saved links, custom feeds, and reading states between the Android phone and Desktop Web browser without any cloud databases or accounts.
- **Mechanism**:
  - Web version displays an encrypted WebRTC QR code.
  - Android app scans the QR code with its camera.
  - Direct peer-to-peer data channel syncs IndexedDB and Room SQLite over the local Wi-Fi network in seconds.
  - Cost: **$0**. Privacy: **100% peer-to-peer**; no data ever leaves the local network.

### 3.2 Optional Encrypted Personal Cloud Backup (Google Drive / GitHub Gist / WebDAV)
- **Objective**: Allow users who want automatic multi-device synchronization to bring their own storage.
- **Mechanism**:
  - Client-side AES-GCM encrypted payload (`serenesec_backup.enc`).
  - Synced to the user's personal Google Drive AppData folder or private GitHub Gist via personal API token.
  - Server bills for SereneSec: **$0 forever**.

---

## Phase 4: Anti-Doomscroll Cognitive Safeguards

### 4.1 Daily Threat Briefing / Digest Mode
- **Objective**: Protect engineers from continuous notification anxiety and doomscrolling.
- **Mechanism**:
  - Optional Scheduled Digest (e.g. 9:00 AM and 5:00 PM).
  - Bundles the top 5 highest-signal critical alerts (0-Days, CISA KEV additions, high-impact exploits) into a single calm summary card.
  - Encourages closing the app once reviewed: *"You are caught up. Go build."*
