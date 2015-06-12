package com.zacharytamas.spotifysampler.ui;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import com.zacharytamas.spotifysampler.R;
import com.zacharytamas.spotifysampler.adapters.ArtistSearchAdapter;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;


/**
 * Fragment providing functionality for searching for Artists.
 */
public class ArtistSearchActivityFragment extends Fragment {

    private static final String KEY_ARTISTS = "KEY_ARTISTS";
    private ListView mListView;
    private ArtistSearchAdapter mAdapter;
    private EditText mSearchBox;
    private FetchArtistsTask mFetchTask;
    private List<Artist> mArtists;
    private final SpotifyApi api = new SpotifyApi();
    private final SpotifyService spotifyService = api.getService();

    public ArtistSearchActivityFragment() {
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

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Artist artist = (Artist) adapterView.getAdapter().getItem(i);
                ArtistSearchActivity activity = (ArtistSearchActivity) getActivity();
                activity.onArtistChosen(artist);
            }
        });

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
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

            if (strings[0].length() > 0) {
                return spotifyService.searchArtists(strings[0]).artists.items;
            } else {
                return new ArrayList();
            }
        }

        @Override
        protected void onPostExecute(List list) {
            mAdapter.clear();
            mAdapter.addAll(list);
            mArtists = list;
        }
    }

}
