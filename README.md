# 🎧 SuperPodcast (Android, Kotlin, Jetpack Compose)

SuperPodcast is a modern Android podcast application built with **Kotlin** and **Jetpack Compose**.  
The app allows users to search podcasts using advanced filters and play podcast episodes directly inside the app.

This project was created as a **college assignment** to practice:
- Android architecture
- API integration
- Modern UI with Jetpack Compose
- Media playback

---

## ✨ Features

- 🔍 **Podcast search** using iTunes Search API  
- 🧠 **Advanced filters**
  - Regular expressions (regex)
  - Minimum number of words in podcast title
- 🎧 **Podcast playback**
  - RSS feed parsing
  - Automatic extraction of audio (MP3) from RSS `<enclosure>`
- ⏯ **Audio controls**
  - Play / Pause
  - Stop
- 🎵 **Mini player**
  - Displays currently playing podcast
  - Control playback from the bottom bar
- 🌙 **Modern dark UI**
  - Built entirely with Jetpack Compose
  - Material 3 design

---

## 🛠 Tech Stack

- **Language:** Kotlin  
- **UI:** Jetpack Compose, Material 3  
- **Architecture:** MVVM  
- **Networking:** Retrofit + Gson  
- **Concurrency:** Kotlin Coroutines  
- **Media playback:** Media3 (ExoPlayer)  
- **RSS parsing:** XmlPullParser (no external RSS dependencies)

---

## 📱 Screens

- **Discover Screen**
  - Podcast categories
  - Trending podcasts
- **Search Screen**
  - Search term input
  - Regex filter
  - Minimum word filter
  - Search results list
- **Mini Player**
  - Play / Pause / Stop
  - Podcast title and author

---

## 🚀 How It Works

1. The app queries the **iTunes Search API** to retrieve podcast metadata.
2. Each podcast provides an **RSS feed URL**.
3. The app parses the RSS feed to extract the audio URL from `<enclosure>`.
4. Audio is played using **ExoPlayer (Media3)**.
5. Playback state is managed through a custom `PlayerManager`.

---


