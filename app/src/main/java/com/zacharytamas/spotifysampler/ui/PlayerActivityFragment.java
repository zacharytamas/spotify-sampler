package com.zacharytamas.spotifysampler.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.zacharytamas.spotifysampler.R;
import com.zacharytamas.spotifysampler.models.SpotifyTrack;
import com.zacharytamas.spotifysampler.services.PlayerService;
import com.zacharytamas.spotifysampler.services.PlayerSubscriber;

import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class PlayerActivityFragment extends Fragment implements PlayerSubscriber {

    public PlayerActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_player, container, false);

        view.findViewById(R.id.playerNextArrow).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PlayerService.getInstance().next();
            }
        });

        view.findViewById(R.id.playerPreviousArrow).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PlayerService.getInstance().previous();
            }
        });

        view.findViewById(R.id.playerPlayPause).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PlayerService.getInstance().playPause();
            }
        });

        if (savedInstanceState == null) {
            Intent intent = getActivity().getIntent();
            queuePlayer(intent);
        } else {
            // TODO This crashes because this method uses getView() which hasn't
            // been set yet because this method hasn't returned the view.
            updateUIForTrack(PlayerService.getInstance().currentlyPlayingTrack());
        }

        return view;
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

    private void queuePlayer(Intent originalIntent) {
        ArrayList<SpotifyTrack> playlist = originalIntent.getParcelableArrayListExtra(
                PlayerService.EXTRA_PLAYLIST);

        PlayerService.getInstance().playNewPlaylistAtIndex(playlist,
                originalIntent.getIntExtra(PlayerService.EXTRA_TRACK_NUMBER, 0));
    }

    @Override
    public void onPlayerEvent(String event, PlayerService service) {
        switch (event) {
            case PlayerService.EVENT_SEEK_TRACK:
                updateUIForTrack(service.currentlyPlayingTrack());
                break;
            case PlayerService.EVENT_PREPARED:
                updateUIForTrack(service.currentlyPlayingTrack());
                break;
            case PlayerService.EVENT_TRACK_COMPLETED:
                if (!service.hasNextTrack()) getActivity().finish();
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
        SeekBar seekBar = (SeekBar) getView().findViewById(R.id.playerSeekBar);
        seekBar.setMax(100);
        seekBar.setProgress(service.getProgress());
    }

    private void updateUIForPlayStatus(PlayerService service) {
        ImageView playPause = (ImageView) getView().findViewById(R.id.playerPlayPause);
        if (service.isPlaying()) {
            playPause.setImageResource(R.drawable.uamp_ic_pause_white_48dp);
        } else {
            playPause.setImageResource(R.drawable.uamp_ic_play_arrow_white_48dp);
        }
    }

    private void updateUIForTrack(SpotifyTrack spotifyTrack) {
        // TODO Don't look these up every time.
        ImageView albumArt = (ImageView) getView().findViewById(R.id.playerAlbumArt);
        Picasso.with(getActivity()).load(spotifyTrack.albumImageUrl).into(albumArt);

        TextView trackName = (TextView) getView().findViewById(R.id.playerTrackName);
        trackName.setText(spotifyTrack.name);

        TextView artistName = (TextView) getView().findViewById(R.id.playerArtistName);
        artistName.setText(spotifyTrack.artistName);
    }
}
