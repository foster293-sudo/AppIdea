# Mindnest 🪺✨

An Android app for catching ideas the moment they show up and sorting them into folders later. It's built for ADHD brains: capturing takes as few taps as possible, the screens stay calm, and mistakes are easy to undo.

## What it does

**Capture fast**
- A big **Capture** button on every screen. The keyboard opens straight away, so you can just type.
- **Save + next** keeps the sheet open, for when several thoughts come at once.
- **Your text is always saved.** If you swipe the sheet away or tap outside it, whatever you typed is kept.
- **Quick Settings tile**: swipe down from anywhere, tap *Capture idea*, type.
- **Home-screen shortcut**: long-press the app icon and pick *Capture*.
- **Share to Mindnest**: share text from any app (browser, messages…) and it lands in your Inbox.

**Sort later**
- New thoughts go to the **Inbox** by default. Each one has a **Sort** button for filing it in one tap.
- **Folders inside folders**, as deep as you like. Each folder gets an emoji and a colour so you can spot it at a glance.
- Breadcrumbs (`Home › Projects › App`) always show where you are. Tap any part to jump there.
- Move ideas and folders anywhere, or create a new folder right from the move sheet.
- Pin important ideas so they stay at the top of their folder.

**Built for ADHD**
- **Undo for everything**: deleting, moving, even deleting a whole folder. You don't get a pile of "are you sure?" pop-ups.
- **"Remember this?"** brings back a random old idea on the Home screen, so good thoughts don't get lost.
- Small rewards: a "captured ✨" counter, haptic feedback, a celebration when your inbox is empty.
- Calm, muted colours, large text and touch targets, plenty of space. Light and dark themes follow your system.
- Search across every idea and folder name.

Everything is stored on your phone in a local database. There's no account and nothing is sent anywhere.

## Getting the APK

Every push runs the **Build APK** GitHub Action. Open the repo's **Actions** tab, pick the latest run, and download the `mindnest-apk` artifact. It contains:

- `app-release.apk` — smaller and faster. Signed with the debug key, so it's fine for installing on your own phone.
- `app-debug.apk`

Copy one to your phone and open it. You'll need to allow "install unknown apps" for your file manager or browser.

## Building locally

Open the folder in Android Studio (Ladybug or newer) and press Run, or:

```bash
./gradlew assembleDebug
# app/build/outputs/apk/debug/app-debug.apk
```

## Tech

Kotlin · Jetpack Compose · Material 3 · Room (SQLite) · minSdk 26 (Android 8.0) · targetSdk 35

```
app/src/main/java/com/mindnest/app/
├── MainActivity.kt          # entry point; handles share + capture intents
├── CaptureTileService.kt    # Quick Settings tile
├── data/                    # Room entities (Folder, Idea), DAO, database
└── ui/
    ├── NestViewModel.kt     # navigation, capture, move, undo logic
    ├── NestApp.kt           # scaffold, FAB, snackbar, sheets
    ├── components/          # cards, tiles, breadcrumbs, swipe-to-delete
    ├── screens/             # Home, Folder, Inbox, Search, sheets & dialogs
    └── theme/               # colours, folder palette, typography
```
