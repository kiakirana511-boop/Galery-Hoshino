package com.radiant.hoshinovault;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;

import java.util.ArrayList;

class VaultStore {
    private final SharedPreferences prefs;

    VaultStore(Context context) {
        prefs = context.getSharedPreferences("hoshino_vault_final", Context.MODE_PRIVATE);
    }

    ArrayList<MediaItem> loadMedia() {
        ArrayList<MediaItem> list = new ArrayList<>();
        try {
            JSONArray arr = new JSONArray(prefs.getString("media", "[]"));
            for (int i = 0; i < arr.length(); i++) {
                list.add(MediaItem.fromJson(arr.getJSONObject(i)));
            }
        } catch (Exception ignored) {}
        return list;
    }

    ArrayList<VoiceItem> loadVoices() {
        ArrayList<VoiceItem> list = new ArrayList<>();
        try {
            JSONArray arr = new JSONArray(prefs.getString("voices", "[]"));
            for (int i = 0; i < arr.length(); i++) {
                list.add(VoiceItem.fromJson(arr.getJSONObject(i)));
            }
        } catch (Exception ignored) {}
        return list;
    }

    void saveMedia(ArrayList<MediaItem> media, ArrayList<VoiceItem> voices) {
        try {
            JSONArray mediaArr = new JSONArray();
            for (MediaItem m : media) mediaArr.put(m.toJson());

            JSONArray voiceArr = new JSONArray();
            for (VoiceItem v : voices) voiceArr.put(v.toJson());

            prefs.edit()
                    .putString("media", mediaArr.toString())
                    .putString("voices", voiceArr.toString())
                    .apply();
        } catch (Exception ignored) {}
    }

    boolean appLockEnabled() {
        return prefs.getBoolean("app_lock", false);
    }

    String getPin() {
        return prefs.getString("pin", "");
    }

    void setPin(String pin) {
        prefs.edit().putString("pin", pin).putBoolean("app_lock", pin != null && pin.length() >= 4).apply();
    }

    void disablePin() {
        prefs.edit().putString("pin", "").putBoolean("app_lock", false).apply();
    }

    boolean showPrivate() {
        return prefs.getBoolean("show_private", false);
    }

    void setShowPrivate(boolean show) {
        prefs.edit().putBoolean("show_private", show).apply();
    }

    void setLiveWallpaperUri(String uri) {
        prefs.edit().putString("live_wallpaper_uri", uri).apply();
    }

    String getLiveWallpaperUri() {
        return prefs.getString("live_wallpaper_uri", "");
    }

    void clearAll() {
        prefs.edit().clear().apply();
    }
}
