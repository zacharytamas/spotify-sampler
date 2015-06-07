package com.zacharytamas.spotifysampler.services;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.util.Log;

import com.zacharytamas.spotifysampler.models.SpotifyTrack;

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

    private static PlayerService defaultService;
    private static final String TAG = PlayerService.class.getSimpleName();

    public static final String EXTRA_PLAYLIST = "extraPlaylist";
    public static final String EXTRA_TRACK_NUMBER = "extraTrackNumber";
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

        // Setup listeners
        mPlayer.setOnPreparedListener(this);
        mPlayer.setOnCompletionListener(this);

        // TODO get wifilock and wakelock
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    public static PlayerService getInstance() {
        return defaultService;
    }


    private void seekTrack(SpotifyTrack track) {
        Log.i(TAG, "Seeking track: " + track.name);
        try {
            this.fire(EVENT_SEEK_TRACK);
            if (mPlayer.isPlaying()) mPlayer.stop();
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
            this.fire(EVENT_PLAYLIST_END);
            mPlayer.release();
        }
    }

    public void previous() {
        if (hasPreviousTrack()) {
            mTrackIndex -= 1;
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
        if (mTimer != null) mTimer.cancel();
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
    }

    /**
     * Allow objects implementing the PlayerSubscriber protocol to unsubscribe.
     * @param subscriber An object conforming to the PlayerSubscriber protocol.
     */
    public void unsubscribe(PlayerSubscriber subscriber) {
        Log.i(TAG, "Lost a subscriber.");
        mSubscribers.remove(subscriber);
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
        mTimer.cancel();
        next();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
