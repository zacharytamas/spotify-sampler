package com.zacharytamas.spotifysampler.ui;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
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
import retrofit.RetrofitError;

/**
 * Fragment providing functionality that lists all an Artist's
 * top tracks, if available.
 */
public class TopTrackListFragment extends Fragment {

    private static final String PLAYER_FRAG_TAG = "player-fragment";
    private static final String KEY_TRACKS = "key-tracks";
    private TopTracksAdapter mAdapter;
    private ArrayList<SpotifyTrack> mArtistTracks;
    private boolean mDialogMode = false;

    public TopTrackListFragment() {
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
                try {
                    ((TopTrackListActivity) getActivity()).nowPlayingItem.setVisible(true);
                } catch (ClassCastException error) {
                    // We must be contained in the ArtistSearchActivity on tablet.
                    ((ArtistSearchActivity) getActivity()).nowPlayingItem.setVisible(true);
                    // I could make a protocol that I make both these implement and this would be
                    // simpler but at this point I'm tired. I don't feel really prepared for all
                    // the nuances of this project, even after completing the supplemental course.
                }
                PlayerService.getInstance().playNewPlaylistAtIndex(mArtistTracks, i);
                PlayerFragment.showInContext(getActivity(), mDialogMode);
            }
        });

        Bundle args = getArguments();

        if (args != null) {
            String artistId = args.getString(TopTrackListActivity.EXTRA_ARTIST_ID);
            String artistName = args.getString(TopTrackListActivity.EXTRA_ARTIST_NAME);

            if (artistName != null) {
                ActionBarActivity activity = (ActionBarActivity) getActivity();
                activity.getSupportActionBar().setSubtitle(artistName);
            }

            if (artistId != null) {
                if (savedInstanceState == null) {
                    this.fetchTracks(artistId);
                } else {
                    mArtistTracks = savedInstanceState.getParcelableArrayList(KEY_TRACKS);
                    mAdapter.addAll(mArtistTracks);
                }
            }
        }

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(KEY_TRACKS, mArtistTracks);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    private void fetchTracks(String artistId) {
        new FetchTrackTask().execute(artistId);
    }

    public void setDialogMode(boolean mDialogMode) {
        this.mDialogMode = mDialogMode;
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
            Tracks tracks;

            try {
                tracks = spotifyService.getArtistTopTrack(strings[0], options);
            } catch (RetrofitError e) {
                return this.doInBackground(strings);
            }

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
