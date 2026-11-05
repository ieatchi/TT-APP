# Taffy

A quick editor for 9:16 TikTok photo and video posts: stretched photos, blurred backdrops,
the TikTok-yellow caption, a library of 4,000+ quotes, batch posts, Quick post straight to TikTok,
drafts, stats, a watermark, video clips and backups.

**Download / update:** https://github.com/ieatchi/TT-APP/releases/latest/download/Taffy.apk

---

## Updating the app

1. Open the download link above on your phone. It always gives you the newest version.
2. Open the file and tap **Update**. Your drafts and settings stay.
3. If Android warns that the app is unsafe, it's because it's not from the Play Store. Tap **More details → Install anyway**.

**Don't uninstall Taffy** unless you've saved a backup first. Uninstalling deletes your drafts.

## Backups (do this every week or two)

- **Save:** **You** tab → **Save a backup** → pick **Google Drive** (safest) or Files.
- **Restore:** **You** tab → **Restore a backup** → pick the backup file. It adds your drafts, stats and quotes back.
  It never deletes anything, and restoring the same backup twice won't make copies.
- The backup is a normal **.zip**. Your photos and videos are inside the `media` folder, so you can always get them out,
  even without Taffy.

## Getting things into Taffy

- **From your gallery:** select a photo or clip → **Share → Taffy** opens Quick post with it.
  Select several photos → **Share → Taffy** opens Batch.
- **Quotes you find anywhere:** select the text → **Share → Taffy**, and it lands in **Quotes → Mine**.
- **Your own quotes:** **Quotes → Mine** → type one, or paste a whole list (one quote per line).

---

## Change Taffy yourself (from your phone)

Every time you save a change on GitHub, a new version of the app is built automatically
(takes about 3 minutes). Then the download link above gives you the new version.

### How to edit a file

1. Open **github.com/ieatchi/TT-APP** in your browser (turn on *Desktop site* if the pencil button is missing) or in the GitHub app.
2. Tap the file you want (paths are below), then the **pencil** icon to edit.
3. Make the change, tap **Commit changes…**, then **Commit changes** again.
4. Wait about 3 minutes. Check the **Actions** tab: a **green tick** means the new version is ready, a **red X** means it didn't build (see *If something breaks*).
5. Download from the link and update.

### Add quotes to the built-in library

Easiest: don't edit any files. Use **Quotes → Mine** in the app.

To edit the built-in library, open **`www/quotes.js`**. Each category looks like this:

```
"Sunsets": [
"I'll never get tired of this view",
"the sky is painting again",
"sunsets remind me that letting go can be beautiful"
],
```

- Every quote goes inside "double quotes".
- Put a comma after every quote **except the last one** in a category.
- Don't use a plain `"` inside a quote. Use `'` or curly quotes “ ” instead.
- New category: copy a whole block (name, `[`, quotes, `],`) and change the name and quotes.
- Categories listed in `window.CALM_CATS` (the last line of the file) also show up under **Aesthetic**.

If Quotes shows **0** after an edit, there's a typo in `quotes.js`, usually a missing comma or quote mark. The rest of the app still works.

### Change colours

All in **`www/index.html`**:

| What | Find this | Change it to |
|---|---|---|
| The app's red | `--accent:#ff2b3a` | any colour, e.g. `--accent:#8b5cf6` |
| The caption yellow | every `#f7ce46` (8 places) | your colour |
| The darker red (pressed buttons) | `--accent-deep:#d0101e` | a darker shade of your colour |

The app icon's background colour is set in **`.github/workflows/build-apk.yml`** (`--iconBackgroundColor '#ff2b3a'`).
The icon pictures themselves are in the **`assets`** folder (`icon-only.png`, `icon-foreground.png`, `icon-background.png`, `splash.png`).
If you replace them, keep the same file names and sizes.

### Other things you can change (in `www/index.html`)

- **Share lines** ("send this to someone who needs it"…): find `SHARE_LINES`.
- **Hashtags:** find `TAGS_BASE` (added to every post) and `CATTAGS` (per category).
- **Reminder messages:** find `remMsg`.

---

## Never change these

- **`keystore/debug.keystore`**: the app's signing key. If it changes or gets deleted, new versions won't install
  over the old one, and you'd have to uninstall (losing drafts that aren't backed up).
- **`"appId"` in `capacitor.config.json`**: changing it makes Android treat Taffy as a different app.
- **`.github/workflows/build-apk.yml`**: the build recipe. The icon colour line is the only safe thing to change in it.
- **`native/`**: the Android code for posting to TikTok and Share to Taffy.

## If something breaks

- **Red X in Actions:** the version on your phone keeps working. Open **Code → History** (the clock icon),
  tap your last change to see what you changed, edit the file back to how it was, and commit again.
- **The app opens but looks broken after an edit to `index.html`:** undo that edit the same way.
  Every old version is kept in the history, so nothing is ever really lost.

## What's where

| Path | What it is |
|---|---|
| `www/index.html` | the whole app: screens, editor, styles |
| `www/quotes.js` | the quote library |
| `www/fonts/` | fonts (TikTok Sans, Montserrat, Anton) |
| `www/vendor/mp4-muxer.js` | the part that makes MP4 videos |
| `assets/` | app icon, splash screen, notification icon |
| `native/` | Android code: open TikTok with your post, Share to Taffy |
| `.github/workflows/build-apk.yml` | builds the APK after every change |
| `keystore/` | the signing key (don't touch) |

## Run it on a PC

Double-click **`start.bat`**. It opens http://localhost:8080 (needs Python installed).
Posting to TikTok, the gallery, reminders and Share to Taffy only work in the Android app.
