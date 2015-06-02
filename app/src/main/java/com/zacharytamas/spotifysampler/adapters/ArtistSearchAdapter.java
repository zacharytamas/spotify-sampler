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

import kaaes.spotify.webapi.android.models.Artist;

/**
 * Created by zacharytamas on 5/31/15.
 */
public class ArtistSearchAdapter extends ArrayAdapter<Artist> {

    private static final int LAYOUT = R.layout.list_item_artist;

    LayoutInflater inflater;

    public ArtistSearchAdapter(Context context) {
        super(context, LAYOUT, new ArrayList<Artist>());
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Artist artist = this.getItem(position);

        ViewHolder holder;

        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(LAYOUT, parent, false);
            holder.artistName = (TextView) convertView.findViewById(R.id.artistName);
            holder.artistImage = (ImageView) convertView.findViewById(R.id.artistImage);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.artistName.setText(artist.name);

        if (! artist.images.isEmpty()) {
            Picasso.with(this.getContext()).load(artist.images.get(0).url).into(holder.artistImage);
        } else {
            // TODO Handle when there is no image. Maybe put a default empty one.
            // Here I am setting a blank photo because since these are re-used it could
            // be an old photo here.
            holder.artistImage.setImageResource(android.R.color.transparent);
        }

        return convertView;
    }

    private static class ViewHolder {
        TextView artistName;
        ImageView artistImage;
    }

}
