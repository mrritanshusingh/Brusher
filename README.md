# 🧭 Brusher - A Feature-Rich Android Web Browser

Brusher is a custom web browser for Android, built with **Kotlin**. It's designed to be a lightweight yet powerful alternative, offering a clean user interface and essential browsing features.  
The project demonstrates core Android development concepts, including **Fragment management**, **WebView integration**, and **local data persistence with SQLite**.

---

## ✨ Key Features

- **Dynamic Tabbed Browsing**: Open and manage multiple tabs simultaneously. The tab state is saved and restored between sessions.
- **Persistent Sessions**: Open tabs and active tab states are automatically saved and restored.
- **Bookmark Management**: Save, view, and remove bookmarks easily.
- **Browsing History**: Tracks visited pages with timestamps; allows revisiting or clearing history.
- **Desktop Mode**: View desktop versions of websites.
- **File Downloads**: Supports downloading files using Android’s native Download Manager.
- **Full-Screen Video**: Immersive video playback experience.
- **Pop-up Window Handling**: Opens pop-ups gracefully in new tabs.
- **Search & URL Bar**: Unified bar for entering URLs or searching the web.

---

## 🛠️ Built With

- **Kotlin** — Primary programming language.  
- **Android SDK** — Core development platform.  
- **WebView** — For rendering web pages.  
- **Fragments** — Modular, responsive UI components (Browse, Bookmarks, History, Settings).  
- **RecyclerView** — Efficient lists for tabs, bookmarks, and history.  
- **SQLite** — Local storage for bookmarks and history.  
- **View Binding** — Safe and easy access to UI elements.

---

## 📂 Code Structure Overview

| File | Description |
|------|--------------|
| **MainActivity.kt** | Main entry point; manages UI, fragments, and tab state. |
| **BrowseFrag.kt** | Core browsing fragment; handles WebView, navigation, JS, and full-screen video. |
| **DBCenter.kt** | SQLite database helper for bookmarks and history (CRUD operations). |
| **BookmarksFrag.kt** | Displays saved bookmarks in a RecyclerView; allows open/remove. |
| **HistoryFrag.kt** | Shows browsing history; supports revisit and clear options. |
| **SettingsFrag.kt** | Displays storage info (Cache, Local Storage). |
| **TabsRecyAdapter.kt** | RecyclerView adapter for open tabs in tab switcher UI. |
| **BrowseTabsInstanceInfo.kt** | Parcelable data class for tab state preservation. |
| **BookmarksDS.kt** | Data class for bookmark record. |
| **HistoryDS.kt** | Data class for history record. |

---

## 🚀 Getting Started

Follow these steps to run the project locally.

### Prerequisites

- Android Studio (latest version recommended)  
- Git

### Installation

1. **Clone the repository:**  
   ```bash
   git clone https://github.com/mrritanshusingh/Brusher.git
   ```

2. **Open in Android Studio:**  
   - Launch Android Studio.  
   - Select **"Open an existing project"**.  
   - Navigate to the cloned directory and open it.

3. **Build the Project:**  
   - Android Studio will sync Gradle automatically.  
   - Click **Run ▶️** to build and install the app on an emulator or device.

---


**Ritanshu Singh** — mrritanshusingh@gmail.com  
**Project Link:** [https://github.com/mrritanshusingh/Brusher](https://github.com/mrritanshusingh/Brusher)
