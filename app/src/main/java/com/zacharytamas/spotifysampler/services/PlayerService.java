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

/**
 * Created by zacharytamas on 6/6/15.
 */
public class PlayerService extends Service implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnCompletionListener {

    public static final String EVENT_PREPARED = "player-prepared";
    public static final String EVENT_TRACK_COMPLETED = "player-track-completed";
    public static final String EVENT_STARTED = "player-started";

    private static final int PLAYLIST_SEEK_FORWARD = 1;
    private static final int PLAYLIST_SEEK_BACKWARD = -1;


    private static PlayerService defaultService;
    private static final String TAG = PlayerService.class.getSimpleName();

    public static final String EXTRA_PLAYLIST = "extraPlaylist";
    public static final String EXTRA_TRACK_NUMBER = "extraTrackNumber";
    private ArrayList<PlayerSubscriber> mSubscribers;

    private ArrayList<SpotifyTrack> mPlaylist;
    private int mTrackIndex;
    private MediaPlayer mPlayer;
    private WifiManager.WifiLock mWifiLock;

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

    public void playNewPlaylistAtIndex(ArrayList<SpotifyTrack> playlist, int index) {
        mPlaylist.clear();
        mPlaylist.addAll(playlist);
        mTrackIndex = index;
        seekTrack(mPlaylist.get(mTrackIndex));
    }

    private void seekTrack(SpotifyTrack track) {
        try {
            mPlayer.stop();
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

    private void seekPlaylist(int delta) {

        if (delta == PLAYLIST_SEEK_FORWARD) {
            if (hasNextTrack()) {
                Log.i(TAG, "Seeking to next track");
                mTrackIndex += 1;
                seekTrack();
            }
        } else if (hasPreviousTrack()) {
            Log.i(TAG, "Seeking to previous track");
            mTrackIndex -= 1;
            seekTrack();
        }

    }

    ////////////////////////////////////////////////////////////////
    // PUBLIC API
    ////////////////////////////////////////////////////////////////

    public void play() {
        mPlayer.start();
    }

    public boolean hasNextTrack() {
        return mTrackIndex < (mPlaylist.size() - 1);
    }

    public boolean hasPreviousTrack() {
        return mTrackIndex != 0;
    }

    public void next() {
        seekPlaylist(PLAYLIST_SEEK_FORWARD);
    }

    public void previous() {
        seekPlaylist(PLAYLIST_SEEK_BACKWARD);
    }

    public SpotifyTrack currentlyPlayingTrack() {
        if (mTrackIndex >= 0) {
            return mPlaylist.get(mTrackIndex);
        }
        // It's possible that a track is not playing, such as if the playlist finished.
        return null;
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
     * @param subscriber
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
        mediaPlayer.start();
        this.fire(EVENT_STARTED);
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        next();
        this.fire(EVENT_TRACK_COMPLETED);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
