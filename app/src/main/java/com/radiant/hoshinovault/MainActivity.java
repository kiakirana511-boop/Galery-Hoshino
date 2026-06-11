package com.radiant.hoshinovault;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.WallpaperManager;
import android.content.ClipData;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageDecoder;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.database.Cursor;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity {
    private static final int PICK_MEDIA = 1001;
    private static final int PICK_VOICE = 1002;

    private final int BG = Color.rgb(5, 9, 22);
    private final int CARD = Color.argb(220, 18, 25, 50);
    private final int CARD2 = Color.argb(194, 24, 32, 62);
    private final int STROKE = Color.argb(70, 210, 220, 255);
    private final int WHITE = Color.rgb(245, 247, 255);
    private final int MUTED = Color.rgb(160, 169, 195);
    private final int PINK = Color.rgb(255, 139, 183);
    private final int BLUE = Color.rgb(142, 167, 255);
    private final int PURPLE = Color.rgb(184, 146, 255);
    private final int RED = Color.rgb(255, 90, 116);

    private FrameLayout root;
    private VaultStore store;
    private ArrayList<MediaItem> media = new ArrayList<>();
    private ArrayList<VoiceItem> voices = new ArrayList<>();
    private MediaPlayer voicePlayer;

    private int activeTab = 0;
    private final String[] tabs = {"Home", "Gallery", "Voice", "Favorite", "Profile"};
    private final String[] tabIcons = {"⌂", "▧", "≋", "♡", "♙"};

    private String activeFilter = "All";
    private String searchQuery = "";
    private boolean controlsVisible = true;
    private float downX, downY;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setupWindow();
        store = new VaultStore(this);
        media = store.loadMedia();
        voices = store.loadVoices();

        root = new FrameLayout(this);
        setContentView(root);

        if (store.appLockEnabled()) buildLockScreen();
        else buildMain();
    }

    private void setupWindow() {
        Window w = getWindow();
        w.setStatusBarColor(Color.TRANSPARENT);
        w.setNavigationBarColor(BG);
        if (Build.VERSION.SDK_INT >= 30) {
            WindowInsetsController c = w.getInsetsController();
            if (c != null) c.hide(WindowInsets.Type.statusBars());
        }
    }

    private void buildLockScreen() {
        root.removeAllViews();
        root.addView(new GradientBackgroundView(this), new FrameLayout.LayoutParams(-1, -1));

        LinearLayout card = glassPanel();
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER_HORIZONTAL);
        card.setPadding(dp(22), dp(24), dp(22), dp(24));

        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(-1, dp(310), Gravity.CENTER);
        lp.setMargins(dp(28), 0, dp(28), 0);
        root.addView(card, lp);

        FrameLayout logo = cardFrame();
        logo.addView(new MiniLogoView(this), new FrameLayout.LayoutParams(-1, -1));
        card.addView(logo, new LinearLayout.LayoutParams(dp(92), dp(92)));

        TextView title = text("Hoshino Vault", 27, WHITE, true);
        title.setGravity(Gravity.CENTER);
        card.addView(title, new LinearLayout.LayoutParams(-1, dp(48)));

        TextView sub = text("Masukin PIN buat buka private gallery.", 13, MUTED, false);
        sub.setGravity(Gravity.CENTER);
        card.addView(sub, new LinearLayout.LayoutParams(-1, dp(34)));

        EditText pin = new EditText(this);
        pin.setTextColor(WHITE);
        pin.setHintTextColor(MUTED);
        pin.setHint("PIN");
        pin.setTextSize(18);
        pin.setGravity(Gravity.CENTER);
        pin.setSingleLine(true);
        pin.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        pin.setBackground(roundBg(Color.argb(100, 10, 16, 34), dp(18), STROKE));
        LinearLayout.LayoutParams pp = new LinearLayout.LayoutParams(-1, dp(54));
        pp.setMargins(0, dp(10), 0, dp(12));
        card.addView(pin, pp);

        TextView unlock = actionButton("Unlock", PINK);
        card.addView(unlock, new LinearLayout.LayoutParams(-1, dp(48)));
        unlock.setOnClickListener(v -> {
            if (pin.getText().toString().equals(store.getPin())) buildMain();
            else toast("PIN salah.");
        });
    }

    private void buildMain() {
        controlsVisible = true;
        root.removeAllViews();
        root.addView(new GradientBackgroundView(this), new FrameLayout.LayoutParams(-1, -1));

        LinearLayout main = new LinearLayout(this);
        main.setOrientation(LinearLayout.VERTICAL);
        main.setPadding(dp(20), dp(20), dp(20), dp(92));
        root.addView(main, new FrameLayout.LayoutParams(-1, -1));

        if (activeTab == 0) buildHome(main);
        else if (activeTab == 1) buildGallery(main, false);
        else if (activeTab == 2) buildVoice(main);
        else if (activeTab == 3) buildGallery(main, true);
        else buildProfile(main);

        buildBottomNav();
    }

    private void buildHome(LinearLayout parent) {
        topTitle(parent, "Welcome back,", "Sensei • Final app source", "Import", v -> pickMedia());

        ScrollView scroll = new ScrollView(this);
        parent.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1f));
        LinearLayout content = vertical();
        scroll.addView(content);

        FrameLayout hero = cardFrame();
        content.addView(hero, new LinearLayout.LayoutParams(-1, dp(260)));

        int first = firstVisibleMedia();
        if (first >= 0) {
            MediaItem item = media.get(first);
            addMediaVisual(hero, item, true);
            addDark(hero, 70);

            TextView title = text(item.title, 28, WHITE, true);
            FrameLayout.LayoutParams tlp = new FrameLayout.LayoutParams(-1, dp(42), Gravity.BOTTOM | Gravity.LEFT);
            tlp.setMargins(dp(18), 0, dp(18), dp(52));
            hero.addView(title, tlp);

            TextView sub = text(item.category + " • " + item.album + " • tap preview", 14, WHITE, false);
            FrameLayout.LayoutParams slp = new FrameLayout.LayoutParams(-1, dp(30), Gravity.BOTTOM | Gravity.LEFT);
            slp.setMargins(dp(18), 0, dp(18), dp(22));
            hero.addView(sub, slp);

            hero.setOnClickListener(v -> openPreview(first));
        } else {
            addEmptyHero(hero);
        }

        LinearLayout row1 = horizontal();
        row1.setPadding(0, dp(18), 0, 0);
        content.addView(row1, new LinearLayout.LayoutParams(-1, dp(110)));
        row1.addView(shortcut("Gallery", "View photos/videos", "▧", BLUE, v -> { activeTab = 1; buildMain(); }), new LinearLayout.LayoutParams(0, -1, 1f));
        addSpace(row1, 12, 1, false);
        row1.addView(shortcut("Voice", "Listen voice", "≋", PINK, v -> { activeTab = 2; buildMain(); }), new LinearLayout.LayoutParams(0, -1, 1f));

        LinearLayout row2 = horizontal();
        row2.setPadding(0, dp(12), 0, 0);
        content.addView(row2, new LinearLayout.LayoutParams(-1, dp(104)));
        row2.addView(shortcut("Favorite", "Saved collection", "♥", PINK, v -> { activeTab = 3; buildMain(); }), new LinearLayout.LayoutParams(0, -1, 1f));
        addSpace(row2, 12, 1, false);
        row2.addView(shortcut("Private", store.showPrivate() ? "Private visible" : "Hidden locked", "▣", PURPLE, v -> togglePrivate()), new LinearLayout.LayoutParams(0, -1, 1f));

        content.addView(section("Today's Moment"));
        LinearLayout moment = glassPanel();
        moment.setOrientation(LinearLayout.VERTICAL);
        moment.setPadding(dp(18), dp(15), dp(18), dp(15));
        content.addView(moment, new LinearLayout.LayoutParams(-1, dp(118)));
        moment.addView(text("Evening with Hoshino", 18, WHITE, true));
        moment.addView(text(visibleCount() + " media • " + countFavorites() + " favorites • " + voices.size() + " voices", 13, MUTED, false));
        moment.addView(text("Preview foto/video udah punya panel glass, voice, favorite, share, wallpaper, detail.", 12, MUTED, false));
    }

    private void buildGallery(LinearLayout parent, boolean onlyFavorites) {
        topTitle(parent, onlyFavorites ? "Favorite" : "Gallery",
                onlyFavorites ? "Foto/video favorit lu." : "Foto/video yang lu import.",
                "Search", v -> searchDialog());
        addChips(parent, onlyFavorites);

        ScrollView scroll = new ScrollView(this);
        parent.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1f));

        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(2);
        grid.setPadding(0, dp(10), 0, dp(20));
        scroll.addView(grid);

        int screenW = getResources().getDisplayMetrics().widthPixels - dp(52);
        int tileW = (screenW - dp(12)) / 2;
        int added = 0;

        for (int i = 0; i < media.size(); i++) {
            MediaItem item = media.get(i);
            if (!passesFilter(item, onlyFavorites)) continue;

            View tile = mediaTile(item, i);
            GridLayout.LayoutParams glp = new GridLayout.LayoutParams();
            glp.width = tileW;
            glp.height = dp(236);
            glp.setMargins(dp(3), dp(7), dp(9), dp(14));
            grid.addView(tile, glp);
            added++;
        }

        if (added == 0) {
            scroll.removeAllViews();
            scroll.addView(emptyState(onlyFavorites ? "Belum ada favorite" : "Belum ada media",
                    onlyFavorites ? "Pencet heart di preview buat masukin favorit." : "Tekan Import buat masukin foto/video."));
        }

        TextView importFab = actionButton("+ Import", PINK);
        FrameLayout.LayoutParams flp = new FrameLayout.LayoutParams(dp(124), dp(48), Gravity.BOTTOM | Gravity.RIGHT);
        flp.setMargins(0, 0, dp(24), dp(104));
        root.addView(importFab, flp);
        importFab.setOnClickListener(v -> pickMedia());
    }

    private void buildVoice(LinearLayout parent) {
        topTitle(parent, "Voice", "Import audio dan attach ke foto/video.", "Import", v -> pickVoice());

        ScrollView scroll = new ScrollView(this);
        parent.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1f));
        LinearLayout content = vertical();
        scroll.addView(content);

        LinearLayout player = glassPanel();
        player.setOrientation(LinearLayout.VERTICAL);
        player.setPadding(dp(18), dp(18), dp(18), dp(18));
        content.addView(player, new LinearLayout.LayoutParams(-1, dp(190)));

        player.addView(text(voices.size() > 0 ? voices.get(0).title : "No voice imported", 20, WHITE, true));
        player.addView(new WaveformView(this), new LinearLayout.LayoutParams(-1, dp(58)));
        player.addView(text("Voice line bisa dipilih dari preview foto/video lewat Attach Voice.", 13, MUTED, false));

        LinearLayout row = horizontal();
        row.setPadding(0, dp(10), 0, 0);
        player.addView(row);
        row.addView(actionButton("▶ Play", PINK), new LinearLayout.LayoutParams(0, dp(46), 1f));
        row.getChildAt(0).setOnClickListener(v -> {
            if (voices.size() > 0) playVoice(Uri.parse(voices.get(0).uri));
            else toast("Import voice dulu.");
        });
        addSpace(row, 10, 1, false);
        row.addView(actionButton("+ Import", BLUE), new LinearLayout.LayoutParams(0, dp(46), 1f));
        row.getChildAt(2).setOnClickListener(v -> pickVoice());

        content.addView(section("Voice Lines"));

        if (voices.size() == 0) {
            content.addView(emptyState("Belum ada voice", "Import MP3/WAV/M4A buat voice line."));
            return;
        }

        for (int i = 0; i < voices.size(); i++) {
            VoiceItem item = voices.get(i);
            LinearLayout r = glassPanel();
            r.setOrientation(LinearLayout.HORIZONTAL);
            r.setGravity(Gravity.CENTER_VERTICAL);
            r.setPadding(dp(14), dp(10), dp(12), dp(10));
            LinearLayout.LayoutParams rlp = new LinearLayout.LayoutParams(-1, dp(68));
            rlp.setMargins(0, 0, 0, dp(10));
            content.addView(r, rlp);

            r.addView(circleText("▶", PINK), new LinearLayout.LayoutParams(dp(46), dp(46)));

            LinearLayout mid = vertical();
            mid.setPadding(dp(12), 0, 0, 0);
            r.addView(mid, new LinearLayout.LayoutParams(0, -1, 1f));
            mid.addView(text(item.title, 15, WHITE, true));
            mid.addView(text(item.category + " • " + item.date, 12, MUTED, false));

            TextView heart = text(item.favorite ? "♥" : "♡", 24, item.favorite ? PINK : MUTED, true);
            heart.setGravity(Gravity.CENTER);
            r.addView(heart, new LinearLayout.LayoutParams(dp(44), -1));

            final int idx = i;
            r.setOnClickListener(v -> playVoice(Uri.parse(voices.get(idx).uri)));
            heart.setOnClickListener(v -> {
                voices.get(idx).favorite = !voices.get(idx).favorite;
                save();
                buildMain();
            });
        }
    }

    private void buildProfile(LinearLayout parent) {
        topTitle(parent, "Profile", "Theme, privacy, stats, settings.", null, null);

        ScrollView scroll = new ScrollView(this);
        parent.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1f));
        LinearLayout content = vertical();
        scroll.addView(content);

        LinearLayout profile = glassPanel();
        profile.setOrientation(LinearLayout.HORIZONTAL);
        profile.setPadding(dp(16), dp(16), dp(16), dp(16));
        content.addView(profile, new LinearLayout.LayoutParams(-1, dp(136)));

        FrameLayout logo = cardFrame();
        logo.addView(new MiniLogoView(this), new FrameLayout.LayoutParams(-1, -1));
        profile.addView(logo, new LinearLayout.LayoutParams(dp(96), dp(96)));

        LinearLayout info = vertical();
        info.setPadding(dp(16), dp(4), 0, 0);
        profile.addView(info, new LinearLayout.LayoutParams(0, -1, 1f));
        info.addView(text("Hoshino Vault", 23, WHITE, true));
        info.addView(text("Private anime gallery", 13, MUTED, false));
        info.addView(text("Final Source • V3", 12, MUTED, false));
        info.addView(text("Dark navy + soft pink/blue", 12, MUTED, false));

        content.addView(section("Collection"));
        content.addView(settingRow("Saved Media", String.valueOf(media.size()), "▧", v -> {}));
        content.addView(settingRow("Visible Media", String.valueOf(visibleCount()), "◉", v -> {}));
        content.addView(settingRow("Favorites", String.valueOf(countFavorites()), "♥", v -> {}));
        content.addView(settingRow("Voice Lines", String.valueOf(voices.size()), "≋", v -> {}));

        content.addView(section("Privacy"));
        content.addView(settingRow("App Lock", store.appLockEnabled() ? "On" : "Off", "▣", v -> pinDialog()));
        content.addView(settingRow("Private Media", store.showPrivate() ? "Visible" : "Hidden", "◌", v -> togglePrivate()));

        content.addView(section("Media"));
        content.addView(settingRow("Import Media", "Photos/Videos", "+", v -> pickMedia()));
        content.addView(settingRow("Import Voice", "Audio", "+", v -> pickVoice()));
        content.addView(settingRow("Clear App List", "Reset", "⌫", v -> confirmClear()));
    }

    private void openPreview(int index) {
        if (index < 0 || index >= media.size()) return;
        controlsVisible = true;
        if ("VIDEO".equals(media.get(index).type)) videoPreview(index);
        else photoPreview(index);
    }

    private void photoPreview(int index) {
        stopVoice();
        root.removeAllViews();
        root.addView(new GradientBackgroundView(this), new FrameLayout.LayoutParams(-1, -1));
        MediaItem item = media.get(index);

        ZoomImageView img = new ZoomImageView(this);
        img.setBackgroundColor(Color.BLACK);
        img.setImageURI(Uri.parse(item.uri));
        root.addView(img, new FrameLayout.LayoutParams(-1, -1));

        img.setListener(new ZoomImageView.Listener() {
            public void onSingleTap() {
                controlsVisible = !controlsVisible;
                photoPreview(index);
            }
            public void onDoubleTap() {
                item.favorite = !item.favorite;
                save();
                toast(item.favorite ? "Added to Favorite" : "Removed Favorite");
                photoPreview(index);
            }
            public void onSwipeLeft() { openPreview(nextVisible(index, true)); }
            public void onSwipeRight() { openPreview(nextVisible(index, false)); }
            public void onSwipeDown() { buildMain(); }
        });

        if (controlsVisible) root.addView(previewOverlay(index, false, null), new FrameLayout.LayoutParams(-1, -1));
    }

    private void videoPreview(int index) {
        stopVoice();
        root.removeAllViews();
        root.addView(new GradientBackgroundView(this), new FrameLayout.LayoutParams(-1, -1));
        MediaItem item = media.get(index);

        VideoView video = new VideoView(this);
        video.setVideoURI(Uri.parse(item.uri));
        MediaController controller = new MediaController(this);
        controller.setAnchorView(video);
        video.setMediaController(controller);
        root.addView(video, new FrameLayout.LayoutParams(-1, -1));
        video.setOnPreparedListener(mp -> {
            mp.setLooping(true);
            video.start();
        });

        if (controlsVisible) root.addView(previewOverlay(index, true, video), new FrameLayout.LayoutParams(-1, -1));

        root.setOnTouchListener((v, e) -> {
            if (e.getAction() == MotionEvent.ACTION_DOWN) {
                downX = e.getX();
                downY = e.getY();
                return true;
            }
            if (e.getAction() == MotionEvent.ACTION_UP) {
                float dx = e.getX() - downX;
                float dy = e.getY() - downY;
                if (dy > dp(130)) {
                    try { video.stopPlayback(); } catch (Exception ignored) {}
                    buildMain();
                    return true;
                }
                if (Math.abs(dx) > dp(120) && Math.abs(dx) > Math.abs(dy)) {
                    try { video.stopPlayback(); } catch (Exception ignored) {}
                    if (dx < 0) openPreview(nextVisible(index, true));
                    else openPreview(nextVisible(index, false));
                    return true;
                }
                controlsVisible = !controlsVisible;
                videoPreview(index);
                return true;
            }
            return true;
        });
    }

    private FrameLayout previewOverlay(int index, boolean isVideo, VideoView video) {
        MediaItem item = media.get(index);
        FrameLayout overlay = new FrameLayout(this);

        LinearLayout top = horizontal();
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.setPadding(dp(16), dp(24), dp(16), dp(8));
        top.setBackgroundColor(Color.argb(85, 0, 0, 0));
        overlay.addView(top, new FrameLayout.LayoutParams(-1, dp(88), Gravity.TOP));

        TextView back = circleText("‹", WHITE);
        top.addView(back, new LinearLayout.LayoutParams(dp(48), dp(48)));
        back.setOnClickListener(v -> buildMain());

        TextView title = text(isVideo ? "Video Preview" : "Photo Preview", 17, WHITE, true);
        title.setGravity(Gravity.CENTER_VERTICAL);
        title.setPadding(dp(12), 0, 0, 0);
        top.addView(title, new LinearLayout.LayoutParams(0, -1, 1f));

        TextView fav = circleText(item.favorite ? "♥" : "♡", item.favorite ? PINK : WHITE);
        top.addView(fav, new LinearLayout.LayoutParams(dp(48), dp(48)));
        fav.setOnClickListener(v -> {
            item.favorite = !item.favorite;
            save();
            openPreview(index);
        });
        addSpace(top, 8, 1, false);

        TextView more = circleText("⋯", WHITE);
        top.addView(more, new LinearLayout.LayoutParams(dp(48), dp(48)));
        more.setOnClickListener(v -> moreMenu(index));

        if (isVideo) {
            LinearLayout vc = horizontal();
            vc.setGravity(Gravity.CENTER);
            overlay.addView(vc, new FrameLayout.LayoutParams(-1, dp(94), Gravity.CENTER));

            TextView back10 = circleText("↺10", WHITE);
            TextView play = circleText("▶/Ⅱ", WHITE);
            TextView next10 = circleText("10↻", WHITE);

            vc.addView(back10, new LinearLayout.LayoutParams(dp(62), dp(62)));
            addSpace(vc, 22, 1, false);
            vc.addView(play, new LinearLayout.LayoutParams(dp(78), dp(78)));
            addSpace(vc, 22, 1, false);
            vc.addView(next10, new LinearLayout.LayoutParams(dp(62), dp(62)));

            back10.setOnClickListener(v -> { if (video != null) video.seekTo(Math.max(0, video.getCurrentPosition() - 10000)); });
            next10.setOnClickListener(v -> { if (video != null) video.seekTo(video.getCurrentPosition() + 10000); });
            play.setOnClickListener(v -> {
                if (video == null) return;
                if (video.isPlaying()) video.pause(); else video.start();
            });
        }

        LinearLayout panel = glassPanel();
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(18), dp(14), dp(18), dp(14));
        FrameLayout.LayoutParams plp = new FrameLayout.LayoutParams(-1, dp(292), Gravity.BOTTOM);
        plp.setMargins(dp(16), 0, dp(16), dp(16));
        overlay.addView(panel, plp);

        TextView drag = text("━━━━", 15, Color.argb(160, 225, 230, 255), true);
        drag.setGravity(Gravity.CENTER);
        panel.addView(drag, new LinearLayout.LayoutParams(-1, dp(18)));
        drag.setOnClickListener(v -> detailsDialog(index));

        panel.addView(text(item.title, 24, WHITE, true), new LinearLayout.LayoutParams(-1, dp(36)));
        String meta = item.date + " • " + item.category + " • " + item.album + (item.hidden ? " • Private" : "");
        panel.addView(text(meta, 12, MUTED, false), new LinearLayout.LayoutParams(-1, dp(26)));

        LinearLayout actions = horizontal();
        panel.addView(actions, new LinearLayout.LayoutParams(-1, dp(82)));

        actions.addView(previewAction("♥", "Favorite", PINK, v -> {
            item.favorite = !item.favorite;
            save();
            openPreview(index);
        }), new LinearLayout.LayoutParams(0, -1, 1f));
        addSpace(actions, 8, 1, false);

        actions.addView(previewAction("⇩", "Export", BLUE, v -> exportMedia(item)), new LinearLayout.LayoutParams(0, -1, 1f));
        addSpace(actions, 8, 1, false);

        actions.addView(previewAction("▧", isVideo ? "Live Wall" : "Wallpaper", BLUE, v -> {
            if (isVideo) setLiveWallpaper(item);
            else setPhotoWallpaper(item);
        }), new LinearLayout.LayoutParams(0, -1, 1f));
        addSpace(actions, 8, 1, false);

        actions.addView(previewAction("⌯", "Share", BLUE, v -> shareMedia(item)), new LinearLayout.LayoutParams(0, -1, 1f));

        LinearLayout voiceCard = glassPanel();
        voiceCard.setGravity(Gravity.CENTER_VERTICAL);
        voiceCard.setOrientation(LinearLayout.HORIZONTAL);
        voiceCard.setPadding(dp(12), dp(8), dp(12), dp(8));
        panel.addView(voiceCard, new LinearLayout.LayoutParams(-1, 0, 1f));

        TextView playVoice = circleText("▶", PINK);
        voiceCard.addView(playVoice, new LinearLayout.LayoutParams(dp(50), dp(50)));

        LinearLayout mid = vertical();
        mid.setPadding(dp(12), 0, 0, 0);
        voiceCard.addView(mid, new LinearLayout.LayoutParams(0, -1, 1f));

        String vtitle = item.voiceTitle != null ? item.voiceTitle : (voices.size() > 0 ? voices.get(0).title : "No voice attached");
        mid.addView(text(vtitle, 16, WHITE, true));
        mid.addView(text(item.voiceUri != null ? "Attached Voice" : "Default voice line", 12, MUTED, false));

        playVoice.setOnClickListener(v -> playMediaVoice(item));

        TextView details = text("Details", 13, BLUE, true);
        details.setGravity(Gravity.CENTER);
        voiceCard.addView(details, new LinearLayout.LayoutParams(dp(70), -1));
        details.setOnClickListener(v -> detailsDialog(index));

        return overlay;
    }

    private View previewAction(String icon, String label, int color, View.OnClickListener click) {
        LinearLayout box = glassPanel();
        box.setOrientation(LinearLayout.VERTICAL);
        box.setGravity(Gravity.CENTER);
        box.setOnClickListener(click);
        TextView i = text(icon, 23, color, true);
        i.setGravity(Gravity.CENTER);
        box.addView(i, new LinearLayout.LayoutParams(-1, dp(38)));
        TextView l = text(label, 10, WHITE, false);
        l.setGravity(Gravity.CENTER);
        box.addView(l, new LinearLayout.LayoutParams(-1, dp(22)));
        return box;
    }

    private View mediaTile(MediaItem item, int index) {
        LinearLayout outer = vertical();
        FrameLayout thumb = cardFrame();
        outer.addView(thumb, new LinearLayout.LayoutParams(-1, dp(160)));
        addMediaVisual(thumb, item, false);

        if (item.favorite) {
            TextView heart = text("♥", 23, PINK, true);
            heart.setGravity(Gravity.CENTER);
            thumb.addView(heart, new FrameLayout.LayoutParams(dp(42), dp(42), Gravity.TOP | Gravity.RIGHT));
        }

        if (item.hidden) {
            TextView lock = pill("Private", PURPLE, true);
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(dp(84), dp(30), Gravity.TOP | Gravity.LEFT);
            lp.setMargins(dp(10), dp(10), 0, 0);
            thumb.addView(lock, lp);
        }

        TextView badge = pill(item.type.equals("VIDEO") ? "Video" : item.category, item.type.equals("VIDEO") ? BLUE : PINK, true);
        FrameLayout.LayoutParams bp = new FrameLayout.LayoutParams(dp(86), dp(30), Gravity.BOTTOM | Gravity.LEFT);
        bp.setMargins(dp(10), 0, 0, dp(10));
        thumb.addView(badge, bp);

        TextView title = text(item.title, 14, WHITE, true);
        title.setSingleLine(true);
        outer.addView(title, new LinearLayout.LayoutParams(-1, dp(30)));

        TextView sub = text(item.album + " • " + item.date, 12, MUTED, false);
        sub.setSingleLine(true);
        outer.addView(sub, new LinearLayout.LayoutParams(-1, dp(24)));

        outer.setOnClickListener(v -> openPreview(index));
        outer.setOnLongClickListener(v -> {
            item.favorite = !item.favorite;
            save();
            buildMain();
            return true;
        });
        return outer;
    }

    private void addMediaVisual(FrameLayout frame, MediaItem item, boolean hero) {
        if ("PHOTO".equals(item.type)) {
            ImageView img = new ImageView(this);
            img.setScaleType(ImageView.ScaleType.CENTER_CROP);
            img.setImageURI(Uri.parse(item.uri));
            frame.addView(img, new FrameLayout.LayoutParams(-1, -1));
            addDark(frame, hero ? 25 : 45);
        } else {
            frame.addView(new MiniVideoBackground(this), new FrameLayout.LayoutParams(-1, -1));
            TextView play = circleText("▶", WHITE);
            FrameLayout.LayoutParams pp = new FrameLayout.LayoutParams(hero ? dp(72) : dp(54), hero ? dp(72) : dp(54), Gravity.CENTER);
            frame.addView(play, pp);
            TextView video = pill("Video", BLUE, true);
            FrameLayout.LayoutParams vp = new FrameLayout.LayoutParams(dp(82), dp(30), Gravity.TOP | Gravity.LEFT);
            vp.setMargins(dp(12), dp(12), 0, 0);
            frame.addView(video, vp);
        }
    }

    private class MiniVideoBackground extends View {
        android.graphics.Paint p = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
        MiniVideoBackground(Activity a) { super(a); }
        @Override protected void onDraw(android.graphics.Canvas c) {
            int w = getWidth(), h = getHeight();
            android.graphics.LinearGradient g = new android.graphics.LinearGradient(0, 0, w, h,
                    new int[]{Color.rgb(10, 19, 44), Color.rgb(50, 36, 82), Color.rgb(255, 139, 183)},
                    null, android.graphics.Shader.TileMode.CLAMP);
            p.setShader(g);
            c.drawRoundRect(0, 0, w, h, dp(22), dp(22), p);
            p.setShader(null);
            p.setColor(Color.argb(60, 255, 255, 255));
            for (int i = 0; i < 9; i++) c.drawCircle(w * (0.12f + i * .11f), h * (0.18f + (i % 2) * .55f), dp(2), p);
        }
    }

    private void moreMenu(final int index) {
        MediaItem item = media.get(index);
        String[] options = new String[]{
                "Add to Album",
                "Change Tag/Mood",
                "Attach Voice",
                item.hidden ? "Unhide from Private" : "Hide to Private",
                item.type.equals("VIDEO") ? "Set as Live Wallpaper" : "Set as Wallpaper",
                "Export to Gallery Folder",
                "Share",
                "View Details",
                "Rename",
                "Delete from App List"
        };

        new AlertDialog.Builder(this)
                .setTitle("More Menu")
                .setItems(options, (d, which) -> {
                    if (which == 0) albumDialog(index);
                    if (which == 1) tagDialog(index);
                    if (which == 2) attachVoiceDialog(index);
                    if (which == 3) {
                        item.hidden = !item.hidden;
                        save();
                        openPreview(index);
                    }
                    if (which == 4) {
                        if (item.type.equals("VIDEO")) setLiveWallpaper(item);
                        else setPhotoWallpaper(item);
                    }
                    if (which == 5) exportMedia(item);
                    if (which == 6) shareMedia(item);
                    if (which == 7) detailsDialog(index);
                    if (which == 8) renameDialog(index);
                    if (which == 9) {
                        media.remove(index);
                        save();
                        buildMain();
                    }
                })
                .show();
    }

    private void albumDialog(int index) {
        EditText input = edit(media.get(index).album == null ? "Default" : media.get(index).album);
        new AlertDialog.Builder(this)
                .setTitle("Add to Album")
                .setView(input)
                .setPositiveButton("Save", (d, w) -> {
                    String s = input.getText().toString().trim();
                    media.get(index).album = s.length() == 0 ? "Default" : s;
                    save();
                    openPreview(index);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void tagDialog(int index) {
        String[] tags = {"Cute", "Wallpaper", "Live", "Night", "School", "Custom"};
        new AlertDialog.Builder(this)
                .setTitle("Change Tag/Mood")
                .setItems(tags, (d, which) -> {
                    media.get(index).category = tags[which];
                    save();
                    openPreview(index);
                })
                .show();
    }

    private void attachVoiceDialog(int index) {
        if (voices.size() == 0) {
            toast("Import voice dulu di menu Voice.");
            return;
        }
        String[] names = new String[voices.size()];
        for (int i = 0; i < voices.size(); i++) names[i] = voices.get(i).title;

        new AlertDialog.Builder(this)
                .setTitle("Attach Voice")
                .setItems(names, (d, which) -> {
                    VoiceItem v = voices.get(which);
                    MediaItem m = media.get(index);
                    m.voiceUri = v.uri;
                    m.voiceTitle = v.title;
                    save();
                    openPreview(index);
                })
                .show();
    }

    private void renameDialog(int index) {
        EditText input = edit(media.get(index).title);
        new AlertDialog.Builder(this)
                .setTitle("Rename")
                .setView(input)
                .setPositiveButton("Save", (d, w) -> {
                    String s = input.getText().toString().trim();
                    if (s.length() > 0) media.get(index).title = s;
                    save();
                    openPreview(index);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void detailsDialog(int index) {
        MediaItem item = media.get(index);
        String msg =
                "Title: " + item.title + "\n" +
                "Date: " + item.date + "\n" +
                "Type: " + item.type + "\n" +
                "Mime: " + item.mime + "\n" +
                "Tag: " + item.category + "\n" +
                "Album: " + item.album + "\n" +
                "Favorite: " + (item.favorite ? "Yes" : "No") + "\n" +
                "Private: " + (item.hidden ? "Yes" : "No") + "\n" +
                "Voice: " + (item.voiceTitle == null ? "Default / none" : item.voiceTitle) + "\n\n" +
                "Gestures:\\n" +
                "Tap = hide/show UI\\n" +
                "Double tap photo = favorite\\n" +
                "Swipe left/right = next/prev\\n" +
                "Swipe down = close preview";

        new AlertDialog.Builder(this)
                .setTitle("Media Details")
                .setMessage(msg)
                .setPositiveButton("OK", null)
                .show();
    }

    private void pinDialog() {
        if (store.appLockEnabled()) {
            new AlertDialog.Builder(this)
                    .setTitle("App Lock")
                    .setMessage("App Lock lagi ON.")
                    .setPositiveButton("Disable", (d, w) -> {
                        store.disablePin();
                        toast("App Lock off.");
                        buildMain();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            return;
        }

        EditText input = edit("");
        input.setHint("Minimal 4 angka");
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        new AlertDialog.Builder(this)
                .setTitle("Set PIN")
                .setView(input)
                .setPositiveButton("Save", (d, w) -> {
                    String pin = input.getText().toString().trim();
                    if (pin.length() < 4) toast("PIN minimal 4 angka.");
                    else {
                        store.setPin(pin);
                        toast("App Lock on.");
                        buildMain();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void togglePrivate() {
        if (store.appLockEnabled() && !store.showPrivate()) {
            EditText input = edit("");
            input.setHint("PIN");
            input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD);
            new AlertDialog.Builder(this)
                    .setTitle("Unlock Private")
                    .setView(input)
                    .setPositiveButton("Unlock", (d, w) -> {
                        if (input.getText().toString().equals(store.getPin())) {
                            store.setShowPrivate(true);
                            activeFilter = "Private";
                            activeTab = 1;
                            buildMain();
                        } else toast("PIN salah.");
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            return;
        }
        store.setShowPrivate(!store.showPrivate());
        if (store.showPrivate()) {
            activeFilter = "Private";
            activeTab = 1;
        }
        buildMain();
    }

    private void searchDialog() {
        EditText input = edit(searchQuery);
        input.setHint("Cari judul, tag, album...");
        new AlertDialog.Builder(this)
                .setTitle("Search")
                .setView(input)
                .setPositiveButton("Search", (d, w) -> {
                    searchQuery = input.getText().toString().trim();
                    buildMain();
                })
                .setNeutralButton("Clear", (d, w) -> {
                    searchQuery = "";
                    buildMain();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void confirmClear() {
        new AlertDialog.Builder(this)
                .setTitle("Clear App List?")
                .setMessage("Ini cuma hapus daftar di app, bukan hapus file asli di HP.")
                .setPositiveButton("Clear", (d, w) -> {
                    media.clear();
                    voices.clear();
                    store.clearAll();
                    save();
                    buildMain();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void pickMedia() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"image/*", "video/*"});
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        startActivityForResult(intent, PICK_MEDIA);
    }

    private void pickVoice() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("audio/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        startActivityForResult(intent, PICK_VOICE);
    }

    @Override
    protected void onActivityResult(int request, int result, Intent data) {
        super.onActivityResult(request, result, data);
        if (result != RESULT_OK || data == null) return;

        if (request == PICK_MEDIA) {
            int added = handlePicked(data, false);
            save();
            activeTab = 1;
            toast("Imported " + added + " media.");
            buildMain();
        }

        if (request == PICK_VOICE) {
            int added = handlePicked(data, true);
            save();
            activeTab = 2;
            toast("Imported " + added + " voice.");
            buildMain();
        }
    }

    private int handlePicked(Intent data, boolean voice) {
        int count = 0;
        ClipData clip = data.getClipData();
        if (clip != null) {
            for (int i = 0; i < clip.getItemCount(); i++) if (addUri(clip.getItemAt(i).getUri(), voice)) count++;
        } else if (data.getData() != null) {
            if (addUri(data.getData(), voice)) count++;
        }
        return count;
    }

    private boolean addUri(Uri uri, boolean voice) {
        if (uri == null) return false;
        try { getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION); } catch (Exception ignored) {}

        String mime = getContentResolver().getType(uri);
        if (mime == null) mime = voice ? "audio/*" : "application/octet-stream";

        String name = cleanName(getDisplayName(uri), voice ? "Voice Line" : mime.startsWith("video/") ? "New Video" : "New Photo");
        String date = new SimpleDateFormat("d MMM yyyy", Locale.US).format(new Date());

        if (voice) {
            VoiceItem v = new VoiceItem();
            v.uri = uri.toString();
            v.title = name;
            v.date = date;
            v.category = "Greeting";
            voices.add(v);
            return true;
        }

        MediaItem m = new MediaItem();
        m.uri = uri.toString();
        m.mime = mime;
        m.title = name;
        m.date = date;
        m.type = mime.startsWith("video/") ? "VIDEO" : "PHOTO";
        m.category = mime.startsWith("video/") ? "Live" : "Wallpaper";
        m.album = "Default";
        media.add(m);
        return true;
    }

    private void playMediaVoice(MediaItem item) {
        if (item.voiceUri != null) playVoice(Uri.parse(item.voiceUri));
        else if (voices.size() > 0) playVoice(Uri.parse(voices.get(0).uri));
        else toast("Belum ada voice. Import dulu di menu Voice.");
    }

    private void playVoice(Uri uri) {
        try {
            stopVoice();
            voicePlayer = new MediaPlayer();
            voicePlayer.setDataSource(this, uri);
            voicePlayer.prepare();
            voicePlayer.start();
        } catch (Exception e) {
            toast("Voice error: " + e.getMessage());
        }
    }

    private void stopVoice() {
        try {
            if (voicePlayer != null) {
                voicePlayer.stop();
                voicePlayer.release();
            }
        } catch (Exception ignored) {}
        voicePlayer = null;
    }

    private void shareMedia(MediaItem item) {
        try {
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType(item.mime == null ? "*/*" : item.mime);
            share.putExtra(Intent.EXTRA_STREAM, Uri.parse(item.uri));
            share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(share, "Share " + item.title));
        } catch (Exception e) {
            toast("Share error: " + e.getMessage());
        }
    }

    private void exportMedia(MediaItem item) {
        try {
            String mime = item.mime == null ? (item.type.equals("VIDEO") ? "video/mp4" : "image/jpeg") : item.mime;
            String ext = mime.contains("png") ? ".png" : mime.contains("video") ? ".mp4" : ".jpg";
            String display = item.title.replaceAll("[^a-zA-Z0-9_ -]", "_") + ext;

            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, display);
            values.put(MediaStore.MediaColumns.MIME_TYPE, mime);

            Uri collection;
            if (item.type.equals("VIDEO")) {
                collection = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                if (Build.VERSION.SDK_INT >= 29) values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_MOVIES + "/HoshinoVault");
            } else {
                collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                if (Build.VERSION.SDK_INT >= 29) values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/HoshinoVault");
            }

            Uri outUri = getContentResolver().insert(collection, values);
            if (outUri == null) {
                toast("Export gagal.");
                return;
            }

            InputStream in = getContentResolver().openInputStream(Uri.parse(item.uri));
            OutputStream out = getContentResolver().openOutputStream(outUri);
            byte[] buf = new byte[8192];
            int len;
            while (in != null && out != null && (len = in.read(buf)) > 0) out.write(buf, 0, len);
            if (in != null) in.close();
            if (out != null) out.close();
            toast("Exported to HoshinoVault folder.");
        } catch (Exception e) {
            toast("Export error: " + e.getMessage());
        }
    }

    private void setPhotoWallpaper(MediaItem item) {
        try {
            Bitmap bitmap = loadBitmap(Uri.parse(item.uri));
            if (bitmap == null) {
                toast("Gagal baca foto.");
                return;
            }
            WallpaperManager.getInstance(this).setBitmap(bitmap);
            toast("Wallpaper set.");
        } catch (Exception e) {
            toast("Wallpaper error: " + e.getMessage());
        }
    }

    private void setLiveWallpaper(MediaItem item) {
        if (!item.type.equals("VIDEO")) {
            toast("Live wallpaper cuma buat video.");
            return;
        }
        try {
            store.setLiveWallpaperUri(item.uri);
            Intent intent = new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
            intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                    new ComponentName(this, VideoLiveWallpaperService.class));
            startActivity(intent);
        } catch (Exception e) {
            toast("Live wallpaper error: " + e.getMessage());
        }
    }

    private Bitmap loadBitmap(Uri uri) {
        try {
            if (Build.VERSION.SDK_INT >= 28) {
                ImageDecoder.Source source = ImageDecoder.createSource(getContentResolver(), uri);
                return ImageDecoder.decodeBitmap(source);
            }
            InputStream is = getContentResolver().openInputStream(uri);
            Bitmap b = BitmapFactory.decodeStream(is);
            if (is != null) is.close();
            return b;
        } catch (Exception e) {
            return null;
        }
    }

    private void topTitle(LinearLayout parent, String title, String sub, String right, View.OnClickListener click) {
        LinearLayout row = horizontal();
        row.setGravity(Gravity.CENTER_VERTICAL);
        parent.addView(row, new LinearLayout.LayoutParams(-1, dp(74)));

        LinearLayout left = vertical();
        row.addView(left, new LinearLayout.LayoutParams(0, -1, 1f));
        left.addView(text(title, 32, WHITE, true), new LinearLayout.LayoutParams(-1, dp(42)));
        left.addView(text(sub == null ? "" : sub, 13, MUTED, false), new LinearLayout.LayoutParams(-1, dp(24)));

        if (right != null) {
            TextView r = actionButton(right, PINK);
            r.setOnClickListener(click);
            row.addView(r, new LinearLayout.LayoutParams(dp(105), dp(42)));
        }
    }

    private void addChips(LinearLayout parent, boolean favoriteScreen) {
        String[] chips = favoriteScreen
                ? new String[]{"All", "Photos", "Videos", "Private"}
                : new String[]{"All", "Cute", "Wallpaper", "Live", "Favorite", "Private"};

        HorizontalScrollView hsv = new HorizontalScrollView(this);
        hsv.setHorizontalScrollBarEnabled(false);
        parent.addView(hsv, new LinearLayout.LayoutParams(-1, dp(50)));

        LinearLayout row = horizontal();
        hsv.addView(row);

        for (String chip : chips) {
            TextView c = pill(chip, chip.equals(activeFilter) ? PINK : BLUE, !chip.equals(activeFilter));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(Math.max(76, chip.length() * 12 + 30)), dp(36));
            lp.setMargins(0, 0, dp(8), 0);
            row.addView(c, lp);
            c.setOnClickListener(v -> {
                if (chip.equals("Private") && !store.showPrivate()) {
                    togglePrivate();
                } else {
                    activeFilter = chip;
                    buildMain();
                }
            });
        }
    }

    private LinearLayout shortcut(String title, String sub, String icon, int color, View.OnClickListener click) {
        LinearLayout box = glassPanel();
        box.setOrientation(LinearLayout.HORIZONTAL);
        box.setGravity(Gravity.CENTER_VERTICAL);
        box.setPadding(dp(14), 0, dp(14), 0);
        box.setOnClickListener(click);
        box.addView(circleText(icon, color), new LinearLayout.LayoutParams(dp(50), dp(50)));

        LinearLayout words = vertical();
        words.setPadding(dp(12), 0, 0, 0);
        box.addView(words, new LinearLayout.LayoutParams(0, -1, 1f));
        words.addView(text(title, 16, WHITE, true), new LinearLayout.LayoutParams(-1, dp(31)));
        words.addView(text(sub, 11, MUTED, false), new LinearLayout.LayoutParams(-1, dp(24)));
        return box;
    }

    private void buildBottomNav() {
        LinearLayout nav = horizontal();
        nav.setGravity(Gravity.CENTER);
        nav.setPadding(dp(8), dp(7), dp(8), dp(7));
        nav.setBackground(roundBg(Color.argb(226, 16, 22, 45), dp(32), STROKE));

        FrameLayout.LayoutParams nlp = new FrameLayout.LayoutParams(-1, dp(72), Gravity.BOTTOM);
        nlp.setMargins(dp(16), 0, dp(16), dp(14));
        root.addView(nav, nlp);

        for (int i = 0; i < tabs.length; i++) {
            LinearLayout item = vertical();
            item.setGravity(Gravity.CENTER);
            if (i == activeTab) item.setBackground(roundBg(Color.argb(48, 255, 139, 183), dp(25), Color.TRANSPARENT));
            item.addView(centerText(tabIcons[i], 22, i == activeTab ? PINK : MUTED, true), new LinearLayout.LayoutParams(-1, dp(30)));
            item.addView(centerText(tabs[i], 10, i == activeTab ? WHITE : MUTED, false), new LinearLayout.LayoutParams(-1, dp(20)));
            final int tab = i;
            item.setOnClickListener(v -> {
                stopVoice();
                activeTab = tab;
                activeFilter = "All";
                buildMain();
            });
            nav.addView(item, new LinearLayout.LayoutParams(0, -1, 1f));
        }
    }

    private boolean passesFilter(MediaItem item, boolean favoriteScreen) {
        if (item.hidden && !store.showPrivate()) return false;
        if (favoriteScreen && !item.favorite) return false;

        if (!searchQuery.trim().isEmpty()) {
            String q = searchQuery.toLowerCase(Locale.US);
            String all = (item.title + " " + item.category + " " + item.album + " " + item.date).toLowerCase(Locale.US);
            if (!all.contains(q)) return false;
        }

        if (activeFilter.equals("All")) return true;
        if (activeFilter.equals("Photos")) return item.type.equals("PHOTO");
        if (activeFilter.equals("Videos")) return item.type.equals("VIDEO");
        if (activeFilter.equals("Favorite")) return item.favorite;
        if (activeFilter.equals("Private")) return item.hidden;
        return item.category.equals(activeFilter);
    }

    private int firstVisibleMedia() {
        for (int i = 0; i < media.size(); i++) if (!media.get(i).hidden || store.showPrivate()) return i;
        return -1;
    }

    private int visibleCount() {
        int c = 0;
        for (MediaItem m : media) if (!m.hidden || store.showPrivate()) c++;
        return c;
    }

    private int countFavorites() {
        int c = 0;
        for (MediaItem m : media) if (m.favorite) c++;
        return c;
    }

    private int nextVisible(int index, boolean forward) {
        if (media.size() == 0) return index;
        int current = index;
        for (int step = 0; step < media.size(); step++) {
            current = forward ? (current + 1) % media.size() : (current - 1 + media.size()) % media.size();
            if (!media.get(current).hidden || store.showPrivate()) return current;
        }
        return index;
    }

    private void save() {
        store.saveMedia(media, voices);
    }

    private String getDisplayName(Uri uri) {
        String result = null;
        try {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null) {
                try {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex >= 0 && cursor.moveToFirst()) result = cursor.getString(nameIndex);
                } finally {
                    cursor.close();
                }
            }
        } catch (Exception ignored) {}
        if (result == null) result = uri.getLastPathSegment();
        return result == null ? "Media" : result;
    }

    private String cleanName(String name, String fallback) {
        if (name == null || name.trim().isEmpty()) return fallback;
        int dot = name.lastIndexOf(".");
        if (dot > 0) name = name.substring(0, dot);
        name = name.replace("_", " ").replace("-", " ").trim();
        return name.isEmpty() ? fallback : name;
    }

    private EditText edit(String value) {
        EditText e = new EditText(this);
        e.setText(value);
        e.setTextColor(WHITE);
        e.setHintTextColor(MUTED);
        e.setSingleLine(true);
        e.setTextSize(16);
        return e;
    }

    private void addEmptyHero(FrameLayout hero) {
        hero.addView(new MiniLogoView(this), new FrameLayout.LayoutParams(-1, -1));
        addDark(hero, 45);
        TextView title = centerText("Hoshino Vault", 30, WHITE, true);
        hero.addView(title, new FrameLayout.LayoutParams(-1, dp(70), Gravity.CENTER));
        TextView sub = centerText("Import foto/video dulu buat mulai.", 14, WHITE, false);
        FrameLayout.LayoutParams sp = new FrameLayout.LayoutParams(-1, dp(40), Gravity.CENTER);
        sp.setMargins(0, dp(70), 0, 0);
        hero.addView(sub, sp);
    }

    private LinearLayout emptyState(String title, String desc) {
        LinearLayout l = glassPanel();
        l.setOrientation(LinearLayout.VERTICAL);
        l.setGravity(Gravity.CENTER);
        l.setPadding(dp(24), dp(36), dp(24), dp(36));
        l.addView(text(title, 22, WHITE, true));
        TextView d = text(desc, 14, MUTED, false);
        d.setGravity(Gravity.CENTER);
        d.setPadding(0, dp(8), 0, dp(18));
        l.addView(d);
        TextView btn = actionButton("+ Import", PINK);
        btn.setOnClickListener(v -> activeTab == 2 ? pickVoice() : pickMedia());
        l.addView(btn, new LinearLayout.LayoutParams(dp(150), dp(46)));
        return l;
    }

    private TextView section(String s) {
        TextView v = text(s, 18, WHITE, true);
        v.setPadding(0, dp(22), 0, dp(10));
        return v;
    }

    private LinearLayout settingRow(String label, String value, String icon, View.OnClickListener click) {
        LinearLayout row = glassPanel();
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(14), 0, dp(14), 0);
        LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(-1, dp(58));
        rowLp.setMargins(0, 0, 0, dp(10));
        row.setLayoutParams(rowLp);
        row.setOnClickListener(click);

        row.addView(text(icon, 19, BLUE, true), new LinearLayout.LayoutParams(dp(36), -1));
        TextView left = text(label, 14, WHITE, true);
        left.setGravity(Gravity.CENTER_VERTICAL);
        row.addView(left, new LinearLayout.LayoutParams(0, -1, 1f));
        TextView right = text(value, 13, MUTED, false);
        right.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);
        row.addView(right, new LinearLayout.LayoutParams(dp(116), -1));
        TextView arrow = text("›", 24, MUTED, true);
        arrow.setGravity(Gravity.CENTER);
        row.addView(arrow, new LinearLayout.LayoutParams(dp(26), -1));
        return row;
    }

    private TextView text(String s, int sp, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextColor(color);
        t.setTextSize(sp);
        t.setTypeface(Typeface.DEFAULT, bold ? Typeface.BOLD : Typeface.NORMAL);
        t.setIncludeFontPadding(true);
        return t;
    }

    private TextView centerText(String s, int sp, int color, boolean bold) {
        TextView t = text(s, sp, color, bold);
        t.setGravity(Gravity.CENTER);
        return t;
    }

    private TextView actionButton(String s, int color) {
        TextView b = text(s, 14, WHITE, true);
        b.setGravity(Gravity.CENTER);
        b.setBackground(roundBg(Color.argb(92, Color.red(color), Color.green(color), Color.blue(color)), dp(18),
                Color.argb(96, Color.red(color), Color.green(color), Color.blue(color))));
        return b;
    }

    private TextView circleText(String s, int color) {
        TextView t = text(s, s.length() > 2 ? 13 : 22, color, true);
        t.setGravity(Gravity.CENTER);
        t.setBackground(roundBg(Color.argb(96, 12, 18, 38), dp(100), Color.argb(80, 220, 225, 255)));
        return t;
    }

    private TextView pill(String s, int color, boolean subtle) {
        TextView t = text(s, 12, subtle ? color : Color.rgb(28, 25, 40), true);
        t.setGravity(Gravity.CENTER);
        int alpha = subtle ? 48 : 220;
        t.setBackground(roundBg(Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color)), dp(100),
                Color.argb(80, Color.red(color), Color.green(color), Color.blue(color))));
        return t;
    }

    private LinearLayout vertical() {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        return l;
    }

    private LinearLayout horizontal() {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.HORIZONTAL);
        return l;
    }

    private LinearLayout glassPanel() {
        LinearLayout l = new LinearLayout(this);
        l.setBackground(roundBg(CARD2, dp(20), STROKE));
        return l;
    }

    private FrameLayout cardFrame() {
        FrameLayout f = new FrameLayout(this);
        f.setBackground(roundBg(CARD, dp(22), STROKE));
        return f;
    }

    private GradientDrawable roundBg(int color, int radius, int stroke) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(radius);
        if (stroke != Color.TRANSPARENT) g.setStroke(dp(1), stroke);
        return g;
    }

    private void addDark(FrameLayout f, int alpha) {
        View v = new View(this);
        v.setBackgroundColor(Color.argb(alpha, 0, 0, 0));
        f.addView(v, new FrameLayout.LayoutParams(-1, -1));
    }

    private void addSpace(LinearLayout parent, int w, int h, boolean vertical) {
        View v = new View(this);
        if (vertical) parent.addView(v, new LinearLayout.LayoutParams(1, dp(h)));
        else parent.addView(v, new LinearLayout.LayoutParams(dp(w), h <= 1 ? dp(1) : dp(h)));
    }

    private int dp(float v) {
        return (int)(v * getResources().getDisplayMetrics().density + 0.5f);
    }

    private void toast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }
}
