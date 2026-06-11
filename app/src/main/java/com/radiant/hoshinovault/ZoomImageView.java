package com.radiant.hoshinovault;

import android.content.Context;
import android.graphics.Matrix;
import android.net.Uri;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

class ZoomImageView extends android.widget.ImageView {
    interface Listener {
        void onSingleTap();
        void onDoubleTap();
        void onSwipeLeft();
        void onSwipeRight();
        void onSwipeDown();
    }

    private final Matrix matrix = new Matrix();
    private final ScaleGestureDetector scaleDetector;
    private final GestureDetector gestureDetector;
    private float scale = 1f;
    private Listener listener;

    ZoomImageView(Context context) {
        super(context);
        setScaleType(ScaleType.MATRIX);

        scaleDetector = new ScaleGestureDetector(context, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                float factor = detector.getScaleFactor();
                float old = scale;
                scale *= factor;
                if (scale < 1f) scale = 1f;
                if (scale > 5f) scale = 5f;
                factor = scale / old;
                matrix.postScale(factor, factor, detector.getFocusX(), detector.getFocusY());
                setImageMatrix(matrix);
                return true;
            }
        });

        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            private static final int SWIPE_DIST = 120;
            private static final int SWIPE_VEL = 120;

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                if (listener != null) listener.onSingleTap();
                return true;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                if (listener != null) listener.onDoubleTap();
                return true;
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (e1 == null || e2 == null || listener == null) return false;
                float dx = e2.getX() - e1.getX();
                float dy = e2.getY() - e1.getY();

                if (Math.abs(dy) > Math.abs(dx) && dy > SWIPE_DIST && Math.abs(velocityY) > SWIPE_VEL) {
                    listener.onSwipeDown();
                    return true;
                }
                if (Math.abs(dx) > Math.abs(dy) && Math.abs(dx) > SWIPE_DIST && Math.abs(velocityX) > SWIPE_VEL) {
                    if (dx < 0) listener.onSwipeLeft();
                    else listener.onSwipeRight();
                    return true;
                }
                return false;
            }
        });
    }

    void setListener(Listener listener) {
        this.listener = listener;
    }

    @Override
    public void setImageURI(Uri uri) {
        super.setImageURI(uri);
        matrix.reset();
        scale = 1f;
        setImageMatrix(matrix);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleDetector.onTouchEvent(event);
        gestureDetector.onTouchEvent(event);

        if (event.getPointerCount() == 1 && scale > 1f && event.getAction() == MotionEvent.ACTION_MOVE) {
            matrix.postTranslate(event.getX() - getWidth() / 2f, event.getY() - getHeight() / 2f);
            setImageMatrix(matrix);
        }
        return true;
    }
}
