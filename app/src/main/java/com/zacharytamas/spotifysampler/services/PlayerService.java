package com.zacharytamas.spotifysampler.services;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.wifi.WifiManager;
import android.os.IBinder;

import java.io.IOException;
import java.util.ArrayList;

import kaaes.spotify.webapi.android.models.Track;

/**
 * Created by zacharytamas on 6/6/15.
 */
public class PlayerService extends Service implements MediaPlayer.OnPreparedListener {

    private static final String PLAYER_SERVICE_NAME = "PlayerService";
    public static final String EXTRA_PLAYLIST = "extraPlaylist";

    public enum PlayerActions {
        START_ANEW,
        START_PAUSE,
        STOP,
        PLAYLIST_NEXT,
        PLAYLIST_PREVIOUS
    }

    private ArrayList<Track> mPlaylist;
    private MediaPlayer mPlayer;
    private WifiManager.WifiLock mWifiLock;

    @Override
    public void onCreate() {
        super.onCreate();

        mPlaylist = new ArrayList<>();
        mPlayer = new MediaPlayer();
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mPlayer.setOnPreparedListener(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {

        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void playTrack(Track track) throws IOException {
        mPlayer.setDataSource(track.preview_url);
        mPlayer.prepareAsync();
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        mediaPlayer.start();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
