package com.zacharytamas.spotifysampler.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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

        Intent intent = getActivity().getIntent();
        queuePlayer(intent);

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
            case PlayerService.EVENT_PREPARED:
                Log.i("PlayerActivityFragment", "The player prepared itself.");
                updateUIForTrack(service.currentlyPlayingTrack());
                break;
            case PlayerService.EVENT_TRACK_COMPLETED:
                Log.i("PlayerActivityFragment", "Track completed");
                break;
            case PlayerService.EVENT_STARTED:
                Log.i("PLayerActivityFragment", "Player started");
                break;
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
