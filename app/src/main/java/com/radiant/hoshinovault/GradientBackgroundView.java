package com.radiant.hoshinovault;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.view.View;

class GradientBackgroundView extends View {
    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);

    GradientBackgroundView(Context c) {
        super(c);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int w = getWidth();
        int h = getHeight();
        LinearGradient bg = new LinearGradient(
                0, 0, w, h,
                new int[]{Color.rgb(5, 9, 22), Color.rgb(10, 17, 42), Color.rgb(8, 9, 22)},
                new float[]{0f, .62f, 1f},
                Shader.TileMode.CLAMP
        );
        p.setShader(bg);
        canvas.drawRect(0, 0, w, h, p);
        p.setShader(null);

        p.setColor(Color.argb(38, 142, 167, 255));
        canvas.drawCircle(w * .82f, h * .12f, w * .42f, p);

        p.setColor(Color.argb(30, 255, 139, 183));
        canvas.drawCircle(w * .18f, h * .72f, w * .40f, p);
    }
}
