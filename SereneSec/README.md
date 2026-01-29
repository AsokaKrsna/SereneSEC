# SereneSec

<p align="center">
  <img src="app/src/main/res/mipmap-xxxhdpi/ic_launcher.png" width="120" alt="SereneSec Logo">
</p>

<p align="center">
  <strong>Distraction-Free Cybersecurity Reading Platform</strong>
</p>

<p align="center">
  <a href="#features">Features</a> •
  <a href="#screenshots">Screenshots</a> •
  <a href="#installation">Installation</a> •
  <a href="#sources">Sources</a> •
  <a href="#philosophy">Philosophy</a> •
  <a href="#license">License</a>
</p>

---

## Features

### 🔒 Privacy-First
- Zero analytics, no telemetry, no cloud sync
- All data stays on your device
- No tracking, no user accounts

### 📖 Focus Mode
- Transform any article into a clean, distraction-free reading view
- Powered by Mozilla Readability.js
- Ad-blocking and cookie banner removal

### 📰 Curated Sources
- 30+ pre-configured cybersecurity sources
- **News**: Krebs, Schneier, Troy Hunt, BleepingComputer, The Hacker News
- **Research**: Project Zero, Talos, Unit42, Mandiant, CrowdStrike
- **CVEs**: CISA Alerts, NIST NVD
- **Tools**: GitHub releases for Nuclei, Nmap, Metasploit, and more

### 🎯 Finite Inbox
- No infinite scroll
- Complete your reading session
- "All Caught Up" satisfaction

### 📁 Collections
- Organize articles into custom folders
- Color-coded with emoji icons
- Quick access from bottom navigation

### 🏷️ Auto-Tagging
- Automatic keyword-based article tagging
- Custom tags with configurable keywords
- Easy article discovery

### ⭐ Favorites & Saved
- Bookmark important articles
- Save for later reading
- Quick access from navigation

### 🔔 Smart Notifications
- Breaking news alerts
- Keyword-based notifications
- Customizable quiet hours

### 📊 Analytics Dashboard
- Source health monitoring
- Reading statistics
- Feed performance tracking

### 📱 Multi-Window Support
- Floating window mode (Samsung Pop-up View)
- Picture-in-Picture support
- Split-screen ready

### 🎨 Theming
- Dark and Light modes
- 7 accent color options
- System theme following

### 📤 Share Intent
- Add articles from Chrome or any app
- Quick-add to your reading queue
- Website bookmarking for non-RSS sites

### 🔄 Background Sync
- Configurable intervals (2h, 6h, 12h, manual)
- Battery-efficient WorkManager integration
- WiFi-only option

### 📲 Home Screen Widget
- Glanceable security headlines
- Quick access to new articles

## Screenshots

![](/images/10.jpeg)
![](/images/9.jpeg)
![](/images/8.jpeg)
![](/images/7.jpeg)
![](/images/6.jpeg)
![](/images/5.jpeg)
![](/images/4.jpeg)
![](/images/3.jpeg)
![](/images/2.jpeg)
![](/images/.jpeg)
![](/images/image.png)

## Installation

### Download APK
1. Go to [Releases](../../releases)
2. Download the latest `serenesec-vX.X.X.apk`
3. Install on your Android device (enable "Unknown sources" if needed)

### Build from Source
```bash
git clone https://github.com/yourusername/serenesec.git
cd serenesec
./gradlew assembleRelease
```

APK will be at `app/build/outputs/apk/release/`

### Signing for Release
Create `keystore.properties` in project root:
```properties
storePassword=your_store_password
keyPassword=your_key_password
keyAlias=your_key_alias
storeFile=path/to/your.keystore
```

Then build signed release:
```bash
./gradlew assembleRelease
```

## Sources

SereneSec comes with 30+ curated cybersecurity sources across 4 categories:

| Category | Sources |
|----------|---------|
| **News** | Krebs on Security, Schneier, Troy Hunt, BleepingComputer, The Hacker News, Dark Reading, NCSC UK |
| **Research** | Google Project Zero, Talos Intelligence, Mandiant, CrowdStrike, Unit42, SentinelOne Labs, Securelist |
| **CVE** | CISA Alerts, CISA ICS Advisories, NIST NVD |
| **Tools** | Nuclei, Nmap, Impacket, Metasploit, BloodHound, Subfinder, ffuf (GitHub releases) |

You can:
- ✅ Enable/disable any built-in source
- ✅ Add your own RSS feeds
- ✅ Bookmark non-RSS websites

## Philosophy

> This project optimizes for **calm, trust, and longevity**, not growth metrics.

### Core Principles
- **Zero-Server Architecture**: No backend, no telemetry
- **Finite Consumption Model**: Static feeds, semi-manual refresh
- **Intentional Friction**: Actively resists context switching
- **Creator Respect**: Default mode loads full webpage with ads intact
- **Privacy First**: No analytics SDKs, no tracking

### What SereneSec Don't Do
- ❌ Recommendations
- ❌ User tracking
- ❌ Cross-device sync
- ❌ Content monetization
- ❌ Social features

## Tech Stack

| Component | Technology |
|-----------|------------|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Architecture | Clean Architecture + MVVM |
| Database | Room (SQLite) |
| Networking | OkHttp |
| Background | WorkManager |
| DI | Hilt |
| Reader | Android WebView + Readability.js |

## Requirements

- **Android 8.0** (API 26) or higher
- Internet permission (for fetching feeds)
- Optional: Overlay permission (for floating window)

## Contributing

Contributions are welcome! Please read our contributing guidelines:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

```
SereneSec - Distraction-Free Cybersecurity Reading
Copyright (C) 2026

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published
by the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.
```

See [LICENSE](LICENSE) for full text.

## Legal Notice

SereneSec displays content from third-party sources. All content remains property of original creators. This application does not host, redistribute, or modify content for distribution. Reader transformations occur locally on the user's device for personal use only.

---

<p align="center">
  Made with 💖 for the cybersecurity community. (Mostly vibe coded)
</p>

<p align="center">
  <a href="../../issues">Report Bug</a> •
  <a href="../../issues">Request Feature</a>
</p>
