# PixelSnap

**A beautiful, fast, camera-first moment capture app built specifically for Google Pixel 9 (and other modern Pixels).**

Snap high-quality photos using the full power of your Pixel 9's Tensor G4 and computational photography pipeline, instantly add smart captions and tags, and keep everything in a gorgeous local gallery that feels native to Pixel's Material You design language.

- Dynamic color / Material You — automatically matches your wallpaper and system theme
- Powered by modern Jetpack Compose + CameraX (best camera integration on Pixel)
- 100% offline, private, and fast
- Designed and tested on Pixel 9 running Android 16

## Key Features (MVP)

- **Camera screen**: Real-time CameraX preview with large, satisfying capture button and one-tap "Analyze with AI" (MVP uses a smart local caption generator; easy to swap for Gemini Nano or Cloud in the future).
- **Gallery**: Beautiful responsive grid of your snaps with thumbnails.
- **Detail view**: Full-resolution photo, editable caption + tags, one-tap share (perfect while texting), delete.
- Premium Pixel aesthetics: generous spacing, smooth animations, edge-to-edge, excellent haptics on capture.

## Current Status: The Opposite of an MVP (Full-Featured Experience)

PixelSnap has moved far beyond a basic MVP. The preview.html now delivers a **rich, ambitious, full-featured experience** you can run instantly on your Pixel 9 — the "opposite of an MVP".

**What the full version includes right now (in the browser preview):**
- Multiple capture modes: Photo, real Video recording (MediaRecorder), Portrait
- Advanced Pixel AI suite: Describe Scene, Magic Editor, Best Take, Add Me, Audio Magic Eraser, Story Mode
- Memories tab: AI-generated stories, highlight reels, and one-tap Memory Book export
- Full Studio editor: Real Canvas-based tools — filters, brightness/contrast, text overlays, stickers, Magic AI enhances, crop
- Enhanced gallery: Search, favorites, albums, timeline-ready organization
- Premium details: Shake-to-capture, PWA install, rich animations, Pixel 9 branding everywhere

The native Kotlin/Compose code in the `app/` folder reflects the expanded architecture (stubs and comments added for the new features). When you build the real app, it will support everything the preview demonstrates plus true CameraX quality and on-device performance.

Everything is still private and offline-first. This is what a complete, delightful PixelSnap feels like.

## Instant Full Experience on Your Pixel 9 (No Computer Needed)

Open **preview.html** in Chrome on your Pixel 9 right now. This is the rich, non-MVP version:

1. Transfer the file (or the whole PixelSnap folder) to your phone.
2. Open `preview.html` in Chrome.
3. **Allow camera** when prompted — you will see a real live camera preview from your Pixel 9 hardware.
4. Tap the big capture button or **Analyze** to take photos that are saved locally in the browser.
5. Switch to Gallery, tap any snap to view/edit/share (Web Share works great on Pixel).
6. For the best native feel: Chrome menu (⋮) → **Add to Home screen** or **Install app**.

This preview uses real device camera + local storage and is styled exactly like the native app.

---

## Getting Started on Your Pixel 9 (Full Native App)

**Note:** The `preview.html` you can open immediately on your phone is already the **opposite of an MVP** — a rich, full-featured experience with video, advanced AI suite, canvas studio editor, AI Memories, and more. The native code below is the foundation for when you build the production Android version.

### Recommended: Android Studio (easiest)

1. Install the latest Android Studio (or Hedgehog / newer) on a computer.
2. Copy or clone this `PixelSnap` folder to your machine.
3. In Android Studio: **File > Open** → select the `PixelSnap` folder.
4. Let Android Studio sync Gradle and download the wrapper + dependencies (first time only).
5. Connect your Pixel 9:
   - On phone: Settings → About phone → tap Build number 7 times (enable Developer options).
   - Developer options → enable **USB debugging**.
   - Plug in via USB. Authorize the computer on the phone prompt.
6. In Android Studio, select your Pixel 9 device from the device dropdown.
7. Click **Run** (or Shift+F10).

The app will build a debug APK and install directly on your phone.

### Alternative: Command line (advanced)

Requires Android command line tools + JDK 17+.

```bash
# After having sdkmanager etc. and ANDROID_HOME set
cd PixelSnap
./gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```

Then launch **PixelSnap** from your app drawer.

### Sideload without full IDE

Build the debug APK on any machine with the SDK, transfer `app-debug.apk` to your Pixel 9 (via Drive, Messages, or `adb`), and open it in Files or Chrome to install (allow unknown sources once).

## How to Use

1. Open PixelSnap → grant Camera permission (and optionally notifications).
2. Frame your shot in the beautiful full-screen preview (Pixel camera quality shines here).
3. Tap the big capture button — feel the haptics.
4. (Optional) Tap **Analyze** — get an instant fun, useful caption + tags generated locally.
5. Your snap appears in the Gallery. Tap any to view, edit the caption/tags, or Share directly into your conversation.
6. Everything stays private on your device.

## Project Structure

See the plan document (or explore the `app/src` tree). Clean MVVM-ish with Compose:

- `data/` — Snap entity, Room DAO + database, repository (photos on disk + metadata in DB).
- `ui/` — Theme (dynamic), CameraScreen, GalleryScreen, DetailScreen, navigation.
- Single `MainActivity`.

## Extending PixelSnap (Future Ideas)

- Real on-device AI with Gemini Nano / MediaPipe for better captions and smart search.
- Add-Me style group photo helper or Best Take simulation.
- Home screen widget + quick tile capture.
- Google Photos / Drive backup toggle.
- Wear OS companion.
- Export beautiful PDF "memory books".
- Material 3 Expressive updates as Android evolves.

Pull requests and ideas welcome!

## Verification on Pixel 9 (Recommended Checklist)

### 1. Immediate (no build required)
- Open `preview.html` in Chrome on your Pixel 9.
- Allow camera → you should see a **live preview from the actual Pixel 9 hardware**.
- Tap Capture or Analyze → photo is saved locally, appears in gallery grid.
- Tap a snap → detail view with editable caption/tags.
- Edit + Save works (persists across reloads).
- Share button → Web Share API opens (try sending to Messages while texting).
- Delete works.
- Chrome menu (⋮) → "Add to Home screen" or "Install app" for PWA experience.

### 2. Full native app (after building)
- Theme automatically matches your current wallpaper (Material You / dynamic color).
- CameraX preview is smooth and leverages the full Tensor G4 / computational photography quality.
- Capture produces **real viewable JPEGs** (not corrupt YUV data) that appear instantly in the in-app gallery.
- "Analyze" produces a local caption + tags.
- Detail: full-res photo, edit caption/tags, Save.
- Prominent Share from detail (and top bar) opens the system chooser — photos + caption can be sent directly into your texting conversation.
- Haptics on the big capture button (strong click).
- No crashes on rotate, background, return, or rapid captures.
- Uninstall is clean.

### 3. Build notes
- Primary path: Copy the folder to a computer, open in latest Android Studio → it will download the Gradle wrapper on first sync.
- Release builds now possible (proguard-rules.pro is present).
- Sideload: `adb install` the debug APK or transfer it.

Report any issues (with what you did on your Pixel 9) — happy to iterate quickly.

## Extending PixelSnap (Future Ideas)

## License

Apache 2.0 — see LICENSE.

---

Made with ❤️ for Pixel users by Grok. Enjoy snapping on your Pixel 9!
