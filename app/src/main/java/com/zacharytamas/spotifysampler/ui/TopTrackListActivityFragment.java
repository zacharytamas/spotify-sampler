package com.zacharytamas.spotifysampler.ui;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.zacharytamas.spotifysampler.R;
import com.zacharytamas.spotifysampler.adapters.TopTracksAdapter;
import com.zacharytamas.spotifysampler.models.SpotifyTrack;
import com.zacharytamas.spotifysampler.services.PlayerService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;

/**
 * Fragment providing functionality that lists all an Artist's
 * top tracks, if available.
 */
public class TopTrackListActivityFragment extends Fragment {

    private TopTracksAdapter mAdapter;
    private ArrayList<SpotifyTrack> mArtistTracks;

    public TopTrackListActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_track_list, container, false);

        mArtistTracks = new ArrayList<>();

        ListView listView = (ListView) view.findViewById(R.id.trackListView);
        mAdapter = new TopTracksAdapter(getActivity());
        listView.setAdapter(mAdapter);
        listView.setEmptyView(view.findViewById(R.id.empty));

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getActivity(), PlayerActivity.class);
                intent.putParcelableArrayListExtra(PlayerService.EXTRA_PLAYLIST, mArtistTracks);
                startActivity(intent);
            }
        });

        Intent intent = getActivity().getIntent();
        String artistId = intent.getStringExtra("artistId");
        String artistName = intent.getStringExtra("artistName");

        if (artistName != null) {
            ActionBarActivity activity = (ActionBarActivity) getActivity();
            activity.getSupportActionBar().setSubtitle(artistName);
        }

        if (artistId != null) {
            this.fetchTracks(artistId);
        }

        return view;
    }



    private void fetchTracks(String artistId) {
        new FetchTrackTask().execute(artistId);
    }

    private class FetchTrackTask extends AsyncTask<String, Void, List<Track>> {
        @Override
        protected List doInBackground(String... strings) {

            if (strings.length != 1) {
                return null;
            }

            SpotifyApi api = new SpotifyApi();
            SpotifyService spotifyService = api.getService();

            Map<String, Object> options = new HashMap<>();
            // TODO This could be a preference.
            options.put("country", "US");

            Tracks tracks = spotifyService.getArtistTopTrack(strings[0], options);

            return tracks.tracks;
        }

        @Override
        protected void onPostExecute(List<Track> list) {
            mAdapter.clear();

            mArtistTracks.clear();

            // I'm new to Java, otherwise I'd do some kind of map here to do this in one line.
            for (Track track : list) {
                mArtistTracks.add(new SpotifyTrack(track));
            }

            mAdapter.addAll(mArtistTracks);
        }
    }
}
