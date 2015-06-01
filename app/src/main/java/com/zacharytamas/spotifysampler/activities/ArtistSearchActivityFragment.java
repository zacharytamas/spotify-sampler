package com.zacharytamas.spotifysampler.activities;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
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
    private EditText mSearchBox;
    private FetchArtistsTask mFetchTask;

    public ArtistSearchActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_artist_search, container, false);

        mListView = (ListView) view.findViewById(R.id.artistListView);
        mAdapter = new ArtistSearchAdapter(getActivity());
        mListView.setAdapter(mAdapter);

        mSearchBox = (EditText) view.findViewById(R.id.searchBox);
        mSearchBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() > 0) {
                    fetchArtists(charSequence.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) { }
        });

        return view;
    }

    public void fetchArtists(String query) {
        // Poor man's debounce:
        // If there is an existing task that hasn't completed, cancel it.
        if (mFetchTask != null && mFetchTask.getStatus() != AsyncTask.Status.FINISHED) {
            mFetchTask.cancel(true);
        }
        mFetchTask = new FetchArtistsTask();
        mFetchTask.execute(query);
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