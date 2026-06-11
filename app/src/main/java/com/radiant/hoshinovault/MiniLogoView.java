package com.radiant.hoshinovault;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.view.View;

class MiniLogoView extends View {
    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);

    MiniLogoView(Context c) {
        super(c);
    }

    private int dp(float v) {
        return (int)(v * getResources().getDisplayMetrics().density + 0.5f);
    }

    @Override
    protected void onDraw(Canvas c) {
        int w = getWidth();
        int h = getHeight();

        LinearGradient g = new LinearGradient(0, 0, w, h,
                new int[]{Color.rgb(10, 20, 50), Color.rgb(45, 34, 80), Color.rgb(255, 139, 183)},
                null, Shader.TileMode.CLAMP);
        p.setShader(g);
        c.drawRoundRect(dp(8), dp(8), w - dp(8), h - dp(8), dp(22), dp(22), p);
        p.setShader(null);

        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(dp(3));
        p.setColor(Color.rgb(255, 139, 183));
        c.drawCircle(w / 2f, h / 2f, Math.min(w, h) * .32f, p);
        p.setColor(Color.rgb(142, 167, 255));
        c.drawArc(w * .23f, h * .27f, w * .77f, h * .78f, 200, 140, false, p);
        p.setStyle(Paint.Style.FILL);

        p.setColor(Color.rgb(248, 221, 235));
        p.setTextAlign(Paint.Align.CENTER);
        p.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        p.setTextSize(Math.min(w, h) * .30f);
        c.drawText("H", w / 2f, h / 2f + Math.min(w, h) * .11f, p);
    }
}
