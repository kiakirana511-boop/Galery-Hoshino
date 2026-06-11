package com.radiant.hoshinovault;

import org.json.JSONObject;

class VoiceItem {
    String uri;
    String title;
    String date;
    String category;
    boolean favorite;

    JSONObject toJson() throws Exception {
        JSONObject o = new JSONObject();
        o.put("uri", uri);
        o.put("title", title);
        o.put("date", date);
        o.put("category", category);
        o.put("favorite", favorite);
        return o;
    }

    static VoiceItem fromJson(JSONObject o) {
        VoiceItem v = new VoiceItem();
        v.uri = o.optString("uri");
        v.title = o.optString("title", "Voice Line");
        v.date = o.optString("date", "");
        v.category = o.optString("category", "Greeting");
        v.favorite = o.optBoolean("favorite");
        return v;
    }
}
