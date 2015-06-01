package com.zacharytamas.spotifysampler.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.zacharytamas.spotifysampler.R;

import java.util.ArrayList;

import kaaes.spotify.webapi.android.models.Track;

/**
 * Created by zacharytamas on 5/31/15.
 */
public class TopTracksAdapter extends ArrayAdapter<Track> {

    private static final int LAYOUT = R.layout.list_item_track;
    private LayoutInflater inflater;

    public TopTracksAdapter(Context context) {
        super(context, LAYOUT, new ArrayList<Track>());
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Track track = this.getItem(position);
        ViewHolder holder;

        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(LAYOUT, parent, false);
            holder.trackTitle = (TextView) convertView.findViewById(R.id.trackTitle);
            holder.albumTitle = (TextView) convertView.findViewById(R.id.albumTitle);
            holder.albumImage = (ImageView) convertView.findViewById(R.id.albumImage);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.albumTitle.setText(track.album.name);
        holder.trackTitle.setText(track.name);

        Picasso.with(getContext()).load(track.album.images.get(2).url).into(holder.albumImage);

        return convertView;
    }

    private static class ViewHolder {
        TextView trackTitle;
        TextView albumTitle;
        ImageView albumImage;
    }
}
