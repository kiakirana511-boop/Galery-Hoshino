# Hoshino Vault Final Source

Ini versi yang lebih "langsung jadi" dari V2. Bukan cuma mockup, tapi sudah dibuat sebagai source app Android native Java yang bisa dibuka di Android Studio atau dibuild lewat GitHub Actions.

## Fitur utama

### Menu app
- Home
- Gallery
- Voice
- Favorite
- Profile
- Bottom navigation dark premium
- Tema dark navy + soft pink + soft blue

### Gallery
- Import foto/video dari HP
- Grid foto/video real
- Search media
- Filter: All, Cute, Wallpaper, Live, Favorite, Private
- Long press media untuk favorite cepat
- Hidden/private media

### Preview foto
- Foto real fullscreen
- Tap untuk hide/show UI
- Double tap untuk favorite
- Swipe kiri/kanan untuk next/previous
- Swipe down untuk keluar
- Bottom glass panel
- Favorite, Export, Wallpaper, Share
- Play Voice
- Details
- More Menu

### Preview video
- VideoView real
- Play/pause
- Skip 10 detik
- Swipe kiri/kanan
- Swipe down close
- Bottom glass panel
- Favorite, Export, Live Wallpaper, Share
- Attach/play voice
- Details
- More Menu

### Voice
- Import audio/voice
- Play voice line
- Favorite voice
- Attach voice ke foto/video

### More menu
- Add to Album
- Change Tag/Mood
- Attach Voice
- Hide/Unhide Private
- Set Wallpaper / Set Live Wallpaper
- Export
- Share
- View Details
- Rename
- Delete from app list

### Privacy
- App Lock PIN
- Private media toggle
- Hidden media tidak muncul kecuali private mode dibuka

### Export
- Export foto ke Pictures/HoshinoVault
- Export video ke Movies/HoshinoVault

### Wallpaper
- Foto bisa dijadikan wallpaper
- Video bisa dipilih sebagai live wallpaper lewat Android live wallpaper picker

## Cara build di Android Studio

1. Extract ZIP ini.
2. Buka folder `HoshinoVault_Final_Source` di Android Studio.
3. Tunggu Gradle Sync.
4. Klik Run ke emulator/HP.
5. Build APK:
   - `Build > Build Bundle(s) / APK(s) > Build APK(s)`

## Cara build di GitHub

Upload isi folder ini ke repo GitHub. Jangan upload ZIP-nya doang.

Struktur repo harus terlihat:

```text
.github/workflows/build-apk.yml
app/
build.gradle
settings.gradle
gradle.properties
README.md
```

Lalu:
1. Buka tab Actions.
2. Pilih `Build Android APK`.
3. Klik Run workflow.
4. Download artifact `HoshinoVault-Final-debug-apk`.

## Catatan jujur

- Ini sudah jauh lebih jadi dari V2.
- Ini masih native Java tanpa external library, jadi lebih ringan dan gampang diotak-atik.
- Kalau mau super final produksi, tahap berikutnya adalah bikin desain XML/Compose lebih rapi, thumbnail video asli, database Room, dan signing APK release.
