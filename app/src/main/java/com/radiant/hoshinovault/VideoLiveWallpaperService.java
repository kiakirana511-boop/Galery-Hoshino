package com.radiant.hoshinovault;

import android.media.MediaPlayer;
import android.net.Uri;
import android.service.wallpaper.WallpaperService;
import android.view.SurfaceHolder;

class VideoLiveWallpaperService extends WallpaperService {
    @Override
    public Engine onCreateEngine() {
        return new VideoEngine();
    }

    class VideoEngine extends Engine {
        private MediaPlayer player;

        @Override
        public void onSurfaceCreated(SurfaceHolder holder) {
            super.onSurfaceCreated(holder);
            start(holder);
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            super.onSurfaceDestroyed(holder);
            stop();
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            if (player == null) return;
            if (visible) player.start();
            else player.pause();
        }

        private void start(SurfaceHolder holder) {
            try {
                String uri = new VaultStore(VideoLiveWallpaperService.this).getLiveWallpaperUri();
                if (uri == null || uri.length() == 0) return;

                stop();
                player = new MediaPlayer();
                player.setDataSource(VideoLiveWallpaperService.this, Uri.parse(uri));
                player.setSurface(holder.getSurface());
                player.setLooping(true);
                player.setVolume(0f, 0f);
                player.prepare();
                player.start();
            } catch (Exception ignored) {
                stop();
            }
        }

        private void stop() {
            try {
                if (player != null) {
                    player.stop();
                    player.release();
                }
            } catch (Exception ignored) {}
            player = null;
        }
    }
}
