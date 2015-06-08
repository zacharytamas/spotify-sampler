package com.zacharytamas.spotifysampler.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.zacharytamas.spotifysampler.R;
import com.zacharytamas.spotifysampler.services.PlayerService;

public class TopTrackListActivity extends ActionBarActivity {

    public static final String EXTRA_ARTIST_ID = "artistId";
    public static final String EXTRA_ARTIST_NAME = "artistName";

    private MenuItem nowPlayingItem;

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_player, menu);

        nowPlayingItem = menu.findItem(R.id.action_now_playing);
        return true;
    }

    @Override
    protected void onResume() {
        PlayerService service = PlayerService.getInstance();
        if (service != null && nowPlayingItem != null) {
            nowPlayingItem.setVisible(service.isPlaying());
        }
        super.onResume();
    }

}
