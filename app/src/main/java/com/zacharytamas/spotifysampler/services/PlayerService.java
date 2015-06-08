package com.zacharytamas.spotifysampler.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import com.zacharytamas.spotifysampler.R;
import com.zacharytamas.spotifysampler.models.SpotifyTrack;
import com.zacharytamas.spotifysampler.ui.PlayerFragment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by zacharytamas on 6/6/15.
 */
public class PlayerService extends Service implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnCompletionListener {

    public static final String EVENT_PREPARED = "player-prepared";
    public static final String EVENT_TRACK_COMPLETED = "player-track-completed";
    public static final String EVENT_SEEK_TRACK = "player-seek-track";
    public static final String EVENT_PLAY = "player-play";
    public static final String EVENT_PAUSED = "player-paused";
    public static final String EVENT_PROGRESS = "player-progress";
    public static final String EVENT_PLAYLIST_END = "player-playlist-end";
    private static final String LOCK_KEY = "player-wifilock";
    private static final int NOTIFICATION_ID = 1;

    private static PlayerService defaultService;
    private static final String TAG = PlayerService.class.getSimpleName();
    private Notification mNotification;

    private ArrayList<PlayerSubscriber> mSubscribers;

    private ArrayList<SpotifyTrack> mPlaylist;
    private int mTrackIndex = -1;
    private MediaPlayer mPlayer;
    private WifiManager.WifiLock mWifiLock;
    private Timer mTimer;

    @Override
    public void onCreate() {
        super.onCreate();

        Log.i(TAG, "Created a new PlayerService.");

        mSubscribers = new ArrayList<>();
        defaultService = this;

        mPlaylist = new ArrayList<>();
        mPlayer = new MediaPlayer();
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);

        mWifiLock = ((WifiManager) getSystemService(Context.WIFI_SERVICE))
                .createWifiLock(WifiManager.WIFI_MODE_FULL, LOCK_KEY);
        mWifiLock.acquire();

        // Setup listeners
        mPlayer.setOnPreparedListener(this);
        mPlayer.setOnCompletionListener(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        mPlayer.stop();
        mPlayer.release();
        mPlayer = null;
        mWifiLock.release();
        super.onDestroy();
    }

    public static PlayerService getInstance() {
        return defaultService;
    }


    private void seekTrack(SpotifyTrack track) {
        Log.i(TAG, "Seeking track: " + track.name);
        try {
            this.fire(EVENT_SEEK_TRACK);
//            updateNotification();
            if (mPlayer.isPlaying()) mPlayer.pause();
            stopTicking();
            mPlayer.reset();
            mPlayer.setDataSource(track.previewUrl);
            mPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void seekTrack() {
        seekTrack(mPlaylist.get(mTrackIndex));
    }

    private void createNotification() {
        mNotification = new Notification();
        mNotification.tickerText = "Spotify Sampler";
        mNotification.icon = R.drawable.ic_notification;
        mNotification.flags |= Notification.FLAG_ONGOING_EVENT;
        startForeground(NOTIFICATION_ID, mNotification);
        this.updateNotification();
    }

    private void updateNotification() {
        if (mNotification == null) return;

        PendingIntent intent = PendingIntent.getActivity(getApplicationContext(), 0,
                new Intent(getApplicationContext(), PlayerFragment.class),
                PendingIntent.FLAG_UPDATE_CURRENT);

        mNotification.setLatestEventInfo(getApplicationContext(),
                "Spotify Sampler",
                "Playing " + currentlyPlayingTrack().name, intent);
    }

    ////////////////////////////////////////////////////////////////
    // PUBLIC API
    ////////////////////////////////////////////////////////////////

    public void playNewPlaylistAtIndex(ArrayList<SpotifyTrack> playlist, int index) {
        Log.i(TAG, "playNewPlaylistAtIndex " + index);
        if (playlist != mPlaylist) {
            Log.i(TAG, "Replaced playlist");
            mPlaylist = playlist;
        }
        mTrackIndex = index;
        seekTrack();
    }

    public void play() {

        if (mNotification == null) {
//            createNotification();
        }

        mPlayer.start();
        this.startTicking();
        this.fire(EVENT_PLAY);
    }

    public void pause() {
        mPlayer.pause();
        this.stopTicking();
        this.fire(EVENT_PAUSED);
    }

    public boolean hasNextTrack() {
        return mTrackIndex < (mPlaylist.size() - 1);
    }

    public boolean hasPreviousTrack() {
        return mTrackIndex != 0;
    }

    public void playPause() {
        if (mPlayer.isPlaying()) {
            pause();
        } else {
            play();
        }
    }

    public boolean isPlaying() {
        return mPlayer.isPlaying();
    }

    public int getDuration() {
        return mPlayer.getDuration();
    }

    public int getCurrentPosition() {
        return mPlayer.getCurrentPosition();
    }

    public void next() {
        if (hasNextTrack()) {
            mTrackIndex += 1;
            seekTrack();
        } else {
            mWifiLock.release();
            this.fire(EVENT_PLAYLIST_END);
        }
    }

    public void previous() {
        if (hasPreviousTrack()) {
            mTrackIndex -= 1;
            seekTrack();
        } else {
            // We can't go to the previous track, but most people would expect
            // the song to start over.
            mPlayer.seekTo(0);
        }
    }

    public SpotifyTrack currentlyPlayingTrack() {
        if (mTrackIndex >= 0) {
            return mPlaylist.get(mTrackIndex);
        }
        // It's possible that a track is not playing, such as if the playlist finished.
        return null;
    }

    public int getProgress() {
        int position = getCurrentPosition();
        float duration = getDuration();
        return (int) (position / duration * 100);
    }

    private void startTicking() {
        // I don't like this... why can't this thing just have proper Progress events?
        if (mTimer != null) stopTicking();
        // Don't tick when no one is listening.
        if (mSubscribers.size() == 0) return;
        if (!isPlaying()) return;

        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Log.i(TAG, "ProgressTimer: Tick...");
                fire(EVENT_PROGRESS);
            }
        }, 0, 500);
    }

    private void stopTicking() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        Log.i(TAG, "ProgressTimer: Stopped ticking.");
    }

    // TODO I know I should be using LocalBroadcastManager here but this was quickest.
    // I'm still learning.

    /**
     * Allow objects implementing the PlayerSubscriber protocol to subscribe
     * for updates to Service events.
     */
    public void subscribe(PlayerSubscriber subscriber) {
        Log.i(TAG, "New subscriber.");
        mSubscribers.add(subscriber);

        // If someone has subscribed while we are playing, let's make sure we
        // are sending ticks.
        startTicking();
    }

    /**
     * Allow objects implementing the PlayerSubscriber protocol to unsubscribe.
     * @param subscriber An object conforming to the PlayerSubscriber protocol.
     */
    public void unsubscribe(PlayerSubscriber subscriber) {
        Log.i(TAG, "Lost a subscriber.");
        mSubscribers.remove(subscriber);

        // If no one is watching, don't bother ticking.
        if (mSubscribers.size() == 0) {
            stopTicking();
        }
    }

    /**
     * Fire an event to subscribers.
     * @param event
     */
    private void fire(String event) {
        for (PlayerSubscriber subscriber : mSubscribers) {
            subscriber.onPlayerEvent(event, this);
        }
    }


    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        this.fire(EVENT_PREPARED);
        play();
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        this.fire(EVENT_TRACK_COMPLETED);
        stopTicking();
        next();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
