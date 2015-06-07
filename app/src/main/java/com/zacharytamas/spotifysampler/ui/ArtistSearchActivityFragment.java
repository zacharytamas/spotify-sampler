package com.zacharytamas.spotifysampler.ui;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import com.zacharytamas.spotifysampler.R;
import com.zacharytamas.spotifysampler.adapters.ArtistSearchAdapter;

import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;


/**
 * Fragment providing functionality for searching for Artists.
 */
public class ArtistSearchActivityFragment extends Fragment {

    static final String QUERY_KEY = "query";

    private ListView mListView;
    private ArtistSearchAdapter mAdapter;
    private EditText mSearchBox;
    private FetchArtistsTask mFetchTask;

    public ArtistSearchActivityFragment() {
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(QUERY_KEY, mSearchBox.getText().toString());
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_artist_search, container, false);

        mListView = (ListView) view.findViewById(R.id.artistListView);
        mAdapter = new ArtistSearchAdapter(getActivity());
        mListView.setAdapter(mAdapter);
        mListView.setEmptyView(view.findViewById(R.id.empty));
        // Hide it by default until they type a query that has no results.
        // TODO I don't like this but it will do for the moment.
        view.findViewById(R.id.empty).setVisibility(View.INVISIBLE);

        mSearchBox = (EditText) view.findViewById(R.id.searchBox);
        mSearchBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() > 0) {
                    fetchArtists(charSequence.toString());
                } else {
                    mAdapter.clear();
                    view.findViewById(R.id.empty).setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) { }
        });

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Artist artist = (Artist) adapterView.getAdapter().getItem(i);
                Intent intent = new Intent(getActivity(), TopTrackListActivity.class);
                intent.putExtra("artistId", artist.id);
                intent.putExtra("artistName", artist.name);
                startActivity(intent);
            }
        });

        if (savedInstanceState != null) {
            mSearchBox.setText(savedInstanceState.getString(QUERY_KEY, ""));
        }

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
            // so we don't need to create one every time. Maybe even a singleton app-wide.
            SpotifyApi api = new SpotifyApi();
            SpotifyService spotifyService = api.getService();

            return spotifyService.searchArtists(strings[0]).artists.items;
        }

        @Override
        protected void onPostExecute(List list) {
            mAdapter.clear();
            mAdapter.addAll(list);
        }
    }

}
