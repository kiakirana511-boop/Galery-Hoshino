package com.radiant.hoshinovault;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

class WaveformView extends View {
    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final int pink = Color.rgb(255, 139, 183);
    private final int blue = Color.rgb(142, 167, 255);

    WaveformView(Context c) {
        super(c);
    }

    private int dp(float v) {
        return (int)(v * getResources().getDisplayMetrics().density + 0.5f);
    }

    @Override
    protected void onDraw(Canvas c) {
        int w = getWidth();
        int h = getHeight();
        float mid = h / 2f;
        p.setStrokeWidth(dp(2));

        for (int i = 0; i < 64; i++) {
            float x = dp(8) + (w - dp(16)) * i / 63f;
            float amp = (float) Math.abs(Math.sin(i * .48f) * Math.cos(i * .17f));
            float len = dp(8) + amp * dp(34);
            int color = i % 2 == 0 ? pink : blue;
            p.setColor(Color.argb(190, Color.red(color), Color.green(color), Color.blue(color)));
            c.drawLine(x, mid - len / 2f, x, mid + len / 2f, p);
        }
    }
}
