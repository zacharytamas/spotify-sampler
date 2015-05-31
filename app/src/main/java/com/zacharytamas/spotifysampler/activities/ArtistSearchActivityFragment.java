package com.zacharytamas.spotifysampler.activities;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.zacharytamas.spotifysampler.R;
import com.zacharytamas.spotifysampler.adapters.ArtistSearchAdapter;

import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;


/**
 * A placeholder fragment containing a simple view.
 */
public class ArtistSearchActivityFragment extends Fragment {

    private ListView mListView;
    private ArtistSearchAdapter mAdapter;

    public ArtistSearchActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_artist_search, container, false);

        mListView = (ListView) view.findViewById(R.id.artistListView);
        mAdapter = new ArtistSearchAdapter(getActivity());
        mListView.setAdapter(mAdapter);

        this.fetchArtists("Ed Sheeran");

        return view;
    }

    public void fetchArtists(String query) {
        new FetchArtistsTask().execute(query);
    }

    private class FetchArtistsTask extends AsyncTask<String, Void, List> {
        @Override
        protected List doInBackground(String... strings) {

            if (strings.length != 1) {
                return null;
            }

            // TODO Possibly move this to be a member at the Fragment level
            // so we don't need to create one every time.
            SpotifyApi api = new SpotifyApi();
            SpotifyService spotifyService = api.getService();

            return spotifyService.searchArtists(strings[0]).artists.items;
        }

        @Override
        protected void onPostExecute(List list) {
            mAdapter.clear();
            mAdapter.addAll(list);
            Log.i("ArtistSearch", list.toString());
        }
    }

}
