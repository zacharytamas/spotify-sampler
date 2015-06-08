package com.zacharytamas.spotifysampler.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.zacharytamas.spotifysampler.R;

public class TopTrackListActivity extends ActionBarActivity {

    public static final String EXTRA_ARTIST_ID = "artistId";
    public static final String EXTRA_ARTIST_NAME = "artistName";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_list);

        if (savedInstanceState == null) {

            Intent intent = getIntent();

            Bundle args = new Bundle();
            args.putString(EXTRA_ARTIST_ID, intent.getStringExtra(EXTRA_ARTIST_ID));
            args.putString(EXTRA_ARTIST_NAME, intent.getStringExtra(EXTRA_ARTIST_NAME));

            TopTrackListFragment fragment = new TopTrackListFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.artistSearch_topTracksContainer, fragment)
                    .commit();
        }
    }

}
