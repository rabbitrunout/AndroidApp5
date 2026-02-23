<div align="center">

# 🎧 SuperPodcast  

Modern Android podcast app built with **Kotlin** & **Jetpack Compose**

![Platform](https://img.shields.io/badge/platform-Android-green)
![Kotlin](https://img.shields.io/badge/Kotlin-1.9-purple)
![Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-blue)
![Architecture](https://img.shields.io/badge/Architecture-MVVM-orange)
![Media3](https://img.shields.io/badge/Player-Media3-success)

</div>

---

## 🚀 Overview

SuperPodcast is a modern Android podcast application built using Jetpack Compose and a clean MVVM architecture.

The app demonstrates:

- Declarative UI with Compose
- API integration with structured networking
- RSS parsing without external RSS libraries
- Media playback using Media3 (ExoPlayer)
- Structured playback state management
- Custom filtering logic (regex & word-count rules)

---

## 🧠 Engineering Highlights

- Implemented full MVVM architecture
- Separated UI, networking, and playback logic layers
- Integrated iTunes Search API via Retrofit
- Built custom RSS parsing using `XmlPullParser`
- Extracted audio URLs directly from RSS `<enclosure>`
- Implemented Media3 (ExoPlayer) for reliable audio playback
- Created a centralized `PlayerManager` to control global playback state
- Designed Compose-based Mini Player component with state observation

---

## ⚙️ Technical Decisions

- **Jetpack Compose** chosen for declarative UI and scalability
- **Media3 (ExoPlayer)** for modern, stable audio playback
- **XmlPullParser** instead of external RSS library for full parsing control
- Custom filter logic implemented for advanced search use cases
- Centralized playback state management to avoid duplicated player instances

---

## ✨ Core Features

### 🔍 Podcast Search

- Search podcasts via **iTunes Search API**
- Regex-based title filtering
- Minimum word-count filter
- Dynamic result rendering

### 🎧 Podcast Playback

- RSS feed parsing per podcast
- Automatic extraction of audio from `<enclosure>`
- Play / Pause / Stop controls
- Background-safe playback management

### 🎵 Mini Player

- Persistent bottom player bar
- Real-time playback state updates
- Current podcast title & author display
- Centralized playback controller

### 🌙 UI & Experience

- Fully Compose-based UI
- Material 3 design system
- Dark theme interface
- State-driven UI updates

---

## 🏗 Architecture

High-level structure:

```
superpodcast/
├── ui/
│   ├── screens/
│   ├── components/
├── viewmodel/
├── network/
├── player/
├── rss/
└── model/
```

Architecture style:

- MVVM
- Retrofit for networking
- Coroutines for async operations
- Media3 for playback
- PlayerManager abstraction layer

---

## 🛠 Tech Stack

- **Language:** Kotlin  
- **UI:** Jetpack Compose + Material 3  
- **Architecture:** MVVM  
- **Networking:** Retrofit + Gson  
- **Concurrency:** Kotlin Coroutines  
- **Media:** Media3 (ExoPlayer)  
- **RSS Parsing:** XmlPullParser  

---

## 🧠 Key Learnings

- Managing playback lifecycle in Android
- Avoiding memory leaks with Media3
- Structuring Compose UI with proper state handling
- Parsing XML manually for better control
- Designing reusable Compose components
- Implementing custom filtering logic with regex

---

## 🔜 Future Improvements

- Download episodes for offline playback
- Persistent playback queue
- Background service + media notification controls
- Podcast favorites & local database storage
- Unit testing layer
- Search result caching

---

## 👩‍💻 Author

Irina Safronova  
Android & iOS Developer  
Kotlin • Swift • Modern Mobile Architecture

