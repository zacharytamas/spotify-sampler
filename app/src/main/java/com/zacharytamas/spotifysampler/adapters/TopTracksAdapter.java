package com.zacharytamas.spotifysampler.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.squareup.picasso.Picasso;
import com.zacharytamas.spotifysampler.R;
import com.zacharytamas.spotifysampler.models.SpotifyTrack;
import com.zacharytamas.spotifysampler.ui.holders.TopTrackViewHolder;

import java.util.ArrayList;

/**
 * Created by zacharytamas on 5/31/15.
 */
public class TopTracksAdapter extends ArrayAdapter<SpotifyTrack> {

    private static final int LAYOUT = R.layout.list_item_track;
    private LayoutInflater inflater;

    public TopTracksAdapter(Context context) {
        super(context, LAYOUT, new ArrayList<SpotifyTrack>());
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        SpotifyTrack track = this.getItem(position);
        TopTrackViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(LAYOUT, parent, false);
            holder = new TopTrackViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (TopTrackViewHolder) convertView.getTag();
        }

        holder.albumTitle.setText(track.albumName);
        holder.trackTitle.setText(track.name);

        Picasso.with(getContext()).load(track.albumImageThumbUrl).into(holder.albumImage);

        return convertView;
    }

}
