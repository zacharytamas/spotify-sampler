package com.zacharytamas.spotifysampler.ui;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.zacharytamas.spotifysampler.R;
import com.zacharytamas.spotifysampler.adapters.ArtistSearchAdapter;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import retrofit.RetrofitError;


/**
 * Fragment providing functionality for searching for Artists.
 */
public class ArtistSearchActivityFragment extends Fragment {

    private static final String KEY_ARTISTS = "KEY_ARTISTS";
    private ListView mListView;
    private ArtistSearchAdapter mAdapter;
    private EditText mSearchBox;
    private FetchArtistsTask mFetchTask;
    private LinearLayout mEmptyView;
    private List<Artist> mArtists;
    private final SpotifyApi api = new SpotifyApi();
    private final SpotifyService spotifyService = api.getService();
    private TextView mEmptyTextView;

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
        mEmptyView = (LinearLayout) view.findViewById(R.id.empty);
        mEmptyTextView = (TextView) mEmptyView.findViewById(R.id.emptyText);
        mEmptyView.setVisibility(View.INVISIBLE);

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
        updateEmptyView("loading");
        mFetchTask = new FetchArtistsTask();
        mFetchTask.execute(query);
    }

    private void updateEmptyView(String status) {
        // TODO these should be constants, I know
        if (status == "loading") {
            mEmptyTextView.setText("Loading results...");
        } else if (status == "error") {
            mEmptyTextView.setText(getActivity().getString(R.string.error_fetch_artist_no_network));
        } else {
            mEmptyTextView.setText(getActivity().getString(R.string.empty_results));
        }
    }

    private class FetchArtistsTask extends AsyncTask<String, Void, List> {

        String mStatus;

        @Override
        protected List doInBackground(String... strings) {

            if (strings.length != 1) {
                return null;
            }

            if (strings[0].length() > 0) {
                try {
                    ArtistsPager artistsPager = spotifyService.searchArtists(strings[0]);
                    if (artistsPager.artists != null) {
                        mStatus = null;
                        return artistsPager.artists.items;
                    }
                } catch (RetrofitError error) {
                    mStatus = "error";
                }
            }

            return new ArrayList();
        }

        @Override
        protected void onPostExecute(List list) {
            updateEmptyView(mStatus);
            mAdapter.clear();
            mAdapter.addAll(list);
            mArtists = list;
        }
    }

}
