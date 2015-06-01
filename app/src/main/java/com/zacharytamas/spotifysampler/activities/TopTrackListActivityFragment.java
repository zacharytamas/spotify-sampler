package com.zacharytamas.spotifysampler.activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.zacharytamas.spotifysampler.R;
import com.zacharytamas.spotifysampler.adapters.TopTracksAdapter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Tracks;

/**
 * A placeholder fragment containing a simple view.
 */
public class TopTrackListActivityFragment extends Fragment {

    private ListView mListView;
    private TopTracksAdapter mAdapter;

    public TopTrackListActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_track_list, container, false);

        mListView = (ListView) view.findViewById(R.id.trackListView);
        mAdapter = new TopTracksAdapter(getActivity());
        mListView.setAdapter(mAdapter);

        Intent intent = getActivity().getIntent();
        String artistId = intent.getStringExtra("artistId");

        if (artistId != null) {
            this.fetchTracks(artistId);
        }

        return view;
    }

    private void fetchTracks(String artistId) {
        new FetchTrackTask().execute(artistId);
    }

    private class FetchTrackTask extends AsyncTask<String, Void, List> {
        @Override
        protected List doInBackground(String... strings) {

            if (strings.length != 1) {
                return null;
            }

            // TODO Possibly move this to be a member at the Fragment level
            // so we don't need to create one every time.
            SpotifyApi api = new SpotifyApi();
            SpotifyService spotifyService = api.getService();

            Map<String, Object> options = new HashMap<>();
            options.put("country", "US");

            Tracks tracks = spotifyService.getArtistTopTrack(strings[0], options);

            return tracks.tracks;
        }

        @Override
        protected void onPostExecute(List list) {
            mAdapter.clear();
            mAdapter.addAll(list);
        }
    }
}
