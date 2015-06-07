package com.zacharytamas.spotifysampler.ui.holders;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.zacharytamas.spotifysampler.R;

/**
 * Created by zacharytamas on 6/6/15.
 */
public class TopTrackViewHolder {

    public TextView trackTitle;
    public TextView albumTitle;
    public ImageView albumImage;

    public TopTrackViewHolder(View convertView) {
        this.trackTitle = (TextView) convertView.findViewById(R.id.trackTitle);
        this.albumTitle = (TextView) convertView.findViewById(R.id.albumTitle);
        this.albumImage = (ImageView) convertView.findViewById(R.id.albumImage);
    }

}
