package com.radiant.hoshinovault;

import org.json.JSONObject;

class MediaItem {
    String uri;
    String mime;
    String title;
    String date;
    String type;       // PHOTO / VIDEO
    String category;   // Cute / Wallpaper / Live / Night / Custom
    String album;      // Default / Custom album
    String voiceUri;
    String voiceTitle;
    boolean favorite;
    boolean hidden;

    JSONObject toJson() throws Exception {
        JSONObject o = new JSONObject();
        o.put("uri", uri);
        o.put("mime", mime);
        o.put("title", title);
        o.put("date", date);
        o.put("type", type);
        o.put("category", category);
        o.put("album", album);
        o.put("voiceUri", voiceUri == null ? "" : voiceUri);
        o.put("voiceTitle", voiceTitle == null ? "" : voiceTitle);
        o.put("favorite", favorite);
        o.put("hidden", hidden);
        return o;
    }

    static MediaItem fromJson(JSONObject o) {
        MediaItem m = new MediaItem();
        m.uri = o.optString("uri");
        m.mime = o.optString("mime");
        m.title = o.optString("title", "Media");
        m.date = o.optString("date", "");
        m.type = o.optString("type", "PHOTO");
        m.category = o.optString("category", "Wallpaper");
        m.album = o.optString("album", "Default");
        m.voiceUri = emptyToNull(o.optString("voiceUri"));
        m.voiceTitle = emptyToNull(o.optString("voiceTitle"));
        m.favorite = o.optBoolean("favorite");
        m.hidden = o.optBoolean("hidden");
        return m;
    }

    static String emptyToNull(String s) {
        if (s == null || s.trim().length() == 0) return null;
        return s;
    }
}
