package com.zacharytamas.spotifysampler.ui;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.zacharytamas.spotifysampler.R;
import com.zacharytamas.spotifysampler.models.SpotifyTrack;
import com.zacharytamas.spotifysampler.services.PlayerService;
import com.zacharytamas.spotifysampler.services.PlayerSubscriber;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * A placeholder fragment containing a simple view.
 */
public class PlayerFragment extends DialogFragment implements PlayerSubscriber {

    @InjectView(R.id.playerSeekBar) SeekBar mSeekBar;
    @InjectView(R.id.playerPlayPause) ImageView mPlayPause;
    @InjectView(R.id.playerAlbumArt) ImageView mAlbumArt;
    @InjectView(R.id.playerTrackName) TextView mTrackName;
    @InjectView(R.id.playerArtistName) TextView mArtistName;

    PlayerService mService;

    public PlayerFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_player, container, false);

        ButterKnife.inject(this, view);
        mSeekBar.setMax(100);
        mService = PlayerService.getInstance();

        view.findViewById(R.id.playerNextArrow).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mService.next();
            }
        });

        view.findViewById(R.id.playerPreviousArrow).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mService.previous();
            }
        });

        view.findViewById(R.id.playerPlayPause).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mService.playPause();
            }
        });

        updateUIForTrack(mService.currentlyPlayingTrack());
        updateUIForPlayStatus(mService);

        return view;
    }

    @NonNull @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setLayout(300, 500);
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        PlayerService.getInstance().subscribe(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        PlayerService.getInstance().unsubscribe(this);
    }

    @Override
    public void onPlayerEvent(String event, PlayerService service) {
        switch (event) {
            case PlayerService.EVENT_SEEK_TRACK:
            case PlayerService.EVENT_PREPARED:
                updateUIForTrack(service.currentlyPlayingTrack());
                break;
            case PlayerService.EVENT_PLAYLIST_END:
                dismiss();
                break;
            case PlayerService.EVENT_PROGRESS:
                updateUIForProgress(service);
                break;
            case PlayerService.EVENT_PLAY:
            case PlayerService.EVENT_PAUSED:
                updateUIForPlayStatus(service);
                break;
        }
    }

    private void updateUIForProgress(PlayerService service) {
        mSeekBar.setProgress(service.getProgress());
    }

    private void updateUIForPlayStatus(PlayerService service) {
        if (service.isPlaying()) {
            mPlayPause.setImageResource(R.drawable.uamp_ic_pause_white_48dp);
        } else {
            mPlayPause.setImageResource(R.drawable.uamp_ic_play_arrow_white_48dp);
        }
    }

    private void updateUIForTrack(SpotifyTrack spotifyTrack) {
        Picasso.with(getActivity()).load(spotifyTrack.albumImageUrl).into(mAlbumArt);
        mTrackName.setText(spotifyTrack.name);
        mArtistName.setText(spotifyTrack.artistName);
    }
}
