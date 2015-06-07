package com.zacharytamas.spotifysampler.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zacharytamas.spotifysampler.R;
import com.zacharytamas.spotifysampler.models.SpotifyTrack;
import com.zacharytamas.spotifysampler.services.PlayerService;

import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class PlayerActivityFragment extends Fragment {

    public PlayerActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_player, container, false);

        Intent intent = getActivity().getIntent();
        ArrayList<SpotifyTrack> playlist = intent.getParcelableArrayListExtra(PlayerService.EXTRA_PLAYLIST);

        Log.i("PlayerActivity", playlist.get(0).name);

        return view;
    }
}
