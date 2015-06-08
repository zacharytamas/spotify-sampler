package com.zacharytamas.spotifysampler.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import com.zacharytamas.spotifysampler.R;
import com.zacharytamas.spotifysampler.services.PlayerService;
import com.zacharytamas.spotifysampler.services.PlayerSubscriber;

import butterknife.ButterKnife;
import butterknife.InjectView;
import kaaes.spotify.webapi.android.models.Artist;


public class ArtistSearchActivity extends ActionBarActivity implements PlayerSubscriber {

    private static final String FRAGMENT_TAG = "topTracksFragment";
    static final String QUERY_KEY = "query";
    private boolean mTwoPane = false;

    @InjectView(R.id.searchBox) EditText mSearchBox;
    ArtistSearchActivityFragment mArtistSearchFragment;
    private MenuItem nowPlayingItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist_search);
        Intent intent = new Intent(this, PlayerService.class);
        this.startService(intent);

        ButterKnife.inject(this);

        if (this.findViewById(R.id.artistSearch_topTracksContainer) != null) {
            mTwoPane = true;
        }

        mArtistSearchFragment = (ArtistSearchActivityFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_artist_search);

        if (savedInstanceState != null) {
            mSearchBox.setText(savedInstanceState.getString(QUERY_KEY, ""));
        }

        mSearchBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                mArtistSearchFragment.fetchArtists(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_player, menu);

        nowPlayingItem = menu.findItem(R.id.action_now_playing);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.action_now_playing) {
            Intent intent = new Intent(this, PlayerActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        PlayerService service = PlayerService.getInstance();
        if (service != null) {
            service.subscribe(this);
            nowPlayingItem.setVisible(service.isPlaying());
        }
        super.onResume();
    }

    @Override
    protected void onStop() {
        PlayerService service = PlayerService.getInstance();
        if (service != null) {
            service.unsubscribe(this);
            nowPlayingItem.setVisible(service.isPlaying());
        }
        super.onStop();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(QUERY_KEY, mSearchBox.getText().toString());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onPlayerEvent(String event, PlayerService service) {
        switch (event) {
            case PlayerService.EVENT_PROGRESS:
            case PlayerService.EVENT_TRACK_COMPLETED:
            case PlayerService.EVENT_PLAYLIST_END:
                nowPlayingItem.setVisible(service.isPlaying());
        }
    }

    public void onArtistChosen(Artist artist) {
        if (mTwoPane) {
            Bundle bundle = new Bundle();
            bundle.putString(TopTrackListActivity.EXTRA_ARTIST_ID, artist.id);
            bundle.putString(TopTrackListActivity.EXTRA_ARTIST_NAME, artist.name);

            TopTrackListFragment fragment = new TopTrackListFragment();
            fragment.setArguments(bundle);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.artistSearch_topTracksContainer, fragment, FRAGMENT_TAG)
                    .commit();

        } else {
            Intent intent = new Intent(this, TopTrackListActivity.class);
            intent.putExtra(TopTrackListActivity.EXTRA_ARTIST_ID, artist.id);
            intent.putExtra(TopTrackListActivity.EXTRA_ARTIST_NAME, artist.name);
            startActivity(intent);
        }
    }
}
