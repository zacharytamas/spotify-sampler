package com.zacharytamas.spotifysampler.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.zacharytamas.spotifysampler.R;
import com.zacharytamas.spotifysampler.services.PlayerService;


public class ArtistSearchActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist_search);

        Intent intent = new Intent(this, PlayerService.class);
        this.startService(intent);
    }

}
