# HTML Fullscreen

A tiny Android app that opens `.html` files in a WebView covering the entire screen —
no address bar, no status bar, no navigation bar.

## Features

- Appears in **"Open with"** for HTML files (file managers, downloads, share sheet).
- Launching from the app icon opens a file picker.
- True immersive fullscreen, including the camera-notch area. Swipe from an edge to peek at the system bars.
- JavaScript, localStorage, pinch-zoom and HTML5 fullscreen video work.
- Back button = page history back, then exit.
- No libraries, minSdk 21 (Android 5.0+).

## Build the APK

### Option A – GitHub

1. Create a new GitHub repository and upload all files in this folder (keep the folder structure, including `.github`).
2. Open the **Actions** tab → *Build APK* runs automatically (or press *Run workflow*).
3. When it finishes (~3 min), open the run and download **HTML-Fullscreen-APK** → `app-debug.apk`.
4. Install it on your phone (allow "install unknown apps" when asked).

### Option B – Android Studio

Open this folder in Android Studio → *Build → Build APK(s)*.

## Note

HTML files opened from storage work best when they are self-contained (CSS/JS/images
inline or loaded from the web). Android's file-sharing system usually doesn't give apps
access to sibling files, so relative paths like `img/photo.png` may not load.
